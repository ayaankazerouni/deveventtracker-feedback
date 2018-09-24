package main.java.webcat.deveventtracker.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import main.java.webcat.deveventtracker.db.Database;
import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Represents an assignment state for a given student. Maps to the
 * FeedbackForStudentProject object in the Web-CAT database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Feedback {
    private String userId;
    private Assignment assignment;
    private String studentProjectId;
    private Map<String, CurrentFileSize> fileSizes;

    private EarlyOften earlyOften;

    /**
     * Initialises a project for the specified student (user) on the given
     * assignment.
     * 
     * @param userId           The id of the user (TUSER.OID)
     * @param studentProjectId The StudentProject id (StudentProject.OID)
     * @param assignment       The assignment offering id (TASSIGNMENTOFFERING.OID)
     * @param fileSizes        The
     *                         {@link main.java.webcat.deveventtracker.models.CurrentFileSize
     *                         CurrentFileSize} for each file seen so far
     * @param earlyOften       The {@link EarlyOften} score and intermediate data
     *                         for this student project
     */
    public Feedback(String userId, String studentProjectId, Assignment assignment,
            Map<String, CurrentFileSize> fileSizes, EarlyOften earlyOften) {
        this.userId = userId;
        this.studentProjectId = studentProjectId;
        this.assignment = assignment;
        this.fileSizes = fileSizes;
        this.earlyOften = earlyOften;
    }

    /**
     * @return the {@code EarlyOften} object, which contains score data and
     *         intermediate edit sizes.
     * @see EarlyOften
     */
    public EarlyOften getEarlyOften() {
        return this.earlyOften;
    }

    /**
     * @return the userId
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * @return the studentProjectId
     */
    public String getStudentProjectId() {
        return this.studentProjectId;
    }

    /**
     * @param studentProjectId the studentProjectId to set
     */
    public void setStudentProjectId(String studentProjectId) {
        this.studentProjectId = studentProjectId;
    }

    /**
     * @return the {@link Assignment}
     */
    public Assignment getAssignment() {
        return this.assignment;
    }

    /**
     * @return the fileSizes
     */
    public Map<String, CurrentFileSize> getFileSizes() {
        return fileSizes;
    }

    /**
     * Given a set of SensorData events, return the total number of edits and the
     * total number of weighted edits contained in those events.
     * 
     * Edits are weighted by time, i.e., the number of days until the assignment
     * deadline.
     * 
     * @param events An array of SensorData events
     * @return A {@code HashMap<String, Long>} containing the keys totalEdits,
     *         totalWeightedEdits, and lastUpdated
     * @see EarlyOften
     */
    public Map<String, Long> processBatch(List<SensorData> events) {
        HashMap<String, Long> newBatch = new HashMap<String, Long>();
        int totalEdits = 0;
        int totalWeightedEdits = 0;
        long lastUpdated = this.getEarlyOften().getLastUpdated();

        for (SensorData event : events) {
            // In case events are not sorted by time
            if (event.getTime() >= lastUpdated) {
                lastUpdated = event.getTime();
            }

            String className = event.getClassName();
            int size = event.getCurrentSize();

            long time = TimeUnit.MILLISECONDS.toDays(event.getTime());
            long deadlineDate = TimeUnit.MILLISECONDS.toDays(this.assignment.getDeadline());
            int daysToDeadline = (int) (deadlineDate - time);

            // If file was seen before, calculate and update edit size
            int oldSize = 0;
            if (this.fileSizes.containsKey(className)) {
                CurrentFileSize current = this.fileSizes.get(className);
                oldSize = current.getSize();
            }

            int editSize = Math.abs(size - oldSize);
            totalEdits += editSize;
            totalWeightedEdits += (editSize * daysToDeadline);

            // Update the last seen file size
            this.fileSizes.put(className, new CurrentFileSize(className, size, true));
        }

        newBatch.put("totalEdits", (long) totalEdits);
        newBatch.put("totalWeightedEdits", (long) totalWeightedEdits);
        newBatch.put("lastUpdated", lastUpdated);
        return newBatch;
    }

    /**
     * Updates this project's early often index based on the given additional data.
     * 
     * @param events Additional editing events.
     * @see SensorData
     * @see EarlyOften
     */
    public void updateEarlyOften(List<SensorData> events) {
        this.earlyOften.update(this.processBatch(events));
    }

    public static Feedback getForStudentOnAssignment(String userId, Assignment assignment) {
        Database db = Database.getInstance();
        return db.getStudentProject(userId, assignment);
    }
}
