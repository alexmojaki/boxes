package alex.mojaki.boxes;

import alex.mojaki.boxes.exceptions.BoxParticipantError;
import alex.mojaki.boxes.exceptions.BoxParticipantException;
import alex.mojaki.boxes.exceptions.ParticipationDetails;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.get.GetObserver;

import java.util.List;

/**
 * This class supplies the logic of applying middleware and observers stored in a family, without specifying how the
 * value or family is stored.
 */
public abstract class AbstractPowerBox<T> implements PowerBox<T> {

    /**
     * Add the given middleware to the box's family (not the box itself). If the family already has this middleware
     * the call will have no effect.
     *
     * @return this object, for chaining
     */
    @Override
    public AbstractPowerBox<T> addChangeMiddleware(ChangeMiddleware... middlewares) {
        getFamily().addChangeMiddleware(middlewares);
        return this;
    }

    /**
     * Add the given middleware to the box's family (not the box itself). If the family already has this middleware
     * the call will have no effect.
     *
     * @return this object, for chaining
     */
    @Override
    public AbstractPowerBox<T> addGetMiddleware(GetMiddleware... middlewares) {
        getFamily().addGetMiddleware(middlewares);
        return this;
    }

    /**
     * Add the given observers to the box's family (not the box itself). If the family already has these observers
     * the call will have no effect.
     *
     * @return this object, for chaining
     */
    @Override
    public AbstractPowerBox<T> addChangeObserver(ChangeObserver... observers) {
        getFamily().addChangeObserver(observers);
        return this;
    }

    /**
     * Add the given observers to the box's family (not the box itself). If the family already has these observers
     * the call will have no effect.
     *
     * @return this object, for chaining
     */
    @Override
    public AbstractPowerBox<T> addGetObserver(GetObserver... observers) {
        getFamily().addGetObserver(observers);
        return this;
    }

    /**
     * Where it all happens!
     *
     * @param clazz (Get|Change)(Middleware|Observer).class, specifying the behaviour
     * @param list  list of instances of clazz to apply
     * @return transformed value (only relevant for middleware)
     */
    @SuppressWarnings("unchecked")
    private T applyParticipants(Class<?> clazz, List list, T originalValue, T requestedValue, T finalValue) {
        T currentValue = null;
        if (clazz == GetMiddleware.class) {
            currentValue = originalValue;
        } else if (clazz == ChangeMiddleware.class) {
            currentValue = requestedValue;
        }
        int size = list.size();
        int i = 0;
        try {
            for (; i < size; i++) {
                Object participant = list.get(i);
                if (clazz == GetMiddleware.class) {
                    currentValue = ((GetMiddleware<T>) participant).onGet(this, originalValue, currentValue);
                } else if (clazz == ChangeMiddleware.class) {
                    currentValue = ((ChangeMiddleware<T>) participant).onChange(this, originalValue, currentValue, requestedValue);
                } else if (clazz == GetObserver.class) {
                    ((GetObserver<T>) participant).onGet(this, originalValue, finalValue);
                } else {
                    ((ChangeObserver<T>) participant).onChange(this, originalValue, finalValue, requestedValue);
                }
            }
        } catch (Throwable throwable) {
            String message = "Error in " + clazz.getSimpleName() + " " + (i + 1) + " out of " + size +
                    " of " + getFamily().description() + ". ";

            if (!getFamily().showsValueStrings()) {
                message += "Values hidden.";
            } else if (this instanceof WrapperBox && this == originalValue && this == finalValue && this == requestedValue) {
                message += "Value = " + ((WrapperBox) this).revealedToString();
            } else {
                message += "Original value = " + originalValue + ". " +
                        (clazz == GetMiddleware.class || clazz == ChangeMiddleware.class ?
                                "Current value = " + currentValue :
                                "Final value = " + finalValue) + "." +
                        (clazz == ChangeObserver.class || clazz == GetObserver.class ?
                                " Requested value = " + requestedValue + "." : "");
            }
            ParticipationDetails details = new ParticipationDetails(this, i, list, clazz);
            if (throwable instanceof Error) {
                throw new BoxParticipantError(message, (Error) throwable).withDetails(details);
            }
            throw new BoxParticipantException(message, (Exception) throwable).withDetails(details);
        }

        return currentValue;
    }

    protected T applyGetMiddleware(T originalValue) {
        return applyParticipants(GetMiddleware.class, getFamily().getGetMiddlewares(), originalValue, null, null);
    }

    protected T applyChangeMiddleware(T originalValue, T requestedValue) {
        return applyParticipants(ChangeMiddleware.class, getFamily().getChangeMiddlewares(), originalValue, requestedValue, null);
    }

    protected void notifyGetObservers(T originalValue, T finalValue) {
        applyParticipants(GetObserver.class, getFamily().getGetObservers(), originalValue, null, finalValue);
    }

    protected void notifyChangeObservers(T originalValue, T finalValue, T requestedValue) {
        applyParticipants(ChangeObserver.class, getFamily().getChangeObservers(), originalValue, requestedValue, finalValue);
    }

    /**
     * Return the raw value this box represents, which may then be processed by middleware and observers.
     * This can mean returning a field, the result of a calculation as in a {@link View}, the value of an inner box
     * as in an {@link AbstractUpgradedBox}, or anything else.
     */
    protected abstract T rawGet();

    /**
     * Store the value this box represents, after middleware has been applied but before notifying {@link ChangeObserver}s.
     * This can mean assigning a field, calling {@code set} on an inner box, etc.
     */
    protected abstract void rawSet(T value);

    @Override
    public T get() {
        T originalValue = rawGet();
        T finalValue = applyGetMiddleware(originalValue);
        notifyGetObservers(originalValue, finalValue);
        return finalValue;
    }

    @Override
    public AbstractPowerBox<T> set(T value) {
        T oldValue = rawGet();
        T finalValue = applyChangeMiddleware(oldValue, value);
        rawSet(finalValue);
        try {
            notifyChangeObservers(oldValue, finalValue, value);
        } catch (BoxParticipantException e) {
            rawSet(oldValue);
            throw e;
        }
        return this;
    }

    /**
     * Returns the string value of the result of {@link PowerBox#get()} (which may be {@code "null"}),
     * meaning that calling this method will call any middleware and observers, unless the family indicates that
     * string values should be hidden.
     *
     * @see BoxFamily#hideValueStrings()
     */
    @Override
    public String toString() {
        BoxFamily family = getFamily();
        return family.showsValueStrings() ?
                String.valueOf(toStringSource()) :
                ("[Hidden value of " + family.description() + "]");
    }

    /**
     * Return the object from which the string representation should be constructed.
     */
    protected T toStringSource() {
        return get();
    }

    /**
     * Convenience method equivalent to {@code addChangeObserver(ThrowOnNull.INSTANCE)}.
     *
     * @return this box for chaining
     */
    public AbstractPowerBox<T> notNull() {
        getFamily().notNull();
        return this;
    }
}
