package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.model.property.Property;

public interface HasFocusability {
  Property<Boolean> focusable();
}
