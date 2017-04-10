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
package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.SimpleComposite;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.List;

class TestComposite
    extends SimpleComposite<TestComposite, TestComposite>
    implements NavComposite<TestComposite>, HasVisibility, HasFocusability, HasBounds, HasFocusableBounds {
  private List<TestComposite> myChildren = new ChildList<>(this);
  private Property<Boolean> myVisible = new ValueProperty<>(true);
  private Property<Boolean> myFocusable = new ValueProperty<>(true);
  private Rectangle myBounds = new Rectangle(Vector.ZERO, Vector.ZERO);
  private Rectangle myFocusableBounds = new Rectangle(Vector.ZERO, Vector.ZERO);

  static TestComposite create(int x, int y, int width, int height) {
    TestComposite composite = new TestComposite();
    composite.setBounds(new Rectangle(x, y, width, height));
    composite.setFocusableBounds(new Rectangle(x, y, width, height));
    return composite;
  }

  @Override
  public TestComposite getParent() {
    return parent().get();
  }

  @Override
  public List<TestComposite> children() {
    return myChildren;
  }

  @Override
  public TestComposite nextSibling() {
    return Composites.nextSibling(this);
  }

  @Override
  public TestComposite prevSibling() {
    return Composites.prevSibling(this);
  }

  @Override
  public TestComposite firstChild() {
    if (myChildren.isEmpty()) return null;
    return myChildren.get(0);
  }

  @Override
  public TestComposite lastChild() {
    if (myChildren.isEmpty()) return null;
    return myChildren.get(myChildren.size() - 1);
  }

  @Override
  public Property<Boolean> visible() {
    return myVisible;
  }

  @Override
  public Property<Boolean> focusable() {
    return myFocusable;
  }

  @Override
  public Rectangle getBounds() {
    return myBounds;
  }

  public void setBounds(Rectangle rect) {
    myBounds = rect;
  }

  public void setFocusableBounds(Rectangle rect) {
    myFocusableBounds = rect;
  }

  @Override
  public Rectangle getFocusableBounds() {
    return myFocusableBounds;
  }
}