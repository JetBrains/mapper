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

public abstract class Registration implements Disposable {
  public static final Registration EMPTY = new Registration() {
    @Override
    protected void doRemove() {
    }

    @Override
    public void remove() {
    }
  };

  public static Registration from(final Disposable disposable) {
    return new Registration() {
      @Override
      protected void doRemove() {
        disposable.dispose();
      }
    };
  }

  public static Registration from(final Disposable... disposables) {
    return new Registration() {
      @Override
      protected void doRemove() {
        for (Disposable d : disposables) {
          d.dispose();
        }
      }
    };
  }

  private boolean myRemoved;

  protected abstract void doRemove();

  //this method should never be overridden except in Registration.EMPTY
  public void remove() {
    if (myRemoved) {
      throw new IllegalStateException("Registration already removed");
    }
    myRemoved = true;
    doRemove();
  }

  @Override
  public void dispose() {
    remove();
  }
}