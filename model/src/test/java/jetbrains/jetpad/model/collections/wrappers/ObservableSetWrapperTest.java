package jetbrains.jetpad.model.collections.wrappers;

import jetbrains.jetpad.base.function.Function;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.collections.set.ObservableSet;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static jetbrains.jetpad.model.collections.ObservableItemEventMatchers.addEvent;
import static jetbrains.jetpad.model.collections.ObservableItemEventMatchers.removeEvent;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

public class ObservableSetWrapperTest {
  private ObservableSet<Double> source = new ObservableHashSet<>();
  private Function<Double, Integer> toTarget = new Function<Double, Integer>() {
    @Override
    public Integer apply(Double value) {
      return value.intValue() + 1;
    }
  };
  private Function<Integer, Double> toSource = new Function<Integer, Double>() {
    @Override
    public Double apply(Integer value) {
      return Integer.valueOf(value - 1).doubleValue();
    }
  };
  private ObservableSet<Integer> target = new ObservableSetWrapper<>(source, toTarget, toSource);

  private List<CollectionItemEvent<?extends Integer>> addEvents = new ArrayList<>();
  private List<CollectionItemEvent<?extends Integer>> setEvents = new ArrayList<>();
  private List<CollectionItemEvent<?extends Integer>> removeEvents = new ArrayList<>();
  private CollectionListener<Integer> listener = new CollectionListener<Integer>() {
    @Override
    public void onItemAdded(CollectionItemEvent<? extends Integer> event) {
      addEvents.add(event);
    }

    @Override
    public void onItemSet(CollectionItemEvent<? extends Integer> event) {
      setEvents.add(event);
    }

    @Override
    public void onItemRemoved(CollectionItemEvent<? extends Integer> event) {
      removeEvents.add(event);
    }
  };
  private void assertEvents(int addCount, int removeCount) {
    assertThat(addEvents,    hasSize(addCount));
    assertThat(setEvents,    is(empty()));
    assertThat(removeEvents, hasSize(removeCount));
  }


  @Before
  public void setup() {
    source.addAll(Arrays.asList(10.0, 20.0, 30.0));
    target.addListener(listener);
  }

  @Test
  public void setMapMaps() {
    assertThat(target, containsInAnyOrder(11, 21, 31));
  }

  @Test
  public void setMapAddSource() {
    source.add(15.0);
    assertThat(target, containsInAnyOrder(11, 16, 21, 31));
    assertEvents(1, 0);
    assertThat(addEvents.get(0), is(addEvent(equalTo(16), equalTo(-1))));
  }

  @Test
  public void listMapAddTarget() {
    target.add(15);
    assertThat(target, containsInAnyOrder(11, 15, 21, 31));
    assertThat(source, containsInAnyOrder(10.0, 14.0, 20.0, 30.0));
    assertEvents(1, 0);
    assertThat(addEvents.get(0), is(addEvent(equalTo(15), equalTo(-1))));
  }

  @Test
  public void setMapRemoveSource() {
    source.remove(30.0);
    assertThat(target, containsInAnyOrder(11, 21));
    assertEvents(0, 1);
    assertThat(removeEvents.get(0), is(removeEvent(equalTo(31), equalTo(-1))));
  }

  @Test
  public void setMapRemoveTarget() {
    target.remove(31);
    assertThat(target, containsInAnyOrder(11, 21));
    assertThat(source, containsInAnyOrder(10.0, 20.0));
    assertEvents(0, 1);
    assertThat(removeEvents.get(0), is(removeEvent(equalTo(31), equalTo(-1))));
  }

  @Test
  public void setMapListenerOrderOnSourceAdd1() {
    final boolean[] sourceFired = {false};
    final boolean[] targetFired = {false};

    source.addListener(new CollectionAdapter<Double>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Double> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(false));
        sourceFired[0] = true;
      }
    });
    target.addListener(new CollectionAdapter<Integer>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Integer> event) {
        assertThat(sourceFired[0], is(true));
        assertThat(targetFired[0], is(false));
        targetFired[0] = true;
      }
    });
    source.add(0.0);
    assertThat(sourceFired[0], is(true));
    assertThat(targetFired[0], is(true));
  }

  @Test
  public void setMapListenerOrderOnSourceAdd2() {
    final boolean[] sourceFired = {false};
    final boolean[] targetFired = {false};

    target.addListener(new CollectionAdapter<Integer>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Integer> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(false));
        targetFired[0] = true;
      }
    });
    source.addListener(new CollectionAdapter<Double>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Double> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(true));
        sourceFired[0] = true;
      }
    });
    source.add(0.0);
    assertThat(sourceFired[0], is(true));
    assertThat(targetFired[0], is(true));
  }

  @Test
  public void setMapListenerOrderOnTargetAdd1() {
    final boolean[] sourceFired = {false};
    final boolean[] targetFired = {false};

    source.addListener(new CollectionAdapter<Double>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Double> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(false));
        sourceFired[0] = true;
      }
    });
    target.addListener(new CollectionAdapter<Integer>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Integer> event) {
        assertThat(sourceFired[0], is(true));
        assertThat(targetFired[0], is(false));
        targetFired[0] = true;
      }
    });
    target.add(0);
    assertThat(sourceFired[0], is(true));
    assertThat(targetFired[0], is(true));
  }

  @Test
  public void setMapListenerOrderOnTargetAdd2() {
    final boolean[] sourceFired = {false};
    final boolean[] targetFired = {false};

    target.addListener(new CollectionAdapter<Integer>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Integer> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(false));
        targetFired[0] = true;
      }
    });
    source.addListener(new CollectionAdapter<Double>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends Double> event) {
        assertThat(sourceFired[0], is(false));
        assertThat(targetFired[0], is(true));
        sourceFired[0] = true;
      }
    });
    target.add(0);
    assertThat(sourceFired[0], is(true));
    assertThat(targetFired[0], is(true));
  }
}
