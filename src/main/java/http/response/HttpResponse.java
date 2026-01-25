package http.response;

import http.HttpMessage;

import java.util.Map;

public record HttpResponse(
        HttpStatus status,
        Map<String, String> headers,
        byte[] body
) implements HttpMessage {
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("HttpResponse {\n");
        sb.append("  status: ").append(status).append(",\n");
        sb.append("  headers: {\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append("    \"").append(entry.getKey()).append("\": \"")
                    .append(entry.getValue()).append("\",\n");
        }
        if (!headers.isEmpty()) {
            sb.setLength(sb.length() - 2); // 마지막 콤마 제거
        }
        sb.append("\n  },\n");
        sb.append("  body: ").append(body == null ? "null" : new String(body)).append("\n");
        sb.append("}");
        return sb.toString();
    }
}
