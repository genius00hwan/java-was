package http.body;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public record FormBody(Map<String, String> formData) implements HttpBody {

    private static final Logger log = LoggerFactory.getLogger(FormBody.class);

    public FormBody(byte[] rawBytes) {
        this(parse(rawBytes));
    }

    private static Map<String, String> parse(byte[] rawBytes) {
        String bodyString = new String(rawBytes, StandardCharsets.UTF_8);
        Map<String, String> map = new HashMap<>();
        if (bodyString.isBlank()) return map;

        for (String pair : bodyString.split("&")) {
            String[] kv = pair.split("=", 2);
            String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String value = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            log.info("[Create Form body] key:{},value:{}", key, value);
            map.put(key, value);
        }
        return map;
    }

    @Override
    public byte[] asBytes() {
        String encoded = formData.entrySet().stream()
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" +
                        URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        return encoded.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return "FormBody " + formData.toString();
    }
}
