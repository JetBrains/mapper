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

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseTestCase {
  private static Level ourLevel;

  @Rule
  public EnableSlowTestsRule enableSlowTestsRule = new EnableSlowTestsRule();
  @Rule
  public EnableRemoteTestsRule enableRemoteTestsRule = new EnableRemoteTestsRule();
  @ClassRule
  public static EnableSlowTestsRule enableSlowSuitesRule = new EnableSlowTestsRule();
  @ClassRule
  public static EnableRemoteTestsRule enableRemoteSuitesRule = new EnableRemoteTestsRule();

  @BeforeClass
  public static void turnLoggingOff() {
    ourLevel = resetLogsLevel(Level.OFF);
  }

  @AfterClass
  public static void turnLoggingBack() {
    resetLogsLevel(ourLevel);
  }

  public static Level resetLogsLevel(Level level) {
    Level oldLevel = Logger.getLogger("").getLevel();
    Logger.getLogger("").setLevel(level);
    for (Handler handler : Logger.getLogger("").getHandlers()) {
      handler.setLevel(level);
    }
    return oldLevel;
  }
}