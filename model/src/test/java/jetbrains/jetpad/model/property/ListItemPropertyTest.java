package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ListItemPropertyTest {
  @Rule
  public final ExpectedException exception = ExpectedException.none();

  @Test
  public void rejectsNegativeIndex() {
    ObservableList<Integer> list = createList(5);
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, -1);
  }

  @Test
  public void rejectsTooLageIndex() {
    ObservableList<Integer> list = createList(5);
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, 5);
  }

  @Test
  public void acceptsEdgeIndices() {
    ObservableList<Integer> list = createList(5);
    new ListItemProperty<>(list, 0);
    new ListItemProperty<>(list, 4);
  }

  @Test
  public void getsTheRightItem() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p2 = new ListItemProperty(list, 2);
    assertEquals(2, p2.get());

    ListItemProperty p4 = new ListItemProperty(list, 4);
    assertEquals(4, p4.get());
  }

  @Test
  public void setsTheRightItem() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p2 = new ListItemProperty(list, 2);
    p2.set(12);
    assertEquals("[0, 1, 12, 3, 4]", "" + list);

    ListItemProperty p4 = new ListItemProperty(list, 4);
    p4.set(14);
    assertEquals("[0, 1, 12, 3, 14]", "" + list);
  }

  @Test
  public void tracksItemOnAdd() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p1 = new ListItemProperty(list, 1);
    ListItemProperty p2 = new ListItemProperty(list, 2);
    ListItemProperty p3 = new ListItemProperty(list, 3);
    list.add(2, 22);
    assertEquals(1, p1.get());
    assertEquals(2, p2.get());
    assertEquals(3, p3.get());

    p1.set(11);
    p2.set(12);
    p3.set(13);
    assertEquals("[0, 11, 22, 12, 13, 4]", "" + list);
  }

  @Test
  public void tracksItemOnRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p1 = new ListItemProperty(list, 1);
    ListItemProperty p2 = new ListItemProperty(list, 2);
    ListItemProperty p3 = new ListItemProperty(list, 3);
    list.remove(2);
    assertEquals(1, p1.get());
    assertEquals(3, p3.get());
    assertFalse(p2.isValid());

    p1.set(11);
    p3.set(13);
    assertEquals("[0, 11, 13, 4]", "" + list);

    exception.expect(IllegalStateException.class);
    p2.set(12);
  }

  @Test
  public void firesOnListSet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p1 = new ListItemProperty(list, 1);
    ListItemProperty p2 = new ListItemProperty(list, 2);
    ListItemProperty p3 = new ListItemProperty(list, 3);

    CountingHandler p1counter = new CountingHandler();
    p1.addHandler(p1counter);
    CountingHandler p2counter = new CountingHandler();
    p2.addHandler(p2counter);
    CountingHandler p3counter = new CountingHandler();
    p3.addHandler(p3counter);

    RecordingHandler recording = new RecordingHandler();
    p2.addHandler(recording);

    list.add(2, 22);
    list.set(3, 12);

    assertEquals(0, p1counter.getCounter());
    assertEquals(1, p2counter.getCounter());
    assertEquals(0, p3counter.getCounter());
    assertEquals(new Integer(2), recording.getOldValue());
    assertEquals(new Integer(12), recording.getNewValue());
  }

  @Test
  public void firesOnPropertySet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty p2 = new ListItemProperty(list, 2);

    CountingHandler p2counter = new CountingHandler();
    p2.addHandler(p2counter);
    RecordingHandler recording = new RecordingHandler();
    p2.addHandler(recording);

    p2.set(12);

    assertEquals(1, p2counter.getCounter());
    assertEquals(new Integer(2), recording.getOldValue());
    assertEquals(new Integer(12), recording.getNewValue());
  }


  private ObservableList<Integer> createList(int n) {
    ObservableList<Integer> list = new ObservableArrayList<>();
    for (int i = 0; i < n; i++) {
      list.add(i);
    }
    return list;
  }

  class CountingHandler implements EventHandler<PropertyChangeEvent<Integer>> {
    private int counter = 0;

    @Override
    public void onEvent(PropertyChangeEvent<Integer> event) {
      counter += 1;
    }

    public int getCounter() {
      return counter;
    }
  }

  class RecordingHandler implements EventHandler<PropertyChangeEvent<Integer>> {
    private Integer oldValue, newValue;

    @Override
    public void onEvent(PropertyChangeEvent<Integer> event) {
      oldValue = event.getOldValue();
      newValue = event.getNewValue();
    }

    public Integer getOldValue() {
      return oldValue;
    }

    public Integer getNewValue() {
      return newValue;
    }
  }
}