package jetbrains.jetpad.model.event;

import java.util.ArrayList;
import java.util.List;

public class CallbackException extends RuntimeException {
  private List<Throwable> myThrowables = new ArrayList<Throwable>();

  public CallbackException(List<Throwable> errors) {
    super(errors.get(0));
    myThrowables.addAll(errors);
  }

  public List<Throwable> getThrowables() {
    return myThrowables;
  }
}
