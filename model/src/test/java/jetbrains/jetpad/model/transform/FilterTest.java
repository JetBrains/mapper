package jetbrains.jetpad.model.transform;

import com.google.common.base.Function;
import jetbrains.jetpad.model.collections.ObservableCollection;
import jetbrains.jetpad.model.collections.set.ObservableHashSet;
import jetbrains.jetpad.model.property.Properties;
import jetbrains.jetpad.model.property.ReadableProperty;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class FilterTest {
  private ObservableCollection<String> from = new ObservableHashSet<String>();
  private ObservableCollection<String> to = new ObservableHashSet<String>();
  Transformer<ObservableCollection<String>, ObservableCollection<String>> filter = Transformers.filter(new Function<String, ReadableProperty<Boolean>>() {
    @Override
    public ReadableProperty<Boolean> apply(String s) {
      Boolean value;
      if (s.equals("null")) {
        value = null;
      } else {
        value = s.length() % 2 == 0;
      }
      return Properties.constant(value);
    }
  });

  @Test
  public void filterReturnsNull() {
    from.add("null");
    from.add("a");
    from.add("aa");
    filter.transform(from, to);
    assertTrue(to.size() == 1);
    assertTrue(to.iterator().next().equals("aa"));
  }

  @Test
  public void addToFromAfterNull() {
    from.add("null");
    filter.transform(from, to);
    from.add("aa");
    assertTrue(to.size() == 1);
    assertTrue(to.iterator().next().equals("aa"));
  }
}
