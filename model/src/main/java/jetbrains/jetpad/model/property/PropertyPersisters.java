package jetbrains.jetpad.model.property;

import jetbrains.jetpad.base.Persister;

public class PropertyPersisters {
  public static <T> Persister<Property<T>> valuePropertyPersister(final Persister<T> itemPersister) {
    return new Persister<Property<T>>() {
      @Override
      public Property<T> deserialize(String value) {
        if (value == null) {
          return null;
        }
        if (!value.isEmpty() && value.charAt(0) == 'v') {
          return new ValueProperty<>(itemPersister.deserialize(value.substring(1)));
        }
        return new ValueProperty<>();
      }

      @Override
      public String serialize(Property<T> value) {
        if (value == null) {
          return null;
        }
        if (value.get() == null) {
          return "n";
        } else {
          return "v" + itemPersister.serialize(value.get());
        }
      }

      @Override
      public String toString() {
        return "valuePropertyPersister[using = " + itemPersister + "]";
      }
    };
  }
}
