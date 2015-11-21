package alex.mojaki.boxes.utils;

import alex.mojaki.boxes.observers.change.TargetedChangeObserver;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This is a thread-safe map-like class that uses weak keys to avoid memory leaks and can associate keys with multiple
 * values. The intended use is to create relationships between boxes and specific objects, as in
 * {@link TargetedChangeObserver}.
 */
public class WeakConcurrentMultiMap<K, V> {

    private final LoadingCache<K, List<V>> cache = CacheBuilder.newBuilder()
            .weakKeys()
            .build(new CacheLoader<K, List<V>>() {
                @Override
                public List<V> load(K key) {
                    return new CopyOnWriteArrayList<V>();
                }
            });

    /**
     * Create an association between the given key and value. If there are any existing associations with this key
     * this does not replace them, it is added to them.
     */
    public void put(K key, V value) {
        cache.getUnchecked(key).add(value);
    }

    /**
     * Return the list of objects associated with this key.
     */
    public List<V> get(K key) {
        return cache.getUnchecked(key);
    }

}
