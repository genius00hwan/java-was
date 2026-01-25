package http.body;

import http.ContentType;
import java.util.Map;

public interface HttpBody {

  byte[] asBytes();

  String toString();

  static HttpBody from(ContentType contentType, byte[] rawBytes) {
    return from(contentType, Map.of(), rawBytes);
  }

  static HttpBody from(ContentType contentType, Map<String, String> meta, byte[] rawBytes) {
    return switch (contentType) {
      case MULTIPART -> new MultipartFormBody(meta.get("boundary"), rawBytes);
      case FORM -> new FormBody(rawBytes);
      default -> new StaticResourceBody(contentType, rawBytes);
    };
  }
}

