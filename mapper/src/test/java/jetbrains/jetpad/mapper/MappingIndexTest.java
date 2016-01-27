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

import jetbrains.jetpad.base.Registration;
import jetbrains.jetpad.test.BaseTestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

public class MappingIndexTest extends BaseTestCase {
  private MappingContext mappingContext;
  private MappingIndex mappingIndex;

  @Before
  public void setUp() {
    mappingContext = new MappingContext();
    mappingIndex = new MappingIndex(mappingContext);
  }

  @Test
  public void mapper() {
    TestMapper mapper = new TestMapper("source", "target");
    mapper.attachRoot(mappingContext);

    assertBySource("source", mapper);
    assertByTarget("target", mapper);

    mapper.detachRoot();

    assertBySource("source");
    assertByTarget("target");
  }

  @Test
  public void manyToMany() {
    TestMapper testMapper11 = new TestMapper("source 1", "target 1");
    testMapper11.attachRoot(mappingContext);
    TestMapper testMapper12 = new TestMapper("source 1", "target 2");
    testMapper12.attachRoot(mappingContext);
    TestMapper testMapper21 = new TestMapper("source 2", "target 1");
    testMapper21.attachRoot(mappingContext);
    TestMapper testMapper22 = new TestMapper("source 2", "target 2");
    testMapper22.attachRoot(mappingContext);

    assertBySource("source 1", testMapper11, testMapper12);
    assertBySource("source 2", testMapper21, testMapper22);
    assertByTarget("target 1", testMapper11, testMapper21);
    assertByTarget("target 2", testMapper12, testMapper22);

    testMapper11.detachRoot();
    testMapper12.detachRoot();
    testMapper21.detachRoot();
    testMapper22.detachRoot();

    assertBySource("source 1");
    assertBySource("source 2");
    assertByTarget("target 1");
    assertByTarget("target 2");
  }

  @Test
  public void synchronizer() {
    final SimpleMapping<String, String> testMapping = new SimpleMapping<>("sync source", "sync target");
    TestMapper mapper = new TestMapper("source", "target") {
      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        conf.add(new TestSynchronizer(testMapping));
      }
    };
    mapper.attachRoot(mappingContext);

    assertBySource("source", mapper);
    assertBySource("sync source", testMapping);
    assertByTarget("target", mapper);
    assertByTarget("sync target", testMapping);

    mapper.detachRoot();

    assertBySource("source");
    assertBySource("sync source");
    assertByTarget("target");
    assertByTarget("sync target");
  }

  @Test
  public void addLater() {
    TestMapper earlyMapper = new TestMapper("early source", "early target");
    earlyMapper.attachRoot(mappingContext);

    MappingIndex lateIndex = new MappingIndex(mappingContext);
    TestMapper mapper = new TestMapper("source", "target");
    mapper.attachRoot(mappingContext);

    Assert.assertTrue(lateIndex.getBySource("early source").contains(earlyMapper));
    Assert.assertTrue(lateIndex.getByTarget("early target").contains(earlyMapper));
    Assert.assertTrue(lateIndex.getBySource("source").contains(mapper));
    Assert.assertTrue(lateIndex.getByTarget("target").contains(mapper));
  }

  private void assertBySource(String source, Mapping... mappings) {
    Collection<Mapping<?, ?>> bySource = mappingIndex.getBySource(source);
    Assert.assertEquals(mappings.length, bySource.size());
    for (Mapping m : mappings) {
      Assert.assertTrue(bySource.contains(m));
    }
  }

  private void assertByTarget(String target, Mapping... mappings) {
    Collection<Mapping<?, ?>> byTarget = mappingIndex.getByTarget(target);
    Assert.assertEquals(mappings.length, byTarget.size());
    for (Mapping m : mappings) {
      Assert.assertTrue(byTarget.contains(m));
    }
  }

  private static class TestMapper extends Mapper<String, String> {
    public TestMapper(String source, String target) {
      super(source, target);
    }
  }

  private static class TestSynchronizer implements Synchronizer {
    private Mapping<?, ?> mapping;
    private Registration r;

    private TestSynchronizer(Mapping<?, ?> mapping) {
      this.mapping = mapping;
    }

    @Override
    public void attach(SynchronizerContext ctx) {
      r = ctx.registerMapping(mapping);
    }

    @Override
    public void detach() {
      r.remove();
    }
  }
}
