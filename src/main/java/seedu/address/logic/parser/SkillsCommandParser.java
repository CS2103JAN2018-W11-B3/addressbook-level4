package seedu.address.logic.parser;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SKILLS;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.logic.commands.SkillsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.skill.Skill;

/**
 * Parses input arguments and creates a new SkillsCommand object
 */
public class SkillsCommandParser implements Parser<SkillsCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the SkillsCommand
     * and returns a SkillsCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format.
     */
    public SkillsCommand parse(String args) throws ParseException {
        requireNonNull(args);
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_SKILLS);

        Index index;
        Skill skill;

        index = getIndexFromArguments(argMultimap);
        skill = getSkillsListFromArguments(argMultimap);

        return new SkillsCommand(index, skill);
        // TODO: Review the necessity of a skills command descriptor.
    }

    private Skill getSkillsListFromArguments(ArgumentMultimap argMultimap) throws ParseException {
        Skill skill;
        try {
            skill = ParserUtil.parseSkill(argMultimap.getValue(PREFIX_SKILLS).get());
        } catch (IllegalValueException ive) {
            throw new ParseException(ive.getMessage(), ive);
        }
        return skill;
    }

    private Index getIndexFromArguments(ArgumentMultimap argMultimap) throws ParseException {
        Index index;
        try {
            index = ParserUtil.parseIndex(argMultimap.getPreamble());
        } catch (IllegalValueException ive) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, SkillsCommand.MESSAGE_USAGE));
        }
        return index;
    }

    // TODO: Review the neccesity of an optional return type function

}
