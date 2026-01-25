package application.model.holder;

import java.util.Base64;

public record Image(
    byte[] data,
    String type
) {

  public String encodeImage() {
    String encoded = "";
    if (data != null && data.length > 0) {
      String base64 = Base64.getEncoder().encodeToString(data);
      encoded = "data:" + this.type() + ";base64," + base64;
    }
    return encoded;
  }
}
