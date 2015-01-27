package jetbrains.jetpad.base;

public class JsDebug {
  public static native void log(String text) /*-{
    $wnd.console.log(text);
  }-*/;
}
