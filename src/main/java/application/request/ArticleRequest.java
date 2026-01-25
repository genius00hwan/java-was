package application.request;

import application.model.User;
import application.model.holder.Image;

public class ArticleRequest {
  public record CreateRequest(
      User user,
      String title,
      String content,
      Image image
  ){}
  public record GetRequest(
      String id
  ){}
}
