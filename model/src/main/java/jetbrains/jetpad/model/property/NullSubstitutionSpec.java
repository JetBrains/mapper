package jetbrains.jetpad.model.property;

public interface NullSubstitutionSpec<ValueT> {
  ValueT createSubstitute();
  boolean isSubstitute(ValueT value);
}
