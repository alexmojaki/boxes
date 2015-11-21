package alex.mojaki.boxes.observers.change;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.utils.InstanceStore;

import static alex.mojaki.boxes.utils.Utils.lessThan;

/**
 * If the value is beyond the given bounds, or if it is equal to a bound that is not inclusive
 * throw an {@code IllegalArgumentException}.
 * For example:
 * <p/>
 * <pre>{@code
 * PowerBox<Double> ratio = new CommonBox<>(Example.class, "ratio")
 *         .addChangeObserver(RequireBounds.between(0.0, false, 1.0, true));
 *                                                         ^           ^
 *                                                     exclusive   inclusive
 * // Valid:
 * ratio.set(0.5);
 * ratio.set(1.0);
 *
 * // Invalid:
 * ratio.set(-1.0);
 * ratio.set(5.0);
 * ratio.set(0.0);  // lower bound is exclusive
 * }</pre>
 * <p/>
 * You can specify two bounds with {@code between}, or a single bound with {@code minimum} or {@code maximum}.
 */
public class RequireBounds<T> implements ChangeObserver<Comparable<T>> {

    private static final InstanceStore<RequireBounds> INSTANCE_STORE = new InstanceStore<RequireBounds>() {
        @Override
        public RequireBounds getNew(Object... args) {
            //noinspection unchecked
            return new RequireBounds((Comparable) args[0], (Boolean) args[1], (Comparable) args[2], (Boolean) args[3]);
        }
    };

    private final Comparable<T> min;
    private final boolean minInclusive;
    private final Comparable<T> max;
    private final boolean maxInclusive;

    private RequireBounds(Comparable<T> min, boolean minInclusive, Comparable<T> max, boolean maxInclusive) {
        if (min == null && max == null) {
            throw new IllegalArgumentException("Both min and max are null");
        }
        if (min != null && max != null) {
            if (lessThan(max, min)) {
                throw new IllegalArgumentException("The maximum (" + max + ") " +
                        "was set to less than the minimum (" + min + ").");
            } else if (min.equals(max) && !(minInclusive && maxInclusive)) {
                throw new IllegalArgumentException("The minimum and maximum are both " + min +
                        " and they are not both inclusive, which is impossible to satisfy.");
            }
        }
        this.min = min;
        this.minInclusive = minInclusive;
        this.max = max;
        this.maxInclusive = maxInclusive;
    }

    public static <T> RequireBounds maximum(Comparable<T> max, boolean inclusive) {
        return between(null, false, max, inclusive);
    }

    public static <T> RequireBounds minimum(Comparable<T> min, boolean inclusive) {
        return between(min, inclusive, null, false);
    }

    public static <T> RequireBounds between(Comparable<T> min, boolean minInclusive, Comparable<T> max, boolean maxInclusive) {
        return INSTANCE_STORE.get(min, minInclusive, max, maxInclusive);
    }

    @Override
    public void onChange(PowerBox<Comparable<T>> box, Comparable<T> originalValue, Comparable<T> finalValue, Comparable<T> requestedValue) {
        if (finalValue == null) {
            return;
        }
        if (min != null) {
            if (lessThan(finalValue, min)) {
                final BoxFamily family = box.getFamily();
                throw new IllegalArgumentException(
                        family.showsValueStrings() ?
                                ("Tried setting " + family.description() +
                                        " to " + finalValue + " which is less than " + min)
                                : "");
            }
            if (!minInclusive && finalValue.equals(min)) {
                final BoxFamily family = box.getFamily();
                throw new IllegalArgumentException(
                        family.showsValueStrings() ?
                                ("Tried setting " + family.description() +
                                        " to " + finalValue + " which is the exclusive minimum")
                                : "");
            }
        }
        if (max != null) {
            if (lessThan(max, finalValue)) {
                final BoxFamily family = box.getFamily();
                throw new IllegalArgumentException(
                        family.showsValueStrings() ?
                                ("Tried setting " + family.description() +
                                        " to " + finalValue + " which is more than " + max)
                                : "");
            }
            if (!maxInclusive && finalValue.equals(max)) {
                final BoxFamily family = box.getFamily();
                throw new IllegalArgumentException(
                        family.showsValueStrings() ?
                                ("Tried setting " + family.description() +
                                        " to " + finalValue + " which is the exclusive maximum")
                                : "");
            }
        }
    }
}
