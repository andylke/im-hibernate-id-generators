package im.hibernate.id.generators;

/**
 * Strategy interface for managing sequence state, defining operations to derive initial state and
 * next state from the current one.
 *
 * @author Andy Lian
 */
public interface SequenceStrategy<T> {

  T initialState();

  T nextState(T currentState);
}
