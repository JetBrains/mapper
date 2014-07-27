package jetbrains.jetpad.base;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ValidatorsTest {
  @Test
  public void signedInteger() {
    assertTrue(Validators.integer().apply("239"));
    assertTrue(Validators.integer().apply("-239"));
    assertFalse(Validators.integer().apply(null));
  }

  @Test
  public void unsignedInteger() {
    assertTrue(Validators.unsignedInteger().apply("239"));
    assertFalse(Validators.unsignedInteger().apply("-239"));
    assertFalse(Validators.unsignedInteger().apply("+239"));
    assertFalse(Validators.unsignedInteger().apply(null));
  }
}
