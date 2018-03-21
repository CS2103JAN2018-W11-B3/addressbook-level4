package seedu.address.model.job;

import java.util.Objects;
import java.util.Set;

import seedu.address.model.tag.Tag;
import seedu.address.model.tag.UniqueTagList;

/***
 * TODO: Write javadoc commenet
 */
public class Job {
    private final JobTitle jobTitle;
    private final Location location;
    private final Skill skills;
    private final UniqueTagList tags;

    public Job(JobTitle jobTitle, Location location, Skill skills, Set<Tag> tags) {
        this.jobTitle = jobTitle;
        this.location = location;
        this.skills = skills;
        this.tags = new UniqueTagList(tags);
    }

    public JobTitle getJobTitle() {
        return jobTitle;
    }

    public Location getLocation() {
        return location;
    }

    public Skill getSkills() {
        return skills;
    }

    public UniqueTagList getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Job)) {
            return false;
        }

        Job otherJob = (Job) other;
        return otherJob.getJobTitle().equals(this.getJobTitle())
                && otherJob.getLocation().equals(this.getLocation())
                && otherJob.getSkills().equals(this.getSkills())
                && otherJob.getTags().equals(this.getTags());
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(jobTitle, skills, location, tags);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append(getJobTitle())
                .append(" Location: ")
                .append(getLocation())
                .append(" Skills: ")
                .append(getSkills())
                .append(" Tags: ");
        getTags().forEach(builder::append);
        return builder.toString();
    }


}
