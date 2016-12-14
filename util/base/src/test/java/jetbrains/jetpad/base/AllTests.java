package jetbrains.jetpad.base;

import jetbrains.jetpad.base.base64.Base64EncoderTest;
import jetbrains.jetpad.base.base64.Base64URLSafeCoderTest;
import jetbrains.jetpad.base.edt.BufferingEdtManagerTest;
import jetbrains.jetpad.base.edt.EdtManagerPoolTest;
import jetbrains.jetpad.base.edt.ExecutorEdtManagerTest;
import jetbrains.jetpad.base.edt.RunningEdtManagerTest;
import jetbrains.jetpad.base.edt.TestEdtManagerTest;
import jetbrains.jetpad.base.edt.TestEventDispatchThreadTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    //base64 tests
    Base64EncoderTest.class,
    Base64URLSafeCoderTest.class,

    //edt tests
    BufferingEdtManagerTest.class,
    EdtManagerPoolTest.class,
    ExecutorEdtManagerTest.class,
    RunningEdtManagerTest.class,
    TestEdtManagerTest.class,
    TestEventDispatchThreadTest.class,

    AsyncsPairTest.class,
    AsyncsTest.class,
    CompositeAsyncTest.class,
    EnumsTest.class,
    IntervalTest.class,
    ObjectsTest.class,
    PersistersTest.class,
    RegistrationMapTest.class,
    SimpleAsyncRegistrationsTest.class,
    SimpleAsyncTest.class,
    ThrowableHandlersTest.class,
})
public class AllTests {
}
