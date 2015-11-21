package alex.mojaki.boxes.collections;

import alex.mojaki.boxes.Box;

import java.util.*;

import static alex.mojaki.boxes.Boxes.box;

/**
 * A map where all the values are contained in boxes, and changes in the boxes are reflected in the map and vice versa.
 * That is, if you call {@code map.putBox(key, box)}, then {@code map.put(key, value)} will always be equivalent to
 * {@code box.set(value)}, and {@code map.get(key)} is the same as {@code box.get()}.
 * <p>
 * By default new boxes must be added via {@link BoxesMap#putBox(Object, Box)}. If you try to {@code put} a key that
 * has no corresponding box, this will throw an exception. There is a boolean box {@code allowsBoxlessKeys} that
 * determines this behaviour. By default it is false. If set to true (you can do this with the chaining method
 * {@code allowBoxlessKeys()}) then you can put any key, and new boxes will be created for unrecognised keys.
 * You can retrieve the box associated with a key using {@link BoxesMap#getBox(Object)}.
 * <p>
 * The map is backed by a {@code HashMap}, and is not thread-safe.
 */
public class BoxesMap<K, V> extends AbstractMap<K, V> {

    protected final Map<K, Box<V>> map = new HashMap<K, Box<V>>();

    /**
     * If false (the default), {@link BoxesMap#put(Object, Object)} will throw an exception for keys that are
     * not already present in the map, and new keys must be added via {@link BoxesMap#putBox(Object, Box)} only.
     */
    public final Box<Boolean> allowsBoxlessKeys = box(false);

    /**
     * Set {@link BoxesMap#allowsBoxlessKeys} to true and return this object.
     */
    public BoxesMap<K, V> allowBoxlessKeys() {
        allowsBoxlessKeys.set(true);
        return this;
    }

    /**
     * Put the given non-null box into the map.
     *
     * @return the previous box associated with the key, or null if there
     * is none (similar to {@link Map#put(Object, Object)}).
     */
    public Box<V> putBox(K key, Box<V> box) {
        if (box == null) {
            throw new NullPointerException("Null boxes are not allowed. What would you do with them? " +
                    "But boxes with null values are allowed.");
        }
        return map.put(key, box);
    }

    /**
     * Return the box associated with the given key.
     *
     * @return the box associated with the given key, or null if there is no such box. Since null boxes cannot be placed
     * in the map, a return value of null guarantees that the key does not exist in the map.
     */
    public Box<V> getBox(K key) {
        return map.get(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    private V valueOrNull(Box<V> box) {
        if (box == null) {
            return null;
        }
        return box.get();
    }

    @Override
    public V get(Object key) {
        return valueOrNull(map.get(key));
    }

    /**
     * Like the usual {@link Map#put(Object, Object)}, but throws {@code NoSuchElementException} if the given key does
     * not already exist in the map and {@link BoxesMap#allowsBoxlessKeys} is false.
     */
    @Override
    public V put(K key, V value) {
        Box<V> box = map.get(key);
        V previous = null;
        if (box == null) {
            if (!allowsBoxlessKeys.get()) {
                throw new NoSuchElementException(
                        "There is no existing box associated with " + key + ". " +
                                "Use either putBox() or allowBoxlessKeys()."
                );
            }
            box = box();
            map.put(key, box);
        } else {
            previous = box.get();
        }
        box.set(value);
        return previous;
    }

    @Override
    public V remove(Object key) {
        return valueOrNull(map.remove(key));
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public Set<K> keySet() {
        return map.keySet();
    }

    @Override
    public Collection<V> values() {
        return new AbstractCollection<V>() {
            @Override
            public Iterator<V> iterator() {
                return new Iterator<V>() {

                    private final Iterator<Box<V>> iter = map.values().iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public V next() {
                        return iter.next().get();
                    }

                    @Override
                    public void remove() {
                        iter.remove();
                    }
                };
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new Iterator<Entry<K, V>>() {

                    private final Iterator<Entry<K, Box<V>>> iter = map.entrySet().iterator();

                    @Override
                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    @Override
                    public Entry<K, V> next() {
                        final Entry<K, Box<V>> entry = iter.next();
                        return new Entry<K, V>() {
                            @Override
                            public K getKey() {
                                return entry.getKey();
                            }

                            @Override
                            public V getValue() {
                                return entry.getValue().get();
                            }

                            @Override
                            public V setValue(V value) {
                                Box<V> box = entry.getValue();
                                V previous = box.get();
                                box.set(value);
                                return previous;
                            }

                            @Override
                            public final String toString() {
                                return getKey() + "=" + getValue();
                            }

                            @Override
                            public final int hashCode() {
                                return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
                            }

                            @Override
                            public final boolean equals(Object o) {
                                if (o == this) {
                                    return true;
                                }
                                if (o instanceof Map.Entry) {
                                    Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
                                    if (Objects.equals(getKey(), e.getKey()) &&
                                            Objects.equals(getValue(), e.getValue())) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };
                    }

                    @Override
                    public void remove() {
                        iter.remove();
                    }
                };
            }

            @Override
            public int size() {
                return map.size();
            }
        };
    }

}
