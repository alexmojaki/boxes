package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.CommonBox;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.symmetric.DefaultValue;
import alex.mojaki.boxes.middleware.symmetric.EnsureBounds;
import alex.mojaki.boxes.middleware.symmetric.SymmetricMiddleware;
import alex.mojaki.boxes.observers.change.ChangePrinter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;

import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class MiddlewareTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private class A {
        PowerBox<Integer> x = new CommonBox<Integer>(MiddlewareTest.class, "x")
                .addChangeMiddleware(triple, increment)
                .addGetMiddleware(triple);
        PowerBox<String> defaultOnGet = new CommonBox<String>(MiddlewareTest.class, "defaultOnGet")
                .addGetMiddleware(DefaultValue.getInstance("default_get"));
        PowerBox<String> defaultOnSet = new CommonBox<String>(MiddlewareTest.class, "defaultOnSet")
                .addChangeMiddleware(DefaultValue.getInstance("default_set"));
    }

    private static final SymmetricMiddleware<Integer> triple = new SymmetricMiddleware<Integer>() {
        @Override
        public Integer apply(PowerBox<Integer> box, Integer firstValue, Integer currentValue) {
            return currentValue * 3;
        }
    };

    private static final SymmetricMiddleware<Integer> increment = new SymmetricMiddleware<Integer>() {
        @Override
        public Integer apply(PowerBox<Integer> box, Integer firstValue, Integer currentValue) {
            return currentValue + 1;
        }
    };

    private A a;

    @Before
    public void setUp() {
        a = new A();
    }

    @Test
    public void testMultipleMiddleware() {
        a.x.set(10);
        assertEquals(3 * (3 * 10 + 1), (int) a.x.get());
    }

    @Test
    public void testDefaultValueGet() {
        assertEquals("default_get", a.defaultOnGet.get());
    }

    @Test
    public void testDefaultValueSet() {
        assertEquals(null, a.defaultOnSet.get());
        a.defaultOnSet.set(null);
        assertEquals("default_set", a.defaultOnSet.get());
        a.defaultOnSet.set("thing");
        assertEquals("thing", a.defaultOnSet.get());
    }

    @Test
    public void testDefaultValueUniqueness() {
        assertSame(DefaultValue.getInstance("a"), DefaultValue.getInstance("a"));
        assertSame(DefaultValue.getInstance("b"), DefaultValue.getInstance("b"));
        assertNotEquals(DefaultValue.getInstance("a"), DefaultValue.getInstance("b"));
    }

    @Test
    public void testPrintWithMiddleware() {
        PowerBox<Integer> y = new CommonBox<Integer>(MiddlewareTest.class, "y")
                .addChangeMiddleware(triple)
                .addChangeObserver(ChangePrinter.I);

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        y.set(1);
        assertEquals(3, (int) y.get());

        System.setOut(null);
        assertEquals("MiddlewareTest.y value changed from null to 3 (requested value was 1)\n", outContent.toString());
    }

    @Test
    public void testEnsureBounds() {
        ExposedCommonBox<Integer> max = new ExposedCommonBox<Integer>(MiddlewareTest.class, "max");
        max.addGetMiddleware(EnsureBounds.maximum(5));
        ExposedCommonBox<Integer> min = new ExposedCommonBox<Integer>(MiddlewareTest.class, "min");
        min.addChangeMiddleware(EnsureBounds.minimum(0));
        PowerBox<Integer> between = new CommonBox<Integer>(MiddlewareTest.class, "between")
                .addGetMiddleware(EnsureBounds.between(10, 20));

        max.set(-1000);
        assertEquals(-1000, (int) max.get());
        max.set(1000);
        assertEquals(5, (int) max.get());
        assertEquals(1000, (int) max.rawGet());

        min.set(1000);
        assertEquals(1000, (int) min.get());
        min.set(-1000);
        assertEquals(0, (int) min.get());
        assertEquals(0, (int) min.rawGet());

        between.set(-1000);
        assertEquals(10, (int) between.get());
        between.set(1000);
        assertEquals(20, (int) between.get());

        //noinspection unchecked
        for (Box<Integer> box : Arrays.asList(min, max, between)) {
            box.set(null);
            assertEquals(null, box.get());
        }
    }

    @Test
    public void testEnsureBoundsForDouble() {
        PowerBox<Double> box = new CommonBox<Double>(MiddlewareTest.class, "doubleBoundsValid")
                .addChangeMiddleware(EnsureBounds.maximum(5.0));
        box.set(10.0);
        assertEquals(Double.valueOf(5.0), box.get());
    }

    @Test
    public void testImpossibleBounds() {
        exception.expect(IllegalArgumentException.class);
        EnsureBounds.between(10.0, 5.0);
    }

    @Test
    public void testNullBounds() {
        exception.expect(IllegalArgumentException.class);
        EnsureBounds.maximum(null);
    }


}
