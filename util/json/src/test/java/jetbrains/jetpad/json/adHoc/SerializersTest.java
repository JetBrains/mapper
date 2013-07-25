package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonArray;
import jetbrains.jetpad.json.JsonBoolean;
import jetbrains.jetpad.json.JsonNull;
import jetbrains.jetpad.json.JsonNumber;
import jetbrains.jetpad.json.JsonObject;
import jetbrains.jetpad.json.JsonString;
import jetbrains.jetpad.json.JsonValue;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SerializersTest {
  private JsonSerializationContext myContext = new JsonSerializationContext();

  @Test
  public void nullValue() {
    byte[] s = myContext.write(new JsonNull());
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonNull);
  }

  @Test
  public void falseValue() {
    byte[] s = myContext.write(new JsonBoolean(false));
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonBoolean);
    assertFalse(((JsonBoolean) value).getBooleanValue());
  }

  @Test
  public void trueValue() {
    byte[] s = myContext.write(new JsonBoolean(true));
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonBoolean);
    assertTrue(((JsonBoolean) value).getBooleanValue());
  }

  @Test
  public void strings() {
    testString("");
    testString("abc");
    testString("a\nbc");
    testString("\\u000a abc");
    testString(new String(new char[] {(char)52, (char)256, (char)65535}));
  }

  private void testString(String data) {
    byte[] s = myContext.write(new JsonString(data));
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonString);
    assertEquals(data, ((JsonString) value).getStringValue());
  }

  @Test
  public void numbers() {
    testNumber(1);
    testNumber(85321);
    testNumber(-982412);
    testNumber(1.0);
    testNumber(1e5);
    testNumber(1456.5545454);
    testNumber(-6.456);
  }

  private void testNumber(double data) {
    byte[] s = myContext.write(new JsonNumber(data));
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonNumber);
    assertEquals(data, ((JsonNumber) value).getDoubleValue(), 0.000001f);
  }

  @Test
  public void arrays() {
    testArray();
    testArray(new JsonNumber(1));
    testArray(new JsonNumber(1), new JsonBoolean(true), new JsonNull(), new JsonString("abc"));
    testArray(new JsonArray());
    testArray(new JsonString("a"), createArray(new JsonNull(), new JsonNumber(5.4)));
    testArray(new JsonString("a"), createArray(new JsonNull(), createArray(new JsonString("q")), new JsonNumber(5.4)));
  }

  @Test
  public void longBuffer() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ComputedSizeSerializer.DEFAULT_BUFFER_SIZE; i++) {
      sb.append('a');
    }
    testArray(new JsonString("a"), createArray(new JsonNull(), createArray(new JsonString(sb.toString())), new JsonNumber(5.4)));
  }

  private void testArray(JsonValue... values) {
    JsonArray data = createArray(values);
    byte[] s = myContext.write(data);
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonArray);
    assertEquals(data.toString(), value.toString());
  }

  @Test
  public void emptyObject() {
    testObject(new JsonObject());
  }

  @Test
  public void objectValue() {
    JsonObject object = new JsonObject();
    object.put("abc", new JsonString("xyz"));
    JsonObject o2 = new JsonObject();
    o2.put("s", createArray(new JsonBoolean(false), new JsonObject()));
    object.put("q", createArray(new JsonNumber(563.32111), o2, new JsonString("k")));
    testObject(object);
  }

  private void testObject(JsonObject data) {
    byte[] s = myContext.write(data);
    JsonValue value = myContext.read(s);
    assertTrue(value instanceof JsonObject);
    assertEquals(data.toString(), value.toString());
  }

  private JsonArray createArray(JsonValue... values) {
    JsonArray array = new JsonArray();
    for (JsonValue value: values) {
      array.add(value);
    }
    return array;
  }
}
