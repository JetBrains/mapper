package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.model.property.Property;

public interface HasVisibility {
  Property<Boolean> visible();
}
