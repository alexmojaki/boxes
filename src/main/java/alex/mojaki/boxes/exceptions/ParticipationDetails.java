package alex.mojaki.boxes.exceptions;

import alex.mojaki.boxes.PowerBox;

import java.util.List;

/**
 * This class holds details about an exception or error that was thrown by an observer or middleware while using a
 * {@code PowerBox}.
 */
public class ParticipationDetails {

    private final PowerBox box;
    private final int participantIndex;
    private final List participantList;
    private final Class<?> participantType;

    public ParticipationDetails(PowerBox box, int participantIndex, List participantList, Class<?> participantType) {
        this.box = box;
        this.participantIndex = participantIndex;
        this.participantList = participantList;
        this.participantType = participantType;
    }

    public PowerBox getBox() {
        return box;
    }

    /**
     * Return the index of the observer/middleware in the list that was being applied when the error
     * occurred.
     */
    public int getParticipantIndex() {
        return participantIndex;
    }

    /**
     * Return the list of observers/middleware that was being applied when the error
     * occurred.
     */
    public List getParticipantList() {
        return participantList;
    }

    /**
     * Return the interface of the observer/middleware that was being applied, i.e. one of the {@code Class} objects
     * of {@code ChangeMiddleware}, {@code ChangeObserver}, {@code GetMiddleware}, or {@code GetObserver}.
     */
    public Class<?> getParticipantType() {
        return participantType;
    }

    /**
     * Get the observer/middleware object that threw the original exception/error.
     */
    public Object getParticipant() {
        return participantList.get(participantIndex);
    }
}
