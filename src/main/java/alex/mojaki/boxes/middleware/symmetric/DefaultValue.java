package alex.mojaki.boxes.middleware.symmetric;

import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.utils.InstanceStore;

// @formatter:off
/**
 * If the current value is null, replace it with some default value.
 * For example:
 *
 * <pre>{@code
 * PowerBox<String> name = new CommonBox<String>(Example.class, "name")
 *      .addGetMiddleware(DefaultValue.getInstance("Unknown"));
 * name.get();  // returns "Unknown"
 * }</pre>
 */
// @formatter:on
public class DefaultValue extends SymmetricMiddleware {

    private static final InstanceStore<DefaultValue> INSTANCE_STORE = new InstanceStore<DefaultValue>() {
        @Override
        public DefaultValue getNew(Object... args) {
            return new DefaultValue(args[0]);
        }
    };

    private final Object value;

    private DefaultValue(Object defaultValue) {
        this.value = defaultValue;
    }

    public static DefaultValue getInstance(Object value) {
        return INSTANCE_STORE.get(value);
    }

    @Override
    public Object apply(PowerBox box, Object firstValue, Object currentValue) {
        if (currentValue == null) {
            return value;
        }
        return currentValue;
    }
}
