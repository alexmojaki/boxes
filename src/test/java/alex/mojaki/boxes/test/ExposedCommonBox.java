package alex.mojaki.boxes.test;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.CommonBox;

public class ExposedCommonBox<T> extends CommonBox<T> {

    public ExposedCommonBox(BoxFamily family) {
        super(family);
    }

    public ExposedCommonBox(Class<?> clazz, String name) {
        super(clazz, name);
    }

    @Override
    public void rawSet(T value) {
        super.rawSet(value);
    }

    @Override
    protected T rawGet() {
        return super.rawGet();
    }
}
