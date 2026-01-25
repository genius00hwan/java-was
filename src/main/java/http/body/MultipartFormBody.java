package http.body;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public record MultipartFormBody(
    String boundary,
    Map<String, String> formFields,   // 텍스트
    Map<String, byte[]> fileFields, // 파일
    Map<String, String> fileTypes, // 파일 타입
    byte[] rawBytes                   // 원본 데이터
) implements HttpBody {

  public MultipartFormBody(String boundary, byte[] rawBytes) {
    this(
        boundary,
        parse(boundary, rawBytes).formFields(),
        parse(boundary, rawBytes).fileFields(),
        parse(boundary, rawBytes).fileTypes(),
        rawBytes
    );
  }

  @Override
  public byte[] asBytes() {
    return rawBytes;
  }

  @Override
  public String toString() {
    return "MultipartFormBody{" +
        "boundary='" + boundary + '\'' +
        ", formFields=" + formFields +
        ", fileFields=" + fileFields.keySet() +
        ", fileTypes=" + fileTypes +
        '}';
  }

  private static MultipartFormBody parse(String boundary, byte[] rawBytes) {
    String delimiter = "--" + boundary;
    String body = new String(rawBytes, StandardCharsets.ISO_8859_1); // 바이너리
    Map<String, String> fields = new HashMap<>();
    Map<String, byte[]> files = new HashMap<>();
    Map<String, String> types = new HashMap<>();

    for (String part : body.split(delimiter)) {
      if (part.isBlank() || part.equals("--")) continue;

      String[] sections = part.split("\r\n\r\n", 2);
      if (sections.length < 2) continue;

      String headers = sections[0].trim();
      String content = sections[1].replaceAll("\r\n$", "");

      if (headers.contains("filename=")) {
        String filename = toUtf8(extractHeader(headers, "name"));
        String type = extractType(headers);
        types.put(filename, type);
        files.put(filename, content.getBytes(StandardCharsets.ISO_8859_1));
      } else {
        String name = extractHeader(headers, "name");
        fields.put(name, toUtf8(content.trim()));
      }
    }

    return new MultipartFormBody(boundary, fields, files, types, rawBytes);
  }

  private static String extractHeader(String headers, String key) {
    for (String part : headers.split(";")) {
      String trimmed = part.trim();
      if (trimmed.startsWith(key + "=")) {
        return trimmed.split("=", 2)[1]
            .replace("\"", "")
            .trim();
      }
    }
    return null;
  }


  private static String extractType(String headers) {
    for (String line : headers.split("\r\n")) {
      if (line.startsWith("Content-Type:")) {
        return line.split(":", 2)[1].trim();
      }
    }
    return "application/octet-stream"; // 기본값
  }

  private static String toUtf8(String iso) {
    return new String(iso.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
  }
}
