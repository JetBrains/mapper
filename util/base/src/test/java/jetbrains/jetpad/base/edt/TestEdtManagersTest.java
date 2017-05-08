package jetbrains.jetpad.base.edt;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestEdtManagersTest extends BaseTestCase {
  private TestEdtManagers managers = new TestEdtManagers();

  @Test
  public void managerRemovedOnFinish() {
    TestEdtManager manager = managers.createEdtManager("a");
    manager.finish();

    assertTrue(managers.getManagers().isEmpty());
  }

  @Test
  public void managerRemovedOnKill() {
    TestEdtManager manager = managers.createEdtManager("a");
    manager.kill();

    assertTrue(managers.getManagers().isEmpty());
  }

  @Test
  public void managerByFullName() {
    String name = "abc";
    managers.createEdtManager(name);

    assertEquals(name, managers.getEdt(name).getName());
  }

  @Test
  public void managerByNamePart() {
    String name = "abc";
    managers.createEdtManager(name);

    assertEquals(name, managers.getEdt("b").getName());
  }

  @Test(expected = IllegalStateException.class)
  public void noManagerByName() {
    managers.getManager("no manager");
  }

  @Test(expected = IllegalStateException.class)
  public void multipleManagersByNamePart() {
    managers.createEdtManager("ab");
    managers.createEdtManager("ac");

    managers.getManager("a");
  }

  @Test
  public void eventsOrder() {
    final List<Integer> order = new ArrayList<>();

    final TestEventDispatchThread a = managers.createEdtManager("a").getEdt();
    final TestEventDispatchThread b = managers.createEdtManager("b").getEdt();

    b.schedule(new Runnable() {
      @Override
      public void run() {
        order.add(1);
        a.schedule(new Runnable() {
          @Override
          public void run() {
            order.add(2);
          }
        });
      }
    });
    a.schedule(10, new Runnable() {
      @Override
      public void run() {
        order.add(3);
        b.schedule(new Runnable() {
          @Override
          public void run() {
            order.add(4);
          }
        });
      }
    });

    managers.flush(10);
    assertEquals(Arrays.asList(1, 2, 3, 4), order);
  }

  @Test
  public void flushMultipleManagers() {
    TestEdtManagers anotherManagers = new TestEdtManagers();

    final TestEventDispatchThread a = managers.createEdtManager("a").getEdt();
    final TestEventDispatchThread b = anotherManagers.createEdtManager("b").getEdt();

    final List<String> events = new ArrayList<>();

    a.schedule(new Runnable() {
      @Override
      public void run() {
        events.add("a");
        b.schedule(new Runnable() {
          @Override
          public void run() {
            events.add("b from a");
          }
        });
      }

    });
    b.schedule(new Runnable() {
      @Override
      public void run() {
        events.add("b");
        a.schedule(new Runnable() {
          @Override
          public void run() {
            events.add("a from b");
          }
        });
      }
    });

    TestEdtManagers.flush(managers, anotherManagers);

    assertEquals(Arrays.asList("a", "b", "b from a", "a from b"), events);
  }
}
