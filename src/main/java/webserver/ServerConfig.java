package webserver;

import container.DIContainer;
import filter.FilterConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import router.Dispatcher;
import webserver.connection.ConnectionManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int THREAD_POOL_SIZE = Integer.parseInt(System.getenv("THREAD_POOL_SIZE"));
    private static final int TIMEOUT = 5000;

    public void startServer() {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        Dispatcher dispatcher = DIContainer.getInstance(Dispatcher.class);
        ConnectionManager connectionManager = DIContainer.getInstance(ConnectionManager.class);
        FilterConfig filterConfig = DIContainer.getInstance(FilterConfig.class);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                connectionManager::cleanIdleConnections, 10, 10, TimeUnit.SECONDS
        );

        try (ServerSocket listenSocket = new ServerSocket(DEFAULT_PORT)) {
            logger.info("The server was started on port {}", DEFAULT_PORT);
            while (true) {
                Socket connection = listenSocket.accept();
                connection.setSoTimeout(TIMEOUT);

                connectionManager.register(connection);
                executor.submit(new RequestHandler(connection, connectionManager, filterConfig, dispatcher));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }
    }
}



