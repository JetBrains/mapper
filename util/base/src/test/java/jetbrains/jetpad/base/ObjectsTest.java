package jetbrains.jetpad.base;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ObjectsTest extends BaseTestCase {
  @Test
  public void bothNull() {
    assertTrue(Objects.equal(null, null));
  }

  @Test
  public void firstNull() {
    assertFalse(Objects.equal(null, new Object()));
  }

  @Test
  public void secondNull() {
    assertFalse(Objects.equal(new Object(), null));
  }

  @Test
  public void objects() {
    assertFalse(Objects.equal(new Object(), new Object()));
    assertTrue(Objects.equal(new Integer("1"), new Integer("1")));
  }

  @Test
  public void strings() {
    assertFalse(Objects.equal("a", "aa"));
    assertFalse(Objects.equal("aa", "a"));
    assertFalse(Objects.equal("aa", "ab"));
    assertTrue(Objects.equal("aa", "aa"));
    assertTrue(Objects.equal("", ""));
  }
}
