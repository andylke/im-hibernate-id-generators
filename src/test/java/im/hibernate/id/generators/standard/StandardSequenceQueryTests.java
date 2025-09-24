package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.sql.spi.NativeQueryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import jakarta.persistence.Tuple;

/**
 * Tests for {@link StandardSequenceQuery}.
 *
 * @author Andy Lian
 */
class StandardSequenceQueryTests {

  private SharedSessionContractImplementor session;
  private StandardSequenceQuery query;

  @BeforeEach
  void beforeEach() {
    session = mock(SharedSessionContractImplementor.class);
    query =
        new StandardSequenceQuery(
            "test_standard_sequence",
            "name",
            "current_value",
            "created_at",
            "last_modified_at",
            "test-sequence");
  }

  @Test
  void loan_returnNull_whenNoRowFound() {
    @SuppressWarnings("unchecked")
    NativeQueryImplementor<Tuple> nativeQuery = mock(NativeQueryImplementor.class);
    when(session.createNativeQuery(anyString(), eq(Tuple.class))).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(eq("name"), any())).thenReturn(nativeQuery);
    when(nativeQuery.uniqueResult()).thenReturn(null);

    StandardSequenceState state = query.load(session);
    assertThat(state).isNull();
  }

  @Test
  void load_returnState_whenRowFound() {
    @SuppressWarnings("unchecked")
    NativeQueryImplementor<Tuple> nativeQuery = mock(NativeQueryImplementor.class);
    when(session.createNativeQuery(anyString(), eq(Tuple.class))).thenReturn(nativeQuery);
    when(nativeQuery.setParameter(eq("name"), any())).thenReturn(nativeQuery);

    Tuple result = mock(Tuple.class);
    when(result.get("current_value", Long.class)).thenReturn(123L);
    when(nativeQuery.uniqueResult()).thenReturn(result);

    StandardSequenceState state = query.load(session);
    assertThat(state)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(123L);
  }

  @Test
  void insert_success_whenAffectedRowsNonZero() {
    MutationQuery mutationQuery = mock(MutationQuery.class);
    when(session.createNativeMutationQuery(anyString())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("name"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("currentValue"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("createdAt"), any())).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1);

    query.insert(session, new StandardSequenceState(1));
    verify(mutationQuery, times(1)).executeUpdate();
  }

  @Test
  void insert_throws_whenAffectedRowsZero() {
    MutationQuery mutationQuery = mock(MutationQuery.class);
    when(session.createNativeMutationQuery(anyString())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("name"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("currentValue"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("createdAt"), any())).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(0);

    assertThrowsExactly(
        IllegalStateException.class, () -> query.insert(session, new StandardSequenceState(123)));
    verify(mutationQuery, times(1)).executeUpdate();
  }

  @Test
  void update_success_whenAffectedRowsNonZero() {
    MutationQuery mutationQuery = mock(MutationQuery.class);
    when(session.createNativeMutationQuery(anyString())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("name"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("currentValue"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("lastModifiedAt"), any())).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(1);

    StandardSequenceState state = query.update(session, new StandardSequenceState(123));
    assertThat(state)
        .isNotNull()
        .extracting(StandardSequenceState::getCurrentValue)
        .isEqualTo(123L);
    verify(mutationQuery, times(1)).executeUpdate();
  }

  @Test
  void update_throws_whenAffectedRowsZero() {
    MutationQuery mutationQuery = mock(MutationQuery.class);
    when(session.createNativeMutationQuery(anyString())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("name"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("currentValue"), any())).thenReturn(mutationQuery);
    when(mutationQuery.setParameter(eq("lastModifiedAt"), any())).thenReturn(mutationQuery);
    when(mutationQuery.executeUpdate()).thenReturn(0);

    assertThrowsExactly(
        IllegalStateException.class, () -> query.update(session, new StandardSequenceState(123)));
    verify(mutationQuery, times(1)).executeUpdate();
  }
}
