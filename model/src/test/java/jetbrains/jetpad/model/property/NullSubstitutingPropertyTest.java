package jetbrains.jetpad.model.property;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NullSubstitutingPropertyTest {
  private static final String SAMPLE = "abc";
  private static final String NULL_SUBSTITUTE = "";

  private Property<String> target = new ValueProperty<>();
  private Property<String> substituting = new NullSubstitutingProperty<>(target, new NullSubstitutionSpec<String>() {
    @Override
    public String createSubstitute() {
      return NULL_SUBSTITUTE;
    }
    @Override
    public boolean isSubstitute(String value) {
      return NULL_SUBSTITUTE.equals(value);
    }
  });

  @Test
  public void forwardNonNull() {
    substituting.set(SAMPLE);
    assertEquals(SAMPLE, substituting.get());
    assertEquals(SAMPLE, target.get());
  }

  @Test
  public void backwardNonNull() {
    target.set(SAMPLE);
    assertEquals(SAMPLE, substituting.get());
    assertEquals(SAMPLE, target.get());
  }

  @Test
  public void forwardNull() {
    substituting.set(null);
    assertEquals(null, substituting.get());
    assertEquals(NULL_SUBSTITUTE, target.get());
  }

  @Test
  public void backwardSubstitute() {
    target.set(NULL_SUBSTITUTE);
    assertEquals(null, substituting.get());
    assertEquals(NULL_SUBSTITUTE, target.get());
  }
}
