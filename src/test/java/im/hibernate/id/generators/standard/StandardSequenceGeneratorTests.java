package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.EventType;
import org.hibernate.generator.EventTypeSets;
import org.hibernate.id.IdentifierGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StandardSequenceGenerator}.
 *
 * @author Andy Lian
 */
class StandardSequenceGeneratorTests {

  private StandardSequence config;
  private StandardSequenceStrategy strategy;
  private StandardSequenceQuery query;

  static class Local {

    @StandardSequence(name = "test")
    private long id;
  }

  @BeforeEach
  void beforeEach() throws NoSuchFieldException, SecurityException {
    config = Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
    strategy = mock(StandardSequenceStrategy.class);
    query = mock(StandardSequenceQuery.class);
  }

  @Test
  void getEventTypes() throws NoSuchFieldException, SecurityException {
    StandardSequenceGenerator generator = new StandardSequenceGenerator(config, strategy, query);

    assertThat(generator.getEventTypes()).isEqualTo(EventTypeSets.INSERT_ONLY);
  }

  @Test
  void generate_throws_whenEventTypesIsUpdate() throws NoSuchFieldException, SecurityException {
    SharedSessionContractImplementor session = mock(SharedSessionContractImplementor.class);
    Object owner = new Object();

    StandardSequenceGenerator generator = new StandardSequenceGenerator(config, strategy, query);

    assertThrows(
        IdentifierGenerationException.class,
        () -> generator.generate(session, owner, null, EventType.UPDATE));
  }

  @Test
  void generate_initialState_whenLoadReturnNull() {
    SharedSessionContractImplementor session = mock(SharedSessionContractImplementor.class);
    Object owner = new Object();

    when(query.load(any())).thenReturn(null);
    when(strategy.initialState()).thenReturn(new StandardSequenceState(10L));

    StandardSequenceGenerator generator = new StandardSequenceGenerator(config, strategy, query);

    assertThat(generator.generate(session, owner, null, EventType.INSERT)).isEqualTo(10L);

    verify(query, times(1)).load(any());
    verify(strategy, times(1)).initialState();
  }

  @Test
  void generate_nextState_whenLoadReturnState() {
    SharedSessionContractImplementor session = mock(SharedSessionContractImplementor.class);
    Object owner = new Object();

    when(query.load(any())).thenReturn(new StandardSequenceState(10L));
    when(strategy.nextState(any())).thenReturn(new StandardSequenceState(11L));

    StandardSequenceGenerator generator = new StandardSequenceGenerator(config, strategy, query);

    assertThat(generator.generate(session, owner, null, EventType.INSERT)).isEqualTo(11L);

    verify(query, times(1)).load(any());
    verify(strategy, times(1)).nextState(any());
  }
}
