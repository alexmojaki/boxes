package alex.mojaki.boxes.test;

import alex.mojaki.boxes.Box;
import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.Boxes;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.exceptions.BoxParticipantException;
import alex.mojaki.boxes.observers.change.ThrowOnNull;
import org.junit.Test;

import java.util.Arrays;

import static alex.mojaki.boxes.Boxes.box;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class UpgradedTest {

    @Test
    public void testUpgraded() {
        Box<Integer> x = box();
        //noinspection unchecked
        for (PowerBox<Integer> upgraded : Arrays.asList(
                Boxes.upgrade(UpgradedTest.class, "upgraded", x),
                Boxes.upgrade(BoxFamily.getInstance(UpgradedTest.class, "upgraded"), x))) {
            x.set(1);
            assertEquals(1, (int) x.get());
            assertEquals(1, (int) upgraded.get());
            x.set(2);
            assertEquals(2, (int) x.get());
            assertEquals(2, (int) upgraded.get());
            upgraded.set(3);
            assertEquals(3, (int) x.get());
            assertEquals(3, (int) upgraded.get());

            upgraded.addChangeObserver(ThrowOnNull.I);
            x.set(null);
            try {
                upgraded.set(null);
                fail();
            } catch (BoxParticipantException e) {
            }
        }
    }
}
