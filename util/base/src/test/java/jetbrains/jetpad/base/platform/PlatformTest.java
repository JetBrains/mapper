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
package jetbrains.jetpad.base.platform;

import jetbrains.jetpad.test.BaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static jetbrains.jetpad.base.platform.Platform.getPlatform;
import static jetbrains.jetpad.base.platform.Platform.setPlatform;
import static org.junit.Assert.assertEquals;

public class PlatformTest extends BaseTestCase {

  @Before
  public void setup() {
    Platform.reset();
  }

  @After
  public void cleanup() {
    Platform.reset();
  }

  @Test
  public void simpleSet() {
    setPlatform(PlatformType.LINUX);
    assertEquals(PlatformType.LINUX, getPlatform());
  }

  @Test(expected = IllegalStateException.class)
  public void noValue() {
    getPlatform();
  }
  
  @Test(expected = IllegalArgumentException.class)
  public void setNull() {
    setPlatform(null);
  }

  @Test(expected = IllegalStateException.class)
  public void overwrite() {
    setPlatform(PlatformType.LINUX);
    setPlatform(PlatformType.MAC);
  }

  @Test
  public void doubleSet() {
    setPlatform(PlatformType.LINUX);
    setPlatform(PlatformType.LINUX);
    assertEquals(PlatformType.LINUX, getPlatform());
  }
}
