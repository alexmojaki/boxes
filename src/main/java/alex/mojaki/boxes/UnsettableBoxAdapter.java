package alex.mojaki.boxes;

/**
 * @see Boxes#unsettableAdapter(Box)
 */
class UnsettableBoxAdapter<T> extends ForwardingBox<T> {

    public UnsettableBoxAdapter(Box<T> box) {
        super(box);
    }

    @Override
    public Box<T> set(T value) {
        throw new UnsupportedOperationException("This is an adapter to a box meant to prevent setting");
    }
}
