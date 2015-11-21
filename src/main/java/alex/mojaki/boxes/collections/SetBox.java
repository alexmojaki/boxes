package alex.mojaki.boxes.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.WrapperBox;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;

import java.util.HashSet;
import java.util.Set;

/**
 * A set that can watch for changes. This is both a {@code Set} and a {@code PowerBox<Set>} and should
 * be declared as this type so that it can use the abilities of both. It is far preferable to creating a
 * {@code PowerBox} in a more usual way, e.g. a {@code CommonBox<Set>}, as you can now attach {@code ChangeObserver}s
 * that know when the set is mutated, which also implies you can create {@code View}s around it.
 *
 * @param <E> the type of the set elements
 * @see WrapperBox
 */
public class SetBox<E> extends CollectionBox<Set<E>, E> implements Set<E> {

    /**
     * Construct a {@code SetBox} belonging to the given family.
     */
    public SetBox(BoxFamily family) {
        super(family);
    }

    /**
     * Construct a {@code SetBox} belonging to a family identified by the given class a name.
     */
    public SetBox(Class<?> clazz, String name) {
        super(clazz, name);
    }

    @Override
    public Set<E> get() {
        return this;
    }

    /**
     * A convenience method that sets the value to an empty {@code HashSet}.
     *
     * @return this object for chaining
     */
    public SetBox<E> init() {
        set(new HashSet<E>());
        return this;
    }

    // Specifying the return type for chaining

    @Override
    public SetBox<E> set(Set<E> value) {
        super.set(value);
        return this;
    }

    @Override
    public SetBox<E> addChangeMiddleware(ChangeMiddleware... middlewares) {
        super.addChangeMiddleware(middlewares);
        return this;
    }

    @Override
    public SetBox<E> addChangeObserver(ChangeObserver... observers) {
        super.addChangeObserver(observers);
        return this;
    }

}
