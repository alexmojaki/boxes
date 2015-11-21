package alex.mojaki.boxes.middleware.symmetric;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;

/**
 * Middleware which implements both {@code GetMiddleware} and {@code ChangeMiddleware} with the same effect on values.
 */
public abstract class SymmetricMiddleware<T> implements ChangeMiddleware<T>, GetMiddleware<T> {

    /**
     * Call {@code apply(box, requestedValue, currentValue);}.
     */
    @Override
    public T onChange(PowerBox<T> box, T originalValue, T currentValue, T requestedValue) {
        return apply(box, requestedValue, currentValue);
    }

    /**
     * Call {@link SymmetricMiddleware#apply(PowerBox, Object, Object)} with the same parameters.
     */
    @Override
    public T onGet(PowerBox<T> box, T originalValue, T currentValue) {
        return apply(box, originalValue, currentValue);
    }

    /**
     * Return the given {@code currentValue} or some transformation of it.
     *
     * @param box          the {@code PowerBox} whose value is being {@code set} or obtained.
     * @param firstValue   the {@code currentValue} of the first middleware in the sequence.
     * @param currentValue the value after transformations by previous middleware.
     */
    public abstract T apply(PowerBox<T> box, T firstValue, T currentValue);
}
