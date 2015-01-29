/*
 * Copyright 2012-2014 JetBrains s.r.o
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
package jetbrains.jetpad.model.property;

public class AdvancedProperties {
  @SafeVarargs
  public static ReadableProperty<Boolean> and(final ReadableProperty<Boolean>... props) {
    return new DerivedProperty<Boolean>(props) {
      @Override
      public Boolean doGet() {
        Boolean res = Boolean.TRUE;
        for (ReadableProperty<Boolean> prop : props) {
          res = Properties.and(res, prop.get());
        }
        return res;
      }

      @Override
      public String getPropExpr() {
        StringBuilder propExpr = new StringBuilder("(");
        boolean first = true;
        for (ReadableProperty<Boolean> prop : props) {
          if (!first) {
            propExpr.append(" && ");
          } else {
            first = false;
          }
          propExpr.append(prop.getPropExpr());
        }
        return propExpr.append(")").toString();
      }
    };
  }
}