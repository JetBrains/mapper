/*
 * Copyright 2012-2017 JetBrains s.r.o
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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