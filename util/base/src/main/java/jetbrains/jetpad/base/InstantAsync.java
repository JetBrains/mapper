/*
 * Copyright 2012-2015 JetBrains s.r.o
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

class InstantAsync<ValueT> implements Async<ValueT> {
  private final ValueT myValue;

  InstantAsync(ValueT value) {
    myValue = value;
  }

  @Override
  public Registration onSuccess(Handler<? super ValueT> successHandler) {
    successHandler.handle(myValue);
    return Registration.EMPTY;
  }

  @Override
  public Registration onResult(Handler<? super ValueT> successHandler, Handler<Throwable> failureHandler) {
    return onSuccess(successHandler);
  }

  @Override
  public Registration onFailure(Handler<Throwable> failureHandler) {
    return Registration.EMPTY;
  }
}