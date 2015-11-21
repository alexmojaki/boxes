package alex.mojaki.boxes;

/**
 * A trivial implementation of the {@link Box} interface that simply stores the value as a field.
 * You can also construct it using {@link Boxes#box()} or {@link Boxes#box(Object)}.
 */
public class BasicBox<T> implements Box<T> {

    protected T value;

    public BasicBox() {
    }

    public BasicBox(T initialValue) {
        set(initialValue);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Box<T> set(T value) {
        this.value = value;
        return this;
    }

    @Override
    public String toString() {
        return String.valueOf(get());
    }
}
