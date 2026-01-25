package application.exception.client;

public class BadRequestException extends ClientException {
    public BadRequestException(String message) {
        super(message);
    }
}
