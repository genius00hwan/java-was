package http.body;

import http.ContentType;

import java.nio.charset.StandardCharsets;

public record StaticResourceBody(ContentType contentType, byte[] rawBody) implements HttpBody {
    @Override
    public byte[] asBytes() {
        return rawBody;
    }

    @Override
    public String toString() {
        return new String(rawBody, StandardCharsets.UTF_8);
    }
}
