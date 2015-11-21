package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.collections.BoxesMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.*;

import static alex.mojaki.boxes.Boxes.box;
import static junit.framework.TestCase.assertSame;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;

public class BoxesMapTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testDemo() {

        // Define your boxes, e.g. as the fields of a class
        Box<Integer> x = box();
        Box<Integer> y = box();

        // Set up the map
        BoxesMap<String, Integer> map = new BoxesMap<String, Integer>();
        assertTrue(map.isEmpty());
        map.putBox("x", x);
        map.putBox("y", y);

        // Now you can use either the boxes or the map as normal...
        map.put("x", 1);
        y.set(2);

        // And the results appear seamlessly
        assertEquals(1, (int) x.get());
        assertEquals(2, (int) map.get("y"));

        assertEquals(1, (int) map.get("x"));
        assertEquals(2, (int) y.get());

        assertEquals(new HashSet<String>(Arrays.asList("x", "y")), map.keySet());
        assertEquals(new HashSet<Integer>(Arrays.asList(1, 2)), new HashSet<Integer>(map.values()));
        assertEquals(2, map.size());
        assertTrue(map.containsKey("x"));
        assertTrue(map.containsKey("y"));
        assertFalse(map.containsKey("z"));
        assertEquals(null, map.get("z"));
        assertTrue(map.containsValue(1));
        assertTrue(map.containsValue(2));
        assertFalse(map.containsValue(3));
        assertFalse(map.isEmpty());
        assertSame(map.getBox("x"), x);
        assertSame(map.getBox("y"), y);

        try {
            map.put("z", 3);
            fail();
        } catch (NoSuchElementException e) {
        }

        map.allowBoxlessKeys();
        map.put("z", 3);
        assertEquals((Integer) 3, map.get("z"));
        assertEquals(3, (int) map.getBox("z").get());
        assertTrue(map.containsValue(3));

        map.put(null, null);
        assertEquals(4, map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Integer oldValue = entry.setValue(5);
            assertTrue(oldValue == null || oldValue < 4);
        }
        for (Integer value : map.values()) {
            assertEquals(5, (int) value);
        }
        assertEquals(5, (int) x.get());
        assertEquals(5, (int) y.get());

        map.remove("z");
        assertEquals(3, map.size());
        assertFalse(map.containsKey("z"));

        map.clear();
        assertTrue(map.isEmpty());

        exception.expect(NullPointerException.class);
        map.putBox("null", null);

    }

}
