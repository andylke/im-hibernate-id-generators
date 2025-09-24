package im.hibernate.id.generators.standard;

/**
 * Centralized {@link StandardSequence} schema-related constants.
 *
 * @author Andy Lian
 */
public final class StandardSequenceConstants {

  private StandardSequenceConstants() {}

  public static final String SEQUENCE_TABLE = "im_standard_sequence";

  public static final String NAME_COLUMN = "name";

  public static final String CURRENT_VALUE_COLUMN = "current_value";

  public static final String CREATED_AT_COLUMN = "created_at";

  public static final String LAST_MODIFIED_AT_COLUMN = "last_modified_at";
}
