package application.exception.client;

public abstract class ClientException extends RuntimeException {
    public ClientException(String message) {
        super(message);
    }
}
