package jetbrains.jetpad.model;

import jetbrains.jetpad.model.collections.ListListenersTest;
import jetbrains.jetpad.model.collections.ObservableArrayListTest;
import jetbrains.jetpad.model.collections.ObservableHashSetTest;
import jetbrains.jetpad.model.collections.ObservableSingleItemListTest;
import jetbrains.jetpad.model.collections.SelectTest;
import jetbrains.jetpad.model.collections.SetListenersTest;
import jetbrains.jetpad.model.collections.TreeListTest;
import jetbrains.jetpad.model.composite.CompositesBetweenTest;
import jetbrains.jetpad.model.composite.CompositesCommonAncestorTest;
import jetbrains.jetpad.model.composite.CompositesTest;
import jetbrains.jetpad.model.composite.TreePathTest;
import jetbrains.jetpad.model.event.EventSourceTest;
import jetbrains.jetpad.model.event.ListenersTest;
import jetbrains.jetpad.model.event.SelectFromListEventSourcesTest;
import jetbrains.jetpad.model.id.BaseIdTest;
import jetbrains.jetpad.model.property.BooleanPropertiesTest;
import jetbrains.jetpad.model.property.DerivedPropertyTest;
import jetbrains.jetpad.model.property.EventSelectionTest;
import jetbrains.jetpad.model.property.ListItemPropertyTest;
import jetbrains.jetpad.model.property.PropertyBindingTest;
import jetbrains.jetpad.model.property.PropertyPersistersTest;
import jetbrains.jetpad.model.property.PropertySelectionTest;
import jetbrains.jetpad.model.property.PropertyTest;
import jetbrains.jetpad.model.property.PropertyValidationTest;
import jetbrains.jetpad.model.property.UpdatablePropertyTest;
import jetbrains.jetpad.model.transform.CollectionTransformationTest;
import jetbrains.jetpad.model.transform.FilterByConstantTest;
import jetbrains.jetpad.model.transform.FilterListTest;
import jetbrains.jetpad.model.transform.FilterTest;
import jetbrains.jetpad.model.transform.FlattenListTest;
import jetbrains.jetpad.model.transform.FlattenPropertyListTest;
import jetbrains.jetpad.model.transform.HighestPriorityErrorCasesTest;
import jetbrains.jetpad.model.transform.HighestPriorityTest;
import jetbrains.jetpad.model.transform.MapTransfomerTest;
import jetbrains.jetpad.model.transform.PropertyToCollectionTest;
import jetbrains.jetpad.model.transform.ReverseTest;
import jetbrains.jetpad.model.transform.SelectListTest;
import jetbrains.jetpad.model.transform.SortByConstantTest;
import jetbrains.jetpad.model.util.ListMapTest;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    //collections
    ListListenersTest.class,
    ObservableArrayListTest.class,
    ObservableHashSetTest.class,
    ObservableSingleItemListTest.class,
    SelectTest.class,
    SetListenersTest.class,
    TreeListTest.class,

    //composite
    CompositesBetweenTest.class,
    CompositesCommonAncestorTest.class,
    CompositesTest.class,
    TreePathTest.class,

    //event
    EventSourceTest.class,
    ListenersTest.class,
    SelectFromListEventSourcesTest.class,

    //id
    BaseIdTest.class,

    //property
    BooleanPropertiesTest.class,
    DerivedPropertyTest.class,
    EventSelectionTest.class,
    ListItemPropertyTest.class,
    PropertyBindingTest.class,
    PropertyPersistersTest.class,
    PropertySelectionTest.class,
    PropertyTest.class,
    PropertyValidationTest.class,
    UpdatablePropertyTest.class,

    //transform
    CollectionTransformationTest.class,
    FilterByConstantTest.class,
    FilterListTest.class,
    FilterTest.class,
    FlattenListTest.class,
    FlattenPropertyListTest.class,
    HighestPriorityErrorCasesTest.class,
    HighestPriorityTest.class,
    MapTransfomerTest.class,
    PropertyToCollectionTest.class,
    ReverseTest.class,
    SelectListTest.class,
    SelectTest.class,
    SortByConstantTest.class,

    //util
    ListMapTest.class
})
public class AllTests {
}
