package jetbrains.jetpad.base;

import com.google.common.base.Objects;

public class Enums {
  /**
   * Value of method for enums which takes into account toString() instead of saved generated name
   */
  public static <EnumT extends Enum<EnumT>> EnumT valueOf(Class<EnumT> cls, String name) {
    for (EnumT e : cls.getEnumConstants()) {
      if (Objects.equal(name, e.toString())) {
        return e;
      }
    }

    throw new IllegalArgumentException(name);
  }
}
