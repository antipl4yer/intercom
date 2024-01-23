package ru.samsung.smartintercom.service.http.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.widget.ImageView;
import com.android.volley.*;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import ru.samsung.smartintercom.framework.serialization.Json;
import ru.samsung.smartintercom.service.http.server.request.CallRequest;
import ru.samsung.smartintercom.service.http.server.response.InfoResponse;
import ru.samsung.smartintercom.util.Callable;
import ru.samsung.smartintercom.R;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AppServerService {
    public enum CallAction {
        OPEN,
        CLOSE
    }

    public static class DataHeaders {
        public String house;
        public String flat;
    }

    public static class Ctx {
        public Context appContext;
        public String endpoint;
    }

    private final Ctx _ctx;

    private final RequestQueue _requestQueue;
    private Callable<String, Void> _errorHandler;
    private Runnable _successHandler;
    private DataHeaders _dataHeaders;

    public AppServerService(Ctx ctx) {
        _ctx = ctx;

        _dataHeaders = new DataHeaders();

        _requestQueue = Volley.newRequestQueue(_ctx.appContext);
    }

    public void setDataHeaders(DataHeaders dataHeaders) {
        _dataHeaders = dataHeaders;
    }

    public void getImage(Callable<Bitmap, Void> responseCallback, Callable<String, Void> errorCallback) {
        ImageRequest imageRequest = new ImageRequest(buildApiUrl("/image"),
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        Log.i("AppServerService", "getImage response ok");
                        if (_successHandler != null) {
                            _successHandler.run();
                        }
                        responseCallback.call(bitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565,
                error -> {
                    String errorDescription = handleErrorAndGetDescription(error);
                    errorCallback.call(errorDescription);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                return getDataHeaders();
            }
        };
        _requestQueue.add(imageRequest);
    }

    public void getInfo(Callable<InfoResponse, Void> responseCallback, Callable<String, Void> errorCallback) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, buildApiUrl("/info"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String responseUtf8 = parseStringResponseAsUtf8(response);
                        InfoResponse infoResponse = Json.deserialize(responseUtf8, InfoResponse.class);
                        try {
                            Log.i("AppServerService", "getInfo response ok");
                            if (_successHandler != null) {
                                _successHandler.run();
                            }
                            responseCallback.call(infoResponse);
                        } catch (Exception e) {
                            Log.e("AppServerService", "getInfo, can't call response callback");
                        }
                    }
                },
                error -> {
                    String errorDescription = handleErrorAndGetDescription(error);
                    errorCallback.call(errorDescription);
                }) {

            @Override
            public Map<String, String> getHeaders() {
                return getDataHeaders();
            }
        };
        _requestQueue.add(stringRequest);
    }

    public void call(CallAction callAction, Runnable responseCallback, Callable<String, Void> errorCallback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, buildApiUrl("/call"),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("AppServerService", "call response ok");
                        if (_successHandler != null) {
                            _successHandler.run();
                        }

                        responseCallback.run();
                    }
                },
                error -> {
                    String errorDescription = handleErrorAndGetDescription(error);
                    errorCallback.call(errorDescription);
                }) {

            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                CallRequest callRequest = new CallRequest();
                if (callAction == CallAction.OPEN) {
                    callRequest.status = "open";
                }
                if (callAction == CallAction.CLOSE) {
                    callRequest.status = "close";
                }

                String callRequestRaw = Json.serialize(callRequest);

                return callRequestRaw.getBytes(StandardCharsets.UTF_8);
            }

            @Override
            public Map<String, String> getHeaders() {
                return getDataHeaders();
            }
        };
        _requestQueue.add(stringRequest);
    }

    public void setErrorHandler(Callable<String, Void> errorHandler) {
        _errorHandler = errorHandler;
    }

    public void setSuccessHandler(Runnable successHandler) {
        _successHandler = successHandler;
    }

    private String buildApiUrl(String path) {
        URI uri = null;
        try {
            uri = new URI(_ctx.endpoint);
            uri = uri.resolve(path);
            return uri.toString();
        } catch (URISyntaxException e) {
            Log.e("AppServerService", String.format("error while buildApiUrl for endpoint: %s", _ctx.endpoint));
            return "";
        }
    }

    private Map<String, String> getDataHeaders() {
        Map<String, String> headersMap = new HashMap<String, String>();
        headersMap.put("house", _dataHeaders.house);
        headersMap.put("flat", _dataHeaders.flat);
        return headersMap;
    }

    private String handleErrorAndGetDescription(VolleyError error) {
        NetworkResponse networkResponse = error.networkResponse;
        String errorDescription = "";

        if (networkResponse == null){
            errorDescription = _ctx.appContext.getString(R.string.network_error);
        }else{
            int statusCode = networkResponse.statusCode;

            if (statusCode != HttpStatusCode.OK) {
                errorDescription = _ctx.appContext.getString(R.string.http_error);
                Log.e("AppServerService", String.format("invalid response, status code: %d", statusCode));
            }

            if (statusCode == HttpStatusCode.BAD_REQUEST) {
                errorDescription = _ctx.appContext.getString(R.string.http_400_error);
            }
            if (statusCode == HttpStatusCode.FORBIDDEN) {
                errorDescription = _ctx.appContext.getString(R.string.http_403_error);
            }
        }


        if (_errorHandler != null) {
            _errorHandler.call(errorDescription);
        }

        return errorDescription;
    }

    private String parseStringResponseAsUtf8(String response) {
        byte[] responseBytes = response.getBytes(StandardCharsets.ISO_8859_1);
        return new String(responseBytes, StandardCharsets.UTF_8);
    }
}
