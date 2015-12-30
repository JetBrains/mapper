package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Disposable;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.model.collections.CollectionAdapter;
import jetbrains.jetpad.model.collections.CollectionItemEvent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.event.EventHandler;
import jetbrains.jetpad.model.event.ListenerCaller;
import jetbrains.jetpad.model.event.Listeners;

import javax.annotation.Nonnull;

public class ListItemProperty<ValueT> extends BaseReadableProperty<ValueT> implements Property<ValueT>, Disposable {
  private ObservableList<ValueT> list;
  private int index;
  private boolean valid = true;
  private Listeners<EventHandler<? super PropertyChangeEvent<ValueT>>> handlers = new Listeners<>();
  private Registration reg;

  public ListItemProperty(@Nonnull ObservableList<ValueT> list, int index) {
    if (index < 0 || index >= list.size()) {
      throw new IndexOutOfBoundsException("Can’t point to a non-existent item");
    }
    this.list = list;
    this.index = index;

    reg = list.addListener(new CollectionAdapter<ValueT>() {
      @Override
      public void onItemAdded(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() <= ListItemProperty.this.index) {
          ListItemProperty.this.index += 1;
        }
      }

      @Override
      public void onItemSet(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() == ListItemProperty.this.index) {
          final PropertyChangeEvent<ValueT> e = new PropertyChangeEvent<>(event.getOldItem(), event.getNewItem());
          handlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
            @Override
            public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
              l.onEvent(e);
            }
          });
        }
      }

      @Override
      public void onItemRemoved(CollectionItemEvent<? extends ValueT> event) {
        if (event.getIndex() < ListItemProperty.this.index) {
          ListItemProperty.this.index -= 1;
        } else if (event.getIndex() == ListItemProperty.this.index) {
          ListItemProperty.this.valid = false;
          final PropertyChangeEvent<ValueT> e = new PropertyChangeEvent<ValueT>(event.getOldItem(), null);
          handlers.fire(new ListenerCaller<EventHandler<? super PropertyChangeEvent<ValueT>>>() {
            @Override
            public void call(EventHandler<? super PropertyChangeEvent<ValueT>> l) {
              l.onEvent(e);
            }
          });
        }
      }
    });
  }

  @Override
  public Registration addHandler(EventHandler<? super PropertyChangeEvent<ValueT>> handler) {
    return handlers.add(handler);
  }

  @Override
  public ValueT get() {
    if (valid) {
      return list.get(index);
    } else {
      return null;
    }
  }

  @Override
  public void set(ValueT value) {
    if (valid) {
      list.set(index, value);
    } else {
      throw new IllegalStateException("Property points to an invalid item, can’t set");
    }
  }

  public boolean isValid() {
    return valid;
  }

  @Override
  public void dispose() {
    valid = false;
    reg.dispose();
    list = null;
  }
}
