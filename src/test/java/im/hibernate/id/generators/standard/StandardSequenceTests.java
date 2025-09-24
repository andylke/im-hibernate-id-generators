package im.hibernate.id.generators.standard;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link StandardSequence}
 *
 * @author Andy Lian
 */
class StandardSequenceTests {

  static class Local {

    @StandardSequence(name = "test")
    private long id;
  }

  static StandardSequence annotation;

  @BeforeAll
  static void beforeAll() throws NoSuchFieldException, SecurityException {
    annotation = Local.class.getDeclaredField("id").getAnnotation(StandardSequence.class);
  }

  @Test
  void sequenceTable_defaultValue() {
    assertThat(annotation.sequenceTable()).isEqualTo("im_standard_sequence");
  }

  @Test
  void nameColumn_defaultValue() {
    assertThat(annotation.nameColumn()).isEqualTo("name");
  }

  @Test
  void nextValueColumn_defaultValue() {
    assertThat(annotation.currentValueColumn()).isEqualTo("current_value");
  }

  @Test
  void initialValue_defaultValue() {
    assertThat(annotation.initialValue()).isZero();
  }

  @Test
  void maxValue_defaultValue() {
    assertThat(annotation.maxValue()).isEqualTo(Long.MAX_VALUE);
  }

  @Test
  void incrementValue_defaultValue() {
    assertThat(annotation.incrementValue()).isOne();
  }

  @Test
  void descending_defaultValue() {
    assertThat(annotation.descending()).isFalse();
  }

  @Test
  void cycle_defaultValue() {
    assertThat(annotation.cycle()).isFalse();
  }
}
