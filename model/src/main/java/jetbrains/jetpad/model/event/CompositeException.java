package jetbrains.jetpad.model.event;

import java.util.ArrayList;
import java.util.List;

public class CompositeException extends RuntimeException {
  private List<Throwable> myThrowables = new ArrayList<Throwable>();

  public CompositeException(List<Throwable> errors) {
    super(errors.get(0));
    myThrowables.addAll(errors);
  }

  public List<Throwable> getThrowables() {
    return myThrowables;
  }
}
