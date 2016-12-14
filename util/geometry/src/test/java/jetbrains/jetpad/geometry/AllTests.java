package jetbrains.jetpad.geometry;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DistanceTest.class,
    DoubleRectangleIntersectionTest.class,
    DoubleSegmentIntersectionTest.class,
    DoubleVectorOperationsTest.class,
    RectanglesTest.class,
    RectangleTest.class,
    VectorTest.class
})
public class AllTests {
}
