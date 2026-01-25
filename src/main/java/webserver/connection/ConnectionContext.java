package webserver.connection;

import http.HttpHeader;
import http.request.HttpRequest;
import http.response.HttpResponse;

import java.net.Socket;


public class ConnectionContext {
    private final Socket socket;
    private int requestCount = 0;
    private long lastActivityTime;

    public ConnectionContext(Socket socket) {
        this.socket = socket;
        this.lastActivityTime = System.currentTimeMillis();
    }

    public void recordRequest() {
        requestCount++;
        lastActivityTime = System.currentTimeMillis();
    }

    public boolean shouldKeepAlive(HttpRequest request) {
        String version = request.version();
        String connection = request.headers().getOrDefault(HttpHeader.CONNECTION.value(), "").toLowerCase();

        if (version.equalsIgnoreCase(HttpHeader.HTTP_1_1.value())) {
            return !connection.equals(HttpHeader.CLOSE.value());
        }
        return connection.equals(HttpHeader.KEEP_ALIVE.value());
    }

    public void applyConnectionHeader(HttpResponse response, boolean keepAlive) {
        String headerValue = keepAlive ? HttpHeader.KEEP_ALIVE.value() : HttpHeader.CLOSE.value();
        response.headers().put(HttpHeader.CONNECTION.value(), headerValue);

    }

    public boolean idleTimeoutExceeded(long timeoutMillis) {
        return System.currentTimeMillis() - lastActivityTime > timeoutMillis;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public Socket socket() {
        return socket;
    }
}
