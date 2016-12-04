package jetbrains.jetpad.json;

public interface JsonSupport {
  JsonValue parse(String input);
  String toString(JsonValue value);
}
