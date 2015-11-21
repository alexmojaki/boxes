package alex.mojaki.boxes.observers.get;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.get.GetMiddleware;

/**
 * A {@link PowerBox} can apply a list of these objects when a user calls {@link PowerBox#get()},
 * typically to incur some side effect.
 *
 * @see PowerBox#get()
 * @see PowerBox#addGetObserver(GetObserver[])
 * @see BoxFamily#addGetObserver(GetObserver[])
 */
public interface GetObserver<T> {

    /**
     * Take some action based on the values involved in the get.
     * Called during {@link PowerBox#get()} for zero or more of these objects
     *
     * @param box           the {@code PowerBox} whose value is being obtained.
     * @param originalValue the value the box contained before the change.
     * @param finalValue    the new value the box will have.
     *                      This is the final result of applying all {@link GetMiddleware}.
     */
    void onGet(PowerBox<T> box, T originalValue, T finalValue);
}
