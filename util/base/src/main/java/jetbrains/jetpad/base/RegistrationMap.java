package jetbrains.jetpad.base;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RegistrationMap<KeyT> {
  private final Map<KeyT, Registration> myMap = new HashMap<>();

  public void put(KeyT key, Registration registration){
    Registration prev = myMap.put(key, registration);
    if (prev != null) {
      prev.remove();
      myMap.remove(key).remove();
      throw new IllegalStateException("Registration for the key '"+key+"' already exists.");
    }
  }

  public boolean replace(KeyT key, Registration registration){
    boolean res = removeOptional(key);
    myMap.put(key, registration);
    return res;
  }

  public void remove(KeyT key) {
    Registration prev = myMap.remove(key);
    if (prev != null) {
      prev.remove();
    } else {
      throw new IllegalStateException("Registration for the key '"+key+"' not found.");
    }
  }

  public boolean removeOptional(KeyT key) {
    Registration prev = myMap.remove(key);
    if (prev != null) {
      prev.remove();
      return true;
    } else {
      return false;
    }
  }

  public Set<KeyT> keys() {
    return myMap.keySet();
  }

  public void clear() {
    try {
      for (Registration r : myMap.values()) {
        r.remove();
      }
    } finally {
      myMap.clear();
    }
  }
}
