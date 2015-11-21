package alex.mojaki.boxes;

/**
 * An abstraction of a value similar to pointers in C and other languages that allows obtaining and changing the
 * value in a generic way. The main expected use is as a public field, removing the need for getters and setters,
 * although it can also be a nice way to manage local variables, and can theoretically hide other storage mechanisms
 * such as files.
 *
 * @param <T> the type of the value of the box
 */
public interface Box<T> {

    /**
     * Return the value in the box.
     */
    T get();

    /**
     * Set the value of the box.
     */
    Box<T> set(T value);

}