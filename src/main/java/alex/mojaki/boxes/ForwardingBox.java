package alex.mojaki.boxes;

/**
 * A box that wraps around another box and forwards the interface methods and {@code toString}.
 * This is not useful on its own but makes it easy it to implement the decorator pattern by subclassing.
 */
public abstract class ForwardingBox<T> implements Box<T> {

    protected final Box<T> box;

    public ForwardingBox(Box<T> box) {
        this.box = box;
    }

    @Override
    public T get() {
        return box.get();
    }

    @Override
    public Box<T> set(T value) {
        return box.set(value);
    }

    @Override
    public String toString() {
        return box.toString();
    }

}
