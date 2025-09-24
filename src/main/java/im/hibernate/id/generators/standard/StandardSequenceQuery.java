package im.hibernate.id.generators.standard;

import java.time.LocalDateTime;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import im.hibernate.id.generators.SequenceQuery;
import jakarta.persistence.Tuple;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides methods to load, insert, and update {@link StandardSequenceState} record using {@link
 * SharedSessionContractImplementor}.
 *
 * @author Andy Lian
 */
@AllArgsConstructor
@Slf4j
public class StandardSequenceQuery implements SequenceQuery<StandardSequenceState> {

  private final String tableName;
  private final String nameColumn;
  private final String currentValueColumn;
  private final String createdAtColumn;
  private final String lastModifiedAtColumn;
  private final String name;

  @Override
  public StandardSequenceState load(SharedSessionContractImplementor session) {
    log.debug("Loading current state for sequence '{}' from table '{}'", name, tableName);

    Tuple result =
        session
            .createNativeQuery(
                "SELECT "
                    + currentValueColumn
                    + " FROM "
                    + tableName
                    + " WHERE "
                    + nameColumn
                    + " = :name"
                    + " FOR UPDATE",
                Tuple.class)
            .setParameter("name", name)
            .uniqueResult();
    if (result == null) {
      log.info("No existing state found for sequence '{}'", name);
      return null;
    }

    long currentValue = result.get(currentValueColumn, Long.class).longValue();
    log.debug("Loaded currentValue={} for sequence '{}'", currentValue, name);

    return new StandardSequenceState(currentValue);
  }

  @Override
  public void insert(SharedSessionContractImplementor session, StandardSequenceState initialState) {
    long currentValue = initialState.getCurrentValue();
    log.debug("Inserting initial state with currentValue={} for sequence '{}'", currentValue, name);

    int affectedRows =
        session
            .createNativeMutationQuery(
                "INSERT INTO "
                    + tableName
                    + "("
                    + nameColumn
                    + ", "
                    + currentValueColumn
                    + ", "
                    + createdAtColumn
                    + ") "
                    + "VALUES("
                    + ":name"
                    + ", :currentValue"
                    + ", :createdAt"
                    + ")")
            .setParameter("name", name)
            .setParameter("currentValue", currentValue)
            .setParameter("createdAt", LocalDateTime.now())
            .executeUpdate();
    if (affectedRows == 0) {
      String msg =
          "Failed to insert sequence state for [" + name + "]. Affected rows = " + affectedRows;
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    log.debug("Inserted initial state with currentValue={} for sequence '{}'", currentValue, name);
  }

  @Override
  public StandardSequenceState update(
      SharedSessionContractImplementor session, StandardSequenceState nextState) {
    long currentValue = nextState.getCurrentValue();
    log.debug("Updating next state with currentValue={} for sequence '{}'", currentValue, name);

    int affectedRows =
        session
            .createNativeMutationQuery(
                "UPDATE "
                    + tableName
                    + " SET "
                    + currentValueColumn
                    + " = :currentValue, "
                    + lastModifiedAtColumn
                    + " = :lastModifiedAt"
                    + " WHERE "
                    + nameColumn
                    + " = :name")
            .setParameter("name", name)
            .setParameter("currentValue", currentValue)
            .setParameter("lastModifiedAt", LocalDateTime.now())
            .executeUpdate();
    if (affectedRows == 0) {
      String msg =
          "Failed to update sequence state for [" + name + "]. Affected rows = " + affectedRows;
      log.error(msg);
      throw new IllegalStateException(msg);
    }

    log.debug("Updated next state with currentValue={} for sequence '{}'", currentValue, name);
    return new StandardSequenceState(currentValue);
  }
}
