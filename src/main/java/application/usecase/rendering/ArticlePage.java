package application.usecase.rendering;

import annotation.Singleton;
import application.db.ArticleDao;
import application.exception.client.BadRequestException;
import application.exception.server.ResourceMissException;
import application.model.holder.ArticleWithWriter;

import http.body.StaticResourceBody;
import http.request.Path;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import staticresource.StaticFileLoader;

@Singleton
public class ArticlePage implements RenderUseCase<Long> {

  private static final Path ARTICLE_TEMPLATE = new Path("/article/template.html");
  private static final String WRITER_PLACEHOLDER = "{{writer}}";
  private static final String TITLE_PLACEHOLDER = "{{title}}";
  private static final String CONTENT_PLACEHOLDER = "{{content}}";
  private static final String IMAGE_PLACEHOLDER = "{{image}}";
  private static final Logger log = LoggerFactory.getLogger(ArticlePage.class);

  private final ArticleDao dao;

  public ArticlePage(ArticleDao dao) {
    this.dao = dao;
  }

  @Override
  public StaticResourceBody render(Long id) {
    StaticResourceBody resourceBody = StaticFileLoader.tryLoad(ARTICLE_TEMPLATE)
        .orElseThrow(() -> new ResourceMissException(ARTICLE_TEMPLATE.toString()));

    ArticleWithWriter article = dao.findById(id);
    if (article == null) {
      throw new BadRequestException("게시글을 못 찾았어요");
    }
    return RenderUseCase.replacePlaceholder(
        resourceBody,
        Map.of(
            WRITER_PLACEHOLDER, article.writer(),
            TITLE_PLACEHOLDER, article.title(),
            CONTENT_PLACEHOLDER, article.content(),
            IMAGE_PLACEHOLDER, article.image().encodeImage()
        )
    );
  }
}
