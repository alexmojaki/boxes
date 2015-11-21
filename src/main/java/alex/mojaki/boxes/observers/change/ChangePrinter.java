package alex.mojaki.boxes.observers.change;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.utils.Utils;

/**
 * Print to standard out changes made to the box this is added to. This uses
 * {@link Utils#describeChange(PowerBox, Object, Object, Object)}, which may help write your own method for
 * your specific logging needs.
 */
public class ChangePrinter implements ChangeObserver {

    public static final ChangePrinter I = new ChangePrinter();

    private ChangePrinter() {
    }

    @Override
    public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
        System.out.println(Utils.describeChange(box, originalValue, finalValue, requestedValue));
    }

}
