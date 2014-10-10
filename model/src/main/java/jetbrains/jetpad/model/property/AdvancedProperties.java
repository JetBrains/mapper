package jetbrains.jetpad.model.property;

public class AdvancedProperties {
  @SafeVarargs
  public static ReadableProperty<Boolean> and(final ReadableProperty<Boolean>... props) {
    return new DerivedProperty<Boolean>(props) {
      @Override
      public Boolean get() {
        Boolean res = Boolean.TRUE;
        for (ReadableProperty<Boolean> prop : props) {
          res = Properties.and(res, prop.get());
        }
        return res;
      }

      @Override
      public String getPropExpr() {
        StringBuilder propExpr = new StringBuilder("(");
        boolean first = true;
        for (ReadableProperty<Boolean> prop : props) {
          if (!first) {
            propExpr.append(" && ");
          } else {
            first = false;
          }
          propExpr.append(prop.getPropExpr());
        }
        return propExpr.append(")").toString();
      }
    };
  }
}
