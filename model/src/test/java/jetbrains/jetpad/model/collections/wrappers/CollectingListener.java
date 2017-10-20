package jetbrains.jetpad.model.collections.wrappers;

import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.CollectionListener;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

class CollectingListener implements CollectionListener<Integer>  {
  private List<CollectionItemEvent<?extends Integer>> addEvents = new ArrayList<>();
  private List<CollectionItemEvent<?extends Integer>> setEvents = new ArrayList<>();
  private List<CollectionItemEvent<?extends Integer>> removeEvents = new ArrayList<>();

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

  void assertEvents(int addCount, int removeCount) {
    assertThat(addEvents, hasSize(addCount));
    assertThat(setEvents, is(empty()));
    assertThat(removeEvents, hasSize(removeCount));
  }

  void assertEvents(int addCount, int setCount, int removeCount) {
    assertThat(addEvents, hasSize(addCount));
    assertThat(setEvents, hasSize(setCount));
    assertThat(removeEvents, hasSize(removeCount));
  }

  List<CollectionItemEvent<? extends Integer>> getAddEvents() {
    return addEvents;
  }

  List<CollectionItemEvent<? extends Integer>> getSetEvents() {
    return setEvents;
  }

  List<CollectionItemEvent<? extends Integer>> getRemoveEvents() {
    return removeEvents;
  }

}
