package webserver.connection;

import annotation.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
    private static final long DEFAULT_TIMEOUT = 5000;
    private static final int MAX_CONNECTIONS = 100;

    private final Map<Socket, ConnectionContext> connectionPool = Collections.synchronizedMap(
            new LinkedHashMap<>(16, 0.75f, true)
    );
    private final long timeoutMillis;

    public ConnectionManager() {
        this.timeoutMillis = DEFAULT_TIMEOUT;
    }

    public synchronized void register(Socket socket) {
        if (connectionPool.size() >= MAX_CONNECTIONS) {
            Iterator<Map.Entry<Socket, ConnectionContext>> it = connectionPool.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<Socket, ConnectionContext> lru = it.next();
                try {
                    lru.getKey().close();
                } catch (IOException ignored) {}
                it.remove();
                logger.warn("LRU 소켓 닫힘 및 제거: {}", lru.getKey());
            }
        }
        connectionPool.put(socket, new ConnectionContext(socket));
        logger.debug("소켓 등록됨: {}", socket);
    }

    public synchronized ConnectionContext getOrCreate(Socket socket) {
        return connectionPool.computeIfAbsent(socket, ConnectionContext::new);
    }

    public synchronized void close(Socket socket) {
        try {
            socket.close();
            logger.info("소켓 정상 종료: {}", socket);
        } catch (IOException e) {
            logger.error("소켓 종료 실패: {}", socket, e);
        }
        connectionPool.remove(socket);
    }

    public synchronized void cleanIdleConnections() {
        Iterator<Map.Entry<Socket, ConnectionContext>> it = connectionPool.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Socket, ConnectionContext> entry = it.next();
            if (entry.getValue().idleTimeoutExceeded(timeoutMillis)) {
                try {
                    entry.getKey().close();
                    logger.info("idleTimeout 초과 소켓 종료: {}", entry.getKey());
                } catch (IOException e) {
                    logger.warn("idleTimeout 소켓 종료 실패: {}", entry.getKey(), e);
                }
                it.remove();
            }
        }
    }
}

