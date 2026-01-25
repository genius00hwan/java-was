package http;

import java.util.Arrays;

public enum ContentType {
    HTML("text/html;charset=utf-8"),
    CSS("text/css"),
    JS("application/javascript"),
    ICO("image/x-icon"),
    SVG("image/svg+xml"),
    PNG("image/png"),
    JPG("image/jpeg"),
    JPEG("image/jpeg"),
    OCTET_STREAM("application/octet-stream"),
    JSON("application/json"),
    FORM("application/x-www-form-urlencoded"),
    MULTIPART("multipart/form-data"),
    ;

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ContentType from(String rawValue) {
        String[] parts = rawValue.split(";");
        return Arrays.stream(ContentType.values())
                .filter(ct -> ct.value.equals(parts[0]))
                .findFirst()
                .orElse(HTML);
    }

    public static ContentType fromExtension(String ext) {
        return switch (ext.toLowerCase()) {
            case "html" -> HTML;
            case "css" -> CSS;
            case "js" -> JS;
            case "ico" -> ICO;
            case "svg" -> SVG;
            case "png" -> PNG;
            case "jpg" -> JPG;
            case "jpeg" -> JPEG;
            default -> OCTET_STREAM;
        };
    }
}
