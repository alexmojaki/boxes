# Boxes

[![Join the chat at https://gitter.im/alexmojaki/boxes](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/alexmojaki/boxes?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Build status](https://travis-ci.org/alexmojaki/boxes.svg?branch=master)](https://travis-ci.org/alexmojaki/boxes) [![Coverage Status](https://coveralls.io/repos/alexmojaki/boxes/badge.svg?branch=master&service=github)](https://coveralls.io/github/alexmojaki/boxes?branch=master)


* [Introduction](#introduction)
* [Boxes in more detail](#boxes-in-more-detail)
  * [Plain Boxes](#plain-boxes)
  * [PowerBoxes](#powerboxes)
  * [Views](#views)
  * [Other kinds of PowerBoxes](#other-kinds-of-powerboxes)
    * [Upgraded boxes](#upgraded-boxes)
    * [WrapperBox](#wrapperbox)
    * [Unsettable adapters](#unsettable-adapters)
* [Writing observers and middleware](#writing-observers-and-middleware)
* [Optimising boxes](#optimising-boxes)
* [Exceptions and errors](#exceptions-and-errors)
* [String values of boxes](#string-values-of-boxes)


## Introduction

Boxes is a library that adds object oriented power to fields, letting you do better than traditional getters and setters. Let's start with a basic example. Below is a class that uses traditional getters and setters:

```
public class Person {

    private String firstName;
    private String lastName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

}
```

Now here is the version with boxes:

```
import static alex.mojaki.boxes.Boxes.box;

public class Person {

    public Box<String> firstName = box();
    public Box<String> lastName = box();

}
```

And here is how you use it:

```
Person person = new Person();

person.firstName.set("John");
person.lastName.set("Doe");

assert person.firstName.get().equals("John");
assert person.lastName.get().equals("Doe");
```

Now, maybe you didn't mind getters and setters. Maybe you generate them with your IDE or you use [Project Lombok](https://projectlombok.org/) or whatever. But dealing with boilerplate is only the beginning. Boxes let you do so much more.

By using boxes for your fields you can write code that is more elegant and reusable and less repetitive. You can write a generic method that can be applied to multiple different fields, not only using their values but setting them. If you want a method that essentially returns multiple values, you don't have to create a struct-like class to hold them or return an `Object[]` and then awkwardly unpack it with casts. You can just pass in boxes of values that need to be set as parameters. It's like having pass-by-reference or pointers in the language. If used properly, it can be extremely powerful.

As a more concrete example, you can transform a collection of boxes into a `Map` such that changes in the `Map` are reflected in the boxes and vice versa:

```
// Set up the map
BoxesMap<String, String> map = new BoxesMap<>();
map.putBox("firstName", firstName);
map.putBox("lastName", lastName);

// Now you can use either the boxes or the map as normal...
map.put("firstName", "John");
lastName.set("Doe");

// And the results appear seamlessly on the other side
assert firstName.get().equals("John");
assert map.get("lastName").equals("Doe");
```

Now you can use your objects in a much more dynamic style. You could achieve something similar using reflection, but it would be much messier.

But this is *still* only the beginning. These are just `Box`es, the most basic interface. Using a `PowerBox`, you can inject functionality into getters and setters at runtime. For example, suppose you want to track all changes to some field to debug an issue. All it takes is two lines, particularly:

`firstPerson.addChangeObserver(ChangePrinter.I);`

and you can expect to see results like this in standard output:

`Person.firstName value changed from John to Jack`

You could probably insert code into a traditional setter to do this kind of thing, but there are limitations. Firstly, if you have a normal field, it's easy to set the value directly (from within the class at least) and bypass the setter. So if you want to use such code you have to ensure that the setter is always used. Boxes provide this automatically. Secondly, inserting this code may be difficult or impossible, e.g. if the class is part of a separate library.

But now you may be worried about the overhead that this is creating. How much memory is each box using? The answer is very little: a typical `PowerBox` has two fields: one for the value it holds, and one for the `BoxFamily` it belongs to. All boxes corresponding to the same field of a class share this `BoxFamily` which in turn keeps track of `ChangeObserver`s  and other information. There is virtually no duplicated information or wasted space. With some extra boilerplate you can even create boxes that don't hold a reference to the `BoxFamily`. Alternatively you may not need the extra functionality and opt for a lightweight `Box`, but users of your class can still 'upgrade' the `Box` to a `PowerBox` if they need it. All this is covered soon. The point is that boxes will not do much harm unless you have very strict performance requirements.

A special kind of `PowerBox` is a `View`. This doesn't correspond to a traditional field: instead its value is calculated from other `PowerBox`es. When any of these values changes the value of the `View` will change, and you can subscribe to these changes just as you would with a normal `PowerBox`. For example, you might have boxes for various metrics of your system, and a `View` calculating an overall 'danger level' from these metrics, sending an alert when the level gets too high.

Another possibility is that computing the `View` is very expensive and you only want to calculate it as often as strictly necessary. You can then ask the view to cache its value, and if between calls to `get` on the view the boxes don't change, the calculation won't happen again.

What if the values within the boxes are mutable, so that their contents can change without any call to `set`? It is still possible under some circumstances to watch the boxes for changes by creating a `WrapperBox`. This is a `PowerBox` which implements the same interface as the value it contains and can watch for all calls to mutating methods. The library provides the classes `ListBox`, `SetBox`, and `MapBox` ready to use. This way you can safely create a `View` referring to a collection that knows when the collection changes in any way.

## Boxes in more detail

### Plain Boxes

So, how do boxes work, and how do you use them?

At the top of the hierarchy is the `Box` interface, which simply has two methods: `get` and `set`. You've already seen a demonstration at the beginning - it's that simple. To create a new lightweight box, use the static method `Boxes.box()` method with no arguments or with one argument providing an initial value. A mere `Box` is the thing to use when you have no need for any special functionality yet but it's possible that you or users of your API will want that in the future. Even if your code doesn't change, users of your API can swap out your `Box` for a `PowerBox` if they want, provided that:

1. The `Box` field is public (it's supposed to be anyway in most cases).
2. The field is not final.
3. The type is declared as the interface `Box`, not a class such as `BasicBox`.
4. You don't hold any references to the `Box` anywhere.

Technically users can still upgrade a `Box` even without the last condition, but the results might be unexpected. So if you store references to a `Box` (not a `PowerBox`) anywhere (e.g. in a `BoxesMap`) then either declare the `Box` as final or document the fact. If possible, document which of the methods `get` and `set` are called on the stored reference to the `Box`.

### PowerBoxes

Extending the `Box` interface is the `PowerBox`. This has 5 new methods, 4 of which look very similar: `addChangeMiddleware`, `addGetMiddleware`, `addChangeObserver`, and `addGetObserver`. For each of these methods there is a corresponding interface without the `add` part. These allow you to inject new behaviour into the `PowerBox` in various ways. The `Get*` interfaces are applied when someone calls `get`, not surprisingly. The `Change*` interfaces are for when the value represented by the box changes in any way, whether someone `set`s a new value, a new value of a `View` is computed, or the contents of a `WrapperBox` are mutated. `Middleware` transforms the value as it is applied, so that the value stored or calculated by a `PowerBox` may not be exactly what you `get` or `set`. For example, if you call `addChangeMiddleware(EnsureBounds.maximum(5))` on a `PowerBox`, followed by `set(10)`, the box will end up with the value `5`. An `Observer` sees the final value after all `Middleware` has been applied and has some side effect based on the result. This can be used for validation, e.g. `RequireBounds` will throw an exception if the value set is too high or too low, and `ThrowOnNull` does you'd expect. You can add as many middleware and observers as you want, and they will be applied in the order they were added.

A `PowerBox` belongs to a `BoxFamily` which stores the middleware, observers, and other metadata. This is so that each box uses as little memory as possible. The easiest way to specify the family is to pass two parameters to the constructor or `box` factory method: a `Class` and a `String` representing the name. The family will be looked up based on these values and so all boxes constructed with the same parameters will have the same family. Generally the parameters should be the class where the box is declared and the actual name of the field. You can find the family that a box belongs to using the `getFamily()` method.

Since many boxes share the same family, they may store different values but they will behave the same when you get or change them. Calling any of the `add` methods simply delegates to the family, so when you add an observer to one instance of a box, it will be seen by all the members of its family. However duplicate observers and middleware are ignored, so you can add them without worrying if they've been added before.

There are many classes that implement `PowerBox` for different needs. A `CommonBox` is most common (surprise!) and simply stores a value and a family. It can be obtained using the `box` factory method with a single `BoxFamily` argument or a `Class` and a `String`, or by using the constructor.

Here is an example of using `PowerBox`es with observers in a class:

```
public class Person extends CaseClass {

    public PowerBox<String> firstName = new CommonBox<>(Person.class, "firstName").notNull();
    public PowerBox<String> lastName = new CommonBox<>(Person.class, "lastName").notNull();
    public PowerBox<Integer> age = new CommonBox<>(Person.class, "age")
            .addChangeObserver(ThrowOnNull.I, RequireBounds.minimum(0, true));

}
```

The box `age` has two `ChangeObservers` attached to it. The first ensures that `set` is not called with `null` as an argument. The seconds ensures that `set` is called with a number that is at least 0 (the `true` means 'inclusive'). Both will throw an `IllegalArgumentException` with an informative message if their conditions are not met, and the box will keep its previous value. The two observers can be added in a single method call since all the `add*` methods take variable arguments. The method can also be called directly on the constructor since it returns the box itself, allowing method chaining. Overall you have a nice concise, declarative description of the fields in your class. There is just one instance of the `ThrowOnNull` observer, named `I` for minimal clutter. Using it is so common that there is a special method just for adding it called `notNull`, which you can see in the first two boxes.

### Views

A `View` is a very different kind of box. You cannot `set` a value on a view; instead, its value is defined by some calculation. The value is derived entirely from other `PowerBox`es, so the value of the view changes only if the value of one of the boxes changes.

For example, suppose you're given the following class:

```
class Rectangle {

    public PowerBox<Integer> width = box(Rectangle.class, "width");
    public PowerBox<Integer> height = box(Rectangle.class, "height");

}
```

and you want to construct an instance of it while ensuring that the area doesn't exceed a certain bound. Here's how you can do this using a `View`:

```
final Rectangle rectangle = new Rectangle();
int maximumArea = 100;
View<Integer> area = new View<>(Example.class, "area",
        rectangle.width, rectangle.height) {
    @Override
    public Integer calculate() {
        if (rectangle.width.get() == null || rectangle.height.get() == null) {
            return null;
        }
        return width.get() * height.get();
    }
}.addChangeObserver(RequireBounds.maximum(maximumArea, true));
rectangle.width.set(20);
rectangle.height.set(2); // No problem
rectangle.height.set(10); // throws exception
```

Let's break this down. A `View` is constructed in a similar manner to a `CommonBox`, but you can also add arguments for the boxes it depends on. It's also an abstract class; the `calculate` method needs to be defined. This defines the value of the `View` at any time. Since a `View` is a `PowerBox` we can directly add an observer to it, in this case enforcing that the area of the rectangle mustn't exceed 100.

Behind the scenes the `View` has added a `ChangeObserver` to both boxes it depends on. When you call `set` on either box it triggers that observer, causing the value of the view to update according to the `calculate` method. This in turn triggers the `RequireBounds` observer. If the value of the view is too high then an exception is thrown which propagates all the way back to when `set` was called.

Note that in the last line, the box value is temporarily set to 10, allowing the view to see that value to perform its calculation, but after the exception is thrown the value is rolled back to 2 so everything stays valid.

Another useful thing about views is that because they know whenever their value has changed, they can cache it safely without you having to do anything. Then when you `get` the value from the view it will only recalculate it if it actually needs to. This is great for avoiding duplicating work for a slow or expensive computation.

For views to work properly they must know when their value changes, meaning their value must depend only on their boxes and they must know when their boxes change. If the contents of the boxes are immutable this is trivial - they will change only when `set` is called. Otherwise you will need a `WrapperBox` to watch for changes within the contained object.

Since a `View` is a `PowerBox` you can include it in another view. In this way you can easily build a graph of complex calculations in a reactive programming style, and with caching in place this can lead to great results.

### Other kinds of PowerBoxes

#### Upgraded boxes

Suppose some class `A` has a public field `b` containing a mere `Box`, and you want to `add` observers or middleware to it but you can't because it's not a `PowerBox`. Here's what you can do:

`a.b = Boxes.upgrade(A.class, "b", a.b);`

where `a` is an instance of `A`. The critical point is that the field is public, so you can reassign it with a new, more powerful box! This isn't perfect though: if any references to the original `Box` are stored and used anywhere, then calls to `get` and `set` will bypass observers and middleware added to the upgraded box. This is why you should be careful about holding references to a `Box` that isn't a `PowerBox`. Nevertheless, the new box delegates to `get` and `set` in the original, so that the underlying value is the same on both sides, which might be good enough for some cases (e.g. if only one of `get` or `set` is called on the stored reference, and you're only interested in affecting the behaviour of the other).

#### WrapperBox

A `WrapperBox` is a special `CommonBox` which implements the same interface as the type of the value it contains, i.e. `WrapperBox<T> implements T`. All methods of this interface delegate directly to the underlying value, but if one of these methods mutates the value, then all `ChangeObserver`s are notified. `ListBox`, `SetBox`, and `MapBox` are `WrapperBox`es for their respective types. So a `ListBox` can be used as a regular `List`, and you can know whenever its contents change, e.g. by a call to `add` or `remove`. There is no way to get at the underlying value to bypass the change notifications as `get` simply returns the `WrapperBox` itself. But there is no need, since apart from the `ChangeObservers`, the box behaves exactly the same as its contents. `equals`, `hashCode`, and `toString` are taken care of. You can write wrappers for your own types quite easily, provided that the types exist as an interface and not only a class.

#### Unsettable adapters

If you want to use a box but you don't want users of your external API to be able to `set` its value, you can do this:

```
private PowerBox<Integer> _id = box(Thing.class, "id");
public PowerBox<Integer> id = Boxes.unsettableAdapter(_id);
```

The adapter delegates all methods except `set`, which will throw an exception. Now users can `get` the value, add observers, etc. without accidentally breaking anything.

## Writing observers and middleware

Here are the definitions of the four interfaces:

```
public interface ChangeMiddleware<T> {
    T onChange(PowerBox<T> box, T originalValue, T currentValue, T requestedValue);
}

public interface GetMiddleware<T> {
    T onGet(PowerBox<T> box, T originalValue, T currentValue);
}

public interface ChangeObserver<T> {
    void onChange(PowerBox<T> box, T originalValue, T finalValue, T requestedValue);
}

public interface GetObserver<T> {
    void onGet(PowerBox<T> box, T originalValue, T finalValue);
}
```

The first argument is always the box whose value changing or being obtained via `get`. The second is the value before any middleware are applied, and in the case of the `onChange` methods the value before the change occurred. `requestedValue`, the last parameter for the `onChange` methods, is the new value before middleware is applied. It is named this way because it is usually applied when someone calls `set`, i.e. the user requests a value by passing it to `set`. `currentValue` is the value after the transformations that have been applied by middleware so far. It's the most important parameter for middleware, and usually the only one needed. For the first `GetMiddleware`, `currentValue` is the same as `originalValue`. For `ChangeMiddleware`, it's `requestedValue`. The return value of the middleware will be the `currentValue` of the next middleware if there is one. If it's the last middleware then the result will become the parameter `finalValue` in each of the observers. It will also be stored in the box in the case of `set`, or returned to the user for `get`. Like `currentValue`, `finalValue` is the most relevant parameter to observers.

Middleware is for changing the value of a `get` or `set`, while observers are for incurring a side effect based on the result. They are separated so that observers can guarantee that `finalValue` really is the final result, whereas middleware don't know if subsequent middleware will change the value. Therefore you shouldn't mutate the parameters of an observer. Rather mutate `currentValue` in a middleware and return it. Also be aware that no cloning or copying occurs in the process, so for example mutations in `GetMiddleware` will change the value within the box. This will probably lead to unpleasant results as users don't expect `get` to affect state.

Most of the time it's sensible for a single class to implement both `GetMiddleware` and `ChangeMiddleware` with the same effect on values. In this case you can extend the abstract class `SymmetricMiddleware` and override the single method `apply`.

In the case of `ChangeObserver`, always assume that the value has changed, even if it doesn't look like it. In particular when a `WrapperBox` is mutated, all four parameters will be identical.

Make sure that instances obtained with the same arguments are equal, or they will be duplicated in the lists maintained by `BoxFamily`. One way to do this is to override `equals`. Alternatively you can make any constructors private and provide static factory methods which return the same instances for the same arguments. The `InstanceStore` class can help with this.

Remember that middleware and observers are shared by all boxes in the same family, so if you must never create a new, unique observer for a box. Instead you should you use a single observer (for each unique combination of arguments) and look up the relevant object based on the box argument of the observer's method. This is what the `View` and `WrapperBox` classes do, using a `TargetedChangeObserver` which in turn is backed by a `WeakConcurrentMultiMap`.

## Optimising boxes

The boxes API is designed to be quick and easy while your classes are new and may change soon, while also allowing you to make them more efficient if you're willing to write some boilerplate. Suppose you have a box like this:

    public PowerBox<String> someField = new CommonBox<>(Example.class, "someField")
        .addChangeObserver(SomeChangeObserver.getInstance(argument));

There are a couple of inefficiencies here. The first is that every time this box is constructed for a new instance of your overall class, it uses the class and name parameters to look up its family in a map. Instead you can create the family once yourself as a static field and pass it directly:

    private static final BoxFamily someFieldFamily = BoxFamily.getInstance(Example.class, "someField");
    public PowerBox<String> someField = new CommonBox<>(someFieldFamily)
            .addChangeObserver(SomeChangeObserver.getInstance(argument));

The next step is very simple: add observers and middleware directly to the family instead of the box.

    private static final BoxFamily someFieldFamily = BoxFamily.getInstance(Example.class, "someField")
            .addChangeObserver(SomeChangeObserver.getInstance(argument));
    public PowerBox<String> someField = new CommonBox<>(someFieldFamily);

These two steps reduce the time taken to create a box. If you want to decrease the amount of memory each box uses, create a nested class (static if possible) extending `DefaultPowerBox`. This is the parent of `CommonBox` and it doesn't have a field to store the `BoxFamily` (hence the reduced memory usage), so you have to tell it how to obtain it:

    private static class SomeField extends DefaultPowerBox<String> {
        @Override
        public BoxFamily getFamily() {
            return someFieldFamily;
        }
    }
    public PowerBox<String> someField = new SomeField();

And that's it! It's ugly, but it's easy, and really you shouldn't need to do it often anyway.

Similar steps can be taken if you're upgrading a `Box` in a separate API to a `PowerBox`. For the last step, instead of `DefaultPowerBox`, you will need to override `AbstractUpgradedBox` for the same effect.

## Exceptions and errors

If an observer or middleware throws an exception, this is wrapped in a `BoxParticipantException` with detailed information like this:

    Error in ChangeObserver 2 out of 3 of Example.number.
    Original value = null. Final value = 15. Requested value = 5.

The exception includes a box called `details` from which you can get some of this information.

If an `Error` is thrown then it will instead be wrapped in a `BoxParticipantError`, which is essentially the same but won't be caught by `catch (Exception e) {...}`.

If the values appearing in the exception message concerns you, read the next section.

## String values of boxes

Usually, `box.toString()` is the same as `box.get().toString()`, unless `box.get()` is null in which case it's `"null"`. For a `PowerBox` this can be suppressed, along with string values appearing in error messages, with `box.getFamily().hideValueStrings()`. This is useful for hiding sensitive data or avoiding the processing of creating very long strings, e.g. for long lists. If you really want to get to the string value despite this, `box.get().toString()` will still work, except for a `WrapperBox` where you will need `box.revealedToString()`.