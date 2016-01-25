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

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

class EnableSlowTestsRule implements TestRule {
  public static final String ENABLE_SLOW_TESTS = "enable.slow.tests";
  private static boolean ourSlowTestsEnabled = "true".equals(System.getProperty(ENABLE_SLOW_TESTS));

  public Statement apply(final Statement statement, final Description description) {
    return new Statement() {
      public void evaluate() throws Throwable {
        if (!ourSlowTestsEnabled && description.getAnnotation(Slow.class) != null) {
          return;
        }
        statement.evaluate();
      }
    };
  }
}