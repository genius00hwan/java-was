package application.db;

import annotation.Singleton;
import application.model.User;
import application.model.holder.Image;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class UserDao {

  private static final Logger log = LoggerFactory.getLogger(UserDao.class);
  private final DataSource dataSource;

  public UserDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  // 사용자 추가
  public void addUser(User user) {
    String sql = "INSERT INTO USERS (USER_ID, PASSWORD, NICKNAME, EMAIL) VALUES (?, ?, ?, ?)";

    log.info("[SQL ] {}", sql);
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, user.userId());
      pstmt.setString(2, user.password());
      pstmt.setString(3, user.getNickname());
      pstmt.setString(4, user.getEmail());
      pstmt.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException("Insert User Failed", e);
    }
  }

  public void updateUser(User user) {
    String sql = "UPDATE USERS SET PASSWORD = ?, NICKNAME = ?, IMAGE =?, IMAGE_TYPE = ? WHERE USER_ID = ?";

//    log.info("[SQL ] {}", sql);
    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, user.password());
      pstmt.setString(2, user.getNickname());
      pstmt.setBytes(3, user.getImage().data());
      pstmt.setString(4, user.getImage().type());
      pstmt.setString(5, user.userId());
      log.info("[SQL ] {}", pstmt);
      pstmt.executeUpdate();

    }catch (SQLException e) {
      throw new RuntimeException("Insert User Failed", e);
    }
  }

  // userId로 조회
  public User findUserById(String userId) {
    String sql = "SELECT USER_ID, PASSWORD, NICKNAME, EMAIL, IMAGE, IMAGE_TYPE FROM USERS WHERE USER_ID = ?";

    log.info("[SQL ] {}", sql);

    try (Connection conn = dataSource.getConnection();
        PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setString(1, userId);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        return User.fromDb(
            rs.getString("USER_ID"),
            rs.getString("PASSWORD"),
            rs.getString("NICKNAME"),
            rs.getString("EMAIL"),
            new Image(
                rs.getBytes("IMAGE"),
                rs.getString("IMAGE_TYPE")
            )
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find User Failed", e);
    }
    return null;
  }

  public Collection<String> findAllIds() {
    String sql = "SELECT USER_ID FROM USERS";

    log.info("[SQL ] {}", sql);

    List<String> userIds = new ArrayList<>();

    try (Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {

      while (rs.next()) {
        userIds.add(
            rs.getString("USER_ID")
        );
      }
    } catch (SQLException e) {
      throw new RuntimeException("Find All Users Failed", e);
    }
    return userIds;
  }


}
