package seedu.address.model.job;

/***
 * TODO: write javadoc for this
 */
public class Skill {
    // TODO: Write the messages and regex here.

    private final String skills;

    public Skill(String skills) {
        this.skills = skills;
    }

    public String getSkills() {
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
