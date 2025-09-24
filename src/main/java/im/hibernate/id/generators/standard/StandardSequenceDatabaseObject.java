package im.hibernate.id.generators.standard;

import org.hibernate.boot.model.relational.AbstractAuxiliaryDatabaseObject;
import org.hibernate.boot.model.relational.SqlStringGenerationContext;

/**
 * Auxiliary database object for {@link StandardSequenceState} table, managing it's DDL create and drop.
 *
 * @author Andy Lian
 */
public class StandardSequenceDatabaseObject extends AbstractAuxiliaryDatabaseObject {

  private static final long serialVersionUID = 1L;

  @Override
  public String[] sqlCreateStrings(SqlStringGenerationContext context) {
    return new String[] {
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
          + ")"
    };
  }

  @Override
  public String[] sqlDropStrings(SqlStringGenerationContext context) {
    return new String[] {"DROP TABLE " + StandardSequenceConstants.SEQUENCE_TABLE};
  }
}
