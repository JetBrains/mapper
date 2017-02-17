package jetbrains.jetpad.base;

interface AsyncResolver<ItemT> {
  void success(ItemT result);

  void failure(Throwable t);
}
