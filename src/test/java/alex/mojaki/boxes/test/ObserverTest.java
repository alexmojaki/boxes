package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.CommonBox;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.collections.CollectionBox;
import alex.mojaki.boxes.collections.ListBox;
import alex.mojaki.boxes.collections.SetBox;
import alex.mojaki.boxes.exceptions.BoxParticipantException;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.ChangePrinter;
import alex.mojaki.boxes.observers.change.RequireBounds;
import alex.mojaki.boxes.observers.get.GetObserver;
import alex.mojaki.boxes.View;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static alex.mojaki.boxes.Boxes.box;
import static org.junit.Assert.*;

public class ObserverTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class A {
        public PowerBox<String> str = new CommonBox<String>(A.class, "str").notNull();
    }

    @Test
    public void testNotNull() {
        A a = new A();
        a.str.set("something");
        try {
            a.str.set(null);
            fail();
        } catch (BoxParticipantException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testNoDuplicates() {
        new A();
        new A();
        new A();
        assertEquals(1, new A().str.getFamily().getChangeObservers().size());
    }

    @Test
    public void testViewUpdate() {
        final A a = new A();
        PowerBox<String> view = new View<String>(ObserverTest.class, "view", a.str) {
            @Override
            public String calculate() {
                return (a.str.get().equals("")) ? null : a.str.get();
            }
        }.notNull().addChangeObserver(ChangePrinter.I);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        a.str.set("x");
        assertEquals("x", view.get());

        System.setOut(null);
        assertEquals("ObserverTest.view value changed from null to x\n", outContent.toString());

        exception.expect(BoxParticipantException.class);
        a.str.set("");
    }

    @Test
    public void testRequireBounds() {
        PowerBox<Integer> max = new CommonBox<Integer>(ObserverTest.class, "max")
                .addChangeObserver(RequireBounds.maximum(5, true));
        PowerBox<Integer> min = new CommonBox<Integer>(ObserverTest.class, "min")
                .addChangeObserver(RequireBounds.minimum(0, false));
        PowerBox<Integer> between = new CommonBox<Integer>(ObserverTest.class, "between")
                .addChangeObserver(RequireBounds.between(10, true, 20, false));

        max.set(-1000);
        max.set(5);
        try {
            max.set(7);
            fail();
        } catch (BoxParticipantException e) {
        }

        assertEquals(5, (int) max.get());

        min.set(1000);
        try {
            min.set(0);
            fail();
        } catch (BoxParticipantException e) {
        }
        try {
            min.set(-2);
            fail();
        } catch (BoxParticipantException e) {
        }

        try {
            between.set(-1000);
            fail();
        } catch (BoxParticipantException e) {
        }
        between.set(10);
        between.set(19);
        try {
            between.set(20);
            fail();
        } catch (BoxParticipantException e) {
        }
        try {
            between.set(1000);
            fail();
        } catch (BoxParticipantException e) {
        }

        //noinspection unchecked
        for (Box<Integer> box : Arrays.asList(min, max, between)) {
            box.set(null);
            assertEquals(null, box.get());
        }
    }

    @Test
    public void testRequireBoundsForDouble() {
        PowerBox<Double> box = new CommonBox<Double>(ObserverTest.class, "doubleBoundsValid")
                .addChangeObserver(RequireBounds.maximum(5.0, false));
        box.set(3.0);
        try {
            box.set(5.0);
            fail();
        } catch (BoxParticipantException e) {
        }
    }

    @Test
    public void testImpossibleBounds() {
        exception.expect(IllegalArgumentException.class);
        RequireBounds.between(10.0, false, 5.0, false);
    }

    @Test
    public void testEqualBounds() {
        RequireBounds.between(5.0, true, 5.0, true);
        exception.expect(IllegalArgumentException.class);
        RequireBounds.between(5.0, true, 5.0, false);
    }

    @Test
    public void testNullBounds() {
        exception.expect(IllegalArgumentException.class);
        RequireBounds.maximum(null, true);
    }

    @Test
    public void testGetObserver() {
        PowerBox<String> observed = box(ObserverTest.class, "getObserved");
        final AtomicInteger count = new AtomicInteger(0);
        observed.addGetObserver(new GetObserver() {
            @Override
            public void onGet(PowerBox box, Object originalValue, Object finalValue) {
                count.incrementAndGet();
            }
        });
        observed.set("1");
        assertEquals("1", observed.get());
        assertEquals("1", observed.get());
        observed.set("2");
        assertEquals("2", observed.get());
        assertEquals(3, count.get());
    }

    @Test
    public void wrapperBoxChangeObserverParametersIdentical() {
        for (CollectionBox<?, String> coll : Arrays.asList(
                new ListBox<String>(ObserverTest.class, "listParamsTest").init(),
                new SetBox<String>(ObserverTest.class, "settParamsTest").init()
        )) {
            coll.addChangeObserver(new ChangeObserver() {
                @Override
                public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                    assertSame(box, originalValue);
                    assertSame(box, finalValue);
                    assertSame(box, requestedValue);
                }
            });
            coll.add("a");
        }
    }

}
