package alex.mojaki.boxes.exceptions;

import alex.mojaki.boxes.Box;

import static alex.mojaki.boxes.Boxes.box;

/**
 * Thrown by a {@code PowerBox} when an observer or middleware throws an {@code Error}.
 * Get the {@code details} box for more information.
 */
public class BoxParticipantError extends Error {

    public Box<ParticipationDetails> details = box();

    public BoxParticipantError(String message, Error cause) {
        super(message, cause);
    }

    public BoxParticipantError withDetails(ParticipationDetails details) {
        this.details.set(details);
        return this;
    }
}
