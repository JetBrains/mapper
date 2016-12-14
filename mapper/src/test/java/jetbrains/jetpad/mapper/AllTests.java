package jetbrains.jetpad.mapper;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    ByTargetIndexTest.class,
    DifferenceBuilderTest.class,
    MapperTest.class,
    MappingContextTest.class,
    PartIteratorsTest.class,
    SynchonizersTest.class,
    TransformingSynchronizerTest.class
})
public class AllTests {
}
