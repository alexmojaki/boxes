package alex.mojaki.boxes.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.WrapperBox;
import com.google.common.collect.ForwardingIterator;

import java.util.Collection;
import java.util.Iterator;

/**
 * A collection that can watch for changes. This is primarily intended as a base class for {@link ListBox}
 * and {@link SetBox} to reuse code, which necessitates the unusual type parameters.
 *
 * @param <T> the type of the contained value (i.e. the specific type of collection that is being wrapped)
 * @param <E> the type of the collection elements
 */
public abstract class CollectionBox<T extends Collection<E>, E> extends WrapperBox<T> implements Collection<E> {

    public CollectionBox(BoxFamily family) {
        super(family);
    }

    public CollectionBox(Class<?> clazz, String name) {
        super(clazz, name);
    }

    @Override
    public int size() {
        return value.size();
    }

    @Override
    public boolean isEmpty() {
        return value.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return value.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        final Iterator<E> valueIterator = value.iterator();
        return new ForwardingIterator<E>() {

            @Override
            protected Iterator<E> delegate() {
                return valueIterator;
            }

            @Override
            public void remove() {
                super.remove();
                change();
            }
        };
    }

    @Override
    public Object[] toArray() {
        return value.toArray();
    }

    @Override
    public <A> A[] toArray(A[] a) {
        return value.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return changeIf(value.add(e));
    }

    @Override
    public boolean remove(Object o) {
        return changeIf(value.remove(o));
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return value.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return changeIf(value.addAll(c));
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return changeIf(value.removeAll(c));
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return changeIf(value.retainAll(c));
    }

    @Override
    public void clear() {
        value.clear();
        change();
    }

}
