package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.PropertyEventHandlers.CountingHandler;
import jetbrains.jetpad.model.property.PropertyEventHandlers.RecordingHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

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
  public void rejectsTooSmallIndex() {
    ObservableList<Integer> list = new ObservableArrayList<>();
    exception.expect(IndexOutOfBoundsException.class);
    new ListItemProperty<>(list, 0);
  }

  @Test
  public void rejectsTooLargeIndex() {
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
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    assertEquals(2, p2.get().intValue());

    ListItemProperty<Integer> p4 = new ListItemProperty<>(list, 4);
    assertEquals(4, p4.get().intValue());
  }

  @Test
  public void setsTheRightItem() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    p2.set(12);
    assertEquals("[0, 1, 12, 3, 4]", "" + list);

    ListItemProperty<Integer> p4 = new ListItemProperty<>(list, 4);
    p4.set(14);
    assertEquals("[0, 1, 12, 3, 14]", "" + list);
  }

  @Test
  public void tracksItemOnAdd() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);
    list.add(2, 22);
    assertEquals(1, p1.get().intValue());
    assertEquals(2, p2.get().intValue());
    assertEquals(3, p3.get().intValue());

    p1.set(11);
    p2.set(12);
    p3.set(13);
    assertEquals("[0, 11, 22, 12, 13, 4]", "" + list);
  }

  @Test
  public void tracksItemOnRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);
    list.remove(2);
    assertEquals(1, p1.get().intValue());
    assertEquals(3, p3.get().intValue());
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
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    CountingHandler<Integer> p1counter = new CountingHandler<>();
    p1.addHandler(p1counter);
    CountingHandler<Integer> p2counter = new CountingHandler<>();
    p2.addHandler(p2counter);
    CountingHandler<Integer> p3counter = new CountingHandler<>();
    p3.addHandler(p3counter);

    RecordingHandler<Integer> recording = new RecordingHandler<>();
    p2.addHandler(recording);

    list.add(2, 22);
    list.set(3, 12);

    assertEquals(0, p1counter.getCounter());
    assertEquals(1, p2counter.getCounter());
    assertEquals(0, p3counter.getCounter());
    assertEquals(2, recording.getOldValue().intValue());
    assertEquals(12, recording.getNewValue().intValue());
  }

  @Test
  public void firesOnTrackedItemRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 2);

    CountingHandler<Integer> counter = new CountingHandler<>();
    p.addHandler(counter);

    RecordingHandler<Integer> recording = new RecordingHandler<>();
    p.addHandler(recording);

    list.remove(2);

    assertEquals(1, counter.getCounter());
    assertEquals(2, recording.getOldValue().intValue());
    assertNull(recording.getNewValue());
  }

  @Test
  public void firesOnPropertySet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);

    CountingHandler<Integer> p2counter = new CountingHandler<>();
    p2.addHandler(p2counter);
    RecordingHandler<Integer> recording = new RecordingHandler<>();
    p2.addHandler(recording);

    p2.set(12);

    assertEquals(1, p2counter.getCounter());
    assertEquals(2, recording.getOldValue().intValue());
    assertEquals(12, recording.getNewValue().intValue());
  }

  @Test
  public void indexFiresOnListAdd() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);

    CountingHandler<Integer> p1indexCounter = new CountingHandler<>();
    p1.index.addHandler(p1indexCounter);
    CountingHandler<Integer> p2indexCounter = new CountingHandler<>();
    p2.index.addHandler(p2indexCounter);

    RecordingHandler<Integer> p2indexRecording = new RecordingHandler<>();
    p2.index.addHandler(p2indexRecording);

    list.add(2, 22);

    assertEquals(0, p1indexCounter.getCounter());
    assertEquals(1, p2indexCounter.getCounter());
    assertEquals(2, p2indexRecording.getOldValue().intValue());
    assertEquals(3, p2indexRecording.getNewValue().intValue());
  }

  @Test
  public void indexFiresOnListRemove() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    CountingHandler<Integer> p1indexCounter = new CountingHandler<>();
    p1.index.addHandler(p1indexCounter);
    CountingHandler<Integer> p2indexCounter = new CountingHandler<>();
    p2.index.addHandler(p2indexCounter);
    CountingHandler<Integer> p3indexCounter = new CountingHandler<>();
    p3.index.addHandler(p3indexCounter);

    RecordingHandler<Integer> p2indexRecording = new RecordingHandler<>();
    p2.index.addHandler(p2indexRecording);
    RecordingHandler<Integer> p3indexRecording = new RecordingHandler<>();
    p3.index.addHandler(p3indexRecording);

    list.remove(2);

    assertEquals(0, p1indexCounter.getCounter());
    assertEquals(1, p2indexCounter.getCounter());
    assertEquals(1, p3indexCounter.getCounter());
    assertEquals(2, p2indexRecording.getOldValue().intValue());
    assertNull(p2indexRecording.getNewValue());
    assertEquals(3, p3indexRecording.getOldValue().intValue());
    assertEquals(2, p3indexRecording.getNewValue().intValue());
  }

  @Test
  public void indexFiresNotOnListSet() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p1 = new ListItemProperty<>(list, 1);
    ListItemProperty<Integer> p2 = new ListItemProperty<>(list, 2);
    ListItemProperty<Integer> p3 = new ListItemProperty<>(list, 3);

    CountingHandler<Integer> p1indexCounter = new CountingHandler<>();
    p1.index.addHandler(p1indexCounter);
    CountingHandler<Integer> p2indexCounter = new CountingHandler<>();
    p2.index.addHandler(p2indexCounter);
    CountingHandler<Integer> p3indexCounter = new CountingHandler<>();
    p3.index.addHandler(p3indexCounter);

    list.set(2, 22);

    assertEquals(0, p1indexCounter.getCounter());
    assertEquals(0, p2indexCounter.getCounter());
    assertEquals(0, p3indexCounter.getCounter());
  }

  @Test
  public void disposeImmediately() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);
    p.dispose();
    exception.expect(IllegalStateException.class);
    p.dispose();
  }

  @Test
  public void disposeInvalid() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);
    list.remove(1);
    assertFalse(p.isValid());
    p.dispose();
    exception.expect(IllegalStateException.class);
    p.dispose();
  }

  @Test
  public void indexFiresNotOnDispose() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    CountingHandler<Integer> iCounter = new CountingHandler<>();
    p.index.addHandler(iCounter);

    p.dispose();

    assertEquals(0, iCounter.getCounter());
  }

  @Test
  public void indexFiresNotOnDisposeInvalid() {
    ObservableList<Integer> list = createList(5);
    ListItemProperty<Integer> p = new ListItemProperty<>(list, 1);

    CountingHandler<Integer> iCounter = new CountingHandler<>();
    p.index.addHandler(iCounter);

    list.remove(1);
    assertEquals(1, iCounter.getCounter());
    p.dispose();
    assertEquals(1, iCounter.getCounter());
  }


  private ObservableList<Integer> createList(int n) {
    ObservableList<Integer> list = new ObservableArrayList<>();
    for (int i = 0; i < n; i++) {
      list.add(i);
    }
    return list;
  }
}