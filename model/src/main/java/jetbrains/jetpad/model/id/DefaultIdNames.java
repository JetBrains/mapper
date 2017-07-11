package jetbrains.jetpad.model.id;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

class DefaultIdNames implements IdNames {
  private final ConcurrentMap<String, String> myMap = new ConcurrentHashMap<>();

  @Override
  public void save(String id, String name) {
    String oldName = myMap.putIfAbsent(id, name);
    if (oldName != null && !oldName.equals(name)) {
      throw new IllegalStateException(
          "Different name for known id " + id + ", first name = " + oldName + ", name = " + name + "]");
    }
  }

  @Override
  public String get(String id) {
    return myMap.get(id);
  }
}
