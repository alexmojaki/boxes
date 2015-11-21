package alex.mojaki.boxes;

/**
 * A {@link PowerBox} that wraps around a (presumably) plain {@link Box} and forwards {@code get} and {@code set}
 * calls to it. This allows you to add observers and middleware to a box that doesn't have this functionality
 * explicitly enabled. However this may not have the desired results if a reference to the original box is held
 * elsewhere.
 * <p/>
 * This class is abstract because it doesn't implement {@link PowerBox#getFamily()}. It is the analog of
 * {@link DefaultPowerBox} - you can subclass it to reduce memory usage by storing the family as a static field.
 * If you are not looking to optimise performance, stick to {@link CommonUpgradedBox}.
 */
public abstract class AbstractUpgradedBox<T> extends AbstractPowerBox<T> {

    private final Box<T> innerBox;

    public AbstractUpgradedBox(Box<T> innerBox) {
        this.innerBox = innerBox;
    }

    @Override
    protected T rawGet() {
        return innerBox.get();
    }

    @Override
    protected void rawSet(T value) {
        innerBox.set(value);
    }

}
