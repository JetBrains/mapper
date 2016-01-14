package jetbrains.jetpad.model.property;

public class NullSubstitutingProperty<ValueT> extends DelegateProperty<ValueT> {
  private final NullSubstitutionSpec<ValueT> mySpec;

  public NullSubstitutingProperty(Property<ValueT> targetProperty, NullSubstitutionSpec<ValueT> spec) {
    super(targetProperty);
    mySpec = spec;
  }

  @Override
  public ValueT get() {
    ValueT val = super.get();
    return mySpec.isSubstitute(val) ? null : val;
  }

  @Override
  public void set(ValueT val) {
    ValueT valueToSet = val == null ? mySpec.createSubstitute() :  val;
    super.set(valueToSet);
  }
}
