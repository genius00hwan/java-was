package webserver;

import filter.FilterChain;
import filter.FilterConfig;
import http.request.HttpRequest;
import http.request.HttpRequestReader;
import http.response.HttpResponse;
import http.response.HttpResponseWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.Dispatcher;
import webserver.connection.ConnectionContext;
import webserver.connection.ConnectionManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class RequestHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private final ConnectionManager connectionManager;
    private final FilterConfig filterConfig;
    private final Dispatcher dispatcher;

    public RequestHandler(Socket connection, ConnectionManager connectionManager, FilterConfig filterConfig, Dispatcher dispatcher) {
        this.connection = connection;
        this.connectionManager = connectionManager;
        this.filterConfig = filterConfig;
        this.dispatcher = dispatcher;
    }

    @Override
    public void run() {
        try (InputStream in = connection.getInputStream();
             OutputStream out = connection.getOutputStream()
        ) {
            processConnectionLoop(in, out);
        } catch (IOException e) {
            logger.error("소켓 처리 중 예외 발생", e);
        } finally {
            connectionManager.close(connection);
        }
    }

    private void processConnectionLoop(InputStream in, OutputStream out) throws IOException {
        while (!connection.isClosed()) {
            HttpRequest request;
            try {
                request = HttpRequestReader.from(in);
                logRequest(request);
                FilterChain chain = new FilterChain(filterConfig, dispatcher);
                HttpResponse response = chain.doChain(request);

                boolean keepAlive = handleResponse(response, request, out);
                if (!keepAlive) break;

            } catch (SocketTimeoutException e) {
                continue;
            } catch (IOException e) {
                break;
            }
        }
    }

    private boolean handleResponse(HttpResponse response, HttpRequest request, OutputStream out) throws IOException {
        ConnectionContext context = connectionManager.getOrCreate(connection);
        context.recordRequest();

        boolean keepAlive = context.shouldKeepAlive(request);

        context.applyConnectionHeader(response, keepAlive);
        HttpResponseWriter.writeTo(out, response);

        return keepAlive;
    }

    private void logRequest(HttpRequest request) {
        logger.info("요청 로그 From {}:{}\n{}",
                connection.getInetAddress().getHostAddress(),
                connection.getPort(),
                request.toString());
    }
}
