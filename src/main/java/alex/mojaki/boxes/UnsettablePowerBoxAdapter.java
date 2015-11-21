package alex.mojaki.boxes;

/**
 * @see Boxes#unsettableAdapter(PowerBox)
 */
class UnsettablePowerBoxAdapter<T> extends ForwardingPowerBox<T> implements PowerBox<T> {

    public UnsettablePowerBoxAdapter(PowerBox<T> box) {
        super(box);
    }

    @Override
    public PowerBox<T> set(T value) {
        throw new UnsupportedOperationException("This is an adapter to a box meant to prevent setting");
    }
}
