/*
 * Copyright 2012-2015 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jetbrains.jetpad.base;

public class Persisters {
  private static final Persister<String> STRING_PERSISTER = new Persister<String>() {
    @Override
    public String deserialize(String value) {
      return value;
    }

    @Override
    public String serialize(String value) {
      return value;
    }

    @Override
    public String toString() {
      return "stringPersister";
    }
  };

  private static final Persister<Integer> INT_PERSISTER = intPersister(0);

  private static final Persister<Long> LONG_PERSISTER = new Persister<Long>() {
    @Override
    public Long deserialize(String value) {
      return value == null ? 0L : Long.parseLong(value);
    }

    @Override
    public String serialize(Long value) {
      return value.equals(0L) ? null : value.toString();
    }

    @Override
    public String toString() {
      return "longPersister";
    }
  };

  private static final Persister<Boolean> BOOLEAN_PERSISTER = new Persister<Boolean>() {
    @Override
    public Boolean deserialize(String value) {
      if (value == null) {
        return false;
      }
      return Boolean.parseBoolean(value);
    }

    @Override
    public String serialize(Boolean value) {
      return "" + value;
    }

    @Override
    public String toString() {
      return "booleanPersister";
    }
  };

  private static final Persister<Double> DOUBLE_PERSISTER = doublePersister(0.0);

  public static Persister<String> stringPersister() {
    return STRING_PERSISTER;
  }

  public static Persister<Integer> intPersister() {
    return INT_PERSISTER;
  }

  public static Persister<Integer> intPersister(final int defaultValue) {
    return new Persister<Integer>() {
      @Override
      public Integer deserialize(String value) {
        return value == null ? defaultValue : Integer.parseInt(value);
      }

      @Override
      public String serialize(Integer value) {
        if (new Integer(defaultValue).equals(value)) return null;
        return "" + value;
      }

      @Override
      public String toString() {
        return "intPersister" + (defaultValue != 0 ? "[default = " + defaultValue + "]" : "");
      }
    };
  }

  public static <E extends Enum<E>> Persister<E> enumPersister(final Class<E> cls, final E defaultValue) {
    return new Persister<E>() {
      @Override
      public E deserialize(String value) {
        return value == null ? defaultValue : Enums.valueOf(cls, value);
      }

      @Override
      public String serialize(E value) {
        return value == null ? (defaultValue != null ? defaultValue.toString() : null) : value.toString();
      }

      @Override
      public String toString() {
        return "enumPersister[default = " + defaultValue + "]";
      }
    };
  }

  public static Persister<Long> longPersister() {
    return LONG_PERSISTER;
  }

  public static Persister<Boolean> booleanPersister() {
    return BOOLEAN_PERSISTER;
  }

  public static Persister<Boolean> booleanPersister(final boolean defaultValue) {
    return new Persister<Boolean>() {
      @Override
      public Boolean deserialize(String value) {
        return value == null ? defaultValue : Boolean.parseBoolean(value);
      }

      @Override
      public String serialize(Boolean value) {
        return "" + value;
      }

      @Override
      public String toString() {
        return "booleanPersister";
      }
    };
  }

  public static Persister<Double> doublePersister() {
    return DOUBLE_PERSISTER;
  }

  public static Persister<Double> doublePersister(final double defaultValue) {
    return new Persister<Double>() {
      @Override
      public Double deserialize(String value) {
        return value == null ? defaultValue : Double.parseDouble(value);
      }

      @Override
      public String serialize(Double value) {
        if (value == null || value == defaultValue) {
          return null;
        }
        return value.toString();
      }

      @Override
      public String toString() {
        return "doublePersister[default = " + defaultValue + "]";
      }
    };
  }
}