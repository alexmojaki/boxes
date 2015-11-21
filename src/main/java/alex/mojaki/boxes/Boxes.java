package alex.mojaki.boxes;

/**
 * Container for static factory methods producing boxes of various kinds.
 * <p/>
 * The various {@code box} methods are just intended to be more convenient than their corresponding constructors,
 * especially in Java 6 which doesn't have the diamond operator. The disadvantage is that type inference doesn't
 * work anymore if you try chaining, i.e. this will work:
 * <p/>
 * {@code PowerBox<Integer> x = box(Example.class, "x");}
 * <p/>
 * but this won't:
 * <p/>
 * {@code PowerBox<Integer> x = box(Example.class, "x").addChangeObserver(ChangePrinter.I);}
 * <p/>
 * so you'll have to resort to:
 * <p/>
 * {@code PowerBox<Integer> x = new CommonBox<>(Example.class, "x").addChangeObserver(ChangePrinter.I);}
 */
public enum Boxes {
    ;

    /**
     * Return a new {@link BasicBox} with initial value null.
     */
    public static <T> Box<T> box() {
        return new BasicBox<T>();
    }

    /**
     * Return a new {@link BasicBox} with the given initial value.
     */
    public static <T> Box<T> box(T initialValue) {
        return new BasicBox<T>(initialValue);
    }

    /**
     * Return a new {@link CommonBox} belonging to the family identified by the given class and name.
     */
    public static <T> PowerBox<T> box(Class<?> clazz, String name) {
        return new CommonBox<T>(clazz, name);
    }

    /**
     * Return a new {@link CommonBox} belonging to the given family.
     */
    public static <T> PowerBox<T> box(BoxFamily family) {
        return new CommonBox<T>(family);
    }

    /**
     * Return a {@code Box} that forwards most methods to the argument but throws an exception
     * in the {@code set} method.
     */
    public static <T> Box<T> unsettableAdapter(Box<T> box) {
        return new UnsettableBoxAdapter<T>(box);
    }

    /**
     * Return a {@code PowerBox} that forwards most methods to the argument but throws an exception
     * in the {@code set} method.
     */
    public static <T> PowerBox<T> unsettableAdapter(PowerBox<T> box) {
        return new UnsettablePowerBoxAdapter<T>(box);
    }

    /**
     * Return a {@code PowerBox} with the given family that forwards {@code get} and {@code set} to the
     * given box.
     * <p/>
     * The return value is meant to replace the box argument by direct reassignment, i.e.
     * <p/>
     * {@code exampleObject.boxField = Boxes.upgrade(family, exampleObject.boxField);}
     * <p/>
     * This essentially makes it possible for a user to add observers and middleware to a plain {@code Box}.
     * However they may be bypassed if a reference to the original box has been held elsewhere.
     */
    public static <T> PowerBox<T> upgrade(BoxFamily family, Box<T> box) {
        return new CommonUpgradedBox<T>(family, box);
    }

    /**
     * Return a {@code PowerBox} with a family identified by the given class and name that forwards
     * {@code get} and {@code set} to the given box.
     * <p/>
     * The return value is meant to replace the box argument by direct reassignment, i.e.
     * <p/>
     * {@code exampleObject.boxField = Boxes.upgrade(family, exampleObject.boxField);}
     * <p/>
     * This essentially makes it possible for a user to add observers and middleware to a plain {@code Box}.
     * However they may be bypassed if a reference to the original box has been held elsewhere.
     */
    public static <T> PowerBox<T> upgrade(Class<?> clazz, String name, Box<T> box) {
        return new CommonUpgradedBox<T>(clazz, name, box);
    }
}
