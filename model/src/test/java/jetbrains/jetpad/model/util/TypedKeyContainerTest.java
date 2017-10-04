package jetbrains.jetpad.model.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;

public final class TypedKeyContainerTest {

  private static <T> Key<T> create(String name) {
    return new BadKey<>(name);
  }

  @Test(expected = ClassCastException.class)
  public void badKey() {
    TypedKeyContainer typedKeyContainer = new TypedKeyHashMap();
    Key<List<String>> stringListTypedKey = create("stringList");
    Key<List<Integer>> integerListTypedKey = create("integerList");
    List<String> stringList = Arrays.asList("a", "b");
    typedKeyContainer.put(stringListTypedKey, stringList);

    List<Integer> integerList = typedKeyContainer.get(integerListTypedKey);
    Integer firstInteger = integerList.get(0);
    assertFalse(firstInteger instanceof Integer);
  }

  private static class Key<T> implements TypedKey<T> {

    private final String myName;

    private Key(String name) {
      myName = name;
    }

    @Override
    public String toString() {
      return myName;
    }

  }

  private static final class BadKey<T> extends Key<T> {

    private BadKey(String name) {
      super(name);
    }

    @Override
    public int hashCode() {
      return 0;
    }

    @Override
    public boolean equals(Object obj) {
      return true;
    }

  }

}
