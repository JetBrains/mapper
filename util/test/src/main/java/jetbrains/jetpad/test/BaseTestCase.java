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
package jetbrains.jetpad.test;

import org.junit.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseTestCase {
  private static final String TEST_MODE_PROPERTY = "jetbrains.jetpad.testMode";
  private static final String TEST_MODE_ON = "1";
  private static Level ourLevel;

  @Rule
  public EnableSlowTestsRule enableSlowTestsRule = new EnableSlowTestsRule();
  @ClassRule
  public static EnableSlowTestsRule enableSlowSuitesRule = new EnableSlowTestsRule();

  static {
    System.setProperty(TEST_MODE_PROPERTY, TEST_MODE_ON);
  }

  @BeforeClass
  public static void turnLoggingOff() {
    ourLevel = Logger.getLogger("").getLevel();
    Logger.getLogger("").setLevel(Level.OFF);
  }

  @AfterClass
  public static void turnLoggingBack() {
    Logger.getLogger("").setLevel(ourLevel);
  }
}