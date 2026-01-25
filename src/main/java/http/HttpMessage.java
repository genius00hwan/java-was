package http;

import java.util.Map;

public interface HttpMessage {
    String VERSION = "HTTP/1.1";
    Map<String, String> headers();
}
