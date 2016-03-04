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
package jetbrains.jetpad.model.parameters;

import jetbrains.jetpad.base.Registration;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class HasMutablePropertiesTest extends HasPropertiesTestCase {
  @Override
  protected HasProperties createContainer() {
    return new HasMutableProperties();
  }

  @Test
  public void removeSetReg() {
    Registration r = myContainer.set(myProperty, 10);
    r.remove();
    assertTrue(myContainer.getConfiguredProperties().isEmpty());
  }

  @Test
  public void setValueEqualToDefaultErases() {
    setToNotDefault();
    myContainer.set(myProperty, myProperty.getDefaultValue(null));
    assertTrue(myContainer.getConfiguredProperties().isEmpty());
  }
}
