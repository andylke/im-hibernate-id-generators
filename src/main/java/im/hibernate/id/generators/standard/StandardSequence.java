package im.hibernate.id.generators.standard;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.hibernate.annotations.IdGeneratorType;

/**
 * Standard sequence generator for unique identifier values.
 *
 * @author Andy Lian
 */
@IdGeneratorType(StandardSequenceGenerator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface StandardSequence {

  String sequenceTable() default StandardSequenceConstants.SEQUENCE_TABLE;

  String nameColumn() default StandardSequenceConstants.NAME_COLUMN;

  String currentValueColumn() default StandardSequenceConstants.CURRENT_VALUE_COLUMN;

  String createdAtColumn() default StandardSequenceConstants.CREATED_AT_COLUMN;

  String lastModifiedAtColumn() default StandardSequenceConstants.LAST_MODIFIED_AT_COLUMN;

  String name();

  long initialValue() default 0;

  long maxValue() default Long.MAX_VALUE;

  int incrementValue() default 1;

  boolean descending() default false;

  boolean cycle() default false;
}
