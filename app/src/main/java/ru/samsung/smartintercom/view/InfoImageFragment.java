package ru.samsung.smartintercom.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;
import ru.samsung.smartintercom.R;
import ru.samsung.smartintercom.framework.BaseFragmentDisposable;
import ru.samsung.smartintercom.framework.ReactiveCommand;
import ru.samsung.smartintercom.framework.ReactiveProperty;
import ru.samsung.smartintercom.state.AppState;
import ru.samsung.smartintercom.util.Converter;
import ru.samsung.smartintercom.util.LoadStatus;

public class InfoImageFragment extends BaseFragmentDisposable {
    public static class Ctx {
        public AppState appState;
        public ReactiveCommand<Void> takeRemotePhoto;
        public ReactiveProperty<LoadStatus> takePhotoStatus;
        public ReactiveProperty<String> lastErrorDescription;
    }

    private Ctx _ctx;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info_image, container, false);
    }

    public void setCtx(Ctx ctx){
        _ctx = ctx;

        View view = getView();

        Button takePhotoButton = view.findViewById(R.id.button_take_photo);
        ImageView lastPhotoImage = view.findViewById(R.id.image_intercom);
        TextView lastPhotoReceivedTime = view.findViewById(R.id.image_last_received_time);

        setErrorMessageVisible(false);

        takePhotoButton.setOnClickListener(v -> {
            _ctx.takeRemotePhoto.execute(null);
        });

        deferDispose(_ctx.takePhotoStatus.subscribe(loadStatus -> {
            if (loadStatus == LoadStatus.FAIL){
                setErrorMessage(_ctx.lastErrorDescription.getValue());
                return;
            }

            setErrorMessageVisible(false);
        }));

        deferDispose(_ctx.appState.lastPhoto.subscribe(bitmap -> {
            lastPhotoImage.setImageBitmap(bitmap);
        }));

        deferDispose(_ctx.appState.lastPhotoReceivedTime.subscribe(value -> {
            String lastPhotoReceivedFormat = getString(R.string.last_photo_received_time);
            lastPhotoReceivedTime.setText(String.format(lastPhotoReceivedFormat, Converter.convertInstantToString(value)));
        }));
    }

    private void setErrorMessageVisible(Boolean value){
        View view = getView();

        TableLayout errorContainer = view.findViewById(R.id.error_container);

        if (value){
            errorContainer.setVisibility(View.VISIBLE);
        }else{
            errorContainer.setVisibility(View.GONE);
        }
    }

    private void setErrorMessage(String message) {
        View view = getView();

        TextView errorText = view.findViewById(R.id.text_error);
        TableLayout errorContainer = view.findViewById(R.id.error_container);

        errorText.setText(message);
        errorContainer.setVisibility(View.VISIBLE);
    }
}