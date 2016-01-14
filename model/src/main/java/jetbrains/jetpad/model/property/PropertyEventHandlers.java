package jetbrains.jetpad.model.property;

import jetbrains.jetpad.model.event.EventHandler;


public class PropertyEventHandlers {
  public static class CountingHandler<ItemT> implements EventHandler<PropertyChangeEvent<ItemT>> {
    private int counter = 0;

    @Override
    public void onEvent(PropertyChangeEvent<ItemT> event) {
      counter += 1;
    }

    public int getCounter() {
      return counter;
    }
  }

  public static class RecordingHandler<ItemT> implements EventHandler<PropertyChangeEvent<ItemT>> {
    private ItemT oldValue, newValue;

    @Override
    public void onEvent(PropertyChangeEvent<ItemT> event) {
      oldValue = event.getOldValue();
      newValue = event.getNewValue();
    }

    public ItemT getOldValue() {
      return oldValue;
    }

    public ItemT getNewValue() {
      return newValue;
    }
  }
}
