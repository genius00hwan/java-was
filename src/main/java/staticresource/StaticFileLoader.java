package staticresource;

import http.ContentType;
import http.body.StaticResourceBody;
import http.request.Path;


import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class StaticFileLoader {
    private static final String STATIC_FILE_DIRECTORY = "static";

    public static Optional<StaticResourceBody> tryLoad(Path rawPath) {
        Path path = PathResolver.resolveForStatic(rawPath);
        try (InputStream fileStream = getInputStream(path)) {
            if (fileStream == null) return Optional.empty();

            byte[] body = fileStream.readAllBytes();

            ContentType contentType = ContentType.fromExtension(getExtension(path.value()));

            return Optional.of(new StaticResourceBody(contentType, body));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private static InputStream getInputStream(Path path) {
        return StaticFileLoader.class.getClassLoader().getResourceAsStream(STATIC_FILE_DIRECTORY + path.value());
    }

    private static String getExtension(String path) {
        int lastDot = path.lastIndexOf('.');
        return (lastDot != -1 && lastDot < path.length() - 1) ? path.substring(lastDot + 1) : "";
    }
}
