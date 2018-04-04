# ChengSashankh
###### \java\seedu\address\logic\commands\ListJobsCommand.java
``` java
package seedu.address.logic.commands;

import static seedu.address.model.Model.PREDICATE_SHOW_ALL_JOBS;

/**
 * Lists all jobs in the address book to the user.
 */
public class ListJobsCommand extends Command {
    public static final String COMMAND_WORD = "listjobs";

    public static final String MESSAGE_SUCCESS = "Listed all jobs";

    @Override
    public CommandResult execute() {
        model.updateFilteredJobList(PREDICATE_SHOW_ALL_JOBS);
        return new CommandResult(MESSAGE_SUCCESS);
    }
}
```
###### \java\seedu\address\logic\commands\MatchJobCommand.java
``` java
package seedu.address.logic.commands;

import java.util.ArrayList;

import seedu.address.commons.core.index.Index;
import seedu.address.model.job.Job;
import seedu.address.model.job.PersonMatchesJobPredicate;

/**
 * Finds and lists all persons in address book whose profiles match the job posting.
 * Keyword matching is case sensitive.
 */

public class MatchJobCommand extends Command {
    public static final String COMMAND_WORD = "matchjob";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Matches all persons whose profiles match job posting "
            + "the specified keywords (case-sensitive) and displays them as a list with index numbers.\n"
            + "Parameters: JOB INDEX...\n"
            + "Example: " + COMMAND_WORD + " 1";

    private PersonMatchesJobPredicate predicate;
    private final Index index;

    public MatchJobCommand(Index index) {
        this.index = index;
    }

    @Override
    public CommandResult execute() {
        ArrayList<Job> listOfJobs = new ArrayList<>(model.getFilteredJobList());
        this.predicate = new PersonMatchesJobPredicate(listOfJobs.get(index.getZeroBased()));
        model.updateFilteredPersonList(predicate);
        return new CommandResult(getMessageForPersonListShownSummary(model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof MatchJobCommand // instanceof handles nulls
                && this.predicate.equals(((MatchJobCommand) other).predicate)); // state check
    }

}
```
###### \java\seedu\address\logic\commands\PostJobCommand.java
``` java
package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_JOBTITLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILLS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.job.Job;
import seedu.address.model.job.exceptions.DuplicateJobException;

/***
 * TODO: Javadoc comment required here.
 */
public class PostJobCommand extends UndoableCommand {

    public static final String COMMAND_WORD = "postjob";
    public static final String COMMAND_ALIAS = "j";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a job posting to the address book. "
            + "Parameters: "
            + PREFIX_JOBTITLE + "JOBTITLE "
            + PREFIX_LOCATION + "LOCATION "
            + PREFIX_SKILLS + "SKILLS "
            + "[" + PREFIX_TAG + "TAG]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_JOBTITLE + "Backend Engineer "
            + PREFIX_LOCATION + "Singapore "
            + PREFIX_SKILLS + "SQL, Javascript "
            + PREFIX_TAG + "Fresh Graduate ";

    public static final String MESSAGE_SUCCESS = "New job added: %1$s";
    public static final String MESSAGE_DUPLICATE_JOB = "This job already exists in the address book";

    private final Job toAdd;

    /**
     * Creates an AddCommand to add the specified {@code Job}
     */
    public PostJobCommand(Job job) {
        requireNonNull(job);
        toAdd = job;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        requireNonNull(model);
        try {
            model.addJob(toAdd);
            // System.out.println(model.getFilteredJobList().get(0).toString());
            return new CommandResult(String.format(MESSAGE_SUCCESS, toAdd));
        } catch (DuplicateJobException e) {
            throw new CommandException(MESSAGE_DUPLICATE_JOB);
        }

    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof PostJobCommand // instanceof handles nulls
                && toAdd.equals(((PostJobCommand) other).toAdd));
    }
}
```
###### \java\seedu\address\logic\parser\PostJobCommandParser.java
``` java
package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_JOBTITLE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILLS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Set;
import java.util.stream.Stream;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.PostJobCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.job.Job;
import seedu.address.model.job.JobTitle;
import seedu.address.model.job.Location;
import seedu.address.model.job.Skill;
import seedu.address.model.tag.Tag;

/***
 * TODO: Write a javadoc comment
 */

public class PostJobCommandParser implements Parser<PostJobCommand> {
    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform the expected format
     */
    public PostJobCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_JOBTITLE, PREFIX_LOCATION, PREFIX_SKILLS, PREFIX_TAG);

        if (!arePrefixesPresent(argMultimap, PREFIX_JOBTITLE, PREFIX_LOCATION)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, PostJobCommand.MESSAGE_USAGE));
        }

        try {
            JobTitle jobTitle = ParserUtil.parseJobTitle(argMultimap.getValue(PREFIX_JOBTITLE)).get();
            Location location = ParserUtil.parseLocation(argMultimap.getValue(PREFIX_LOCATION)).get();
            Skill skills = ParserUtil.parseSkill(argMultimap.getValue(PREFIX_SKILLS)).get();
            Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));

            Job job = new Job(jobTitle, location, skills, tagList);

            return new PostJobCommand(job);
        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
```
###### \java\seedu\address\model\AddressBook.java
``` java
    public void setJobs(List<Job> jobs) throws  DuplicateJobException {
        this.jobs.setJobs(jobs);
    }
```
###### \java\seedu\address\model\AddressBook.java
``` java
    /**
     * Adds a job to the address book.
     *
     * TODO: Write the javadoc comment
     * TODO: Write the exception.
     * @throws DuplicatePersonException if an equivalent person already exists.
     */
    public void addJob(Job j) throws DuplicateJobException {
        // TODO: Mimic the implementation of the addperson method.
        jobs.add(j);
    }
```
###### \java\seedu\address\model\AddressBook.java
``` java
    @Override
    public ObservableList<Job> getJobList() {
        return jobs.asObservableList();
    }
```
###### \java\seedu\address\model\job\exceptions\DuplicateJobException.java
``` java
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
```
###### \java\seedu\address\model\job\exceptions\JobNotFoundException.java
``` java
package seedu.address.model.job.exceptions;

/**
 * Signals that the operation is unable to find the specified job.
 */
public class JobNotFoundException extends Exception {}
```
###### \java\seedu\address\model\job\Job.java
``` java
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
```
###### \java\seedu\address\model\job\JobTitle.java
``` java
package seedu.address.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Job's title in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidTitle(String)}
 */

public class JobTitle {
    public static final String MESSAGE_TITLE_CONSTRAINTS =
            "Job title should only contain alphanumeric characters and spaces, and it should not be blank";

    /*
     * The first character of the name must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String TITLE_VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String fullTitle;

    /**
     * Constructs a {@code JobTitle}.
     *
     * @param title A valid job title.
     */
    public JobTitle(String title) {
        requireNonNull(title);
        checkArgument(isValidTitle(title), MESSAGE_TITLE_CONSTRAINTS);
        this.fullTitle = title;
    }

    /**
     * Returns true if a given string is a valid job title.
     */
    public static boolean isValidTitle(String test) {
        return test.matches(TITLE_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return fullTitle;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof seedu.address.model.job.JobTitle // instanceof handles nulls
                && this.fullTitle.equals(((JobTitle) other).fullTitle)); // state check
    }

    @Override
    public int hashCode() {
        return fullTitle.hashCode();
    }
}
```
###### \java\seedu\address\model\job\Location.java
``` java
package seedu.address.model.job;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Job's location in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidLocation(String)}
 */
public class Location {

    public static final String MESSAGE_LOCATION_CONSTRAINTS =
            "Job locations can take any values, and it should not be blank";


    /*
     * The first character of the location must not be a whitespace,
     * otherwise " " (a blank string) becomes a valid input.
     */
    public static final String LOCATION_VALIDATION_REGEX = "[^\\s].*";

    public final String value;

    /**
     * Constructs an {@code Location}.
     *
     * @param location A valid location.
     */
    public Location(String location) {
        requireNonNull(location);
        checkArgument(isValidLocation(location), MESSAGE_LOCATION_CONSTRAINTS);
        this.value = location;
    }

    /**
     * Returns true if a given string is a valid job location.
     */
    public static boolean isValidLocation(String test) {
        return test.matches(LOCATION_VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof seedu.address.model.job.Location // instanceof handles nulls
                && this.value.equals(((seedu.address.model.job.Location) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
```
###### \java\seedu\address\model\job\PersonMatchesJobPredicate.java
``` java
package seedu.address.model.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.StringUtil;
import seedu.address.model.person.Person;

/**
 * Tests that a {@code Person}'s {@code skills or address} matches any of the keywords given.
 */

public class PersonMatchesJobPredicate implements Predicate<Person> {
    private final List<String> keywords;
    private final boolean notLocationBound;

    public PersonMatchesJobPredicate(Job job) {
        this.keywords = new ArrayList<String>();
        this.keywords.addAll(Arrays.asList(job.getSkills().toString().split(",")));
        this.keywords.addAll(Arrays.asList(job.getLocation().toString().split(" ")));

        notLocationBound = (job.getLocation().toString().compareTo("##") == 0);
    }

    @Override
    public boolean test(Person person) {
        String toMatchPerson = person.getAddress().toString();
        toMatchPerson.concat(person.getSkills().toString());
        toMatchPerson.concat(person.getTags().toString());


        return keywords.stream()
                .anyMatch(keyword -> StringUtil.containsWordIgnoreCase(toMatchPerson, keyword));
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof PersonMatchesJobPredicate// instanceof handles nulls
                && this.keywords.equals((
                        (PersonMatchesJobPredicate) other).keywords)); // state check
    }
}
```
###### \java\seedu\address\model\job\Skill.java
``` java
package seedu.address.model.job;

/***
 * TODO: write javadoc for this
 */
public class Skill {
    // TODO: Write the messages and regex here.

    public static final String MESSAGE_SKILL_CONSTRAINTS = "Job skills can take any values, and it should not be blank";

    private final String skills;

    public Skill(String skills) {
        this.skills = skills;
    }

    public String toString() {
        return skills;
    }

    /***
     * TODO: Write javadoc comment
     * @param test
     * @return
     */
    public static boolean isValidSkill(String test) {
        // TODO: Write this based on REGEX
        return true;

    }
}
```
###### \java\seedu\address\model\job\UniqueJobList.java
``` java
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
```
###### \java\seedu\address\model\person\Skill.java
``` java
package seedu.address.model.person;

/***
 * TODO: write javadoc for this
 */
public class Skill {
    // TODO: Write the messages and regex here.

    private final String skills;

    public Skill(String skills) {
        this.skills = skills;
    }

    public String toString() {
        return skills;
    }

    /***
     * TODO: Write javadoc comment
     * @param test
     * @return
     */
    public static boolean isValidSkill(String test) {
        // TODO: Write this based on REGEX
        return true;

    }
}
```
###### \java\seedu\address\ui\JobCard.java
``` java
package seedu.address.ui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import seedu.address.model.job.Job;

/**
 * An UI component that displays information of a {@code Job}.
 */
public class JobCard extends UiPart<Region> {
    private static final String FXML = "JobListCard.fxml";
    private static final String[] TAG_COLORS = {"red", "pink", "blue"};

    /**
     * Note: Certain keywords such as "location" and "resources" are reserved keywords in JavaFX.
     * As a consequence, UI elements' variable names cannot be set to such keywords
     * or an exception will be thrown by JavaFX during runtime.
     *
     * @see <a href="https://github.com/se-edu/addressbook-level4/issues/336">The issue on AddressBook level 4</a>
     */

    public final Job job;

    @FXML
    private HBox cardPane;
    @FXML
    private Label id;
    @FXML
    private Label jobTitle;
    @FXML
    private Label jobLocation;
    @FXML
    private Label jobSkills;
    @FXML
    private FlowPane tags;

    public JobCard(Job job, int displayedIndex) {
        super(FXML);
        this.job = job;
        id.setText(displayedIndex + ". ");
        jobTitle.setText(job.getJobTitle().fullTitle);
        jobLocation.setText(job.getLocation().value);
        jobSkills.setText(job.getSkills().toString());
        job.getTags().forEach(tag -> tags.getChildren().add(new Label(tag.tagName)));
        initTags(job);
    }

    /**
     * TODO: Add comment!!
     * @param job
     */
    private void initTags(Job job) {
        job.getTags().forEach(tag -> {
            Label label = new Label(tag.tagName);
            label.getStyleClass().add(getTagColorFor(tag.tagName));
            tags.getChildren().add(label);
        });
    }

    /**
     * TODO: Add comment!!
     * @param tagName
     * @return
     */
    private String getTagColorFor(String tagName) {
        //some explanation
        return TAG_COLORS[Math.abs(tagName.hashCode()) % TAG_COLORS.length];
    }

    @Override
    public boolean equals(Object other) {
        // short circuit if same object
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof JobCard)) {
            return false;
        }

        // state check
        JobCard card = (JobCard) other;
        return id.getText().equals(card.id.getText())
                && job.equals(card.job);
    }
}
```
###### \java\seedu\address\ui\JobListPanel.java
``` java
package seedu.address.ui;

import java.util.logging.Logger;

import org.fxmisc.easybind.EasyBind;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.ui.JobPanelSelectionChangedEvent;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.model.job.Job;

/**
 * Panel containing the list of jobs.
 */
public class JobListPanel extends UiPart<Region> {
    private static final String FXML = "JobListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(JobListPanel.class);

    @FXML
    private ListView<JobCard> jobListView;

    public JobListPanel(ObservableList<Job> jobList) {
        super(FXML);
        setConnections(jobList);
        registerAsAnEventHandler(this);
    }

    private void setConnections(ObservableList<Job> jobList) {
        ObservableList<JobCard> mappedList = EasyBind.map(
                jobList, (job) -> new JobCard(job, jobList.indexOf(job) + 1));
        jobListView.setItems(mappedList);
        jobListView.setCellFactory(listView -> new JobListViewCell());
        setEventHandlerForSelectionChangeEvent();
    }

    private void setEventHandlerForSelectionChangeEvent() {
        jobListView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.fine("Selection in job list panel changed to : '" + newValue + "'");
                        raise(new JobPanelSelectionChangedEvent(newValue));
                    }
                });
    }


    /**
     * Scrolls to the {@code JobCard} at the {@code index} and selects it.
     */
    private void scrollTo(int index) {
        Platform.runLater(() -> {
            jobListView.scrollTo(index);
            jobListView.getSelectionModel().clearAndSelect(index);
        });
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        scrollTo(event.targetIndex);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code JobCard}.
     */
    class JobListViewCell extends ListCell<JobCard> {

        @Override
        protected void updateItem(JobCard job, boolean empty) {
            super.updateItem(job, empty);

            if (empty || job == null) {
                setGraphic(null);
                setText(null);
            } else {
                setGraphic(job.getRoot());
            }
        }
    }


}
```
