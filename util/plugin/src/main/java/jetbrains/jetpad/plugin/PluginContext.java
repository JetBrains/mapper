/*
 * Copyright 2012-2015 JetBrains s.r.o
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
package jetbrains.jetpad.plugin;

import java.util.Arrays;
import java.util.Collection;

public abstract class PluginContext {
  public abstract <ExtensionT> PluginContext add(ExtensionPoint<ExtensionT> ep, Collection<? extends ExtensionT> exts);

  @SafeVarargs
  public final <ExtensionT> PluginContext add(ExtensionPoint<ExtensionT> ep, ExtensionT... exts) {
    return add(ep, Arrays.asList(exts));
  }
}
