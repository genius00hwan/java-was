package application.db;

import annotation.Singleton;
import application.model.Article;
import application.model.holder.ArticleWithWriter;
import application.model.holder.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class ArticleDao {

  private static final Logger log = LoggerFactory.getLogger(ArticleDao.class);
  private final DataSource dataSource;

  public ArticleDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public void add(Article article) {
    String sql = """
        INSERT INTO ARTICLE 
            (WRITER_ID, TITLE, CONTENT, IMAGE, IMAGE_TYPE, CREATED_AT) 
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    log.info("[SQL] {}", sql);

    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, article.getWriterId());
      pstmt.setString(2, article.getTitle());
      pstmt.setString(3, article.getContent());

      if (article.getImage() != null) {
        pstmt.setBytes(4, article.getImage().data());
        pstmt.setString(5, article.getImage().type());
      } else {
        pstmt.setNull(4, Types.BLOB);
        pstmt.setNull(5, Types.VARCHAR);
      }

      pstmt.setTimestamp(6, Timestamp.valueOf(article.getCreatedAt()));

      pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Insert Article Failed", e);
    }
  }

  // ID로 단일 글 조회
  public ArticleWithWriter findById(Long id) {
    String sql = """
            SELECT U.NICKNAME AS WRITER, A.TITLE, A.CONTENT, A.ID, A.IMAGE, A.IMAGE_TYPE
            FROM ARTICLE A
            JOIN USERS U ON A.WRITER_ID = U.USER_ID
            WHERE A.ID = ?
        """;
    log.info("[SQL ] {}", sql);
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setLong(1, id);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return new ArticleWithWriter(
              rs.getString("WRITER"),
              rs.getLong("ID"),
              rs.getString("TITLE"),
              rs.getString("CONTENT"),
              new Image(
                  rs.getBytes("IMAGE"),
                  rs.getString("IMAGE_TYPE")
              )
          );
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find Board By Id Failed", e);
    }
    return null;
  }

  public List<ArticleWithWriter> findAll() {
    String sql = """
            SELECT U.NICKNAME 
                AS WRITER, A.TITLE, A.CONTENT, A.ID, A.IMAGE, A.IMAGE_TYPE
            FROM ARTICLE A
            JOIN USERS U ON A.WRITER_ID = U.USER_ID
            ORDER BY A.CREATED_AT DESC
        """;

    log.info("[SQL] {}", sql);
    List<ArticleWithWriter> articles = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql);
        ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        articles.add(
            new ArticleWithWriter(
                rs.getString("WRITER"),
                rs.getLong("ID"),
                rs.getString("TITLE"),
                rs.getString("CONTENT"),
                new Image(
                    rs.getBytes("IMAGE"),
                    rs.getString("IMAGE_TYPE")
                )
            )
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find All Articles Failed", e);
    }

    return articles;
  }


}
