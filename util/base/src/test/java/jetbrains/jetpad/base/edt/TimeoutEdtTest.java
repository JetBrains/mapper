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
package jetbrains.jetpad.base.edt;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.mockito.Mockito;

public final class TimeoutEdtTest {

  @Rule
  public final Timeout testTimeout = new Timeout(10000);

  private TestEventDispatchThread edt = new TestEventDispatchThread();

  @Before
  public void setUp() {
    edt.resetOwner();
  }

  @Test
  public void testInNewThread() {
    Runnable r = Mockito.mock(Runnable.class);
    edt.schedule(r);
    edt.executeUpdates();
    Mockito.verify(r).run();
  }

}