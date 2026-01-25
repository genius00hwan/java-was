package application.auth;

import annotation.Singleton;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class AuthSession {
    private final Map<String, String> sessionStore = new ConcurrentHashMap<>();

    public String addSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, userId);
        return sessionId;
    }

    public String getUserId(String sessionId) {
        return sessionStore.get(sessionId);
    }

    public void removeSession(String sessionId) {
        sessionStore.remove(sessionId);
    }

    public boolean isValid(String sessionId) {
        return sessionStore.containsKey(sessionId);
    }
}
