package alex.mojaki.boxes.middleware.symmetric;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.utils.InstanceStore;

import static alex.mojaki.boxes.utils.Utils.lessThan;

// @formatter:off
/**
 * If the current value is beyond the given bounds, replace it with the bound it is above/below.
 * For example:
 *
 * <pre>{@code
 * PowerBox<Integer> percentage = new CommonBox<>(Example.class, "percentage")
 *         .addGetMiddleware(EnsureBounds.between(0, 100));
 * percentage.set(200);
 * name.get();  // returns 100
 * }</pre>
 *
 * You can specify two bounds with {@code between}, or a single bound with {@code minimum} or {@code maximum}.
 */
// @formatter:on
public class EnsureBounds<T> extends SymmetricMiddleware<Comparable<T>> {

    private static final InstanceStore<EnsureBounds> INSTANCE_STORE = new InstanceStore<EnsureBounds>() {
        @Override
        public EnsureBounds getNew(Object... args) {
            Comparable min = (Comparable) args[0];
            Comparable max = (Comparable) args[1];
            if (min == null && max == null) {
                throw new IllegalArgumentException("Both min and max are null");
            }
            if (min != null && max != null && lessThan(max, min)) {
                throw new IllegalArgumentException("The maximum (" + max + ") was set to less than the minimum (" + min + ")");
            }
            //noinspection unchecked
            return new EnsureBounds(min, max);
        }
    };

    private final Comparable<T> min;
    private final Comparable<T> max;

    private EnsureBounds(Comparable<T> min, Comparable<T> max) {
        this.min = min;
        this.max = max;
    }

    public static <T> EnsureBounds maximum(Comparable<T> max) {
        return between(null, max);
    }

    public static <T> EnsureBounds minimum(Comparable<T> min) {
        return between(min, null);
    }

    public static <T> EnsureBounds between(Comparable<T> min, Comparable<T> max) {
        return INSTANCE_STORE.get(min, max);
    }

    @Override
    public Comparable<T> apply(PowerBox<Comparable<T>> box, Comparable<T> firstValue, Comparable<T> currentValue) {
        if (currentValue == null) {
            return null;
        }
        if (min != null && lessThan(currentValue, min)) {
            return min;
        }
        if (max != null && lessThan(max, currentValue)) {
            return max;
        }
        return currentValue;
    }

}
