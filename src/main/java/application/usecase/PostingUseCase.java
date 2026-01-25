package application.usecase;

import annotation.Singleton;
import application.db.ArticleDao;
import application.model.Article;
import application.request.ArticleRequest;
import application.utils.ValidateUtil;

@Singleton
public class PostingUseCase {

  private ArticleDao dao;

  public PostingUseCase(ArticleDao dao) {
    this.dao = dao;
  }

  public boolean postArticle(ArticleRequest.CreateRequest request) {
    if (!isValidArticle(request)) {
      return false;
    }
    Article article = new Article(
        request.user().userId(),
        request.title(),
        request.content(),
        request.image()
    );

    dao.add(article);
    return true;
  }

  private boolean isValidArticle(ArticleRequest.CreateRequest request) {
    return !ValidateUtil.isNullOrBlank(request.title(), request.content());
  }
}
