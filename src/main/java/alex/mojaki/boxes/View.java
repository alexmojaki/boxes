package alex.mojaki.boxes;

import alex.mojaki.boxes.exceptions.BoxParticipantException;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.TargetedChangeObserver;
import alex.mojaki.boxes.observers.get.GetObserver;

/**
 * A {@link PowerBox} whose value is calculated based on the values of other {@code PowerBox}es and knows when those
 * boxes change, allowing it to safely cache its own value to save computation and notify {@link ChangeObserver}s
 * that the result of its calculation is now different.
 * <p>
 * Note that views have some base performance overhead and writing your code to use a view for caching wherever
 * possible will not necessarily speed up your program - it may even slow it down. You should use views for caching
 * only when the computation is slow or expensive.
 * <p>
 * The {@link PowerBox#set(Object)} method and the use of {@link ChangeMiddleware} are unsupported.
 *
 * @param <T> the type of the calculated value
 */
public abstract class View<T> extends CommonBox<T> {

    private static final TargetedChangeObserver<Object, View> TARGETED_CHANGE_OBSERVER = new TargetedChangeObserver<Object, View>() {
        @Override
        public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue, View target) {
            target.update();
        }
    };

    private boolean cacheValid = false;

    /**
     * Construct a view with the given family and whose value depends on the given boxes
     */
    public View(BoxFamily family, PowerBox... boxes) {
        super(family);
        construct(boxes);
    }

    /**
     * Construct a view by looking up a family with the given class and name and whose value depends on the given boxes.
     */
    public View(Class<?> clazz, String name, PowerBox... boxes) {
        super(clazz, name);
        construct(boxes);
    }

    private void construct(PowerBox[] boxes) {
        addBoxes(boxes);
        getFamily().getChangeMiddlewares().disable();
    }

    /**
     * Indicate that the value of this view depends on the given boxes in addition to any previously added boxes or
     * boxes given in the constructor.
     */
    public void addBoxes(PowerBox... boxes) {
        for (PowerBox box : boxes) {
            //noinspection unchecked
            TARGETED_CHANGE_OBSERVER.register(box, this);
        }
    }

    /**
     * Indicate that one of the boxes that this view depends on has changed in value, meaning that this view has likely
     * changed its value as well. If this view has any {@code ChangeObserver}s they will be notified immediately
     * with a new value from the {@link View#calculate()} method.
     * Since there is no middleware involved, the last two parameters of
     * {@link ChangeObserver#onChange(PowerBox, Object, Object, Object)} will be the same.
     * If a new value is successfully calculated, the cache will now be valid. Otherwise it will now be invalid.
     */
    private void update() {
        if (!getFamily().getChangeObservers().isEmpty()) {
            T oldValue = value;
            calculateAndCache();
            try {
                notifyChangeObservers(oldValue, value, value);
            } catch (BoxParticipantException e) {
                value = oldValue;
                cacheValid = false;
                throw e;
            }
        } else {
            cacheValid = false;
        }
    }

    // @formatter:off
    /**
     * Return the value of this view as derived from the boxes it depends on. For the view to function properly, it is
     * essential that:
     * <ol>
     *     <li>The result of this method is a deterministic function of the boxes the view depends on, meaning that:
     *     <ol>
     *         <li>The method does not depend on any other values of any kind, so add all appropriate boxes in
     *         the constructor and/or the {@link View#addBoxes(PowerBox[])}
     *         method and do not rely on any variables not found in boxes.</li>
     *         <li>Separate calls to this method return the same results if the boxes haven't changed, i.e.
     *         there is no randomness or internal, invisible state involved.</li>
     *     </ol></li>
     *     <li>The values of the boxes cannot change without notifying their {@code ChangeObserver}s
     *     and thus this view. If the {@code set} method is called on a box its observers will certainly be
     *     notified, so it remains to ensure that the state of an object cannot change in such a way that
     *     the result of this method would change without notifying observers. Thus each box must either:
     *     <ol>
     *         <li>Contain an immutable type such as {@code Integer} or {@code String}, or</li>
     *         <li>Intercept any calls that change the value of the box with respect to this method, typically
     *         by being a {@link WrapperBox}.</li>
     *     </ol></li>
     * </ol>
     * <p>
     * When {@link PowerBox#get()} is called, {@code calculate()} will be called only if a box has changed its value
     * since the previous call to {@code calculate()}. In addition, if the view has any {@code ChangeObserver}s added,
     * {@code calculate()} is called every time one of the boxes changes, i.e. the value of view might change.
     */
    // @formatter:on
    public abstract T calculate();

    @Override
    protected T rawGet() {
        if (cacheValid) {
            return value;
        }
        calculateAndCache();
        return value;
    }

    private void calculateAndCache() {
        value = calculate();
        cacheValid = true;
    }

    @Override
    public AbstractPowerBox<T> set(T value) {
        throw new UnsupportedOperationException("You cannot set a value on a view. It must be calculated.");
    }

    // Specifying the return type for chaining

    @Override
    public View<T> addChangeObserver(ChangeObserver... observers) {
        super.addChangeObserver(observers);
        return this;
    }

    @Override
    public View<T> addGetObserver(GetObserver... observers) {
        super.addGetObserver(observers);
        return this;
    }

    @Override
    public View<T> addGetMiddleware(GetMiddleware... middleware) {
        super.addGetMiddleware(middleware);
        return this;
    }

}
