package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StandardSequenceDatabaseObject}
 *
 * @author Andy Lian
 */
class StandardSequenceDatabaseObjectTests {

  private StandardSequenceDatabaseObject databaseObject;
  private SqlStringGenerationContext context;

  @BeforeEach
  void beforeEach() {
    databaseObject = new StandardSequenceDatabaseObject();
    context = mock(SqlStringGenerationContext.class);
  }

  @Test
  void sqlCreateStrings() {
    String[] createStrings = databaseObject.sqlCreateStrings(context);
    assertThat(createStrings).isNotNull().hasSize(1);
    assertThat(createStrings[0])
        .isEqualTo(
            "CREATE TABLE "
                + StandardSequenceConstants.SEQUENCE_TABLE
                + "("
                + StandardSequenceConstants.NAME_COLUMN
                + " VARCHAR(100) NOT NULL PRIMARY KEY,"
                + StandardSequenceConstants.CURRENT_VALUE_COLUMN
                + " BIGINT NOT NULL,"
                + StandardSequenceConstants.CREATED_AT_COLUMN
                + " TIMESTAMP NOT NULL,"
                + StandardSequenceConstants.LAST_MODIFIED_AT_COLUMN
                + " TIMESTAMP"
                + ")");
  }

  @Test
  void sqlDropStrings() {
    String[] dropStrings = databaseObject.sqlDropStrings(context);
    assertThat(dropStrings).isNotNull().hasSize(1);
    assertThat(dropStrings[0])
        .isEqualTo("DROP TABLE IF EXISTS " + StandardSequenceConstants.SEQUENCE_TABLE);
  }
}
