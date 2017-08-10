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
    PersistersTest.class,
    RegistrationMapTest.class,
    SimpleAsyncRegistrationsTest.class,
    SimpleAsyncTest.class,
    ThrowableHandlersTest.class,
})
public class AllTests {
}