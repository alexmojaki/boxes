package alex.mojaki.boxes.test;

import alex.mojaki.boxes.BoxFamily;
import alex.mojaki.boxes.CommonBox;
import alex.mojaki.boxes.PowerBox;
import alex.mojaki.boxes.exceptions.BoxParticipantError;
import alex.mojaki.boxes.exceptions.BoxParticipantException;
import alex.mojaki.boxes.exceptions.ParticipationDetails;
import alex.mojaki.boxes.middleware.change.ChangeMiddleware;
import alex.mojaki.boxes.observers.change.ChangeObserver;
import alex.mojaki.boxes.observers.change.ChangePrinter;
import alex.mojaki.boxes.observers.change.RequireBounds;
import alex.mojaki.boxes.observers.change.ThrowOnNull;
import org.junit.Test;

import static alex.mojaki.boxes.Boxes.box;
import static junit.framework.TestCase.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ErrorTest {

    @Test
    public void testParticipantException() {
        String name = "exceptionBox";
        ChangeObserver errorObserver = RequireBounds.between(0, true, 10, true);
        Class<? extends Throwable> type = BoxParticipantException.class;
        Class<? extends Throwable> causeClass = IllegalArgumentException.class;
        testThrowable(name, errorObserver, type, causeClass);
    }

    @Test
    public void testParticipantError() {
        String name = "errorBox";
        ChangeObserver errorObserver = new ChangeObserver() {
            @Override
            public void onChange(PowerBox box, Object originalValue, Object finalValue, Object requestedValue) {
                throw new StackOverflowError();
            }
        };
        Class<? extends Throwable> type = BoxParticipantError.class;
        Class<? extends Throwable> causeClass = StackOverflowError.class;
        testThrowable(name, errorObserver, type, causeClass);
    }

    private void testThrowable(String name, ChangeObserver errorObserver, Class<? extends Throwable> type, Class<? extends Throwable> causeClass) {
        PowerBox<Integer> box = box(ErrorTest.class, name);
        box.addChangeObserver(
                ThrowOnNull.INSTANCE,
                errorObserver,
                ChangePrinter.I)
                .addChangeMiddleware(new ChangeMiddleware<Integer>() {
                    @Override
                    public Integer onChange(PowerBox<Integer> box, Integer originalValue, Integer currentValue, Integer requestedValue) {
                        return currentValue * 3;
                    }
                });

        try {
            box.set(5);
            fail();
        } catch (Throwable e) {
            assertSame(e.getClass(), type);
            ParticipationDetails details;
            if (e instanceof BoxParticipantException) {
                details = ((BoxParticipantException) e).details.get();
            } else {
                assertTrue(e instanceof BoxParticipantError);
                details = ((BoxParticipantError) e).details.get();
            }
            assertSame(ChangeObserver.class, details.getParticipantType());
            assertSame(errorObserver, details.getParticipant());
            assertEquals(1, details.getParticipantIndex());
            assertEquals(box.getFamily().getChangeObservers(), details.getParticipantList());
            assertSame(box, details.getBox());
            assertSame(e.getCause().getClass(), causeClass);
            assertEquals("Error in ChangeObserver 2 out of 3 of ErrorTest." + name + ". " +
                    "Original value = null. Final value = 15. Requested value = 5.", e.getMessage());
        }
    }

    @Test
    public void testHiddenValues() {
        PowerBox<Integer> box = new CommonBox<Integer>(
                BoxFamily.getInstance(ErrorTest.class, "hidden").hideValueStrings()
        ).notNull();
        box.set(3);
        try {
            box.set(null);
            fail();
        } catch (BoxParticipantException e) {
            assertEquals(
                    "Error in ChangeObserver 1 out of 1 of ErrorTest.hidden. Values hidden.",
                    e.getMessage());
        }
    }

}
