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
package jetbrains.jetpad.model.composite;

import java.util.List;

import static java.util.Arrays.asList;

class SimpleComposite implements NavComposite<SimpleComposite> {
  private final String name;
  private SimpleComposite parent;
  private List<SimpleComposite> children;

  SimpleComposite(String name, SimpleComposite... children) {
    this.name = name;
    this.children = asList(children);
    for (SimpleComposite c : children) {
      c.parent = this;
    }
  }

  @Override
  public SimpleComposite nextSibling() {
    if (parent == null) {
      return null;
    } else {
      int index = parent.children().indexOf(this);
      if (index < 0) {
        throw new IllegalStateException("SimpleComposite isn't a child of it's parent.");
      } else if (index == parent.children.size() - 1) {
        return null;
      } else {
        return parent.children().get(index + 1);
      }
    }
  }

  @Override
  public SimpleComposite prevSibling() {
    if (parent == null) {
      return null;
    } else {
      int index = parent.children().indexOf(this);
      if (index < 0) {
        throw new IllegalStateException("SimpleComposite isn't a child of it's parent.");
      } else if (index == 0) {
        return null;
      } else {
        return parent.children().get(index - 1);
      }
    }
  }

  @Override
  public SimpleComposite firstChild() {
    return children.isEmpty() ? null : children.get(0);
  }

  @Override
  public SimpleComposite lastChild() {
    return children.isEmpty() ? null : children.get(children.size() - 1);
  }

  @Override
  public List<SimpleComposite> children() {
    return children;
  }

  @Override
  public SimpleComposite getParent() {
    return parent;
  }

  @Override
  public String toString() {
    return "Composite(" +name + ')';
  }
}