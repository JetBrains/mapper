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
package jetbrains.jetpad.model.transform;

import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.property.ValueProperty;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class PropertyToCollectionTest {
  ValueProperty<Integer> from;
  ObservableCollection<Integer> to;
  Transformer<ReadableProperty<Integer>, ObservableCollection<Integer>> transformer;

  @Before
  public void setup() {
    from = new ValueProperty<Integer>(0);
    to = new ObservableHashSet<Integer>();
    transformer = Transformers.propertyToCollection();
  }

  @Test
  public void initTransformation() {
    transformer.transform(from, to);

    assertTrue(to.size() == 1);
    assertTrue(to.contains(0));
  }

  @Test
  public void changeProperty() {
    transformer.transform(from, to);
    from.set(1);

    assertTrue(to.size() == 1);
    assertTrue(to.contains(1));
  }

  @Test
  public void disposeTransformation() {
    Transformation<ReadableProperty<Integer>, ObservableCollection<Integer>> transformation = transformer.transform(from, to);
    transformation.dispose();
    from.set(1);

    assertTrue(to.size() == 1);
    assertTrue(to.contains(0));
  }

  @Test(expected = IllegalStateException.class)
  public void notEmptyCollection() {
    to.add(0);
    transformer.transform(from, to);
  }
}