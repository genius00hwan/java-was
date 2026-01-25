package application.exception.server;

public abstract class ServerException extends RuntimeException {
    public ServerException(String message) {
        super(message);
    }
}
