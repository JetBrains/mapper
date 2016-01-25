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
package jetbrains.jetpad.mapper;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.list.ObservableArrayList;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import jetbrains.jetpad.model.transform.Transformers;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class TransformingSynchronizerTest extends BaseTestCase {
  private ObservableList<String> source;
  private MyMapper mapper;
  private ObservableList<String> target;


  private void init(String... items) {
    source = new ObservableArrayList<>();
    Collections.addAll(source, items);
    mapper = new MyMapper(source);
    mapper.attachRoot();
    target = mapper.getTarget();
  }

  @Test
  public void initalMapping() {
    init("z", "c", "b");

    assertTarget("b", "c", "z");
  }

  @Test
  public void add() {
    init("a", "c");

    source.add("b");

    assertTarget("a", "b", "c");
  }

  @Test
  public void remove() {
    init("a", "c", "b");

    source.remove(1);

    assertTarget("a", "b");
  }

  private void assertTarget(String... items) {
    Assert.assertEquals(Arrays.asList(items), target);
  }

  static class MyMapper extends Mapper<ObservableList<String>, ObservableList<String>> {
    MyMapper(ObservableList<String> source) {
      super(source, new ObservableArrayList<String>());
    }

    @Override
    protected void registerSynchronizers(SynchronizersConfiguration conf) {
      super.registerSynchronizers(conf);

      conf.add(new TransformingObservableCollectionRoleSynchronizer<>(this, getSource(), Transformers.<String, String, String, ObservableList<String>>sortBy(new Function<String, ReadableProperty<String>>() {
          @Override
          public ReadableProperty<String> apply(String s) {
            return Properties.constant(s);
          }
        }), getTarget(), new MapperFactory<String, String>() {
        @Override
        public Mapper<? extends String, ? extends String> createMapper(String source) {
          return new Mapper<String, String>(source, source) {
          };
        }
      }));
    }
  }
}