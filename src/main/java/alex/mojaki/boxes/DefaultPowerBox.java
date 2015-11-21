package alex.mojaki.boxes;

/**
 * An implementation of {@link PowerBox} that stores a value but not a family to save memory.
 * Instead you must implement {@link PowerBox#getFamily()}, something like this:
 * <pre>{@code
 *  private static final BoxFamily someFieldFamily = BoxFamily.getInstance(Example.class, "someField")
 *  private static class SomeField extends DefaultPowerBox<String> {
 *      public BoxFamily getFamily() {
 *          return someFieldFamily;
 *      }
 *  }
 *  public PowerBox<String> someField = new SomeField();
 * }</pre>
 */
public abstract class DefaultPowerBox<T> extends AbstractPowerBox<T> {

    protected volatile T value;

    @Override
    protected T rawGet() {
        return value;
    }

    @Override
    protected void rawSet(T value) {
        this.value = value;
    }

}
