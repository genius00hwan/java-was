package application.model;

import application.model.holder.Image;
import java.time.LocalDateTime;

public class Article {
  private long id;
  private String writerId;
  private String title;
  private String content;
  private Image image;


  private LocalDateTime createdAt;

  public Article(String userId, String title, String content, Image image) {
    this.writerId = userId;
    this.title = title;
    this.content = content;
    this.image = image;
    this.createdAt = LocalDateTime.now();
  }

  public long getId() {
    return id;
  }

  public String getWriterId() {
    return writerId;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }

  public Image getImage() {
    return image;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

}
