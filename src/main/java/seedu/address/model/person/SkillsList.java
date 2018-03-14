package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Optional;

public class SkillsList {
    public static final String MESSAGE_SKILLS_CONSTRAINTS =
            "Skills should only contain alphabets and special characters, and it should not be blank";

    /*
    * Accepts any string as a valid skills list.
    * TODO: Update above comment and update the REGEX as well
    */
    public static final String SKILLS_VALIDATION_REGEX = ".*";

    public final String value;

    /**
     * Constructs a {@code SkillsList}
     *
     * @param skillsList A valid skills list.
     */
    public SkillsList(String skillsList) {
        requireNonNull(skillsList);
        checkArgument(isValidSkillsList(skillsList), MESSAGE_SKILLS_CONSTRAINTS);
        this.value = skillsList;
    }

    /***
     * Returns the full skills list as string.
     * @return fullSkillsList The complete list of skills
     */

    public String getList() {
        return this.value;
    }

    /**
     * Returns true if a given string is a valid person skills list.
     */
    public static boolean isValidSkillsList(String test) {
        return test.matches(SKILLS_VALIDATION_REGEX);
    }


    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this // short circuit if same object
                || (other instanceof Name // instanceof handles nulls
                && this.value.equals(((SkillsList) other).value)); // state check
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}

