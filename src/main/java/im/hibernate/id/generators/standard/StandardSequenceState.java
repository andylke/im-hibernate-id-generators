package im.hibernate.id.generators.standard;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the state of a standard sequence.
 *
 * @author Andy Lian
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class StandardSequenceState {

  private long currentValue;
}
