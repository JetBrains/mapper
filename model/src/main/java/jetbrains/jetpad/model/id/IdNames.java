package jetbrains.jetpad.model.id;

interface IdNames {
  /**
   * @throws IllegalStateException if the id already has another name.
   */
  void save(String id, String name);
  String get(String id);
}
