package alex.mojaki.boxes.middleware.change;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.observers.change.ChangeObserver;

/**
 * A {@link PowerBox} can apply a list of these objects when a value is set to change the final value that is actually
 * stored by the box.
 *
 * @see PowerBox#set(Object)
 * @see PowerBox#addChangeMiddleware(ChangeMiddleware[])
 * @see BoxFamily#addChangeMiddleware(ChangeMiddleware[])
 */
public interface ChangeMiddleware<T> {

    /**
     * Return the given {@code currentValue} or some transformation of it.
     * Called during {@link PowerBox#set(Object)} for zero or more of these objects before applying the list of
     * {@link ChangeObserver}s.
     *
     * @param box            the {@code PowerBox} whose value is being {@code set}.
     * @param originalValue  the value the box contained before it was set.
     * @param currentValue   for the first middleware in the sequence, this is the same as {@code requestedValue}.
     *                       For subsequent middleware, this is the return value of the previous middleware.
     * @param requestedValue the parameter of {@link PowerBox#set(Object)}.
     * @return the final value to be stored in the box and the parameter {@code finalValue} in the
     * {@link ChangeObserver#onChange(PowerBox, Object, Object, Object)} method for any {@code ChangeObserver}s this
     * box has.
     */
    T onChange(PowerBox<T> box, T originalValue, T currentValue, T requestedValue);
}
