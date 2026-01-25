package http.response;

import http.HttpMessage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpResponseWriter {

    public static void writeTo(OutputStream out, HttpResponse response) throws IOException {
        out.write(toBytes(response));
    }

    private static byte[] toBytes(HttpResponse response) {
        StringBuilder builder = new StringBuilder();

        // 1. 상태 라인
        builder.append(HttpMessage.VERSION)
                .append(" ")
                .append(response.status().code())
                .append(" ")
                .append(response.status().reasonPhrase())
                .append("\r\n");

        // 2. 헤더
        for (Map.Entry<String, String> entry : response.headers().entrySet()) {
            builder.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue())
                    .append("\r\n");
        }

        // 3. 빈 줄
        builder.append("\r\n");

        // 4. 바디
        byte[] headerBytes = builder.toString().getBytes(StandardCharsets.UTF_8);
        byte[] body = response.body();

        byte[] result = new byte[headerBytes.length + (body != null ? body.length : 0)];
        System.arraycopy(headerBytes, 0, result, 0, headerBytes.length);
        if (body != null) {
            System.arraycopy(body, 0, result, headerBytes.length, body.length);
        }

        return result;
    }
}

