package alex.mojaki.boxes;

import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.ThrowOnNull;
import alex.mojaki.boxes.observers.get.GetObserver;
import alex.mojaki.boxes.utils.InstanceStore;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.ForwardingList;
import com.google.common.collect.ForwardingListIterator;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class holds metadata for a group of {@code PowerBox}es, including middleware and observers.
 * Boxes which return the same value from {@link PowerBox#getFamily()} are said to belong to the
 * same family and share all this metadata.
 * <p/>
 * Here is an example of declaring a box and its family:
 * <p/>
 * <pre>{@code
 * class MyClass {
 *     public static final BoxFamily myFieldFamily = BoxFamily.getInstance(MyClass.class, "myField");
 *     PowerBox<Integer> myField = box(myFieldFamily);
 * }
 * }</pre>
 * <p/>
 * This is equivalent to:
 * <p/>
 * <pre>{@code
 *     PowerBox<Integer> myField = box(MyClass.class, "myField");
 * }</pre>
 * <p/>
 * which is more concise but less efficient as the family has to be looked up.
 */
public class BoxFamily {

    private static final InstanceStore<BoxFamily> INSTANCE_STORE = new InstanceStore<BoxFamily>() {
        @Override
        public BoxFamily getNew(Object... args) {
            return new BoxFamily((Class<?>) args[0], (String) args[1]);
        }
    };

    private final String name;
    private final Class<?> clazz;

    private ParticipantList<ChangeMiddleware> changeMiddlewares = new ParticipantList<ChangeMiddleware>();
    private ParticipantList<ChangeObserver> changeObservers = new ParticipantList<ChangeObserver>();
    private ParticipantList<GetMiddleware> getMiddlewares = new ParticipantList<GetMiddleware>();
    private ParticipantList<GetObserver> getObservers = new ParticipantList<GetObserver>();

    private boolean showsValueStrings = true;

    /**
     * Obtain an instance of {@code BoxFamily} identified by the given class and name.
     * Repeated calls with the same arguments return the same instance.
     *
     * @param clazz the class in which the boxes belonging to this family are declared.
     *              This can be obtained later by {@link BoxFamily#getDeclaringClass()}
     * @param name  the name of the boxes belonging to this family.
     *              This can be obtained later by {@link BoxFamily#getName()}
     * @return the unique instance with this class and name.
     */
    public static BoxFamily getInstance(Class<?> clazz, String name) {
        return INSTANCE_STORE.get(clazz, name);
    }

    private BoxFamily(Class<?> clazz, String name) {
        Objects.requireNonNull(clazz, "Class must not be null");
        Objects.requireNonNull(name, "Name must not be null");
        this.clazz = clazz;
        this.name = name;
    }

    /**
     * Return the name of the boxes belonging to this family.
     */
    public String getName() {
        return name;
    }

    /**
     * Return the class in which the boxes belonging to this family are declared.
     */
    public Class<?> getDeclaringClass() {
        return clazz;
    }

    /**
     * Add {@link ChangeMiddleware}s which will be applied by boxes belonging to this family
     * when {@link PowerBox#set(Object)} is called.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    public BoxFamily addChangeMiddleware(ChangeMiddleware... middlewares) {
        return add(changeMiddlewares, (Object[]) middlewares);
    }

    /**
     * Add {@link GetMiddleware}s which will be applied by boxes belonging to this family
     * when {@link PowerBox#get()} is called.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    public BoxFamily addGetMiddleware(GetMiddleware... middlewares) {
        return add(getMiddlewares, (Object[]) middlewares);
    }

    /**
     * Add {@link ChangeObserver}s which will be applied by boxes belonging to this family
     * when the value of the box changes.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    public BoxFamily addChangeObserver(ChangeObserver... observers) {
        return add(changeObservers, (Object[]) observers);
    }

    /**
     * Add {@link GetObserver}s which will be applied by boxes belonging to this family
     * when {@link PowerBox#get()} is called.
     * Arguments equal to instances that have already been added will be ignored.
     *
     * @return this object for chaining
     */
    public BoxFamily addGetObserver(GetObserver... observers) {
        return add(getObservers, (Object[]) observers);
    }

    private BoxFamily add(List list, Object... participants) {
        Collections.addAll(list, participants);
        return this;
    }

    /**
     * Get the {@link ChangeMiddleware}s of this family.
     */
    public ParticipantList<ChangeMiddleware> getChangeMiddlewares() {
        return changeMiddlewares;
    }

    /**
     * Get the {@link ChangeObserver}s of this family.
     */
    public ParticipantList<ChangeObserver> getChangeObservers() {
        return changeObservers;
    }

    /**
     * Get the {@link GetMiddleware}s of this family.
     */
    public ParticipantList<GetMiddleware> getGetMiddlewares() {
        return getMiddlewares;
    }

    /**
     * Get the {@link GetObserver}s of this family.
     */
    public ParticipantList<GetObserver> getGetObservers() {
        return getObservers;
    }

    /**
     * A simple human readable description of the boxes belonging to this family, equal to:
     * <p/>
     * {@code getDeclaringClass().getSimpleName() + "." + getName()}
     */
    public String description() {
        return getDeclaringClass().getSimpleName() + "." + getName();
    }

    @Override
    public String toString() {
        return "BoxFamily " + description();
    }

    /**
     * Return whether boxes in this family should show the string representations of their values in
     * {@link PowerBox#toString()} and in exception messages. By default this returns {@code true}.
     * If {@link BoxFamily#hideValueStrings()} has been called it returns {@code false}.
     */
    public boolean showsValueStrings() {
        return showsValueStrings;
    }

    /**
     * Prevent boxes in this family from showing the string representations of their values in
     * {@link PowerBox#toString()} and in exception messages. This is to avoid leaking sensitive data in logs
     * or computing expensive string representations. It is still possible to explicitly
     * obtain the string representation if desired.
     *
     * @see BoxFamily#showsValueStrings()
     * @see AbstractPowerBox#toString()
     * @see WrapperBox#revealedToString()
     */
    public BoxFamily hideValueStrings() {
        showsValueStrings = false;
        return this;
    }

    /**
     * Convenience method equivalent to {@code addChangeObserver(ThrowOnNull.INSTANCE)}.
     *
     * @return this object for chaining
     */
    public BoxFamily notNull() {
        return addChangeObserver(ThrowOnNull.I);
    }

    /**
     * A thread-safe, append-only list of observers/middleware. All modifications other than {@code add()}
     * are forbidden.
     *
     * @see ParticipantList#disable()
     */
    public static class ParticipantList<E> extends ForwardingList<E> {

        private ParticipantList() {
        }

        private volatile List<E> inner = new CopyOnWriteArrayList<E>();

        @Override
        public boolean add(E e) {
            if (inner == Collections.emptyList()) {
                throw new UnsupportedOperationException("This kind of observer/middleware is disabled for this BoxFamily.");
            }
            return ((CopyOnWriteArrayList<E>) inner).addIfAbsent(e);
        }

        /**
         * Clear this list and prevent any further modifications to it. For example:
         * <p/>
         * {@code box.getFamily().getChangeMiddlewares().disable();}
         * <p/>
         * means that {@code ChangeMiddleware} can't be used on boxes in this family.
         */
        public void disable() {
            inner = Collections.emptyList();
        }

        @Override
        protected List<E> delegate() {
            return inner;
        }

        @Override
        public E set(int index, E element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, E element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public E remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        @Override
        public ListIterator<E> listIterator(final int index) {
            final ListIterator<E> delegateIterator = delegate().listIterator(index);
            return new ForwardingListIterator<E>() {

                @Override
                protected ListIterator<E> delegate() {
                    return delegateIterator;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void set(E e) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void add(E e) {
                    throw new UnsupportedOperationException();
                }

            };
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            return Collections.unmodifiableList(delegate().subList(fromIndex, toIndex));
        }

        @Override
        public Iterator<E> iterator() {
            final Iterator<E> delegateIterator = delegate().iterator();
            return new ForwardingIterator<E>() {

                @Override
                protected Iterator<E> delegate() {
                    return delegateIterator;
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> coll) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

    }

}
