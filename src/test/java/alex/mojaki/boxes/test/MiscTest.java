package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.middleware.symmetric.DefaultValue;
import alex.mojaki.boxes.observers.change.ThrowOnNull;
import alex.mojaki.boxes.utils.Utils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static alex.mojaki.boxes.Boxes.box;
import static alex.mojaki.boxes.Boxes.unsettableAdapter;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MiscTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private void assertLess(Comparable a, Comparable b) {
        assertTrue(Utils.lessThan(a, b));
        assertFalse(Utils.lessThan(b, a));
        assertFalse(Utils.lessThan(a, a));
        assertFalse(Utils.lessThan(b, b));
    }

    @Test
    public void testLessThan() {
        assertLess(3, 5);
        assertLess(3f, 5f);
        assertLess(3.0, 5.0);
        assertLess(3L, 5L);
        assertLess((short) 3, (short) 5);
        assertLess((byte) 3, (byte) 5);
        assertLess((byte) 3, (byte) 5);
        assertLess(new BigDecimal(3), new BigDecimal(5));
        assertLess(new BigInteger("3"), new BigInteger("5"));
    }

    @Test
    public void testFamilyToString() {
        assertEquals("BoxFamily MiscTest.toStringTest", BoxFamily.getInstance(MiscTest.class, "toStringTest").toString());
    }

    @Test
    public void testNormalToString() {
        Box<List<Integer>> box = box(Arrays.asList(1, 2, 3));
        String expected = "[1, 2, 3]";
        assertEquals(expected, box.toString());
        Box<List<Integer>> unsettable = unsettableAdapter(box);
        assertEquals(expected, unsettable.toString());
    }

    @Test
    public void testDisabledParticipants() {
        BoxFamily family = BoxFamily.getInstance(MiscTest.class, "disabled")
                .addChangeMiddleware(DefaultValue.getInstance(0))
                .addGetMiddleware(DefaultValue.getInstance(0));
        family.getChangeObservers().disable();
        exception.expect(UnsupportedOperationException.class);
        family.addChangeObserver(ThrowOnNull.I);
    }

    @Test
    public void hiddenToStringNormal() {
        PowerBox<Integer> hiddenBox = box(MiscTest.class, "hidden");
        hiddenBox.set(3);
        String revealedString = "3";
        assertEquals(revealedString, hiddenBox.toString());
        hiddenBox.getFamily().hideValueStrings();
        String hiddenString = "[Hidden value of MiscTest.hidden]";
        assertEquals(hiddenString, hiddenBox.toString());
        assertEquals(hiddenString, unsettableAdapter(hiddenBox).toString());
        assertEquals(revealedString, hiddenBox.get().toString());
    }

}
