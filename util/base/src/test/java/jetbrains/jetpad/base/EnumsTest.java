package jetbrains.jetpad.base;

import org.junit.Assert;
import org.junit.Test;

public class EnumsTest {

  @Test
  public void enumParsing() {
    Assert.assertEquals(TestEnum.A, Enums.valueOf(TestEnum.class, "aaa"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void illegalArgument() {
    Enums.valueOf(TestEnum.class, "A");
  }

  enum TestEnum {
    A() {
      @Override
      public String toString() {
        return "aaa";
      }
    },

    B() {
      @Override
      public String toString() {
        return "bbb";
      }
    }
  }
}
