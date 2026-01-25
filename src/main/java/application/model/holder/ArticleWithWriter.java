package application.model.holder;

public record ArticleWithWriter(
    String writer,
    long articleId,
    String title,
    String content,
    Image image) {
  // Compact Constructor: null 체크
  public ArticleWithWriter {
    if (writer == null || title == null || content == null) {
      throw new IllegalArgumentException("writer, title, content는 null일 수 없습니다.");
    }
  }

  // 기존 getLink() 유지
  public String getLink() {
    return "/article?id=" + articleId;
  }
}
