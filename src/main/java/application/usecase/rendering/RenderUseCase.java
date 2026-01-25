package application.usecase.rendering;

import http.ContentType;
import http.body.StaticResourceBody;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public interface RenderUseCase<T> {
  StaticResourceBody render(T target);

  static StaticResourceBody replacePlaceholder(
      StaticResourceBody resource,
      Map<String,String> placeholders
  ) {
    if (!resource.contentType().equals(ContentType.HTML)) {
      return resource;
    }

    String html = new String(resource.rawBody(), StandardCharsets.UTF_8);

    for (Map.Entry<String, String> placeholder : placeholders.entrySet()) {
      html = html.replace(placeholder.getKey(), placeholder.getValue());
    }

    return new StaticResourceBody(
        ContentType.HTML,
        html.getBytes(StandardCharsets.UTF_8)
    );
  }
}
