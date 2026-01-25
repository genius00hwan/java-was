package http.request;

import application.auth.Cookie;
import http.ContentType;
import http.HttpHeader;
import http.body.HttpBody;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HttpRequestReader {

  public static HttpRequest from(InputStream in) throws IOException {
    List<String> headerLines = readUpper(in);

    String[] requestLine = parseRequest(headerLines);

    Map<String, String> headers = parseHeaders(headerLines);

    byte[] bodyBytes = readBody(in, headers);

    String[] contentTypeAndMeta = parseContentTypeAndMeta(headers);
    ContentType contentType = parseContentType(contentTypeAndMeta);
    Map<String, String> contentMeta = parseContentMeta(contentTypeAndMeta);

    return new HttpRequest(
        HttpMethod.from(requestLine[0]),
        parsePath(requestLine[1]),
        requestLine[2],
        headers,
        parseCookies(headers.get(HttpHeader.COOKIE.value())),
        parseParameters(requestLine[1]),
        HttpBody.from(contentType, contentMeta, bodyBytes)
    );
  }


  private static List<String> readUpper(InputStream in) throws IOException {
    List<String> headerLines = new ArrayList<>();
    StringBuilder currentLine = new StringBuilder();
    int b;

    while ((b = in.read()) != -1) {
      if (b == '\r') {
        continue;
      }
      if (b == '\n') {
        if (currentLine.isEmpty()) {
          break;
        }
        headerLines.add(currentLine.toString());
        currentLine.setLength(0);
        continue;
      }
      currentLine.append((char) b);
    }
    return headerLines;
  }

  private static Map<String, String> parseHeaders(List<String> headerLines) {
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    for (int i = 1; i < headerLines.size(); i++) {
      String[] parts = headerLines.get(i).split(":", 2);
      if (parts.length == 2) {
        headers.put(parts[0].trim(), parts[1].trim());
      }
    }
    return headers;
  }

  private static String[] parseRequest(List<String> headerLines) {
    return headerLines.get(0).trim().split("\\s+");
  }

  private static Path parsePath(String rawPath) {
    return new Path(rawPath.contains("?") ? rawPath.substring(0, rawPath.indexOf("?")) : rawPath);
  }

  private static Map<String, String> parseParameters(String rawPath) {
    Map<String, String> queryParams = new HashMap<>();
    int queryIdx = rawPath.indexOf('?');
    if (queryIdx != -1 && queryIdx < rawPath.length() - 1) {
      String queryString = rawPath.substring(queryIdx + 1);
      for (String pair : queryString.split("&")) {
        String[] entry = pair.split("=", 2);
        String key = URLDecoder.decode(entry[0], StandardCharsets.UTF_8);
        String value = entry.length > 1 ? URLDecoder.decode(entry[1], StandardCharsets.UTF_8) : "";
        queryParams.put(key, value);
      }
    }
    return queryParams;
  }

  private static List<Cookie> parseCookies(String cookieHeader) {
    List<Cookie> cookies = new ArrayList<>();
    if (cookieHeader == null || cookieHeader.isBlank()) {
      return cookies;
    }

    for (String cookie : cookieHeader.split(";")) {
      String[] pair = cookie.trim().split("=", 2);
      String name = pair[0].trim();
      String value = pair[1].trim();
      if (pair.length == 2) {
        cookies.add(Cookie.defaultCookie(name, value));
      }
    }
    return cookies;
  }


  private static byte[] readBody(InputStream in, Map<String, String> headers) throws IOException {
    byte[] bodyBytes = new byte[0];
    //todo: CONTENT_LENGTH 없는 경우 처리
    if (headers.containsKey(HttpHeader.CONTENT_LENGTH.value())) {
      int length = Integer.parseInt(headers.get(HttpHeader.CONTENT_LENGTH.value()));
      bodyBytes = in.readNBytes(length);
    }
    return bodyBytes;
  }

  private static String[] parseContentTypeAndMeta(Map<String, String> headers) {
    String rawContentType = headers.getOrDefault(HttpHeader.CONTENT_TYPE.value(), "text/plain");
    return rawContentType.split(";");
  }

  private static ContentType parseContentType(String[] contentTypeParts) {
    return ContentType.from(contentTypeParts[0].trim());

  }

  private static Map<String, String> parseContentMeta(String[] contentTypeParts) {
    Map<String, String> meta = new HashMap<>();
    for (int i = 1; i < contentTypeParts.length; i++) {
      String[] kv = contentTypeParts[i].trim().split("=", 2);
      if (kv.length == 2) {
        meta.put(kv[0], kv[1]);
      }
    }
    return meta;
  }
}
