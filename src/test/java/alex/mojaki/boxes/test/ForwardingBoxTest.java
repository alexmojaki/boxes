package alex.mojaki.boxes.test;

import alex.mojaki.boxes.*;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.middleware.get.GetMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.get.GetObserver;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static alex.mojaki.boxes.Boxes.box;
import static alex.mojaki.boxes.Boxes.unsettableAdapter;
import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.fail;

public class ForwardingBoxTest {

    @Test
    public void testUnsettableBox() {
        Box<String> box = box();
        Box<String> unsettable = unsettableAdapter(box);
        testUnsettableSetBehaviour(box, unsettable);
    }

    @Test
    public void testUnsettablePowerBox() {
        PowerBox<String> box = box(BoxFamily.getInstance(ForwardingBoxTest.class, "box"));
        PowerBox<String> unsettable = unsettableAdapter(box);
        testUnsettableSetBehaviour(box, unsettable);
        assertParticipantListSize(unsettable, 0);

        unsettable.addChangeMiddleware(new ChangeMiddleware() {
            @Override
            public Object onChange(PowerBox box, Object originalValue, Object currentValue, Object requestedValue) {
                return currentValue;
            }
        }).addChangeObserver(new ChangeObserver() {
            @Override
            public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
            }
        }).addGetObserver(new GetObserver() {
            @Override
            public void onGet(PowerBox box, Object originalValue, Object finalValue) {
            }
        }).addGetMiddleware(new GetMiddleware() {
            @Override
            public Object onGet(PowerBox box, Object originalValue, Object currentValue) {
                return currentValue;
            }
        });
        assertParticipantListSize(unsettable, 1);
    }

    private void assertParticipantListSize(PowerBox<String> unsettable, int expectedSize) {
        BoxFamily family = unsettable.getFamily();
        //noinspection unchecked
        for (List list : Arrays.asList(
                family.getChangeMiddlewares(),
                family.getChangeObservers(),
                family.getGetMiddlewares(),
                family.getGetObservers())) {
            assertEquals(expectedSize, list.size());
        }
    }

    private void testUnsettableSetBehaviour(Box<String> box, Box<String> unsettable) {
        box.set("a");
        assertEquals("a", box.get());
        assertEquals("a", unsettable.get());
        try {
            unsettable.set("b");
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void testForwardingBoxSet() {
        Box<String> box = box();
        Box<String> forwarding = new ForwardingBox<String>(box) {
        };
        testForwardingSetBehaviour(box, forwarding);
    }

    @Test
    public void testForwardingPowerBoxSet() {
        PowerBox<String> box = box(ForwardingBoxTest.class, "forwarded");
        PowerBox<String> forwarding = new ForwardingPowerBox<String>(box) {
        };
        testForwardingSetBehaviour(box, forwarding);
    }

    private void testForwardingSetBehaviour(Box<String> box, Box<String> forwarding) {
        box.set("a");
        assertEquals("a", box.get());
        assertEquals("a", forwarding.get());
        forwarding.set("b");
        assertEquals("b", box.get());
        assertEquals("b", forwarding.get());
    }

}
