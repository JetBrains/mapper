package jetbrains.jetpad.model.id;

import java.util.HashMap;
import java.util.Map;

public class GwtIdNames implements IdNames {
  private Map<String, String> myMap = new HashMap<>();

  @Override
  public void save(String id, String name) {
    String oldName = myMap.get(id);
    if (oldName == null) {
      myMap.put(id, name);
    } else if (!oldName.equals(name)) {
      throw new IllegalStateException(
          "Different name for known id " + id + ", first name = " + oldName + ", name = " + name + "]");
    }
  }

  @Override
  public String get(String id) {
    return myMap.get(id);
  }
}
