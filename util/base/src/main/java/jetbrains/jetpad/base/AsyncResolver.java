package jetbrains.jetpad.base;

public interface AsyncResolver<ItemT> {
  void success(ItemT result);

  void failure(Throwable t);
}
