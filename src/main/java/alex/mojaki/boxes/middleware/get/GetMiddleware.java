package alex.mojaki.boxes.middleware.get;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.observers.get.GetObserver;

/**
 * A {@link PowerBox} can apply a list of these objects when a user calls {@link PowerBox#get()} to change the final
 * value that is returned to the user.
 *
 * @see PowerBox#get()
 * @see PowerBox#addGetMiddleware(GetMiddleware[])
 * @see alex.mojaki.boxes.BoxFamily#addGetMiddleware(GetMiddleware[])
 */
public interface GetMiddleware<T> {

    /**
     * Return the given {@code currentValue} or some transformation of it.
     * Called during {@link PowerBox#get()} for zero or more of these objects before applying the list of
     * {@link GetObserver}s.
     *
     * @param box           the {@code PowerBox} whose value is being obtained by {@code get}.
     * @param originalValue the value the box contained before any middleware was applied.
     * @param currentValue  for the first middleware in the sequence, this is the same as {@code originalValue}.
     *                      For subsequent middleware, this is the return value of the previous middleware.
     * @return the final value to be returned to the user and the parameter {@code finalValue} in the
     * {@link GetObserver#onGet(PowerBox, Object, Object)} method for any {@code GetObserver}s this
     * box has.
     */
    T onGet(PowerBox<T> box, T originalValue, T currentValue);
}
