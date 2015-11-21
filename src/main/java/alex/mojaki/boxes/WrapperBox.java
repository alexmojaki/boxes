package alex.mojaki.boxes;

import alex.mojaki.boxes.collections.CollectionBox;
import alex.mojaki.boxes.collections.ListBox;
import alex.mojaki.boxes.collections.MapBox;
import alex.mojaki.boxes.collections.SetBox;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.TargetedChangeObserver;

import java.util.Objects;

/**
 * A box which implements the same interface as the type of the value it contains and notifies its {@link ChangeObserver}s
 * when it is mutated via the {@link WrapperBox#change()} method.
 * <p>
 * This class does not enforce this description, but provides the machinery to do so easily, and you should
 * subclass it with this intent. The source of {@link MapBox} is a good example of how this is done. Here are the steps
 * to follow to create your own:
 * <p>
 * <ol>
 * <li>Ensure that the type you want to wrap exists as an interface and that code typically uses that interface
 * as a type instead of an implementing class. If implementations override {@code equals}, it should be possible for them
 * to be equal to other implementations of the interface. If any methods of the interface return an object that
 * is capable of mutating the callee, that return type should also be an interface.</li>
 * <li>Create a class that extends {@code WrapperBox<T>} and implements {@code T}, where {@code T} is your interface.</li>
 * <li>Implement the methods of your interface by forwarding method calls to the field {@code value}, e.g.
 * {@code @Override public int size() { return value.size(); }}.</li>
 * <li>
 * <ul>
 * <li>If the method might cause the value to change, call the {@link WrapperBox#change()} method afterwards,
 * e.g. {@code void clear() { value.clear(); change(); }}.</li>
 * <li> If the method must also return a value then the method {@link WrapperBox#change(Object)} is convenient,
 * e.g. {@code V remove(Object key) { return change(value.remove(key)); }}. In some cases
 * {@link WrapperBox#changeIf(boolean)} is also useful.</li>
 * <li>If the method returns an object that is capable of mutating the callee, the return type must be an
 * interface and the return value must be an implementation that forwards and calls {@code change()} in a
 * similar way. See the source of {@link CollectionBox#iterator()} and {@link ListBox#subList(int, int)}
 * for examples.</li>
 * </ul>
 * </li>
 * <li>Override {@link WrapperBox#get()} to just "{@code return this;}".</li>
 * </ol>
 * <p>
 * This class should not allow users to access the underlying value or they could change it without notifying observers.
 * This is why {@code get()} returns the object itself (by you overriding it as such). Because of this it doesn't make
 * sense to add {@code GetObserver} or {@code GetMiddleware}, so these are disabled.
 * <p>
 * Note that when you set a new value on a {@code PowerBox}, it will revert to the original value if an exception occurs
 * in the middleware or observers, but no such rollback will occur here if there is an exception thrown by an observer
 * after the value changes internally.
 *
 * @param <T> the type of the contained value, which subclasses should also implement.
 * @see ListBox
 * @see SetBox
 * @see MapBox
 */
public abstract class WrapperBox<T> extends CommonBox<T> {

    protected static final TargetedChangeObserver<?, WrapperBox> TARGETED_CHANGE_OBSERVER = new TargetedChangeObserver<Object, WrapperBox>() {
        @Override
        public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue, WrapperBox target) {
            target.change();
        }
    };

    public WrapperBox(BoxFamily family) {
        super(family);
        construct();
    }

    public WrapperBox(Class<?> clazz, String name) {
        super(clazz, name);
        construct();
    }

    private void construct() {
        getFamily().getGetMiddlewares().disable();
        getFamily().getGetObservers().disable();
    }

    /**
     * Notify all {@link ChangeObserver}s. The parameters to
     * {@link ChangeObserver#onChange(PowerBox, Object, Object, Object)} will all be this object. Call this method
     * whenever the internal state of this value might have changed in such a way that is externally visible.
     */
    protected void change() {
        T thisValue = get();
        notifyChangeObservers(thisValue, thisValue, thisValue);
    }

    /**
     * Call {@link WrapperBox#change()} and return the given value. This is just easier and shorter than manually
     * saving the return value, calling {@code change()}, and returning the saved value.
     *
     * @param returnValue the value to return
     * @return the parameter {@code returnValue}
     */
    protected <R> R change(R returnValue) {
        change();
        return returnValue;
    }

    /**
     * Similar to {@link WrapperBox#change(Object)}, but only calls {@link WrapperBox#change()} if the parameter is
     * {@code true}. As well as being slightly nicer than a full if statement, this is especially convenient when
     * the return value is an indication of whether there was a change, e.g. see {@link CollectionBox#add(Object)}.
     *
     * @param condition the return value, and also whether the box's inner value has changed.
     * @return the parameter {@code condition}.
     */
    protected boolean changeIf(boolean condition) {
        if (condition) {
            change();
        }
        return condition;
    }

    @Override
    protected T rawGet() {
        return get();
    }

    /**
     * Return this object.
     */
    @Override
    public abstract T get();

    @Override
    protected T toStringSource() {
        return value;
    }

    /**
     * Return the string representation of the underlying value (or {@code "null"} if the value is {@code null}),
     * even if string values should be hidden for this box.
     *
     * @see BoxFamily#hideValueStrings()
     * @see WrapperBox#toString()
     */
    public String revealedToString() {
        return String.valueOf(value);
    }

    /**
     * Return whether the underlying value is equal to the argument, handling nulls appropriately.
     * To avoid breaking symmetry in the equals contract, it is essential that implementations of the interface that
     * this box contains allow equality between different implementations of the interface. For example, a
     * {@code LinkedList} can be equal to an {@code ArrayList} because they check {@code obj instanceof List}, not e.g.
     * {@code obj instanceof LinkedList}. Without this {@code ListBox} would not be possible.
     */
    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object obj) {
        //noinspection EqualsBetweenInconvertibleTypes
        return Objects.equals(value, obj);
    }

    /**
     * Return the hash code of the underlying value, handling nulls appropriately.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Return whether the underlying value is null.
     */
    public boolean isNull() {
        return value == null;
    }

}
