package application.db;

import annotation.Singleton;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Singleton
public class DataSource {

  private static final String JDBC_URL = "jdbc:h2:./data/testdb";
  private static final String USER = "sa";
  private static final String PASSWORD = "";

  public DataSource() {
    try {
      Class.forName("org.h2.Driver");
      initSchema();
    } catch (Exception e) {
      throw new RuntimeException("H2 DataSource Init Failed", e);
    }
  }

  private void initSchema() throws SQLException {
    try (Connection conn = getConnection();
        Statement stmt = conn.createStatement()) {

      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS USERS (" +
              "USER_ID VARCHAR(50) PRIMARY KEY, " +
              "PASSWORD VARCHAR(100) NOT NULL, " +
              "NICKNAME VARCHAR(50) NOT NULL, " +
              "EMAIL VARCHAR(100) NOT NULL UNIQUE, " +
              "IMAGE BLOB,"+
              "IMAGE_TYPE VARCHAR(50)" +
              ")"
      );
      stmt.executeUpdate(
          "CREATE TABLE IF NOT EXISTS ARTICLE (" +
              "ID BIGINT AUTO_INCREMENT PRIMARY KEY, " +
              "WRITER_ID VARCHAR(50) NOT NULL, " +
              "TITLE VARCHAR(200) NOT NULL, " +
              "CONTENT TEXT, " +
              "IMAGE BLOB,"+
              "IMAGE_TYPE VARCHAR(50)," +
              "CREATED_AT TIMESTAMP, " +
              "FOREIGN KEY (WRITER_ID) REFERENCES USERS(USER_ID)" +
              ")"
      );
    }
  }



  public Connection getConnection() throws SQLException {
    return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
  }
}

