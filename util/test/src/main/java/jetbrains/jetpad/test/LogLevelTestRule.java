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

import java.util.logging.Level;

import static jetbrains.jetpad.test.BaseTestCase.resetLogsLevel;

public final class LogLevelTestRule implements TestRule {
  private static final Level DEFAULT_LEVEL = Level.WARNING;

  private final boolean myApplyDefault;

  public LogLevelTestRule(boolean applyDefault) {
    this.myApplyDefault = applyDefault;
  }

  @Override
  public Statement apply(final Statement statement, final Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        Level level = getLevel(description.getAnnotation(LogLevel.class));

        Level prevLevel;
        if (level != null) {
          prevLevel = resetLogsLevel(level);
        } else {
          prevLevel = null;
        }

        try {
          statement.evaluate();
        } finally {
          if (prevLevel != null) {
            resetLogsLevel(prevLevel);
          }
        }
      }
    };
  }

  private Level getLevel(LogLevel logLevel) {
    if (logLevel != null) {
      return Level.parse(logLevel.value().toUpperCase());
    } else if (myApplyDefault) {
      return DEFAULT_LEVEL;
    } else {
      return null;
    }
  }
}
