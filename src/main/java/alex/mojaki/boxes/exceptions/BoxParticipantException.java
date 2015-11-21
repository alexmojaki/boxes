package alex.mojaki.boxes.exceptions;

import alex.mojaki.boxes.Box;

import static alex.mojaki.boxes.Boxes.box;

/**
 * Thrown by a {@code PowerBox} when an observer or middleware throws an {@code Exception}.
 * Get the {@code details} box for more information.
 */
public class BoxParticipantException extends RuntimeException {

    public Box<ParticipationDetails> details = box();

    public BoxParticipantException(String message, Exception cause) {
        super(message, cause);
    }

    public BoxParticipantException withDetails(ParticipationDetails details) {
        this.details.set(details);
        return this;
    }
}
