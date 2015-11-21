package alex.mojaki.boxes.utils;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.WrapperBox;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.ChangePrinter;

/**
 * Container for miscellaneous static utility methods
 */
public enum Utils {
    ;

    public static boolean lessThan(Comparable x, Comparable y) {
        //noinspection unchecked
        return x.compareTo(y) < 0;
    }

    /**
     * Return a concise human readable description of a change to a {@code PowerBox} using the arguments to
     * {@link ChangeObserver#onChange(PowerBox, Object, Object, Object)}. This is what is printed by
     * {@link ChangePrinter}.
     */
    public static String describeChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
        String beginning = box.getFamily().description() + " value changed ";
        if (box instanceof WrapperBox && box == originalValue && box == finalValue && box == requestedValue) {
            return beginning + "to " + ((WrapperBox) box).revealedToString();
        }
        return beginning + "from " + originalValue +
                " to " + finalValue +
                (requestedValue.equals(finalValue) ? "" :
                        " (requested value was " + requestedValue + ")");
    }

}
