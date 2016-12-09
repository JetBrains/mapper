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
package jetbrains.jetpad.test;

import org.junit.experimental.categories.Category;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class NoCategoryRule implements TestRule {
  public static final String PROP_CATEGORIZED_DISABLED = "categorized.tests.disabled";

  private final boolean myCategorizedDisabled;

  public NoCategoryRule() {
    myCategorizedDisabled = Boolean.getBoolean(PROP_CATEGORIZED_DISABLED);
  }

  public Statement apply(final Statement statement, final Description description) {
    return new Statement() {
      public void evaluate() throws Throwable {
        if (myCategorizedDisabled && description.getAnnotation(Category.class) != null) {
          return;
        }
        statement.evaluate();
      }
    };
  }
}
