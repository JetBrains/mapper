package jetbrains.jetpad.mapper;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PartIteratorsTest extends BaseTestCase {
  @Test
  public void synchronizersIterator() {
    Mapper<Void, Void> mapper = new Mapper<Void, Void>(null, null) {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(Synchronizers.forRegistration(Registration.EMPTY));
      }
    };
    Iterator<Synchronizer> i = mapper.synchronizersIterator();
    assertFalse(i.hasNext());

    mapper.attachRoot();
    checkIterator(1, mapper.synchronizersIterator());

    ObservableSet<Mapper<?, ?>> children = mapper.createChildSet();
    children.add(new Mapper<Void, Void>(null, null) {});
    checkIterator(1, mapper.synchronizersIterator());
  }

  @Test
  public void childrenIterator() {
    Mapper<Object, Object> mapper = new Mapper<Object, Object>(new Object(), new Object()) {
      @Override
      protected void registerSynchronizers(Mapper.SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);
        conf.add(Synchronizers.forRegistration(Registration.EMPTY));
      }
    };

    checkIterator(0, mapper.childrenIterator());

    mapper.attachRoot();
    checkIterator(0, mapper.childrenIterator());
    checkIterator(1, mapper.synchronizersIterator());

    mapper.createChildList().add(new Mapper<Object, Object>(new Object(), new Object()) {});
    Mapper<Object, Object> setChild = new Mapper<Object, Object>(new Object(), new Object()) {};
    ObservableSet<Mapper<?, ?>> childSet = mapper.createChildSet();
    childSet.add(setChild);
    Property<Mapper<?, ?>> childProperty = mapper.createChildProperty();
    childProperty.set(new Mapper<Object, Object>(new Object(), new Object()) {});
    checkIterator(3, mapper.childrenIterator());

    childProperty.set(null);
    checkIterator(2, mapper.childrenIterator());

    Iterator<Mapper<?, ?>> i = childSet.iterator();
    i.next();
    i.remove();

    checkIterator(1, mapper.childrenIterator());
  }

  private void checkIterator(int expectedCount, Iterator<?> i) {
    int count = 0;
    while (i.hasNext()) {
      count++;
      i.next();
    }
    assertEquals(expectedCount, count);
  }
}
