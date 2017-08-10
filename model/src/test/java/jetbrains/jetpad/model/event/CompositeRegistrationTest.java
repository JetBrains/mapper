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
package jetbrains.jetpad.model.event;

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CompositeRegistrationTest extends BaseTestCase {
  private int myRemoveCounter = 0;

  @Test
  public void removalOrder() {
    CompositeRegistration r = new CompositeRegistration(createReg(1), createReg(0));
    r.remove();
    assertEquals(2, myRemoveCounter);
  }

  @Test
  public void removalOrderManualAdd() {
    CompositeRegistration r = new CompositeRegistration();
    r.add(createReg(1)).add(createReg(0));
    r.remove();
    assertEquals(2, myRemoveCounter);
  }

  private Registration createReg(final int expectedOrder) {
    return new Registration() {
      @Override
      protected void doRemove() {
        assertEquals(expectedOrder, myRemoveCounter++);
      }
    };
  }
}