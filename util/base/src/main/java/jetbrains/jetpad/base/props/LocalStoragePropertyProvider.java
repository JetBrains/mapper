package jetbrains.jetpad.base.props;

import com.google.gwt.storage.client.Storage;

public class LocalStoragePropertyProvider implements PropertyProvider {
  private String myPrefix;

  public LocalStoragePropertyProvider(String myPrefix) {
    this.myPrefix = myPrefix;
  }

  @Override
  public String get(String key) {
    Storage storage = Storage.getLocalStorageIfSupported();
    if (storage != null) {
      return storage.getItem(myPrefix + "." + key);
    }
    return null;
  }
}
