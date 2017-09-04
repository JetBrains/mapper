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
import jetbrains.jetpad.model.transform.MapTransformerTest;
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
    MapTransformerTest.class,
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