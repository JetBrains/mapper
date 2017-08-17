package jetbrains.jetpad.values;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ColorsTest {
  @Test
  public void namedColors() {
    assertTrue(Colors.isColorName("pink"));
    assertTrue(Colors.isColorName("pInk"));
    assertFalse(Colors.isColorName("unknown"));

    assertNotNull(Colors.forName("red"));
    assertNotNull(Colors.forName("rEd"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void unknownColor() {
    Colors.forName("unknown");
  }
}
