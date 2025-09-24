package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.hibernate.dialect.H2Dialect;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Database integration tests for {@link StandardSequenceDatabaseObject}
 *
 * @author Andy Lian
 */
class StandardSequenceDatabaseObjecIntegrationtTests {

  private StandardSequenceDatabaseObject databaseObject;
  private SqlStringGenerationContext context;

  private static Connection connection;

  @BeforeAll
  static void beforeAll() throws SQLException {
    connection = DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
  }

  @AfterAll
  static void afterAll() throws SQLException {
    connection.close();
  }

  @BeforeEach
  void beforeEach() {
    databaseObject = new StandardSequenceDatabaseObject();
    context = mock(SqlStringGenerationContext.class);

    when(context.getDialect()).thenReturn(new H2Dialect());
  }

  @Test
  void executeCreateAndDropSqlString() throws SQLException {
    String[] createSqls = databaseObject.sqlCreateStrings(context);
    assertThat(createSqls).hasSize(1);
    executeSql(createSqls[0]);

    assertThat(tableExists(StandardSequenceConstants.SEQUENCE_TABLE)).isTrue();
    assertThat(
            columnExists(
                StandardSequenceConstants.SEQUENCE_TABLE, StandardSequenceConstants.NAME_COLUMN))
        .isTrue();
    assertThat(
            columnExists(
                StandardSequenceConstants.SEQUENCE_TABLE,
                StandardSequenceConstants.CURRENT_VALUE_COLUMN))
        .isTrue();
    assertThat(
            columnExists(
                StandardSequenceConstants.SEQUENCE_TABLE,
                StandardSequenceConstants.CREATED_AT_COLUMN))
        .isTrue();
    assertThat(
            columnExists(
                StandardSequenceConstants.SEQUENCE_TABLE,
                StandardSequenceConstants.LAST_MODIFIED_AT_COLUMN))
        .isTrue();

    String[] dropSqls = databaseObject.sqlDropStrings(context);
    assertThat(dropSqls).hasSize(1);
    executeSql(dropSqls[0]);
    assertThat(tableExists(StandardSequenceConstants.SEQUENCE_TABLE)).isFalse();
  }

  private void executeSql(String sql) throws SQLException {
    try (Statement statement = connection.createStatement()) {
      statement.execute(sql);
    }
  }

  private boolean tableExists(String tableName) throws SQLException {
    try (ResultSet rs =
        connection.getMetaData().getTables(null, null, tableName.toUpperCase(), null)) {
      return rs.next();
    }
  }

  private boolean columnExists(String tableName, String columnName) throws SQLException {
    try (ResultSet rs =
        connection
            .getMetaData()
            .getColumns(null, null, tableName.toUpperCase(), columnName.toUpperCase())) {
      return rs.next();
    }
  }
}
