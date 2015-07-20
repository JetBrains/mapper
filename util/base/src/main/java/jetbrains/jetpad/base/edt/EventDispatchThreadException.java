package jetbrains.jetpad.base.edt;

public class EventDispatchThreadException extends RuntimeException {
  EventDispatchThreadException() {}

  EventDispatchThreadException(Throwable cause) {
    super(cause);
  }
}
