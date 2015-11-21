package alex.mojaki.boxes;

/**
 * A {@link PowerBox} that wraps around a (presumably) plain {@link Box} and forwards {@code get} and {@code set}
 * calls to it. This allows you to add observers and middleware to a box that doesn't have this functionality
 * explicitly enabled. However this may not have the desired results if a reference to the original box is held
 * elsewhere.
 * <p/>
 * You can also create one using {@link Boxes#upgrade(BoxFamily, Box)} or {@link Boxes#upgrade(Class, String, Box)}.
 * <p/>
 * If you're willing to add some boilerplate to your code to save memory, see the parent {@link AbstractUpgradedBox}.
 */
public class CommonUpgradedBox<T> extends AbstractUpgradedBox<T> {

    private final BoxFamily family;

    public CommonUpgradedBox(BoxFamily family, Box<T> innerBox) {
        super(innerBox);
        this.family = family;
    }

    public CommonUpgradedBox(Class<?> clazz, String name, Box<T> innerBox) {
        this(BoxFamily.getInstance(clazz, name), innerBox);
    }

    @Override
    public BoxFamily getFamily() {
        return family;
    }
}
