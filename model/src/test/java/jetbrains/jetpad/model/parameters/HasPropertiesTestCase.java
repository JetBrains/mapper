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

import com.google.common.collect.Sets;
import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class HasPropertiesTestCase extends BaseTestCase {
  PropertySpec<Integer> myProperty = PropertySpec.of("test", 5);
  HasProperties myContainer;

  protected abstract HasProperties createContainer();

  @Before
  public void init() {
    myContainer = createContainer();
  }

  @Test
  public void isConfigured() {
    PropertySpec<Integer> prop = PropertySpec.withoutDefaultValue("test");
    assertFalse(myContainer.isConfigured(prop));
    assertTrue(myContainer.getConfiguredProperties().isEmpty());
    myContainer.set(prop, 1);
    assertTrue(myContainer.isConfigured(prop));
    assertEquals(Sets.<PropertySpec<?>>newHashSet(prop), myContainer.getConfiguredProperties());
  }

  @Test
  public void getNotConfiguredPropWithNullDefaultValue() {
    PropertySpec<Integer> prop = PropertySpec.of("test");
    assertNull(myContainer.get(prop));
  }

  @Test
  public void getNotConfiguredPropWithDefaultValue() {
    assertEquals(5, (int) myContainer.get(myProperty));
    assertTrue(myContainer.getConfiguredProperties().isEmpty());
  }

  @Test(expected = IllegalStateException.class)
  public void getNotConfiguredPropWithoutDefaultValue() {
    PropertySpec<Integer> prop = PropertySpec.withoutDefaultValue("test");
    myContainer.get(prop);
  }

  @Test
  public void setValueEqualToDefaultDoesNotSet() {
    Registration reg = myContainer.set(myProperty, 5);
    assertSame(Registration.EMPTY, reg);
    assertTrue(myContainer.getConfiguredProperties().isEmpty());
  }

  @Test
  public void setToNotDefault() {
    myContainer.set(myProperty, 10);
    assertEquals(10, (int) myContainer.get(myProperty));
    assertEquals(Sets.<PropertySpec<?>>newHashSet(myProperty), myContainer.getConfiguredProperties());
  }

  @Test
  public void setNullAsValue() {
    myContainer.set(myProperty, null);
    assertNull(myContainer.get(myProperty));
    assertEquals(Sets.<PropertySpec<?>>newHashSet(myProperty), myContainer.getConfiguredProperties());
  }
}