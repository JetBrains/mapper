package jetbrains.jetpad.mapper;

import jetbrains.jetpad.base.Value;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SynchonizersTest extends BaseTestCase {
  @Test
  public void forEventSourceOnAttach() {
    final Value<Integer> runNum = new Value<Integer>(0);
    Property<Boolean> prop = new ValueProperty<Boolean>();
    final Synchronizer synchronizer = Synchronizers.forEventSource(prop, new Runnable() {
      @Override
      public void run() {
        runNum.set(runNum.get() + 1);
      }
    });

    Mapper<Void, Void> mapper = new Mapper<Void, Void>(null, null) {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(synchronizer);
      }
    };
    mapper.attachRoot();

    assertEquals(1, (int)runNum.get());
  }
}
