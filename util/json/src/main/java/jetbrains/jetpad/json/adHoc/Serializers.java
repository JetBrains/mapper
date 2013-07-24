package jetbrains.jetpad.json.adHoc;

import jetbrains.jetpad.json.JsonArray;
import jetbrains.jetpad.json.JsonBoolean;
import jetbrains.jetpad.json.JsonNull;
import jetbrains.jetpad.json.JsonNumber;
import jetbrains.jetpad.json.JsonObject;
import jetbrains.jetpad.json.JsonString;
import jetbrains.jetpad.json.JsonValue;

import java.util.ArrayList;
import java.util.List;

class Serializers {
  static class IntegerSerializer extends NumericSerializer<Integer> {
    IntegerSerializer() {
      super(INT_MAX_LENGTH, true);
    }

    @Override
    Integer read(byte[] input, int position) {
      setPosition(position);
      byte size = isOptimized() ? input[incPosition()] : (byte)getMaxLength();
      int value = 0;
      for (int i = size - 1; i >= 0; i--) {
        value = value | (input[incPosition()] & 0xFF) << 8 * i;
      }
      return value;
    }

    @Override
    protected long getLongValue(Integer data) {
      return (long)data;
    }

    @Override
    protected byte rightShift(long value, int shift) {
      return (byte)(value >> shift);
    }
  }

  static class DoubleSerializer extends NumericSerializer<Double>{
    DoubleSerializer() {
      super(LONG_MAX_LENGTH, false);
    }

    @Override
    Double read(byte[] input, int position) {
      setPosition(position);
      byte size = isOptimized() ? input[incPosition()] : (byte)getMaxLength();
      long value = 0;
      for (int i = size - 1; i >= 0; i--) {
        if (i == LONG_MAX_LENGTH - 1) {
          value = value | (long)input[incPosition()] << 8 * i;
        } else if (i > INT_MAX_LENGTH - 2) {
          value = value | (long)(input[incPosition()] & 0xFF) << 8 * i;
        } else {
          value = value | (input[incPosition()] & 0xFF) << 8 * i;
        }
      }
      return Double.longBitsToDouble(value);
    }

    @Override
    protected long getLongValue(Double data) {
      return Double.doubleToLongBits(data);
    }

    @Override
    protected byte rightShift(long value, int shift) {
      return (byte)(value >>> shift);
    }
  }

  static class NullSerializer extends Serializer<JsonNull> {
    NullSerializer() {
      super(2);
    }

    @Override
    byte[] write(JsonNull data) {
      byte[] buffer = new byte[1];
      buffer[0] = getId();
      setPosition(1);
      return buffer;
    }

    @Override
    JsonNull read(byte[] input, int position) {
      setPosition(position);
      return new JsonNull();
    }
  }

  static class BooleanSerializer extends Serializer<JsonBoolean> {
    BooleanSerializer() {
      super(3);
    }

    @Override
    byte[] write(JsonBoolean data) {
      byte[] buffer = new byte[2];
      buffer[0] = getId();
      buffer[1] = (byte)(data.getBooleanValue() ? 1 : 0);
      setPosition(2);
      return buffer;
    }

    @Override
    JsonBoolean read(byte[] input, int position) {
      setPosition(position);
      byte value = input[getPosition()];
      incPosition();
      if (value == 1) return new JsonBoolean(true);
      if (value == 0) return new JsonBoolean(false);
      throw new JsonSerializationException();
    }
  }

  static class StringSerializer extends ComputedSizeSerializer<String> {
    StringSerializer(Serializer<Integer> intSerializer) {
      super(intSerializer);
    }

    @Override
    protected void doWrite(String data) {
      write(data.getBytes());
    }

    @Override
    protected String doRead(byte[] input, int size) {
      byte[] content = new byte[size];
      System.arraycopy(input, getPosition(), content, 0, size);
      incPosition(size);
      return new String(content);
    }
  }

  static class JsonStringSerializer extends Serializer<JsonString> {
    private Serializer<String> myStringSerializer;
    JsonStringSerializer(Serializer<String> stringSerializer) {
      super(4);
      myStringSerializer = stringSerializer;
    }

    @Override
    byte[] write(JsonString data) {
      byte[] value = myStringSerializer.write(data.getStringValue());
      byte[] result = new byte[value.length + 1];
      result[0] = getId();
      System.arraycopy(value, 0, result, 1, value.length);
      return result;
    }

    @Override
    JsonString read(byte[] input, int position) {
      String s = myStringSerializer.read(input, position);
      setPosition(myStringSerializer.getPosition());
      return new JsonString(s);
    }
  }

  static class NumberSerializer extends ComputedSizeSerializer<JsonNumber> {
    private static final byte INT_ID = 0;
    private static final byte DOUBLE_ID = 1;

    private Serializer<Double> myDoubleSerializer;
    NumberSerializer(Serializer<Integer> intSerializer, Serializer<Double> doubleSerializer) {
      super(5, intSerializer);
      myDoubleSerializer = doubleSerializer;
    }

    @Override
    protected void doWrite(JsonNumber data) {
      double value = data.getDoubleValue();
      if (value == (int)value) {
        write(INT_ID);
        write(getIntSerializer().write((int)value));
      } else {
        write(DOUBLE_ID);
        write(myDoubleSerializer.write(value));
      }
    }

    @Override
    protected JsonNumber doRead(byte[] input, int size) {
      byte type = input[incPosition()];
      double value;
      if (type == INT_ID) {
        value = getIntSerializer().read(input, getPosition());
        setPosition(getIntSerializer().getPosition());
      } else if (type == DOUBLE_ID) {
        value = myDoubleSerializer.read(input, getPosition());
        setPosition(myDoubleSerializer.getPosition());
      } else {
        throw new JsonSerializationException();
      }
      return new JsonNumber(value);
    }
  }

  static class ObjectSerializer extends RecursiveSerializer<JsonObject> {
    private Serializer<String> myStringSerializer;

    protected ObjectSerializer(JsonSerializationContext context) {
      super(6, context);
      myStringSerializer = context.getSerializer(String.class);
    }

    @Override
    protected void doWrite(JsonObject data) {
      for (String key: data.getKeys()) {
        write(myStringSerializer.write(key));
        writeValue(data.get(key));
      }
    }

    @Override
    protected JsonObject doRead(byte[] input, int size) {
      JsonObject result = new JsonObject();
      int end = getPosition() + size;
      while (getPosition() < end) {
        String key = myStringSerializer.read(input, getPosition());
        setPosition(myStringSerializer.getPosition());
        result.put(key, readValue(input));
      }
      return result;
    }

    @Override
    protected RecursiveSerializer<JsonObject> createInstance(JsonSerializationContext context) {
      return new ObjectSerializer(context);
    }
  }

  public static class ArraySerializer extends RecursiveSerializer<JsonArray> {
    ArraySerializer(JsonSerializationContext context) {
      super(7, context);
    }

    @Override
    protected void doWrite(JsonArray data) {
      for (JsonValue value: data.getValues()) {
        writeValue(value);
      }
    }

    @Override
    protected JsonArray doRead(byte[] input, int size) {
      List<JsonValue> values = new ArrayList<JsonValue>();
      int end = getPosition() + size;
      while (getPosition() < end) {
        values.add(readValue(input));
      }
      return new JsonArray(values);
    }

    @Override
    protected RecursiveSerializer<JsonArray> createInstance(JsonSerializationContext context) {
      return new ArraySerializer(context);
    }
  }
}
