package alex.mojaki.boxes.observers.change;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;

/**
 * A {@link PowerBox} can apply a list of these objects after the value of a box changes, typically to incur some
 * side effect such as logging or to throw an exception if the value is invalid.
 *
 * @see PowerBox#set(Object)
 * @see PowerBox#addChangeObserver(ChangeObserver[])
 * @see BoxFamily#addChangeObserver(ChangeObserver[])
 */
public interface ChangeObserver<T> {

    /**
     * Take some action based on the values involved in the change.
     * Called when the value of a {@code PowerBox} changes for zero or more of these objects.
     *
     * @param box            the {@code PowerBox} whose value changed.
     * @param originalValue  the value the box contained before the change.
     * @param finalValue     the new value the box will have. In the case of {@link PowerBox#set(Object)},
     *                       this is the final result of applying all {@link ChangeMiddleware}.
     * @param requestedValue the parameter of {@link PowerBox#set(Object)}, or just the same as {@code originalValue}
     *                       in the case of a mutation to a {@code WrapperBox}.
     */
    void onChange(PowerBox<T> box, T originalValue, T finalValue, T requestedValue);
}
