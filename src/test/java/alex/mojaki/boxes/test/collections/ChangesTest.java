package alex.mojaki.boxes.test.collections;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.collections.ListBox;
import alex.mojaki.boxes.collections.MapBox;
import alex.mojaki.boxes.collections.SetBox;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.test.BoxesMapTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static alex.mojaki.boxes.test.Utils.assertEqualThorough;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.*;


public class ChangesTest {

    private static void collectionsEqual(Collection<String> c1, Collection<String> c2) {
        assertEqualThorough(ensureSet(c1), ensureSet(c2));
        assertEqualThorough(ensureSet(c2), ensureSet(c1));
        assertEqualThorough(sortedList(c1), sortedList(c2));
        assertEqualThorough(sortedList(c2), sortedList(c1));
        if (c1.getClass() == c2.getClass()) {
            assertEqualThorough(c1, c2);
        } else {
            assertNotEquals(c1, c2);
        }
    }

    private static void collectionsNotEqual(Collection<String> c1, Collection<String> c2) {
        assertNotEquals(sortedList(c1), sortedList(c2));
    }

    private static Set<String> ensureSet(Collection<String> c) {
        if (c instanceof Set) {
            return (Set<String>) c;
        }
        return new HashSet<String>(c);
    }

    private static List<String> sortedList(Collection<String> c) {
        final List<String> list = new ArrayList<String>(c);
        Collections.sort(list);
        return list;
    }

    @Test
    public void testCollections() {
        List<ListBox<String>> lists = Lists.newArrayList();
        lists.add(new ListBox<String>(BoxesMapTest.class, "list1"));
        lists.get(0).set(new LinkedList<String>());
        lists.add(new ListBox<String>(BoxesMapTest.class, "list2").init());
        lists.add(new ListBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "list3")).init());
        List<String> innerListBox = new ListBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "list4")).init();
        ListBox<String> outerListBox = new ListBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "list5"));
        outerListBox.set(innerListBox);
        lists.add(outerListBox);

        List<SetBox<String>> sets = Lists.newArrayList();
        sets.add(new SetBox<String>(BoxesMapTest.class, "set1"));
        sets.get(0).set(new TreeSet<String>());
        sets.add(new SetBox<String>(BoxesMapTest.class, "set2").init());
        sets.add(new SetBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "set3")).init());
        Set<String> innerSetBox = new SetBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "set4")).init();
        SetBox<String> outerSetBox = new SetBox<String>(BoxFamily.getInstance(BoxesMapTest.class, "set5"));
        outerSetBox.set(innerSetBox);
        sets.add(outerSetBox);

        for (ListBox<String> list : lists) {
            for (SetBox<String> set : sets) {
                list.clear();
                set.clear();
                final List<String> changesList = new ArrayList<String>();
                list.addChangeObserver(new ChangeObserver() {
                    @Override
                    public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                        changesList.add("l");
                    }
                });
                set.addChangeObserver(new ChangeObserver() {
                    @Override
                    public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                        changesList.add("s");
                    }
                });
                collectionsEqual(list, set);
                assertTrue(list.isEmpty());
                assertTrue(set.isEmpty());
                assertFalse(list.contains("a"));
                assertFalse(set.contains("a"));

                list.add("a");
                set.add("a");

                assertFalse(list.isEmpty());
                assertFalse(set.isEmpty());
                assertEquals(1, list.size());
                assertEquals(1, set.size());
                assertTrue(list.contains("a"));
                assertTrue(set.contains("a"));
                assertFalse(list.contains("b"));
                assertFalse(set.contains("b"));

                collectionsEqual(list, set);

                list.add("a");
                set.add("a");

                assertEquals(2, list.size());
                assertEquals(1, set.size());
                collectionsNotEqual(list, set);

                assertEquals(Arrays.asList("l", "s", "l"), changesList);

                list.remove(1);
                list.remove("a");
                set.remove("a");
                collectionsEqual(list, set);
                assertTrue(list.isEmpty());
                assertTrue(set.isEmpty());

                assertEquals(Arrays.asList("l", "s", "l", "l", "l", "s"), changesList);

                changesList.clear();

                list.add("a"); // 1
                list.addAll(Arrays.asList("b", "c")); // 2
                list.addAll(Collections.<String>emptyList()); // no change: added nothing
                list.remove(0); // 3, removes a
                list.remove("b"); // 4
                list.remove("d"); // no change: element to remove not present
                assertEquals(1, list.size());
                assertTrue(list.contains("c"));
                assertFalse(list.isEmpty());
                list.set(new ArrayList<String>(Arrays.asList("1", "2", "3"))); // 5
                for (Iterator<String> iterator = list.iterator(); iterator.hasNext(); ) {
                    String c = iterator.next();
                    if (c.equals("2")) {
                        iterator.remove(); // 6
                    }
                }
                assertArrayEquals(new String[] {"1", "3"}, list.toArray());
                assertArrayEquals(new String[] {"1", "3"}, list.toArray(new String[list.size()]));
                assertTrue(list.containsAll(Collections.singletonList("1")));
                assertFalse(list.containsAll(Arrays.asList("1", "4")));
                list.removeAll(Arrays.asList("4", "5")); // no change: elements to remove not present
                list.removeAll(Arrays.asList("3", "5")); // 7
                list.set(new ArrayList<String>(Arrays.asList("1", "2", "3"))); // 8
                list.retainAll(Arrays.asList("2", "3", "4")); // 9
                list.clear(); // 10
                list.addAll(0, Arrays.asList("1", "2")); // 11
                list.addAll(1, Collections.<String>emptyList()); // no change: added nothing
                list.set(1, "3"); // 12
                assertEquals("3", list.get(1));
                list.add(1, "4"); // 13
                assertEquals(1, list.indexOf("4"));
                assertEquals(1, list.lastIndexOf("4"));
                assertEquals(Collections.singletonList("4"), list.subList(1,2));
                list.subList(1, 3).set(0, "5"); // 14
                assertEquals("5", list.get(1));
                list.listIterator().add("6"); // 15
                list.listIterator(1).add("7"); // 16


                assertEquals(16, changesList.size());

                changesList.clear();

                set.add("a"); // 1
                set.addAll(Arrays.asList("b", "c")); // 2
                set.addAll(Collections.<String>emptyList()); // no change: added nothing
                set.remove("b"); // 3
                set.remove("d"); // no change: element to remove not present
                assertEquals(2, set.size());
                assertTrue(set.contains("c"));
                assertFalse(set.isEmpty());
                set.set(new HashSet<String>(Arrays.asList("1", "2", "3"))); // 4
                for (Iterator<String> iterator = set.iterator(); iterator.hasNext(); ) {
                    String c = iterator.next();
                    if (c.equals("2")) {
                        iterator.remove(); // 5
                    }
                }
                assertEquals(2, set.toArray().length);
                assertEquals(2, set.toArray(new String[set.size()]).length);
                assertTrue(set.containsAll(Collections.singletonList("1")));
                assertFalse(set.containsAll(Arrays.asList("1", "4")));
                set.removeAll(Arrays.asList("4", "5")); // no change: elements to remove not present
                set.removeAll(Arrays.asList("3", "5")); // 6
                set.set(new HashSet<String>(Arrays.asList("1", "2", "3"))); // 7
                set.retainAll(Arrays.asList("2", "3", "4")); // 8
                set.clear(); // 9

                assertEquals(9, changesList.size());

            }
        }
    }

    @Test
    public void testMap() {
        final AtomicInteger count = new AtomicInteger(0);
        Map<String, String> inner = new MapBox<String, String>(ChangesTest.class, "map").init();
        MapBox<String, String> map = new MapBox<String, String>(ChangesTest.class, "map");
        map.set(inner);
        map.addChangeObserver(new ChangeObserver() {
            @Override
            public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                count.incrementAndGet();
            }
        });

        assertTrue(map.isEmpty());
        map.put("1", "2"); // 1
        map.put("3", "4"); // 2
        assertEquals("2", map.get("1"));
        assertTrue(map.containsKey("1"));
        assertTrue(map.containsValue("2"));
        Map<String, String> put = Maps.newHashMap();
        put.put("3", "9");
        put.put("5", "6");
        map.putAll(put); // 3
        assertEquals(6, count.get());
        map.keySet().remove("5"); // 4
        map.putAll(put); // 5
        map.remove("3"); // 6
        map.put("0", "0"); // 7
        for (Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> e = iterator.next();
            if (e.getKey().equals("0")) {
                e.setValue(""); // 8
                iterator.remove(); // 9
            }
        }
        map.clear(); // 10

        // Both the inner and outer maps experience the changes, so double the count
        assertEquals(20, count.get());
    }

}
