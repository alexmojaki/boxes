package alex.mojaki.boxes.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.WrapperBox;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import com.google.common.collect.ForwardingListIterator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

/**
 * A list that can watch for changes. This is both a {@code List} and a {@code PowerBox<List>} and should
 * be declared as this type so that it can use the abilities of both. It is far preferable to creating a
 * {@code PowerBox} in a more usual way, e.g. a {@code CommonBox<List>}, as you can now attach {@code ChangeObserver}s
 * that know when the list is mutated, which also implies you can create {@code View}s around it.
 *
 * @param <E> the type of the list elements
 * @see WrapperBox
 */
public class ListBox<E> extends CollectionBox<List<E>, E> implements List<E> {

    private static final BoxFamily SUB_LIST_FAMILY = BoxFamily.getInstance(ListBox.class, "subList");

    /**
     * Construct a {@code ListBox} belonging to the given family.
     */
    public ListBox(BoxFamily family) {
        super(family);
    }

    /**
     * Construct a {@code ListBox} belonging to a family identified by the given class a name.
     */
    public ListBox(Class<?> clazz, String name) {
        super(clazz, name);
    }

    @Override
    public List<E> get() {
        return this;
    }

    /**
     * A convenience method that sets the value to an empty {@code ArrayList}.
     *
     * @return this object for chaining
     */
    public ListBox<E> init() {
        return set(new ArrayList<E>());
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        return changeIf(value.addAll(index, c));
    }

    @Override
    public E get(int index) {
        return value.get(index);
    }

    @Override
    public E set(int index, E element) {
        return change(value.set(index, element));
    }

    @Override
    public void add(int index, E element) {
        value.add(index, element);
        change();
    }

    @Override
    public E remove(int index) {
        return change(value.remove(index));
    }

    @Override
    public int indexOf(Object o) {
        return value.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return value.lastIndexOf(o);
    }

    @Override
    public ListIterator<E> listIterator() {
        return listIterator(0);
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        final ListIterator<E> delegate = value.listIterator(index);
        return new ForwardingListIterator<E>() {

            @Override
            protected ListIterator<E> delegate() {
                return delegate;
            }

            @Override
            public void remove() {
                super.remove();
                change();
            }

            @Override
            public void set(E e) {
                super.set(e);
                change();
            }

            @Override
            public void add(E e) {
                super.add(e);
                change();
            }
        };
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        ListBox<E> subList = new ListBox<E>(SUB_LIST_FAMILY)
                .set(value.subList(fromIndex, toIndex));
        //noinspection unchecked
        TARGETED_CHANGE_OBSERVER.register((PowerBox) subList, this);
        return subList;
    }

    // Specifying the return type for chaining

    @Override
    public ListBox<E> set(List<E> value) {
        super.set(value);
        return this;
    }

    @Override
    public ListBox<E> addChangeMiddleware(ChangeMiddleware... middlewares) {
        super.addChangeMiddleware(middlewares);
        return this;
    }

    @Override
    public ListBox<E> addChangeObserver(ChangeObserver... observers) {
        super.addChangeObserver(observers);
        return this;
    }

}
