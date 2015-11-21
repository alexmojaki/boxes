package alex.mojaki.boxes.observers.change;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.utils.WeakConcurrentMultiMap;

import java.util.List;

/**
 * A {@link ChangeObserver} which can associate specific objects with boxes, to overcome the fact that boxes share
 * observers in a family rather than each storing their own. The {@link TargetedChangeObserver#onChange(PowerBox, Object, Object, Object, Object)} method receives an additional
 * parameter {@code target} which it can use to take a more specific action. Use the {@link TargetedChangeObserver#register(PowerBox, Object)} method to create an association.
 *
 * @param <T> the type parameter of the {@code PowerBox}es
 * @param <V> the type of the targets
 */
public abstract class TargetedChangeObserver<T, V> implements ChangeObserver<T> {

    private final WeakConcurrentMultiMap<PowerBox, V> map = new WeakConcurrentMultiMap<PowerBox, V>();

    @Override
    public void onChange(PowerBox<T> box, T originalValue, T finalValue, T requestedValue) {
        List<V> targets = map.get(box);
        int size = targets.size();
        // Don't want to create an iterator object to iterate through a tiny (likely singleton) list
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < size; i++) {
            onChange(box, originalValue, finalValue, requestedValue, targets.get(i));
        }
    }

    /**
     * Create an association between {@code box} and {@code target}, and add this observer to {@code box}.
     * When {@code box} changes, this observer's special
     * {@link TargetedChangeObserver#onChange(PowerBox, Object, Object, Object, Object)} method will be called
     * with {@code target} as the last argument.
     */
    public void register(PowerBox<T> box, V target) {
        map.put(box, target);
        box.addChangeObserver(this);
    }

    /**
     * Similar to the normal {@link ChangeObserver#onChange(PowerBox, Object, Object, Object)} method, except that
     * when {@code box} changes, this method is called once for every value of {@code target} that was associated
     * with {@code box} via the {@link TargetedChangeObserver#register(PowerBox, Object)} method.
     */
    public abstract void onChange(PowerBox<T> box, T originalValue, T finalValue, T requestedValue, V target);

}
