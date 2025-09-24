package im.hibernate.id.generators;

import org.hibernate.engine.spi.SharedSessionContractImplementor;

/**
 * Query interface for managing sequence state, defining operations to load the current state,
 * insert initial state and update derived next state using {@link
 * SharedSessionContractImplementor}.
 *
 * @author Andy Lian
 */
public interface SequenceQuery<T> {

  T load(SharedSessionContractImplementor session);

  void insert(SharedSessionContractImplementor session, T initialState);

  T update(SharedSessionContractImplementor session, T nextState);
}
