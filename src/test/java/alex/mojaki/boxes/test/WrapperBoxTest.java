package alex.mojaki.boxes.test;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.WrapperBox;
import alex.mojaki.boxes.collections.ListBox;
import alex.mojaki.boxes.collections.MapBox;
import alex.mojaki.boxes.collections.SetBox;
import alex.mojaki.boxes.exceptions.BoxParticipantError;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.symmetric.DefaultValue;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static alex.mojaki.boxes.utils.Utils.describeChange;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.*;

public class WrapperBoxTest {

    @Test
    public void testWrapperCommon() {
        for (WrapperBox box : new WrapperBox[]{
                new ListBox<Integer>(BoxFamily.getInstance(WrapperBoxTest.class, "list")),
                new SetBox<Integer>(BoxFamily.getInstance(WrapperBoxTest.class, "set")),
                new MapBox<Integer, Integer>(BoxFamily.getInstance(WrapperBoxTest.class, "map"))
        }) {
            // Box does not reveal self
            assertSame(box, box.get());

            // addChangeMiddleware behaves
            ChangeMiddleware middleware = DefaultValue.getInstance(3);
            assertEquals(0, box.getFamily().getChangeMiddlewares().size());
            box.addChangeMiddleware(middleware, middleware)
                    .addChangeMiddleware(middleware);
            assertEquals(Collections.singletonList(middleware),
                    box.getFamily().getChangeMiddlewares());

        }
    }

    @Test
    public void testIsNull() {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ListBox<String> box = new ListBox<String>(WrapperBoxTest.class, "isNull");
        assertTrue(box.isNull());
        box.init();
        assertFalse(box.isNull());
    }

    @Test
    public void hiddenToStringWrapper() {
        ListBox<Integer> hiddenBox = new ListBox<Integer>(WrapperBoxTest.class, "hiddenWrapper")
                .set(Arrays.asList(1, 2, 3));
        assertEquals("[1, 2, 3]", hiddenBox.toString());
        hiddenBox.getFamily().hideValueStrings();
        assertEquals("[Hidden value of WrapperBoxTest.hiddenWrapper]", hiddenBox.toString());
        assertEquals("[Hidden value of WrapperBoxTest.hiddenWrapper]", hiddenBox.get().toString());
        assertEquals("[1, 2, 3]", hiddenBox.revealedToString());
    }

    @Test
    public void describeWrapperChange() {
        ListBox<Integer> list = new ListBox<Integer>(BoxFamily.getInstance(WrapperBoxTest.class, "describeChanges")).init();
        list.add(1);
        list.addChangeObserver(new ChangeObserver() {
            @Override
            public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                assertEquals("WrapperBoxTest.describeChanges value changed to [1, 2]",
                        describeChange(box, originalValue, finalValue, requestedValue));
            }
        });
        list.add(2);
        try {
            list.add(3);
            fail();
        } catch (BoxParticipantError e) {
            assertEquals(
                    "Error in ChangeObserver 1 out of 1 of WrapperBoxTest.describeChanges. Value = [1, 2, 3]",
                    e.getMessage());
        }
    }
}
