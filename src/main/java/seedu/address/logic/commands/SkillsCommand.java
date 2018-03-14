package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.commands.EditCommand.MESSAGE_DUPLICATE_PERSON;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILLS;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.List;

import seedu.address.commons.core.Messages;
import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.person.Person;
import seedu.address.model.person.SkillsList;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * Add a list of skills to a chosen person from the address book.
 */
public class SkillsCommand extends UndoableCommand {
    // TODO: Add a command alias
    public static final String COMMAND_WORD = "skills";
    public static final String MESSAGE_SUCCESS = "Skills have been added!";
    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds skill list to person identified "
            + "by the index number used in the last person listing. "
            + "Existing skills list will be appended by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_SKILLS + "NAME]"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_SKILLS + "Java, JavaScript, C++";

    private final Index index;
    private final SkillsList skillsList;
    private Person personToAddSkills;
    private Person personWithSkills;

    // TODO: Create a skillsCommandDescriptor to contain the skills as obejects later
    public SkillsCommand(Index index, SkillsList skillsList) {
        requireNonNull(index);
        requireNonNull(skillsList);

        this.index = index;
        this.skillsList = skillsList;
    }

    @Override
    public CommandResult executeUndoableCommand() throws CommandException {
        // TODO: Exception to be thrown
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        personToAddSkills = lastShownList.get(index.getZeroBased());
        personWithSkills = addSkillsToPerson(personToAddSkills, skillsList);

        try {
            model.updatePerson(personToAddSkills, personWithSkills);
        } catch (DuplicatePersonException dpe) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        } catch (PersonNotFoundException pnfe) {
            throw new AssertionError("The target person cannot be missing");
        }
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(MESSAGE_SUCCESS);
    }

    /***
     * Adds a {@code SkillsList} to an exisiting {@code Person}
     * @return {@code Person} with skillsList
     */
    public static Person addSkillsToPerson(Person personToAddSkills, SkillsList skillsList) {
        Person personWithSkills = new Person(
                personToAddSkills.getName(),
                personToAddSkills.getPhone(),
                personToAddSkills.getEmail(),
                personToAddSkills.getAddress(),
                personToAddSkills.getTags(),
                skillsList
        );

        return personWithSkills;
    }
}

