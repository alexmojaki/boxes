package alex.mojaki.boxes.observers.change;

import alex.mojaki.boxes.PowerBox;

/**
 * Throws an {@code IllegalArgumentException} when the value changes to {@code null}.
 * <p/>
 * Note that all boxes start with a null value, and adding this does not affect that. It will only throw an exception
 * when you explicitly set null as the value.
 * <p/>
 * You can't create a new instance of this class. Use the public constant {@code I}, or {@code INSTANCE} if you prefer.
 */
public class ThrowOnNull implements ChangeObserver {

    public static final ThrowOnNull I = new ThrowOnNull();
    public static final ThrowOnNull INSTANCE = I;

    private ThrowOnNull() {
    }

    @Override
    public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
        if (finalValue == null) {
            throw new IllegalArgumentException("Cannot set " + box.getFamily().description() + " to null.");
        }
    }

}
