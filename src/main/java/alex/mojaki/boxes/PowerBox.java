package alex.mojaki.boxes;

import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.get.GetObserver;

/**
 * A {@link Box} which can dynamically change its behaviour regarding its value changing or {@link PowerBox#get()}
 * being called.
 */
public interface PowerBox<T> extends Box<T> {

    /**
     * Return the result of applying the {@link GetMiddleware}s added to the box's family, in the order they were
     * added, to the value stored by this box, then similarly notify the {@link GetObserver}s.
     */
    @Override
    T get();

    /**
     * Change the value stored by this box to the result of applying the {@link ChangeMiddleware}s
     * added to the box's family, in the order they were added, to the given parameter, then similarly notify
     * the {@link ChangeObserver}s.
     */
    @Override
    PowerBox<T> set(T value);

    /**
     * Add {@link ChangeMiddleware}s to this box's family.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    PowerBox<T> addChangeMiddleware(ChangeMiddleware... middlewares);

    /**
     * Add {@link GetMiddleware}s to this box's family.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    PowerBox<T> addGetMiddleware(GetMiddleware... middlewares);

    /**
     * Add {@link ChangeObserver}s to this box's family.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    PowerBox<T> addChangeObserver(ChangeObserver... observers);

    /**
     * Add {@link GetObserver}s to this box's family.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    PowerBox<T> addGetObserver(GetObserver... observers);

    /**
     * Return the family that this box belongs to.
     */
    BoxFamily getFamily();
}
