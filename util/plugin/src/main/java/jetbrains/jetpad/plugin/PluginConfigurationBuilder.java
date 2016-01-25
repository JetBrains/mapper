/*
 * Copyright 2012-2016 JetBrains s.r.o
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

import java.util.*;

public class PluginConfigurationBuilder {
  public static final PluginConfiguration EMPTY = new PluginConfigurationBuilder().build();

  private final List<Plugin> myPlugins;

  public PluginConfigurationBuilder() {
    myPlugins = new ArrayList<>();
  }

  protected PluginConfigurationBuilder(List<Plugin> plugins) {
    myPlugins = plugins;
  }

  public PluginConfigurationBuilder add(Plugin... plugins) {
    List<Plugin> np = new ArrayList<>();
    np.addAll(myPlugins);
    np.addAll(Arrays.asList(plugins));
    return new PluginConfigurationBuilder(np);
  }

  public PluginConfiguration build() {
    final Map<ExtensionPoint<?>, List<?>> context = new HashMap<>();

    for (Plugin p : myPlugins) {
      p.install(new PluginContext() {
        @Override
        public <ExtensionT> PluginContext add(ExtensionPoint<ExtensionT> ep, Collection<? extends ExtensionT> exts) {
          if (exts.isEmpty()) {
            return this;
          }

          if (!context.containsKey(ep)) {
            context.put(ep, new ArrayList<ExtensionT>());
          }

          ((List<ExtensionT>) context.get(ep)).addAll(exts);

          return this;
        }
      });
    }

    return new PluginConfiguration() {
      @Override
      public <ExtensionT> Collection<ExtensionT> getExts(ExtensionPoint<ExtensionT> ep) {
        if (!context.containsKey(ep)) {
          return Collections.emptyList();
        }
        return Collections.unmodifiableCollection((List<ExtensionT>) context.get(ep));
      }

      @Override
      public <ExtensionT> ExtensionT getExt(ExtensionPoint<ExtensionT> ep) {
        Collection<ExtensionT> exts = getExts(ep);
        if (exts.size() != 1) {
          throw new IllegalStateException("There should be exactly one instance of " + ep);
        }
        return exts.iterator().next();
      }
    };
  }
}