package alex.mojaki.boxes;

/**
 * The simplest concrete implementation of {@link PowerBox}.
 * You can also create one using {@link Boxes#box(BoxFamily)} or {@link Boxes#box(Class, String)}.
 * <p>
 * If you're willing to add some boilerplate to your code to save memory, see the parent {@link DefaultPowerBox}.
 */
public class CommonBox<T> extends DefaultPowerBox<T> {

    private BoxFamily family;

    public CommonBox(BoxFamily family) {
        this.family = family;
    }

    public CommonBox(Class<?> clazz, String name) {
        this(BoxFamily.getInstance(clazz, name));
    }

    @Override
    public BoxFamily getFamily() {
        return family;
    }

}
