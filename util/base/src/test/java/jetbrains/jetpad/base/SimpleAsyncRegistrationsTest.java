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
package jetbrains.jetpad.base;

import org.junit.Test;

import java.util.ConcurrentModificationException;
import java.util.function.Consumer;

public class SimpleAsyncRegistrationsTest {
  private SimpleAsync<Void> async = new SimpleAsync<>();

  @Test
  public void removeSuccessRegistration() {
    Registration reg = async.onSuccess(throwingHandler());
    reg.remove();
    async.success(null);
  }

  @Test
  public void removeFailureRegistration() {
    Registration reg = async.onFailure(throwingFailureHandler());
    reg.remove();
    async.failure(null);
  }

  @Test
  public void removeCompositeRegistration1() {
    Registration reg = async.onResult(throwingHandler(), throwingFailureHandler());
    reg.remove();
    async.success(null);
  }

  @Test
  public void removeCompositeRegistration2() {
    Registration reg = async.onResult(throwingHandler(), throwingFailureHandler());
    reg.remove();
    async.failure(null);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeRegistrationInSuccessHandler() {
    final Value<Registration> regValue = new Value<>();
    Registration reg = async.onSuccess(item -> regValue.get().remove());
    regValue.set(reg);
    async.success(null);
  }

  @Test(expected = ConcurrentModificationException.class)
  public void removeRegistrationInFailureHandler() {
    final Value<Registration> regValue = new Value<>();
    Registration reg = async.onFailure(item -> regValue.get().remove());
    regValue.set(reg);
    async.failure(null);
  }

  @Test
  public void addSuccessHandlerAfterFailure() {
    async.failure(new Throwable());
    Registration reg = async.onSuccess(item -> { });
    reg.remove();
  }

  @Test
  public void addFailureHandlerAfterSuccess() {
    async.success(null);
    Registration reg = async.onFailure(item -> { });
    reg.remove();
  }

  @Test
  public void removeSuccessRegistrationAfterSuccess() {
    Registration reg = async.onSuccess(item -> { });
    async.success(null);
    reg.remove();
  }

  @Test
  public void removeSuccessRegistrationAfterFailure() {
    Registration reg = async.onSuccess(item -> { });
    async.failure(null);
    reg.remove();
  }

  @Test
  public void removeFailureRegistrationAfterSuccess() {
    Registration reg = async.onFailure(item -> { });
    async.success(null);
    reg.remove();
  }

  @Test
  public void removeFailureRegistrationAfterFailure() {
    Registration reg = async.onFailure(item -> { });
    async.failure(null);
    reg.remove();
  }

  private Consumer<Throwable> throwingFailureHandler() {
    return throwingHandler();
  }

  private <ItemT> Consumer<ItemT> throwingHandler() {
    return item -> {
      throw new RuntimeException();
    };
  }
}