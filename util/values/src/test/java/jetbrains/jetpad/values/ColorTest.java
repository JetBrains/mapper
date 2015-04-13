package jetbrains.jetpad.values;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ColorTest {

  @Test
  public void parseHex() {
    assertEquals(Color.RED, Color.parseHex(Color.RED.toHexColor()));
  }
}
