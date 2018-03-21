package seedu.address.model.job.exceptions;

import seedu.address.commons.exceptions.DuplicateDataException;

/**
 * Signals that the operation will result in duplicate Job objects.
 */
public class DuplicateJobException extends DuplicateDataException {
    public DuplicateJobException() {
        super("Operation would result in duplicate jobs");
    }
}
