package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.DefaultPowerBox;

import static alex.mojaki.boxes.Boxes.box;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class Utils {
    public static void assertEqualThorough(Object o1, Object o2) {
        assertEquals(o1, o2);
        assertEquals(o2, o1);
        assertEquals(o1.hashCode(), o2.hashCode());
        assertTrue(o1.toString().equals(o2.toString()) || o1.getClass() != o2.getClass());
    }

}
