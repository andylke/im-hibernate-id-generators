package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StandardSequenceStrategy}
 *
 * @author Andy Lian
 */
class StandardSequenceStrategyTests {

  @Test
  void instantiate_throws_whenNameIsEmpty() throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(name = "")
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    assertThrows(IdentifierGenerationException.class, () -> new StandardSequenceStrategy(config));
  }

  @Test
  void instantiate_throws_whenMaxValueLessThanEqualsInitialValue()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(name = "test-sequence", initialValue = 2, maxValue = 1)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    assertThrows(IdentifierGenerationException.class, () -> new StandardSequenceStrategy(config));
  }

  @Test
  void instantiate_throws_whenIncrementValueLessThanEqualsZero()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(name = "test-sequence", incrementValue = 0)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    assertThrows(IdentifierGenerationException.class, () -> new StandardSequenceStrategy(config));
  }

  @Test
  void initialState_whenIsNotDescending() throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          descending = false)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState initialState = strategy.initialState();

    assertThat(initialState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(10L);
  }

  @Test
  void initialState_whenIsDescending() throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          descending = true)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState initialState = strategy.initialState();

    assertThat(initialState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(20L);
  }

  @Test
  void nextState_whenIsAscending_nextValueLessThanEqualsMaxValue()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = false,
          descending = false)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState nextState = strategy.nextState(new StandardSequenceState(15L));

    assertThat(nextState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(16L);
  }

  @Test
  void nextState_whenIsAscending_nextValueGreaterThanEqualsMaxValue_cycling()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = true,
          descending = false)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState nextState = strategy.nextState(new StandardSequenceState(20L));

    assertThat(nextState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(10L);
  }

  @Test
  void nextState_whenIsAscending_nextValueGreaterThanEqualsMaxValue_nonCycling()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = false,
          descending = false)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    assertThrows(
        IdentifierGenerationException.class,
        () -> strategy.nextState(new StandardSequenceState(20L)));
  }

  @Test
  void nextState_whenIsDescending_nextValueLessThanEqualsMaxValue()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = false,
          descending = true)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState nextState = strategy.nextState(new StandardSequenceState(15L));

    assertThat(nextState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(14L);
  }

  @Test
  void nextState_whenIsDescending_nextValueGreaterThanEqualsMaxValue_cycling()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = true,
          descending = true)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    StandardSequenceState nextState = strategy.nextState(new StandardSequenceState(10L));

    assertThat(nextState)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(20L);
  }

  @Test
  void nextState_whenIsDescending_nextValueGreaterThanEqualsMaxValue_nonCycling()
      throws NoSuchFieldException, SecurityException {
    class Local {

      @StandardSequence(
          name = "test-sequence",
          initialValue = 10L,
          maxValue = 20L,
          incrementValue = 1,
          cycle = false,
          descending = true)
      long id;
    }

    StandardSequence config =
        Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    StandardSequenceStrategy strategy = new StandardSequenceStrategy(config);
    assertThrows(
        IdentifierGenerationException.class,
        () -> strategy.nextState(new StandardSequenceState(10L)));
  }
}
