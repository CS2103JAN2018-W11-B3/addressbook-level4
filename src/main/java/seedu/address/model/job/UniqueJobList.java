package seedu.address.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Iterator;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.index.Index;
import seedu.address.model.job.exceptions.DuplicateJobException;
import seedu.address.model.job.exceptions.JobNotFoundException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * A list of jobs that enforces uniqueness between its elements and does not allow nulls.
 *
 * Supports a minimal set of list operations.
 *
 * @see Job#equals(Object)
 */
public class UniqueJobList implements Iterable<Job> {
    private final ObservableList<Job> internalList = FXCollections.observableArrayList();

    /***
     * Returns true if the list contains an equivalent job as the given argument.
     */
    public boolean contains(Job toCheck) {
        requireNonNull(toCheck);
        return internalList.contains(toCheck);
    }

    /**
     * Adds a job to the list.
     *
     * @throws DuplicateJobException if the job to add is a duplicate of an existing person in the list.
     */
    public void add(Job toAdd) throws DuplicateJobException {
        requireNonNull(toAdd);
        if (contains(toAdd)) {
            // TODO: Replace with new exception
            throw new DuplicateJobException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the job {@code target} in the list with {@code editedJob}.
     *
     * @throws DuplicateJobException if the replacement is equivalent to another existing person in the list.
     * @throws seedu.address.model.job.exceptions.JobNotFoundException if {@code target} could not be found in the list.
     */
    public void setJob(Job target, Job editedJob)
            throws DuplicateJobException, JobNotFoundException {
        requireNonNull(editedJob);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new JobNotFoundException();
        }

        if (!target.equals(editedJob) && internalList.contains(editedJob)) {
            throw new DuplicateJobException();
        }

        internalList.set(index, editedJob);
    }

    /**
     * Removes the equivalent person from the list.
     *
     * @throws PersonNotFoundException if no such person could be found in the list.
     */
    public boolean remove(Job toRemove) throws JobNotFoundException {
        requireNonNull(toRemove);
        final boolean jobFoundAndDeleted = internalList.remove(toRemove);
        if (!jobFoundAndDeleted) {
            throw new JobNotFoundException();
        }
        return jobFoundAndDeleted;
    }

    public void setJobs(UniqueJobList replacement) {
        this.internalList.setAll(replacement.internalList);
    }

    public void setJobs(List<Job> jobs) throws DuplicateJobException {
        requireAllNonNull(jobs);
        final UniqueJobList replacement = new UniqueJobList();
        for (final Job job : jobs) {
            replacement.add(job);
        }
        setJobs(replacement);
    }

    public Job getJob(Index index) {
        return internalList.get(index.getZeroBased());
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Job> asObservableList() {
        return FXCollections.unmodifiableObservableList(internalList);
    }

    @Override
    public Iterator<Job> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof UniqueJobList // instanceof handles nulls
                && this.internalList.equals(((UniqueJobList) other).internalList));
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }
}











