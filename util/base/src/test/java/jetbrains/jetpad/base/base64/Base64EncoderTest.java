package jetbrains.jetpad.base.base64;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class Base64EncoderTest {
  @Test
  public void lenIs0Mod3() {
    assertEquals("Q2F0", Base64Coder.encode(new byte[] { 'C', 'a', 't' }));
  }

  @Test
  public void lenIs1Mod3() {
    assertEquals("Q2F0cw==", Base64Coder.encode(new byte[] { 'C', 'a', 't', 's' }));
  }

  @Test
  public void lenIs2Mod3() {
    assertEquals("QnJhaW4=", Base64Coder.encode(new byte[] { 'B', 'r', 'a', 'i', 'n' }));
  }

}
