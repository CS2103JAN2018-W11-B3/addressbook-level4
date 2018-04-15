# ChengSashankh
###### \java\guitests\guihandles\JobCardHandle.java
``` java
/**
 * Provides a handle to a job card in the job list panel.
 */
public class JobCardHandle extends NodeHandle<Node> {
    private static final String ID_FIELD_ID = "#id";
    private static final String JOBTITLE_FIELD_ID = "#jobTitle";
    private static final String LOCATION_FIELD_ID = "#jobLocation";
    private static final String SKILLS_FIELD_ID = "#jobSkills";
    private static final String TAGS_FIELD_ID = "#tags";

    private final Label idLabel;
    private final Label jobTitleLabel;
    private final Label locationLabel;
    private final Label skillsLabel;
    private final List<Label> tagLabels;

    public JobCardHandle(Node cardNode) {
        super(cardNode);
        this.idLabel = getChildNode(ID_FIELD_ID);
        this.jobTitleLabel = getChildNode(JOBTITLE_FIELD_ID);
        this.locationLabel = getChildNode(LOCATION_FIELD_ID);
        this.skillsLabel = getChildNode(SKILLS_FIELD_ID);

        Region tagsContainer = getChildNode(TAGS_FIELD_ID);
        this.tagLabels = tagsContainer
                .getChildrenUnmodifiable()
                .stream()
                .map(Label.class::cast)
                .collect(Collectors.toList());
    }

    public String getId() {
        return idLabel.getText();
    }

    public String getJobTitle() {
        return jobTitleLabel.getText();
    }

    public String getLocation() {
        return locationLabel.getText();
    }

    public String getSkills() {
        return skillsLabel.getText();
    }

    public List<String> getTags() {
        return tagLabels
                .stream()
                .map(Label::getText)
                .collect(Collectors.toList());
    }
}
```
###### \java\guitests\guihandles\JobListPanelHandle.java
``` java
/**
 * Provides a handle for {@code JobListPanel} containing the list of {@code JobCard}.
 */
public class JobListPanelHandle extends NodeHandle<ListView<JobCard>> {
    public static final String JOB_LIST_VIEW_ID = "#jobListView";

    private Optional<JobCard> lastRememberedSelectedJobCard;

    public JobListPanelHandle(ListView<JobCard> jobListPanelNode) {
        super(jobListPanelNode);
    }

    /**
     * Returns a handle to the selected {@code JobCardHandle}.
     * A maximum of 1 item can be selected at any time.
     * @throws AssertionError if no card is selected, or more than 1 card is selected.
     */
    public JobCardHandle getHandleToSelectedCard() {
        List<JobCard> jobList = getRootNode().getSelectionModel().getSelectedItems();

        if (jobList.size() != 1) {
            throw new AssertionError("Job list size expected 1.");
        }

        return new JobCardHandle(jobList.get(0).getRoot());
    }

    /**
     * Returns the index of the selected card.
     */
    public int getSelectedCardIndex() {
        return getRootNode().getSelectionModel().getSelectedIndex();
    }

    /**
     * Returns true if a card is currently selected.
     */
    public boolean isAnyCardSelected() {
        List<JobCard> selectedCardsList = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedCardsList.size() > 1) {
            throw new AssertionError("Card list size expected 0 or 1.");
        }

        return !selectedCardsList.isEmpty();
    }

    /**
     * Navigates the listview to display and select the person.
     */
    public void navigateToCard(Job job) {
        List<JobCard> cards = getRootNode().getItems();
        Optional<JobCard> matchingCard = cards.stream().filter(card -> card.job.equals(job)).findFirst();

        if (!matchingCard.isPresent()) {
            throw new IllegalArgumentException("Job does not exist.");
        }

        guiRobot.interact(() -> {
            getRootNode().scrollTo(matchingCard.get());
            getRootNode().getSelectionModel().select(matchingCard.get());
        });
        guiRobot.pauseForHuman();
    }

    /**
     * Returns the job card handle of a job associated with the {@code index} in the list.
     */
    public JobCardHandle getJobCardHandle(int index) {
        return getJobCardHandle(getRootNode().getItems().get(index).job);
    }

    /**
     * Returns the {@code PersonCardHandle} of the specified {@code person} in the list.
     */
    public JobCardHandle getJobCardHandle(Job job) {
        Optional<JobCardHandle> handle = getRootNode().getItems().stream()
                .filter(card -> card.job.equals(job))
                .map(card -> new JobCardHandle(card.getRoot()))
                .findFirst();
        return handle.orElseThrow(() -> new IllegalArgumentException("Job does not exist."));
    }

    /**
     * Selects the {@code JobCard} at {@code index} in the list.
     */
    public void select(int index) {
        getRootNode().getSelectionModel().select(index);
    }

    /**
     * Remembers the selected {@code JobCard} in the list.
     */
    public void rememberSelectedJobCard() {
        List<JobCard> selectedItems = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedItems.size() == 0) {
            lastRememberedSelectedJobCard = Optional.empty();
        } else {
            lastRememberedSelectedJobCard = Optional.of(selectedItems.get(0));
        }
    }

    /**
     * Returns true if the selected {@code JobCard} is different from the value remembered by the most recent
     * {@code rememberSelectedJobCard()} call.
     */
    public boolean isSelectedJobCardChanged() {
        List<JobCard> selectedItems = getRootNode().getSelectionModel().getSelectedItems();

        if (selectedItems.size() == 0) {
            return lastRememberedSelectedJobCard.isPresent();
        } else {
            return !lastRememberedSelectedJobCard.isPresent()
                    || !lastRememberedSelectedJobCard.get().equals(selectedItems.get(0));
        }
    }

    /**
     * Returns the size of the list.
     */
    public int getListSize() {
        return getRootNode().getItems().size();
    }
}
```
###### \java\seedu\address\logic\commands\DeleteJobCommandTest.java
``` java
public class DeleteJobCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_validIndexUnfilteredList_success() throws Exception {
        Job jobToDelete = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        DeleteJobCommand deleteJobCommand = prepareCommand(INDEX_FIRST_JOB);

        String expectedMessage = String.format(DeleteJobCommand.MESSAGE_DELETE_JOB_SUCCESS, jobToDelete);

        ModelManager expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteJob(jobToDelete);

        assertCommandSuccess(deleteJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexUnfilteredList_throwsCommandException() throws Exception {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredJobList().size() + 1);
        DeleteJobCommand deleteJobCommand = prepareCommand(outOfBoundIndex);

        assertCommandFailure(deleteJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validIndexFilteredList_success() throws Exception {
        showJobAtIndex(model, INDEX_FIRST_JOB);

        Job jobToDelete = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        DeleteJobCommand deleteJobCommand = prepareCommand(INDEX_FIRST_JOB);

        String expectedMessage = String.format(DeleteJobCommand.MESSAGE_DELETE_JOB_SUCCESS, jobToDelete);

        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        expectedModel.deleteJob(jobToDelete);
        showNoJob(expectedModel);

        assertCommandSuccess(deleteJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        showJobAtIndex(model, INDEX_FIRST_JOB);

        Index outOfBoundIndex = INDEX_SECOND_JOB;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getJobList().size());

        DeleteJobCommand deleteJobCommand = prepareCommand(outOfBoundIndex);

        assertCommandFailure(deleteJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndoRedo_validIndexUnfilteredList_success() throws Exception {
        UndoRedoStack undoRedoStack = new UndoRedoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoRedoStack);
        RedoCommand redoCommand = prepareRedoCommand(model, undoRedoStack);
        Job jobToDelete = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        DeleteJobCommand deleteJobCommand = prepareCommand(INDEX_FIRST_JOB);
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        // delete -> first job deleted
        deleteJobCommand.execute();
        undoRedoStack.push(deleteJobCommand);

        // undo -> reverts addressbook back to previous state and filtered job list to show all jobs
        assertCommandSuccess(undoCommand, model, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        // redo -> same first person deleted again
        expectedModel.deleteJob(jobToDelete);
        assertCommandSuccess(redoCommand, model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void executeUndoRedo_invalidIndexUnfilteredList_failure() {
        UndoRedoStack undoRedoStack = new UndoRedoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoRedoStack);
        RedoCommand redoCommand = prepareRedoCommand(model, undoRedoStack);
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredJobList().size() + 1);
        DeleteJobCommand deleteJobCommand = prepareCommand(outOfBoundIndex);

        // execution failed -> deleteCommand not pushed into undoRedoStack
        assertCommandFailure(deleteJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);

        // no commands in undoRedoStack -> undoCommand and redoCommand fail
        assertCommandFailure(undoCommand, model, UndoCommand.MESSAGE_FAILURE);
        assertCommandFailure(redoCommand, model, RedoCommand.MESSAGE_FAILURE);
    }

    @Test
    public void equals() throws Exception {
        DeleteJobCommand deleteFirstCommand = prepareCommand(INDEX_FIRST_JOB);
        DeleteJobCommand deleteSecondCommand = prepareCommand(INDEX_SECOND_JOB);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteJobCommand deleteFirstCommandCopy = prepareCommand(INDEX_FIRST_JOB);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // one command preprocessed when previously equal -> returns false
        deleteFirstCommandCopy.preprocessUndoableCommand();
        assertFalse(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }

    /**
     * Returns a {@code DeleteCommand} with the parameter {@code index}.
     */
    private DeleteJobCommand prepareCommand(Index index) {
        DeleteJobCommand deleteJobCommand = new DeleteJobCommand(index);
        deleteJobCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return deleteJobCommand;
    }

    /**
     * Updates {@code model}'s filtered list to show no one.
     */
    private void showNoJob(Model model) {
        model.updateFilteredJobList(p -> false);

        assertTrue(model.getFilteredJobList().isEmpty());
    }
}
```
###### \java\seedu\address\logic\commands\EditJobCommandTest.java
``` java
public class EditJobCommandTest {
    private Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute_allFieldsSpecifiedUnfilteredList_success() throws Exception {
        Job editedJob = new JobBuilder().build();
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(editedJob).build();
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB, descriptor);

        String expectedMessage = String.format(EditJobCommand.MESSAGE_EDIT_JOB_SUCCESS, editedJob);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateJob(model.getFilteredJobList().get(0), editedJob);

        assertCommandSuccess(editJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_someFieldsSpecifiedUnfilteredList_success() throws Exception {
        Index indexLastJob = Index.fromOneBased(model.getFilteredJobList().size());
        Job lastJob = model.getFilteredJobList().get(indexLastJob.getZeroBased());

        JobBuilder jobInList = new JobBuilder(lastJob);
        Job editedJob = jobInList.withJobTitle(VALID_JOBTITLE_BE).withLocation(VALID_LOCATION_BE)
                .withTags(VALID_TAG_BE).build();

        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder().withJobTitle(VALID_JOBTITLE_BE)
                .withLocation(VALID_LOCATION_BE).withTags(VALID_TAG_BE).build();
        EditJobCommand editJobCommand = prepareCommand(indexLastJob, descriptor);

        String expectedMessage = String.format(EditJobCommand.MESSAGE_EDIT_JOB_SUCCESS, editedJob);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateJob(lastJob, editedJob);

        assertCommandSuccess(editJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_noFieldSpecifiedUnfilteredList_success() {
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB, new EditJobCommand.EditJobDescriptor());
        Job editedJob = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());

        String expectedMessage = String.format(EditJobCommand.MESSAGE_EDIT_JOB_SUCCESS, editedJob);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        assertCommandSuccess(editJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_filteredList_success() throws Exception {
        showJobAtIndex(model, INDEX_FIRST_JOB);

        Job jobInFilteredList = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        Job editedJob = new JobBuilder(jobInFilteredList).withJobTitle(VALID_JOBTITLE_BE).build();
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB,
                new EditJobDescriptorBuilder().withJobTitle(VALID_JOBTITLE_BE).build());

        String expectedMessage = String.format(EditJobCommand.MESSAGE_EDIT_JOB_SUCCESS, editedJob);

        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());
        expectedModel.updateJob(model.getFilteredJobList().get(0), editedJob);

        assertCommandSuccess(editJobCommand, model, expectedMessage, expectedModel);
    }

    @Test
    public void execute_duplicateJobUnfilteredList_failure() {
        Job firstJob = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(firstJob).build();
        EditJobCommand editJobCommand = prepareCommand(INDEX_SECOND_JOB, descriptor);

        assertCommandFailure(editJobCommand, model, EditJobCommand.MESSAGE_DUPLICATE_JOB);
    }

    @Test
    public void execute_duplicateJobFilteredList_failure() {
        showJobAtIndex(model, INDEX_FIRST_JOB);

        // edit job in filtered list into a duplicate in address book
        Job jobInList = model.getAddressBook().getJobList().get(INDEX_SECOND_JOB.getZeroBased());
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB,
                new EditJobDescriptorBuilder(jobInList).build());

        assertCommandFailure(editJobCommand, model, EditJobCommand.MESSAGE_DUPLICATE_JOB);
    }

    @Test
    public void execute_invalidJobIndexUnfilteredList_failure() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredJobList().size() + 1);
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder()
                .withJobTitle(VALID_JOBTITLE_BE).build();
        EditJobCommand editJobCommand = prepareCommand(outOfBoundIndex, descriptor);

        assertCommandFailure(editJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);
    }

    /**
     * Edit filtered list where index is larger than size of filtered list,
     * but smaller than size of address book
     */
    @Test
    public void execute_invalidJobIndexFilteredList_failure() {
        showJobAtIndex(model, INDEX_FIRST_JOB);
        Index outOfBoundIndex = INDEX_SECOND_JOB;
        // ensures that outOfBoundIndex is still in bounds of address book list
        assertTrue(outOfBoundIndex.getZeroBased() < model.getAddressBook().getJobList().size());

        EditJobCommand editJobCommand = prepareCommand(outOfBoundIndex,
                new EditJobDescriptorBuilder().withJobTitle(VALID_JOBTITLE_BE).build());

        assertCommandFailure(editJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);
    }

    @Test
    public void executeUndoRedo_validIndexUnfilteredList_success() throws Exception {
        UndoRedoStack undoRedoStack = new UndoRedoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoRedoStack);
        RedoCommand redoCommand = prepareRedoCommand(model, undoRedoStack);
        Job editedJob = new JobBuilder().build();
        Job jobToEdit = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(editedJob).build();
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        // edit -> first job edited
        editJobCommand.execute();
        undoRedoStack.push(editJobCommand);

        // undo -> reverts addressbook back to previous state and filtered job list to show all job
        assertCommandSuccess(undoCommand, model, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        // redo -> same first job edited again
        expectedModel.updateJob(jobToEdit, editedJob);
        assertCommandSuccess(redoCommand, model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void executeUndoRedo_invalidIndexUnfilteredList_failure() {
        UndoRedoStack undoRedoStack = new UndoRedoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoRedoStack);
        RedoCommand redoCommand = prepareRedoCommand(model, undoRedoStack);
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredJobList().size() + 1);
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder()
                .withJobTitle(VALID_JOBTITLE_BE).build();
        EditJobCommand editJobCommand = prepareCommand(outOfBoundIndex, descriptor);

        // execution failed -> editJobCommand not pushed into undoRedoStack
        assertCommandFailure(editJobCommand, model, Messages.MESSAGE_INVALID_JOB_DISPLAYED_INDEX);

        // no commands in undoRedoStack -> undoCommand and redoCommand fail
        assertCommandFailure(undoCommand, model, UndoCommand.MESSAGE_FAILURE);
        assertCommandFailure(redoCommand, model, RedoCommand.MESSAGE_FAILURE);
    }

    /**
     * 1. Edits a {@code Person} from a filtered list.
     * 2. Undo the edit.
     * 3. The unfiltered list should be shown now. Verify that the index of the previously edited person in the
     * unfiltered list is different from the index at the filtered list.
     * 4. Redo the edit. This ensures {@code RedoCommand} edits the person object regardless of indexing.
     */
    @Test
    public void executeUndoRedo_validIndexFilteredList_sameJobEdited() throws Exception {
        UndoRedoStack undoRedoStack = new UndoRedoStack();
        UndoCommand undoCommand = prepareUndoCommand(model, undoRedoStack);
        RedoCommand redoCommand = prepareRedoCommand(model, undoRedoStack);
        Job editedJob = new JobBuilder().build();
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(editedJob).build();
        EditJobCommand editJobCommand = prepareCommand(INDEX_FIRST_JOB, descriptor);
        Model expectedModel = new ModelManager(new AddressBook(model.getAddressBook()), new UserPrefs());

        showJobAtIndex(model, INDEX_SECOND_JOB);
        Job jobToEdit = model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased());
        // edit -> edits second job in unfiltered job list / first job in filtered job list
        editJobCommand.execute();
        undoRedoStack.push(editJobCommand);

        // undo -> reverts addressbook back to previous state and filtered job list to show all jobs
        assertCommandSuccess(undoCommand, model, UndoCommand.MESSAGE_SUCCESS, expectedModel);

        expectedModel.updateJob(jobToEdit, editedJob);
        assertNotEquals(model.getFilteredJobList().get(INDEX_FIRST_JOB.getZeroBased()), jobToEdit);
        // redo -> edits same second person in unfiltered person list
        assertCommandSuccess(redoCommand, model, RedoCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void equals() throws Exception {
        final EditJobCommand standardCommand = prepareCommand(INDEX_FIRST_JOB, DESC_FE);

        // same values -> returns true
        EditJobCommand.EditJobDescriptor copyDescriptor = new EditJobCommand.EditJobDescriptor(DESC_FE);
        EditJobCommand commandWithSameValues = prepareCommand(INDEX_FIRST_JOB, copyDescriptor);
        assertTrue(standardCommand.equals(commandWithSameValues));

        // same object -> returns true
        assertTrue(standardCommand.equals(standardCommand));

        // one command preprocessed when previously equal -> returns false
        commandWithSameValues.preprocessUndoableCommand();
        assertFalse(standardCommand.equals(commandWithSameValues));

        // null -> returns false
        assertFalse(standardCommand.equals(null));

        // different types -> returns false
        assertFalse(standardCommand.equals(new ClearCommand()));

        // different index -> returns false
        assertFalse(standardCommand.equals(new EditJobCommand(INDEX_SECOND_JOB, DESC_FE)));

        // different descriptor -> returns false
        assertFalse(standardCommand.equals(new EditJobCommand(INDEX_FIRST_JOB, DESC_BE)));
    }

    /**
     * Returns an {@code EditJobCommand} with parameters {@code index} and {@code descriptor}
     */
    private EditJobCommand prepareCommand(Index index, EditJobCommand.EditJobDescriptor descriptor) {
        EditJobCommand editJobCommand = new EditJobCommand(index, descriptor);
        editJobCommand.setData(model, new CommandHistory(), new UndoRedoStack());
        return editJobCommand;
    }

}
```
###### \java\seedu\address\logic\commands\ListJobCommandTest.java
``` java
/**
 * Contains integration tests (interaction with the Model) and unit tests for ListJobsCommand.
 */
public class ListJobCommandTest {

    private Model model;
    private Model expectedModel;
    private ListJobsCommand listJobsCommand;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        listJobsCommand = new ListJobsCommand();
        listJobsCommand.setData(model, new CommandHistory(), new UndoRedoStack());
    }

    @Test
    public void execute_jobListIsNotFiltered_showsSameList() {
        assertCommandSuccess(listJobsCommand, model, ListJobsCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_jobListIsFiltered_showsEverything() {
        showJobAtIndex(model, INDEX_FIRST_JOB);
        assertCommandSuccess(listJobsCommand, model, ListJobsCommand.MESSAGE_SUCCESS, expectedModel);
    }
}
```
###### \java\seedu\address\logic\commands\MatchJobCommandTest.java
``` java
/**
 * Contains integration tests (interaction with the Model) and unit tests for MatchJobCommand.
 */
public class MatchJobCommandTest {
    private Model model;
    private Model expectedModel;
    private ListJobsCommand listJobsCommand;

    @Before
    public void setUp() {
        model = new ModelManager(getTypicalAddressBook(), new UserPrefs());
        expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());

        listJobsCommand = new ListJobsCommand();
        listJobsCommand.setData(model, new CommandHistory(), new UndoRedoStack());
    }

    @Test
    public void execute_listIsNotFiltered_showsSameList() {
        assertCommandSuccess(listJobsCommand, model, ListJobsCommand.MESSAGE_SUCCESS, expectedModel);
    }

    @Test
    public void execute_listIsFiltered_showsEverything() {
        showJobAtIndex(model, INDEX_FIRST_JOB);
        assertCommandSuccess(listJobsCommand, model, ListJobsCommand.MESSAGE_SUCCESS, expectedModel);
    }
}
```
###### \java\seedu\address\logic\commands\PostJobCommandTest.java
``` java
public class PostJobCommandTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void constructor_nullJob_throwsNullPointerException() {
        thrown.expect(NullPointerException.class);
        new PostJobCommand(null);
    }

    @Test
    public void execute_jobAcceptedByModel_addSuccessful() throws Exception {
        ModelStubAcceptingJobAdded modelStub = new PostJobCommandTest.ModelStubAcceptingJobAdded();
        Job validJob = new JobBuilder().build();

        CommandResult commandResult = getPostJobCommandForJob(validJob, modelStub).execute();

        assertEquals(String.format(PostJobCommand.MESSAGE_SUCCESS, validJob), commandResult.feedbackToUser);
        assertEquals(Arrays.asList(validJob), modelStub.jobsAdded);
    }

    @Test
    public void execute_duplicateJob_throwsCommandException() throws Exception {
        PostJobCommandTest.ModelStub modelStub = new PostJobCommandTest.ModelStubThrowingDuplicateJobException();
        Job validJob = new JobBuilder().build();

        thrown.expect(CommandException.class);
        thrown.expectMessage(PostJobCommand.MESSAGE_DUPLICATE_JOB);

        getPostJobCommandForJob(validJob, modelStub).execute();
    }

    @Test
    public void equals() {
        Job backendJob = new JobBuilder().withJobTitle("Backend Job").build();
        Job frontendJob = new JobBuilder().withJobTitle("Frontend Job").build();

        PostJobCommand addBackendJobCommand = new PostJobCommand(backendJob);
        PostJobCommand addFrontendJobCommand = new PostJobCommand(frontendJob);

        // same object -> returns true
        assertTrue(addBackendJobCommand.equals(addBackendJobCommand));

        // same values -> returns true
        PostJobCommand addBackendJobCommandCopy = new PostJobCommand(backendJob);
        assertTrue(addBackendJobCommand.equals(addBackendJobCommandCopy));

        // different types -> returns false
        assertFalse(addBackendJobCommand.equals(1));

        // null -> returns false
        assertFalse(addBackendJobCommand.equals(null));

        // different person -> returns false
        assertFalse(addBackendJobCommand.equals(addFrontendJobCommand));
    }

    /**
     * Generates a new PostJobCommand with the details of the given job.
     */
    private PostJobCommand getPostJobCommandForJob(Job job, Model model) {
        PostJobCommand command = new PostJobCommand(job);
        command.setData(model, new CommandHistory(), new UndoRedoStack());
        return command;
    }

    /**
     * A default model stub that have all of the methods failing.
     */
    private class ModelStub implements Model {
        @Override
        public void addPerson(Person person) throws DuplicatePersonException {
            fail("This method should not be called.");
        }

        @Override
        public void addJob(Job job) throws DuplicateJobException {
            fail("This method should not be called.");
        }

        @Override
        public void addInterview(Interview interview) throws DuplicateInterviewException {
            fail("This method should not be called.");
        }

        @Override
        public void deleteInterview(Interview target) throws InterviewNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void addReport(Report report) {

            fail("This method should not be called.");
        }

        @Override
        public void resetData(ReadOnlyAddressBook newData) {
            fail("This method should not be called.");
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void deletePerson(Person target) throws PersonNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void updatePerson(Person target, Person editedPerson)
                throws DuplicatePersonException {
            fail("This method should not be called.");
        }

        @Override
        public void updateReport(Tag population) {
            fail("This method should not be called.");
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public Report getReport() {
            fail("This method should not be called.");
            return null;
        };

        @Override
        public void refreshReport() {
            fail("This method should not be called.");
        };

        @Override
        public ObservableList<Report> getReportHistory() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            fail("This method should not be called.");
        }

        @Override
        public void updateFilteredJobList(Predicate<Job> predicate) {
            fail("This method should not be called.");
        }

        @Override
        public ObservableList<Job> getFilteredJobList() {
            fail("This method should not be called.");
            return null;
        }

        @Override
        public void deleteJob(Job target) throws JobNotFoundException {
            fail("This method should not be called.");
        }

        @Override
        public void updateJob(Job target, Job editedJob)
                throws DuplicateJobException {
            fail("This method should not be called.");
        }

        @Override
        public ObservableList<Interview> getFilteredInterviewList() {
            fail("this method should not be called");
            return null;
        }

        @Override
        public void updateFilteredInterviewList(Predicate<Interview> predicate) {
            fail("This method should not be called.");
        }

    }

    /**
     * A Model stub that always throw a DuplicateJobException when trying to add a job.
     */
    private class ModelStubThrowingDuplicateJobException extends PostJobCommandTest.ModelStub {
        @Override
        public void addJob(Job job) throws DuplicateJobException {
            throw new DuplicateJobException();
        }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

    /**
     * A Model stub that always accept the job being added.
     */
    private class ModelStubAcceptingJobAdded extends PostJobCommandTest.ModelStub {
        final ArrayList<Job> jobsAdded = new ArrayList<>();

        @Override
        public void addJob(Job job) throws DuplicateJobException {
            requireNonNull(job);
            jobsAdded.add(job);
        }

        @Override
        public void refreshReport() { }

        @Override
        public ReadOnlyAddressBook getAddressBook() {
            return new AddressBook();
        }
    }

}
```
###### \java\seedu\address\logic\parser\AddressBookParserTest.java
``` java
    @Test
    public void parseCommand_postJob() throws Exception {
        Job job = new JobBuilder().build();
        PostJobCommand command = (PostJobCommand) parser.parseCommand(JobUtil.getPostJobCommand(job));
        assertEquals(new PostJobCommand(job), command);
    }
```
###### \java\seedu\address\logic\parser\AddressBookParserTest.java
``` java
    @Test
    public void parseCommand_deleteJob() throws Exception {
        DeleteJobCommand command = (DeleteJobCommand) parser.parseCommand(
                DeleteJobCommand.COMMAND_WORD + " " + INDEX_FIRST_JOB.getOneBased());
        assertEquals(new DeleteJobCommand(INDEX_FIRST_JOB), command);
    }

    @Test
    public void parseCommand_deleteJobAlias() throws Exception {
        DeleteJobCommand command = (DeleteJobCommand) parser.parseCommand(
                DeleteJobCommand.COMMAND_ALIAS + " " + INDEX_FIRST_JOB.getOneBased());
        assertEquals(new DeleteJobCommand(INDEX_FIRST_JOB), command);
    }
```
###### \java\seedu\address\logic\parser\AddressBookParserTest.java
``` java
    @Test
    public void parseCommand_editJob() throws Exception {
        Job job = new JobBuilder().build();
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(job).build();
        EditJobCommand command = (EditJobCommand) parser.parseCommand(EditJobCommand.COMMAND_WORD + " "
                + INDEX_FIRST_JOB.getOneBased() + " " + JobUtil.getJobDetails(job));
        assertEquals(new EditJobCommand(INDEX_FIRST_JOB, descriptor), command);
    }

    @Test
    public void parseCommand_editJobAlias() throws Exception {
        Job job = new JobBuilder().build();
        EditJobCommand.EditJobDescriptor descriptor = new EditJobDescriptorBuilder(job).build();
        EditJobCommand command = (EditJobCommand) parser.parseCommand(EditJobCommand.COMMAND_ALIAS + " "
                + INDEX_FIRST_JOB.getOneBased() + " " + JobUtil.getJobDetails(job));
        assertEquals(new EditJobCommand(INDEX_FIRST_JOB, descriptor), command);
    }
```
###### \java\seedu\address\logic\parser\AddressBookParserTest.java
``` java
    @Test
    public void parseCommand_findJob() throws Exception {
        List<String> keywords = Arrays.asList("foo", "bar", "baz");
        FindJobCommand command = (FindJobCommand) parser.parseCommand(
                FindJobCommand.COMMAND_WORD + " " + keywords.stream().collect(Collectors.joining(" ")));
        assertEquals(new FindJobCommand(new JobMatchesKeywordsPredicate(keywords)), command);
    }
```
###### \java\seedu\address\logic\parser\AddressBookParserTest.java
``` java
    @Test
    public void parseCommand_listJob() throws Exception {
        assertTrue(parser.parseCommand(ListJobsCommand.COMMAND_WORD) instanceof ListJobsCommand);
        assertTrue(parser.parseCommand(ListJobsCommand.COMMAND_WORD + " 3") instanceof ListJobsCommand);
    }
```
###### \java\seedu\address\model\job\JobTitleTest.java
``` java
public class JobTitleTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new JobTitle(null));
    }

    @Test
    public void constructor_invalidJobTitle_throwsIllegalArgumentException() {
        String invalidJobTitle = "";
        Assert.assertThrows(IllegalArgumentException.class, () -> new JobTitle(invalidJobTitle));
    }

    @Test
    public void isValidJobTitle() {
        // null name
        Assert.assertThrows(NullPointerException.class, () -> JobTitle.isValidTitle(null));

        // invalid name
        assertFalse(JobTitle.isValidTitle("")); // empty string
        assertFalse(JobTitle.isValidTitle(" ")); // spaces only
        assertFalse(JobTitle.isValidTitle("^")); // only non-alphanumeric characters
        assertFalse(JobTitle.isValidTitle("peter*")); // contains non-alphanumeric characters

        // valid name
        assertTrue(JobTitle.isValidTitle("backend software engineer")); // alphabets only
        assertTrue(JobTitle.isValidTitle("20180001")); // numbers only
        assertTrue(JobTitle.isValidTitle("Software Engineer Intern 2018")); // alphanumeric characters
        assertTrue(JobTitle.isValidTitle("Backend software Engineer")); // with capital letters
        assertTrue(JobTitle.isValidTitle("Summer Software Engineering Intern Cloud Operations")); // long names
    }
}
```
###### \java\seedu\address\model\job\PersonMatchesJobPredicateTest.java
``` java
public class PersonMatchesJobPredicateTest {
    @Test
    public void equals() {
        Job firstJobPosting = new JobBuilder().withJobTitle("Sample Title").withLocation("Geylang")
                .withSkill("CSS").build();
        Job secondJobPosting = new JobBuilder().withJobTitle("Second Sample").withLocation("Tampines")
                .withSkill("HTML").build();

        PersonMatchesJobPredicate firstPredicate;
        firstPredicate = new PersonMatchesJobPredicate(firstJobPosting);
        PersonMatchesJobPredicate secondPredicate;
        secondPredicate = new PersonMatchesJobPredicate(secondJobPosting);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        PersonMatchesJobPredicate firstPredicateCopy;
        firstPredicateCopy = new PersonMatchesJobPredicate(firstJobPosting);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_personMatchesJob_returnsTrue() {
        // One keyword
        PersonMatchesJobPredicate predicate;
        predicate = new PersonMatchesJobPredicate(new JobBuilder().withLocation("Geylang").withSkill("ALL")
                .withTags("ALL").build());
        assertTrue(predicate.test(new PersonBuilder().withAddress("Geylang Street Area").build()));

        predicate = new PersonMatchesJobPredicate(new JobBuilder().withSkill("CSS").withLocation("ALL")
                .withTags("ALL").build());
        assertTrue(predicate.test(new PersonBuilder().withSkills("CSS").build()));

        // Multiple keywords
        predicate = new PersonMatchesJobPredicate(new JobBuilder().withSkill("CSS").withLocation("Geylang")
                .withTags("ALL").build());
        assertTrue(predicate.test(new PersonBuilder().withSkills("CSS").withAddress("Geylang Street Area").build()));

        predicate = new PersonMatchesJobPredicate(new JobBuilder().withSkill("CSS").withLocation("Geylang")
                .withTags("FreshGrad").build());
        assertTrue(predicate.test(new PersonBuilder().withSkills("CSS").withAddress("Geylang Street Area")
                .withTags("FreshGrad").build()));
    }

    @Test
    public void test_personDoesNotMatchJobs_returnsFalse() {
        // Non-matching jobs and candidates
        PersonMatchesJobPredicate predicate = new PersonMatchesJobPredicate(new JobBuilder().withSkill("UnknownSkill")
                .withLocation("Batcave").withTags("UnknownTag").build());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").build()));

        // Keywords match location, but does not match skills or tags
        predicate = new PersonMatchesJobPredicate(new JobBuilder().withSkill("UnknownSkill")
                .withLocation("Matching Location").withTags("UnknownTag").build());
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Matching Street").withTags("school", "friends")
                .withSkills("Non-matching skills").build()));
    }

}
```
###### \java\seedu\address\model\job\SkillsTest.java
``` java
public class SkillsTest {
    @Test
    public void constructor_null_throwsNullPointerException() {
        Assert.assertThrows(NullPointerException.class, () -> new Skill(null));
    }

    @Test
    public void constructor_invalidName_throwsIllegalArgumentException() {
        String invalidSkill = " ";
        Assert.assertThrows(IllegalArgumentException.class, () -> new Skill(invalidSkill));
    }

    @Test
    public void isValidSkill() {
        // null skill
        Assert.assertThrows(NullPointerException.class, () -> Skill.isValidSkill(null));

        // invalid skill
        assertFalse(Skill.isValidSkill("")); // empty string
        assertFalse(Skill.isValidSkill(" ")); // spaces only

        // valid skill
        assertTrue(Skill.isValidSkill("peter*")); // contains non-alphanumeric characters
        assertTrue(Skill.isValidSkill("peter jack")); // alphabets only
        assertTrue(Skill.isValidSkill("12345")); // numbers only
        assertTrue(Skill.isValidSkill("probability theory 2")); // alphanumeric characters
        assertTrue(Skill.isValidSkill("Backend Engineering")); // with capital letters
        assertTrue(Skill.isValidSkill("Statistics 2")); // long names
    }
}
```
###### \java\seedu\address\storage\XmlAdaptedJobTest.java
``` java
public class XmlAdaptedJobTest {
    private static final String INVALID_JOBTITLE = "B!@@#";
    private static final String INVALID_LOCATION = " ";
    private static final String INVALID_TAG = "#friend";
    private static final String INVALID_SKILL = "";

    private static final String VALID_JOBTITLE = FRONTEND.getJobTitle().toString();
    private static final String VALID_LOCATION = FRONTEND.getLocation().toString();
    private static final String VALID_SKILL = FRONTEND.getSkills().toString();
    private static final List<XmlAdaptedTag> VALID_TAGS = FRONTEND.getTags().stream()
            .map(XmlAdaptedTag::new)
            .collect(Collectors.toList());

    @Test
    public void toModelType_invalidJobTitle_throwsIllegalValueException() {
        XmlAdaptedJob job =
                new XmlAdaptedJob(INVALID_JOBTITLE, VALID_LOCATION, VALID_SKILL, VALID_TAGS);
        String expectedMessage = JobTitle.MESSAGE_TITLE_CONSTRAINTS;
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_nullJobTitle_throwsIllegalValueException() {
        XmlAdaptedJob job = new XmlAdaptedJob(null, VALID_LOCATION, VALID_SKILL, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, JobTitle.class.getSimpleName());
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_invalidLocation_throwsIllegalValueException() {
        XmlAdaptedJob job =
                new XmlAdaptedJob(VALID_JOBTITLE, INVALID_LOCATION, VALID_SKILL, VALID_TAGS);
        String expectedMessage = Location.MESSAGE_LOCATION_CONSTRAINTS;
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_nullLocation_throwsIllegalValueException() {
        XmlAdaptedJob job = new XmlAdaptedJob(VALID_JOBTITLE, null, VALID_SKILL, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Location.class.getSimpleName());
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_invalidSkills_throwsIllegalValueException() {
        XmlAdaptedJob job =
                new XmlAdaptedJob(VALID_JOBTITLE, VALID_LOCATION, INVALID_SKILL, VALID_TAGS);
        String expectedMessage = Skill.MESSAGE_SKILL_CONSTRAINTS;
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_nullSkills_throwsIllegalValueException() {
        XmlAdaptedJob job = new XmlAdaptedJob(VALID_JOBTITLE, VALID_LOCATION, null, VALID_TAGS);
        String expectedMessage = String.format(MISSING_FIELD_MESSAGE_FORMAT, Skill.class.getSimpleName());
        Assert.assertThrows(IllegalValueException.class, expectedMessage, job::toModelType);
    }

    @Test
    public void toModelType_invalidTags_throwsIllegalValueException() {
        List<XmlAdaptedTag> invalidTags = new ArrayList<>(VALID_TAGS);
        invalidTags.add(new XmlAdaptedTag(INVALID_TAG));
        XmlAdaptedJob job =
                new XmlAdaptedJob(VALID_JOBTITLE, VALID_LOCATION, VALID_SKILL, invalidTags);
        Assert.assertThrows(IllegalValueException.class, job::toModelType);
    }

}
```
###### \java\seedu\address\testutil\EditJobDescriptorBuilder.java
``` java
/**
 * A utility class to help with building EditJobDescriptor objects.
 */
public class EditJobDescriptorBuilder {
    private EditJobCommand.EditJobDescriptor descriptor;

    public EditJobDescriptorBuilder() {
        descriptor = new EditJobCommand.EditJobDescriptor();
    }

    public EditJobDescriptorBuilder(EditJobCommand.EditJobDescriptor descriptor) {
        this.descriptor = new EditJobCommand.EditJobDescriptor(descriptor);
    }

    /**
     * Returns an {@code EditJobDescriptor} with fields containing {@code job}'s details
     */
    public EditJobDescriptorBuilder(Job job) {
        descriptor = new EditJobCommand.EditJobDescriptor();
        descriptor.setJobTitle(job.getJobTitle());
        descriptor.setLocation(job.getLocation());
        descriptor.setSkill(job.getSkills());
        descriptor.setTags(job.getTags());
    }

    /**
     * Sets the {@code JobTitle} of the {@code EditJobDescriptor} that we are building.
     */
    public EditJobDescriptorBuilder withJobTitle(String jobTitle) {
        descriptor.setJobTitle(new JobTitle(jobTitle));
        return this;
    }

    /**
     * Sets the {@code Location} of the {@code EditJobDescriptor} that we are building.
     */
    public EditJobDescriptorBuilder withLocation(String location) {
        descriptor.setLocation(new Location(location));
        return this;
    }

    /**
     * Sets the {@code Skills} of the {@code EditJobDescriptor} that we are building.
     */
    public EditJobDescriptorBuilder withSkills(String skills) {
        descriptor.setSkill(new Skill(skills));
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and set it to the {@code EditJobDescriptor}
     * that we are building.
     */
    public EditJobDescriptorBuilder withTags(String... tags) {
        Set<Tag> tagSet = Stream.of(tags).map(Tag::new).collect(Collectors.toSet());
        descriptor.setTags(tagSet);
        return this;
    }

    public EditJobCommand.EditJobDescriptor build() {
        return descriptor;
    }
}
```
###### \java\seedu\address\testutil\JobBuilder.java
``` java
/**
 * A utility class to help with building Job objects.
 */
public class JobBuilder {

    public static final String DEFAULT_JOBTITLE = "Software Engineer";
    public static final String DEFAULT_LOCATION = "Geylang";
    public static final String DEFAULT_SKILL = "Java, C, JavaScript";
    public static final String DEFAULT_TAGS = "FreshGrad";

    private JobTitle jobTitle;
    private Skill skill;
    private Location location;
    private Set<Tag> tags;

    public JobBuilder() {
        jobTitle = new JobTitle(DEFAULT_JOBTITLE);
        location = new Location(DEFAULT_LOCATION);
        skill = new Skill(DEFAULT_SKILL);
        tags = SampleDataUtil.getTagSet(DEFAULT_TAGS);
    }

    /**
     * Initializes the JobBuilder with the data of {@code jobToCopy}.
     */
    public JobBuilder(Job jobToCopy) {
        jobTitle = jobToCopy.getJobTitle();
        location = jobToCopy.getLocation();
        skill = jobToCopy.getSkills();
        tags = new HashSet<>(jobToCopy.getTags());
    }

    /**
     * Sets the {@code JobTitle} of the {@code Job} that we are building.
     */
    public JobBuilder withJobTitle(String title) {
        this.jobTitle = new JobTitle(title);
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and set it to the {@code Job} that we are building.
     */
    public JobBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Location} of the {@code Job} that we are building.
     */
    public JobBuilder withLocation(String title) {
        this.location = new Location(title);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that
     * we are building.
     */
    public JobBuilder withSkill(String skill) {
        this.skill = new Skill(skill);
        return this;
    }
    public Job build() {
        return new Job(jobTitle, location, skill, tags);
    }

}
```
###### \java\seedu\address\testutil\JobUtil.java
``` java
/***
 * A utility class for Job.
 */
public class JobUtil {

    /**
     * Returns an addjob command string for adding the {@code job}.
     */
    public static String getPostJobCommand(Job job) {
        return PostJobCommand.COMMAND_WORD + " " + getJobDetails(job);
    }

    /**
     * Returns the part of command string for the given {@code job}'s details.
     */
    public static String getJobDetails(Job job) {
        StringBuilder sb = new StringBuilder();
        sb.append(PREFIX_JOBTITLE + job.getJobTitle().fullTitle + " ");
        sb.append(PREFIX_LOCATION + job.getLocation().value + " ");
        sb.append(PREFIX_SKILLS + job.getSkills().value + " ");
        job.getTags().stream().forEach(s -> sb.append(PREFIX_TAG + s.tagName + " "));
        return sb.toString();
    }
}
```
###### \java\seedu\address\testutil\TypicalJobs.java
``` java
/**
 * A utility class containing a list of {@code Job} objects to be used in tests.
 */
public class TypicalJobs {

    public static final Job FRONTEND = new JobBuilder().withJobTitle("Frontend Engineer")
            .withLocation("Geylang").withSkill("HTML, CSS, JavaScript")
            .withTags("FreshGrad").build();

    public static final Job BACKEND = new JobBuilder().withJobTitle("Backend Engineer")
            .withLocation("Clementi").withSkill("Java, Python, SQL")
            .withTags("Intern").build();

    public static final Job SYSTEMS = new JobBuilder().withJobTitle("Systems Engineer")
            .withLocation("Ang Mo Kio").withSkill("Java, C, Operating Systems")
            .withTags("Experienced").build();

    public static final String KEYWORD_MATCHING_FRONTEND = "Frontend"; // A keyword that matches FRONTEND

    private TypicalJobs() {} // prevents instantiation

    /**
     * Returns an {@code AddressBook} with all the typical jobs.
     */
    public static AddressBook getTypicalAddressBook() {
        AddressBook ab = new AddressBook();
        for (Job job : getTypicalJobs()) {
            try {
                ab.addJob(job);
            } catch (DuplicateJobException e) {
                throw new AssertionError("not possible");
            }
        }
        return ab;
    }

    public static List<Job> getTypicalJobs() {
        return new ArrayList<>(Arrays.asList(FRONTEND, BACKEND));
    }
}
```
###### \java\seedu\address\ui\CommandBoxTest.java
``` java
    @Test
    public void handleKeyPress_startingWithTab() {
        // no completion exists
        assertInputHistory(KeyCode.TAB, "");

        // one completion exists
        commandBoxHandle.run(COMMAND_WITH_ONE_COMPLETION);
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_WITH_ONE_COMPLETION);

        // no change on multiple tab press
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_WITH_ONE_COMPLETION);

        // three possible completions exist
        commandBoxHandle.run(COMMAND_WITH_MULTIPLE_COMPLETIONS);
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_FIRST_COMPLETION);
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_SECOND_COMPLETION);
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_THIRD_COMPLETION);

        // on further tab press it should cycle through
        assertInputHistory(KeyCode.TAB, COMPLETE_COMMAND_FIRST_COMPLETION);

        // incorrect command phrase is attempted to be completed
        commandBoxHandle.run(COMMAND_THAT_FAILS);
        assertInputHistory(KeyCode.TAB, COMMAND_THAT_FAILS);
    }
```
###### \java\seedu\address\ui\JobCardTest.java
``` java
public class JobCardTest extends GuiUnitTest {
    @Test
    public void display() {
        // no tags
        Job jobWithNoTags = new JobBuilder().withTags(new String[0]).build();
        JobCard jobCard = new JobCard(jobWithNoTags, 1);
        uiPartRule.setUiPart(jobCard);
        assertCardDisplay(jobCard, jobWithNoTags, 1);

        // with tags
        Job jobWithTags = new JobBuilder().build();
        jobCard = new JobCard(jobWithTags, 2);
        uiPartRule.setUiPart(jobCard);
        assertCardDisplay(jobCard, jobWithTags, 2);
    }

    @Test
    public void equals() {
        Job job = new JobBuilder().build();
        JobCard jobCard = new JobCard(job, 0);

        // same job, same index -> returns true
        JobCard copy = new JobCard(job, 0);
        assertTrue(jobCard.equals(copy));

        // same object -> returns true
        assertTrue(jobCard.equals(jobCard));

        // null -> returns false
        assertFalse(jobCard.equals(null));

        // different types -> returns false
        assertFalse(jobCard.equals(0));

        // different job, same index -> returns false
        Job differentJob = new JobBuilder().withJobTitle("differentName").build();
        assertFalse(jobCard.equals(new JobCard(differentJob, 0)));

        // same person, different index -> returns false
        assertFalse(jobCard.equals(new JobCard(job, 1)));
    }

    /**
     * Asserts that {@code jobCard} displays the details of {@code expectedJob} correctly and matches
     * {@code expectedId}.
     */
    private void assertCardDisplay(JobCard jobCard, Job expectedJob, int expectedId) {
        guiRobot.pauseForHuman();

        JobCardHandle jobCardHandle = new JobCardHandle(jobCard.getRoot());

        // verify id is displayed correctly
        assertEquals(Integer.toString(expectedId) + ". ", jobCardHandle.getId());

        // verify person details are displayed correctly
        assertCardDisplaysJob(expectedJob, jobCardHandle);
    }

}
```
###### \java\seedu\address\ui\JobListPanelTest.java
``` java
public class JobListPanelTest extends GuiUnitTest {
    private static final ObservableList<Job> TYPICAL_JOB =
            FXCollections.observableList(getTypicalJobs());

    private static final JumpToJobListRequestEvent JUMP_TO_SECOND_EVENT =
            new JumpToJobListRequestEvent(INDEX_SECOND_JOB);

    private JobListPanelHandle jobListPanelHandle;

    @Before
    public void setUp() {
        JobListPanel jobListPanel = new JobListPanel(TYPICAL_JOB);
        uiPartRule.setUiPart(jobListPanel);

        jobListPanelHandle = new JobListPanelHandle(getChildNode(jobListPanel.getRoot(),
                JobListPanelHandle.JOB_LIST_VIEW_ID));
    }

    @Test
    public void display() {
        for (int i = 0; i < TYPICAL_JOB.size(); i++) {
            jobListPanelHandle.navigateToCard(TYPICAL_JOB.get(i));
            Job expectedJob = TYPICAL_JOB.get(i);
            JobCardHandle actualCard = jobListPanelHandle.getJobCardHandle(i);

            assertCardDisplaysJob(expectedJob, actualCard);
            assertEquals(Integer.toString(i + 1) + ". ", actualCard.getId());
        }
    }

    @Test
    public void handleJumpToListRequestEvent() {
        postNow(JUMP_TO_SECOND_EVENT);
        guiRobot.pauseForHuman();

        JobCardHandle expectedCard = jobListPanelHandle.getJobCardHandle(INDEX_SECOND_JOB.getZeroBased());
        JobCardHandle selectedCard = jobListPanelHandle.getHandleToSelectedCard();
        assertCardEquals(expectedCard, selectedCard);
    }

}
```
