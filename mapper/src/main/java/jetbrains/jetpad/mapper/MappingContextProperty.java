package jetbrains.jetpad.mapper;

public class MappingContextProperty<ValueT> {
  private final String myName;

  public MappingContextProperty(String name) {
    myName = name;
  }

  @Override
  public String toString() {
    return "MappingContextProperty[" + myName + ']';
  }
}
