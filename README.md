#JetBrains Mappers MVC Framework

##Introduction

Modern web (and not only web) applications contain a lot of UI code. Until recently, such code was usually written in an ad-hoc way. As a result,
it was bloated, buggy, leaked memory, and was hard to maintain. In this article, I will overview common problems in UI development, and present the
JetPad Mapper framework, a solution to these problems which was developed internally at JetBrains as part of a product which is currently under development.

##UI development and its problems

Duplicate code - we often write a lot of similar code. For example, if we bind a property from a model to the UI property, we write essentially
the same code. The same situation happens if we synchronize a collection from a model with a list view. The code is essentially the same, but it’s hard to generalize so that it won’t be duplicated.

Memory/resource leaks - when we develop UI, we usually use some kind of MVC architecture. This architecture uses observer pattern, which is a common
source of memory leaks. I.e. we forget to remove a listener, which we added. This kind of error is hard to debug even with good tools.

Model dependency tracking - One UI element might depend on several model elements. If such dependency tracking is implemented in an ad-hoc way
we might make a mistake in incremental dependency update. Again, such errors are laborious to debug.

Hierarchical data editing - In simple UI, we have one model, one view, and several controllers. However, if we have a hierarchical structure, which
needs to be mapped into a hierarchy of views, things get complicated very quickly. When container object, is deleted, we need to dispose all children
and dispose their resources which is hard to do in a right way if we do so in an ad-hoc way.

Testing - UI code is very hard to test. Test are usually brittle, slow, and unreliable. Some people think that UI code can be kept as simple as possible and all
logic should be moved to business logic layer but if you create custom components with complicated behavior, this is impossible. The complex logic should be kept in UI layer.

##Mapper Framework

###Requirements

Here were our requirements which we had in mind when we designed the framework:

* It should be suitable for both for traditional UI (i.e. forms) and structural editors such as rich text editors, diagrams, mind maps, and projectional editors (like MPS).
* It shouldn’t depend on any specific client technology, i.e. it should work equally well with GWT, JavaFX, AWT, SWT, or any other client side framework.
* It should be as model agnostic as possible. I.e. it should be possible to work with model object which doesn’t depend on our framework.
* It should provide solutions to the typical UI reuse problems which I mentioned previously.

##Mapper example

Before delving into details of the framework let’s take a look at an example code which illustrates features of the framework.

Let’s imagine we have an application which works with a file system (real or virtual). File system entity is represented with the following class:

    class FsNode {
      final Property<String> name = new ValueProperty<String>();
      final Property<Boolean> isFile = new ValueProperty<Boolean>();
      final ObservableList<FsNode> children = new ObservableArrayList<FsNode>();
    }

We want to map this model object into a tree widget which has the following interface for TreeNode:

    class TreeNode {
      void setIcon(Icon icon) {..}
      void setName(String name) {..}
      List<TreeNode> getChildren() {..}
    }

The interface for a tree class is the following:

    class Tree {
       TreeNode getRoot() { .. }
       void setRoot(TreeNode node) {...}
    }

Here is a mapper which maps a FsNode to a TreeNode:

    public class FsNodeMapper extends Mapper<FsNode, TreeNode> {
      public FsNodeMapper(FsNode source) {
        super(source, new TreeNode());
      }

      @Override
      protected void registerSynchronizers(SynchronizersConfiguration conf) {
        super.registerSynchronizers(conf);

        conf.add(Synchronizers.forProperties(getSource().name, new WritableProperty<String>() {
          @Override
          public void set(String value) {
            getTarget().setName(value);
          }
        }));

        conf.add(Synchronizers.forProperties(getSource().isFile, new WritableProperty<Boolean>() {
          @Override
          public void set(Boolean value) {
            getTarget().setIcon(value : FILE_ICON : FOLDER_ICON);
          }
        }));

        conf.add(Synchronizers.forObservableRole(this, getSource().children, getTarget().getChildren())
          .addMapperFactory(new MapperFactory<FsNode, TreeNode>() {
            @Override
            public Mapper<? extends FsNode, ? extends TreeNode> createMapper(FsNode source) {
              return new FsNodeMapper(source);
            }
          })
        );
      }
    }

Here is how we can use this mapper to create a Tree instance:

    static Tree createTree(FsNode node) {

      FsNodeMapper mapper = new FsNodeMapper(node);
      mapper.attachRoot();

      Tree result = new Tree();
      result.setRoot(mapper.getTarget());

      return result;
    }

##Mappers

The framework uses a variant of MVC architecture. Here are the entities which we have in our variant of this architecture:
Model - represents an application data. It provides methods for retrieving attributes and methods for subscribing to changes in them. Model shouldn’t have any dependencies on mapper framework.
View - represent the resulting UI which model is mapped to. View shouldn’t depend on model and mapper.
Mapper - an entity which binds model to view and creates child mappers, i.e. mappers for sub-part of the model.

Our architecture differs from orthodoxical architecture in several ways:
View doesn’t know about model. Instead, Mapper listens to changes in Model and update the View accordingly.
Instead of a Controller we have a Mapper

###Synchronizers

In the section on UI development problems I mentioned two common sources of code duplication, one of them is property synchronization
(aka data binding) and another is collection synchronization. To express such things we introduced a concept of a Synchronizer, a part
of a mapper which synchronizes some feature of a model with a feature of a view.
Life cycle of a synchronizer is tied to the lifecycle of a mapper: it has two methods attach() and detach(). Usually synchronizers add
listeners in attach() and remove them in detach(). This allows us to hide details of resource management from synchronizer’s user
and solve one of the problems with I mentioned in the section on common UI problems.

###Model support

Mappers and synchronizers represent the core of the framework. However, different model objects and views have different conventions on
how to represent parts of the model, i.e. how we access and listen to property/child changes etc. If we provide a set of synchronizers
and utility classes for supporting such conventions, we can substantially reduce number of duplicate code in an application.
In Java world the most popular such a convention is a JavaBeans specification. Unfortunately, it heavily relies on reflection mechanism
which isn’t available in GWT, our main target framework. As a result, we created our own convention and provided a set of synchronizers/other
utility classes for working with it.

###Properties

Properties represent a simple attribute of an object. There are  two property interfaces: ReadableProperty and WritableProperty. There are some utility classes:
ValueProperty - the simplest possible implementation of a Property which stores a property value inside of a field
DerivedProperty - the base class for properties which derive its value from values of several other properties. This class provides automatic
dependency tracking which solves the problem with the same name from the section on problems.
Properties - a class which contains a large number of property combinators

In order to simplify working with properties we have several property synchronizers:
* Synchronizers.forProperties(Property, Property) - for bi-directioanl synchronization
* Synchronizers.forProperty(ReadableProperty, WritableProperty) - for single directional synchronization
* Synchronizers.forSingleRole - for creating a new child mapper for property value
These two synchronizers implement our own data binding system.
Observable Collections

Properties aren’t always enough to represent object attributes. Sometimes, the value of an attribute is a collection. We have several interfaces to represent such collections:
* ObservableCollection
* ObservableSet
* ObservableList

We have several synchronizers to map such collections to views:
* Synchronizers.forObservableRole - there are two variants of this method. One takes a source list and another takes an additional transformer argument. We will discuss transformers in the next sections

###Transformers

Sometimes mapping a collection isn’t enough. We often need collections sorted, filtered or grouped in some way. To represent
such a thing, we have a concept of Transformers. This transformers can be passed as an additional argument in Synchronizers.forObservableRole.

Here is a list of the most common transformers:
* Transformers.sortBy - transforms a collection into a sorted list
* Transformers.listFilter - filters a list
* Transformers.filter - filters a collection
* Transformers.firstN - takes first n items of a list
* Transfomers.flattenList - flattens a list of lists
* Transfomers.addFirst - adds a fixed element to a list

##Applications


With the framework we created a large number of complex UI applications:
* A rich text editor
* A kanban board editor
* An internal rich internet application for video conferences scheduling (it will be open sourced).
* Different diagram editors
* A domain specific 3D editor
* A projectional editing framework (i.e. MPS on the web)
* All these applications were created with GWT.

Most of the mentioned applications provided collaborative editing with operation transformation framework, a topic which I will cover in future articles.