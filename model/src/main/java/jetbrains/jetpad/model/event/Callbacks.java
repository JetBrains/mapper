package jetbrains.jetpad.model.event;


import java.util.ArrayList;
import java.util.List;

public class Callbacks {
  public static interface Caller<CallbackT> {
    void call(CallbackT callback);
  }

  public static <CallbackT> void call(Iterable<CallbackT> callbacks, Caller<CallbackT> caller) {
    List<Throwable> exceptions = new ArrayList<Throwable>();
    for (final CallbackT c : callbacks) {
      try {
        caller.call(c);
      } catch (Throwable t) {
        exceptions.add(t);
      }

      if (!exceptions.isEmpty()) {
        if (exceptions.size() == 1) {
          throw new RuntimeException(exceptions.get(0));
        }
        throw new CompositeException(exceptions);
      }
    }
  }

  private static class CompositeException extends RuntimeException {
    private List<Throwable> myErrors = new ArrayList<Throwable>();

    private CompositeException(List<Throwable> errors) {
      super(errors.get(0));
      myErrors.addAll(errors);
    }
  }
}