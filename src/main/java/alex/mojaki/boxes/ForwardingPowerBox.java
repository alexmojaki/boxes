package alex.mojaki.boxes;

import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.get.GetObserver;

/**
 * A {@code PowerBox} that wraps around another {@code PowerBox} and forwards the interface methods
 * and {@code toString}.
 * This is not useful on its own but makes it easy it to implement the decorator pattern by subclassing.
 */
public abstract class ForwardingPowerBox<T> implements PowerBox<T> {

    protected final PowerBox<T> box;

    public ForwardingPowerBox(PowerBox<T> box) {
        this.box = box;
    }

    @Override
    public PowerBox<T> addChangeMiddleware(ChangeMiddleware... middlewares) {
        box.addChangeMiddleware(middlewares);
        return this;
    }

    @Override
    public PowerBox<T> addGetMiddleware(GetMiddleware... middlewares) {
        box.addGetMiddleware(middlewares);
        return this;
    }

    @Override
    public PowerBox<T> addChangeObserver(ChangeObserver... observers) {
        box.addChangeObserver(observers);
        return this;
    }

    @Override
    public PowerBox<T> addGetObserver(GetObserver... observers) {
        box.addGetObserver(observers);
        return this;
    }

    @Override
    public BoxFamily getFamily() {
        return box.getFamily();
    }

    @Override
    public T get() {
        return box.get();
    }

    @Override
    public PowerBox<T> set(T value) {
        return box.set(value);
    }

    @Override
    public String toString() {
        return box.toString();
    }
}
