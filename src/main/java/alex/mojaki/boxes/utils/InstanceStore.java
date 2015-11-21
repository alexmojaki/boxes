package alex.mojaki.boxes.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A helper class for caching instances of objects based on constructor arguments in a thread-safe manner.
 * <p>
 * Override the {@link InstanceStore#getNew(Object...)} method to create a new instance, typically by calling a
 * constructor. Then then {@link InstanceStore#get(Object...)} method will only return a new instance when it
 * encounters new arguments. Otherwise it will return a previously created instance.
 * <p>
 * Usage of this class will look something like this:
 * <pre>
 * {@code
 * public class T {
 *
 *     private static final InstanceStore<T> INSTANCE_STORE = new InstanceStore<T>() {
 *         public T getNew(Object... args) {
 *             return new T((X) args[0], (Y) args[1]);
 *         }
 *     };
 *
 *     private T(X x, Y y) {
 *         // constructor here...
 *     }
 *
 *     public static T getInstance(X x, Y y) {
 *         return INSTANCE_STORE.get(x, y);
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of class being stored
 */
public abstract class InstanceStore<T> {

    private final ConcurrentMap<List<Object>, T> map = new ConcurrentHashMap<List<Object>, T>();

    /**
     * Override this method to return a new instance of the class of interest using the given arguments. This usually
     * means calling a constructor.
     */
    public abstract T getNew(Object... args);

    /**
     * Get an instance using the given arguments. If the arguments are new then a new instance
     * will be returned via {@link InstanceStore#getNew(Object...)}. Otherwise a cached instance will be returned.
     */
    public T get(Object... args) {
        List<Object> key = Arrays.asList(args);
        T result = map.get(key);
        if (result != null) {
            return result;
        }
        synchronized (map) {
            result = map.get(key);
            if (result == null) {
                result = getNew(args);
                map.put(key, result);
            }
            return result;
        }
    }

}
