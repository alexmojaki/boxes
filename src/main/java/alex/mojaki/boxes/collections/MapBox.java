package alex.mojaki.boxes.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.WrapperBox;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.ForwardingMapEntry;
import com.google.common.collect.ForwardingSet;

import java.util.*;

/**
 * A map that can watch for changes. This is both a {@code Map} and a {@code PowerBox<Map>} and should
 * be declared as this type so that it can use the abilities of both. It is far preferable to creating a
 * {@code PowerBox} in a more usual way, e.g. a {@code CommonBox<Map>}, as you can now attach {@code ChangeObserver}s
 * that know when the map is mutated, which also implies you can create {@code View}s around it.
 *
 * @param <K> the type of the map keys
 * @param <V> the type of the map values
 * @see WrapperBox
 */
public class MapBox<K, V> extends WrapperBox<Map<K, V>> implements Map<K, V> {

    private static final BoxFamily KEY_SET_FAMILY = BoxFamily.getInstance(MapBox.class, "keySet");
    private static final BoxFamily ENTRY_SET_FAMILY = BoxFamily.getInstance(MapBox.class, "entrySet");

    /**
     * Construct a {@code MapBox} belonging to the given family.
     */
    public MapBox(BoxFamily family) {
        super(family);
    }

    /**
     * Construct a {@code MapBox} belonging to a family identified by the given class a name.
     */
    public MapBox(Class<?> clazz, String name) {
        super(clazz, name);
    }

    @Override
    public Map<K, V> get() {
        return this;
    }

    /**
     * A convenience method that sets the value to an empty {@code HashMap}.
     *
     * @return this object for chaining
     */
    public MapBox<K, V> init() {
        set(new HashMap<K, V>());
        return this;
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
    public boolean containsKey(Object key) {
        return value.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return this.value.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return value.get(key);
    }

    @Override
    public V put(K key, V value) {
        return change(this.value.put(key, value));
    }

    @Override
    public V remove(Object key) {
        return change(value.remove(key));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        value.putAll(m);
        change();
    }

    @Override
    public void clear() {
        value.clear();
        change();
    }

    @Override
    public Set<K> keySet() {
        return watchedSet(this.value.keySet(), KEY_SET_FAMILY);
    }

    private <T> Set<T> watchedSet(Set<T> innerSet, BoxFamily family) {
        SetBox<T> setBox = new SetBox<T>(family).set(innerSet);
        //noinspection unchecked
        TARGETED_CHANGE_OBSERVER.register((PowerBox) setBox, this);
        return setBox;
    }

    @Override
    public Collection<V> values() {
        return value.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        final Set<Entry<K, V>> delegateSet = value.entrySet();
        Set<Entry<K, V>> innerSet = new ForwardingSet<Entry<K, V>>() {
            @Override
            protected Set<Entry<K, V>> delegate() {
                return delegateSet;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                final Iterator<Entry<K, V>> delegateIterator = super.iterator();
                return new ForwardingIterator<Entry<K, V>>() {
                    @Override
                    protected Iterator<Entry<K, V>> delegate() {
                        return delegateIterator;
                    }

                    @Override
                    public Entry<K, V> next() {
                        final Entry<K, V> delegateEntry = super.next();
                        return new ForwardingMapEntry<K, V>() {
                            @Override
                            protected Entry<K, V> delegate() {
                                return delegateEntry;
                            }

                            @Override
                            public V setValue(V value) {
                                return change(super.setValue(value));
                            }
                        };
                    }
                };
            }
        };
        return watchedSet(innerSet, ENTRY_SET_FAMILY);
    }

    // Specifying the return type for chaining

    @Override
    public MapBox<K, V> set(Map<K, V> value) {
        super.set(value);
        return this;
    }

    @Override
    public MapBox<K, V> addChangeMiddleware(ChangeMiddleware... middlewares) {
        super.addChangeMiddleware(middlewares);
        return this;
    }

    @Override
    public MapBox<K, V> addChangeObserver(ChangeObserver... observers) {
        super.addChangeObserver(observers);
        return this;
    }

}
