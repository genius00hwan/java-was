package http;

public enum HttpHeader {
    CONTENT_LENGTH("Content-Length"),
    CONNECTION("Connection"),
    KEEP_ALIVE("keep-alive"),
    CLOSE("close"),
    HTTP_1_1("HTTP/1.1"),
    CONTENT_TYPE("Content-Type"),
    LOCATION("Location"),
    SET_COOKIE("Set-Cookie"),
    COOKIE("Cookie"),
    ;

    private final String value;
    HttpHeader(String value) { this.value = value; }
    public String value() { return value; }
}
