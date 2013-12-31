package jetbrains.jetpad.model.composite;

import jetbrains.jetpad.geometry.Rectangle;
import jetbrains.jetpad.geometry.Vector;
import jetbrains.jetpad.model.children.ChildList;
import jetbrains.jetpad.model.children.HasParent;
import jetbrains.jetpad.model.collections.list.ObservableList;
import jetbrains.jetpad.model.property.Property;
import jetbrains.jetpad.model.property.ValueProperty;

import java.util.List;

class TestComposite
    extends HasParent<TestComposite, TestComposite>
    implements Composite<TestComposite>, HasVisibility, HasFocusability, HasBounds {
  private ObservableList<TestComposite> myChildren = new ChildList<TestComposite, TestComposite>(this);
  private Property<Boolean> myVisible = new ValueProperty<Boolean>(true);
  private Property<Boolean> myFocusable = new ValueProperty<Boolean>(true);
  private Rectangle myBounds = new Rectangle(Vector.ZERO, Vector.ZERO);

  @Override
  public List<TestComposite> children() {
    return myChildren;
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
}
