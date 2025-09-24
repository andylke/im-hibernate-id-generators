package im.hibernate.id.generators.standard;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.hibernate.id.IdentifierGenerationException;
import im.hibernate.id.generators.SequenceStrategy;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link SequenceStrategy} for {@link StandardSequenceGenerator}.
 *
 * @author Andy Lian
 */
@Slf4j
public class StandardSequenceStrategy implements SequenceStrategy<StandardSequenceState> {

  private final StandardSequence config;

  public StandardSequenceStrategy(StandardSequence config) {
    Validate.notNull(config, "config cannot be null");

    if (StringUtils.isEmpty(config.name())) {
      throw new IdentifierGenerationException(
          "Invalid sequence configuration. name must not be empty");
    }

    if (config.maxValue() <= config.initialValue()) {
      throw new IdentifierGenerationException(
          "Invalid sequence configuration. maxValue ("
              + config.maxValue()
              + ") must be greater than initialValue ("
              + config.initialValue()
              + ")");
    }

    if (config.incrementValue() <= 0) {
      throw new IdentifierGenerationException(
          "Invalid sequence configuration. incrementValue ("
              + config.incrementValue()
              + ") must be greater than 0");
    }

    this.config = config;
    log.debug(
        "Initialized {} with "
            + "name={}, initialValue={}, maxValue={}, incrementValue={}, descending={}, cycle={}",
        this.getClass().getSimpleName(),
        config.name(),
        config.initialValue(),
        config.maxValue(),
        config.incrementValue(),
        config.descending(),
        config.cycle());
  }

  public StandardSequenceState initialState() {
    long initialValue = config.descending() ? config.maxValue() : config.initialValue();
    log.debug(
        "Creating initial state for sequence '{}' with value={}", config.name(), initialValue);
    return new StandardSequenceState(initialValue);
  }

  @Override
  public StandardSequenceState nextState(StandardSequenceState currentState) {
    Validate.notNull(currentState, "currentState cannot be null");
    log.debug(
        "Calculating next state for sequence '{}' from currentValue={} (descending={})",
        config.name(),
        currentState.getCurrentValue(),
        config.descending());

    return config.descending()
        ? nextDescendingState(currentState)
        : nextAccendingState(currentState);
  }

  private StandardSequenceState nextAccendingState(StandardSequenceState currentState) {
    long currentValue = currentState.getCurrentValue();
    long nextValue = Math.addExact(currentValue, config.incrementValue());
    if (nextValue <= config.maxValue()) {
      log.debug("Next ascending value for sequence '{}' = {}", config.name(), nextValue);
      return new StandardSequenceState(nextValue);
    } else if (config.cycle()) {
      log.warn(
          "Ascending sequence '{}' reached maxValue={} at currentValue={}. Cycling back to initialValue={}",
          config.name(),
          config.maxValue(),
          currentValue,
          config.initialValue());
      return initialState();
    } else {
      String msg =
          "Sequence '"
              + config.name()
              + "' exhausted. nextValue ("
              + nextValue
              + ") exceeded maxValue ("
              + config.maxValue()
              + ")";
      log.error(msg);
      throw new IdentifierGenerationException(msg);
    }
  }

  private StandardSequenceState nextDescendingState(StandardSequenceState currentState) {
    long currentValue = currentState.getCurrentValue();
    long nextValue = Math.subtractExact(currentValue, config.incrementValue());
    if (nextValue >= config.initialValue()) {
      log.debug("Next descending value for sequence '{}' = {}", config.name(), nextValue);
      return new StandardSequenceState(nextValue);
    } else if (config.cycle()) {
      log.warn(
          "Descending sequence '{}' reached initialValue={} at currentValue={}. Cycling back to maxValue={}",
          config.name(),
          config.initialValue(),
          currentValue,
          config.maxValue());
      return initialState();
    } else {
      String msg =
          "Sequence '"
              + config.name()
              + "' exhausted: nextValue ("
              + nextValue
              + ") went below initialValue ("
              + config.initialValue()
              + ")";
      log.error(msg);
      throw new IdentifierGenerationException(msg);
    }
  }
}
