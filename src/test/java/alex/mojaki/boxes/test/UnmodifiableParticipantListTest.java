package alex.mojaki.boxes.test;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Iterator;
import java.util.ListIterator;

import static junit.framework.TestCase.assertFalse;

public class UnmodifiableParticipantListTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private BoxFamily.ParticipantList<ChangeMiddleware> list = BoxFamily.getInstance(UnmodifiableParticipantListTest.class, "").getChangeMiddlewares();

    private ListIterator<ChangeMiddleware> listIterator = list.listIterator();

    @Before
    public void setUp() {
        exception.expect(UnsupportedOperationException.class);
    }

    @Test
    public void testSet() {
        list.set(0, null);
    }

    @Test
    public void testAdd() {
        list.add(0, null);
    }

    @Test
    public void testRemove() {
        list.remove(0);
    }

    @Test
    public void testAddAllList() {
        list.addAll(0, null);
    }

    @Test
    public void testAddAll() {
        list.addAll(null);
    }

    @Test
    public void testListIteratorAdd() {
        listIterator.add(null);
    }

    @Test
    public void testListIteratorRemove() {
        listIterator.remove();
    }

    @Test
    public void testListIterator1() {
        listIterator.set(null);
    }

    @Test
    public void testSubList() {
        list.subList(0, 0).clear();
    }

    @Test
    public void testIteratorRemove() {
        Iterator<ChangeMiddleware> iterator = list.iterator();
        assertFalse(iterator.hasNext());
        iterator.remove();
    }

    @Test
    public void testRemove1() {
        list.remove(null);
    }

    @Test
    public void testRetainAll() {
        list.retainAll(null);
    }

    @Test
    public void testClear() {
        list.clear();
    }

    @Test
    public void testRemoveAll() {
        list.removeAll(null);
    }}