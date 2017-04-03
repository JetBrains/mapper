package jetbrains.jetpad.model.id;

public interface IdNames {
  /**
   * @throws IllegalStateException if name already reserved.
   */
  void save(String id, String name);
  String get(String id);
}
