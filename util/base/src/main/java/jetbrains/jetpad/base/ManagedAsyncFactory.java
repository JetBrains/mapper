package jetbrains.jetpad.base;

public interface ManagedAsyncFactory {
  <ItemT> ManagedAsync<ItemT> createAsync();
}
