package im.hibernate.id.generators.standard;

import java.lang.reflect.Member;
import java.util.EnumSet;
import org.apache.commons.lang3.Validate;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.id.IdentifierGenerationException;
import org.hibernate.id.factory.spi.CustomIdGeneratorCreationContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Standard sequence generator that produces unique, incrementing values with optional cycling.
 *
 * @author Andy Lian
 */
@Slf4j
public class StandardSequenceGenerator implements BeforeExecutionGenerator {

  private static final long serialVersionUID = 1L;

  private final StandardSequenceStrategy strategy;
  private final StandardSequence config;
  private final StandardSequenceQuery query;

  public StandardSequenceGenerator(
      StandardSequence config, Member member, CustomIdGeneratorCreationContext creationContext) {
    this(
        config,
        new StandardSequenceStrategy(config),
        new StandardSequenceQuery(
            config.sequenceTable(),
            config.nameColumn(),
            config.currentValueColumn(),
            config.createdAtColumn(),
            config.lastModifiedAtColumn(),
            config.name()));
  }

  StandardSequenceGenerator(
      StandardSequence config, StandardSequenceStrategy strategy, StandardSequenceQuery query) {
    this.config = Validate.notNull(config, "config cannot be null");
    this.strategy = Validate.notNull(strategy, "strategy cannot be null");
    this.query = Validate.notNull(query, "query cannot be null");

    log.debug(
        "Initialized {} for sequence '{}' on table '{}'",
        this.getClass(),
        config.name(),
        config.sequenceTable());
  }

  @Override
  public EnumSet<EventType> getEventTypes() {
    return EventTypeSets.INSERT_ONLY;
  }

  @Override
  public Object generate(
      SharedSessionContractImplementor session,
      Object owner,
      Object currentValue,
      EventType eventType) {
    if (eventType != EventType.INSERT) {
      String msg = "Identifier generator for '" + owner + "' only supports INSERT event";
      log.error(msg);
      throw new IdentifierGenerationException(msg);
    }

    log.debug("Generating identifier for '{}' using sequence '{}'", owner, config.name());
    StandardSequenceState currentState = query.load(session);

    if (currentState == null) {
      StandardSequenceState initialState = strategy.initialState();
      query.insert(session, initialState);

      log.debug(
          "Initialized identifier '{}' for '{}' using sequence '{}'",
          initialState.getCurrentValue(),
          owner,
          config.name());
      return initialState.getCurrentValue();

    } else {
      StandardSequenceState nextState = strategy.nextState(currentState);
      query.update(session, nextState);

      log.debug(
          "Generated identifier '{}' for '{}' using sequence '{}'",
          nextState.getCurrentValue(),
          owner,
          config.name());
      return nextState.getCurrentValue();
    }
  }
}
