package application.exception.server;

public class ResourceMissException extends ServerException {
    public ResourceMissException(String resourcePath) {
        super(String.format("Resource missing: %s", resourcePath));
    }
}
