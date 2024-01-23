package ru.samsung.smartintercom.service.http.server;

public class HttpStatusCode {

    // --- 1xx Informational ---

    // 100 Continue
    public static final int CONTINUE = 100;

    // 101 Switching Protocols
    public static final int SWITCHING_PROTOCOLS = 101;

    // 102 Processing
    public static final int PROCESSING = 102;

    //  --- 2xx Success ---

    // 200 OK
    public static final int OK = 200;

    // 201 Created
    public static final int CREATED = 201;

    // 202 Accepted
    public static final int ACCEPTED = 202;

    // 203 Non Authoritative Information
    public static final int NON_AUTHORITATIVE_INFORMATION = 203;

    // 204 No Content
    public static final int NO_CONTENT = 204;

    // 205 Reset Content
    public static final int RESET_CONTENT = 205;

    // 206 Partial Content
    public static final int PARTIAL_CONTENT = 206;


    // 207 Multi-Status or 207 Partial Update OK
    public static final int MULTI_STATUS = 207;

    //  --- 3xx Redirection ---

    // 300 Mutliple Choices
    public static final int MULTIPLE_CHOICES = 300;

    // 301 Moved Permanently
    public static final int MOVED_PERMANENTLY = 301;

    // 302 Moved Temporarily
    public static final int MOVED_TEMPORARILY = 302;

    // 303 See Other
    public static final int SEE_OTHER = 303;

    // 304 Not Modified
    public static final int NOT_MODIFIED = 304;

    // 305 Use Proxy
    public static final int USE_PROXY = 305;

    // 307 Temporary Redirect
    public static final int TEMPORARY_REDIRECT = 307;

    //  --- 4xx Client Error ---

    // 400 Bad Request
    public static final int BAD_REQUEST = 400;

    // 401 Unauthorized
    public static final int UNAUTHORIZED = 401;

    // 402 Payment Required
    public static final int PAYMENT_REQUIRED = 402;

    // 403 Forbidden
    public static final int FORBIDDEN = 403;

    // 404 Not Found
    public static final int NOT_FOUND = 404;

    // 405 Method Not Allowed
    public static final int METHOD_NOT_ALLOWED = 405;

    // 406 Not Acceptable
    public static final int NOT_ACCEPTABLE = 406;

    // 407 Proxy Authentication Required
    public static final int PROXY_AUTHENTICATION_REQUIRED = 407;

    // 408 Request Timeout
    public static final int REQUEST_TIMEOUT = 408;

    // 409 Conflict
    public static final int CONFLICT = 409;

    // 410 Gone
    public static final int GONE = 410;

    // 411 Length Required
    public static final int LENGTH_REQUIRED = 411;

    // 412 Precondition Failed
    public static final int PRECONDITION_FAILED = 412;

    // 413 Request Entity Too Large
    public static final int REQUEST_TOO_LONG = 413;

    // 414 Request-URI Too Long
    public static final int REQUEST_URI_TOO_LONG = 414;

    // 415 Unsupported Media Type
    public static final int UNSUPPORTED_MEDIA_TYPE = 415;

    // 416 Requested Range Not Satisfiable
    public static final int REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    // 417 Expectation Failed
    public static final int EXPECTATION_FAILED = 417;

    // 419 Insufficient Space on Resource or 419 Proxy Reauthentication Required
    public static final int INSUFFICIENT_SPACE_ON_RESOURCE = 419;

    // 420 Method Failure
    public static final int METHOD_FAILURE = 420;

    // 422 Unprocessable Entity
    public static final int UNPROCESSABLE_ENTITY = 422;

    // 423 Locked
    public static final int LOCKED = 423;

    // 424 Failed Dependency
    public static final int FAILED_DEPENDENCY = 424;

    //  --- 5xx Server Error ---

    // 500 Server Error
    public static final int INTERNAL_SERVER_ERROR = 500;

    // 501 Not Implemented
    public static final int NOT_IMPLEMENTED = 501;

    // 502 Bad Gateway
    public static final int BAD_GATEWAY = 502;

    // 503 Service Unavailable
    public static final int SERVICE_UNAVAILABLE = 503;

    // 504 Gateway Timeout
    public static final int GATEWAY_TIMEOUT = 504;

    // 505 HTTP Version Not Supported
    public static final int HTTP_VERSION_NOT_SUPPORTED = 505;

    // 507 Insufficient Storage
    public static final int INSUFFICIENT_STORAGE = 507;

}