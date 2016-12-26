package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompositeRegistrationTest extends BaseTestCase {
  private int myRemoveCounter = 0;

  @Test
  public void removalOrder() {
    CompositeRegistration r = new CompositeRegistration(createReg(1), createReg(0));
    r.remove();
    assertEquals(2, myRemoveCounter);
  }

  @Test
  public void removalOrderManualAdd() {
    CompositeRegistration r = new CompositeRegistration();
    r.add(createReg(1)).add(createReg(0));
    r.remove();
    assertEquals(2, myRemoveCounter);
  }

  private Registration createReg(final int expectedOrder) {
    return new Registration() {
      @Override
      protected void doRemove() {
        assertEquals(expectedOrder, myRemoveCounter++);
      }
    };
  }
}