package main.java.webcat.deveventtracker.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Represents an assignment state for a given student. Encapsulates current file
 * sizes, the assignment offering for this project, and incremental development
 * metrics.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Feedback {
    private String id;
    private String userId;
    private Assignment assignment;
    private Map<String, CurrentFileSize> fileSizes;

    private EarlyOften earlyOften;

    /**
     * Initialises a project for the specified student (user) on the given
     * assignment.
     * 
     * @param id         The id of the Feedback Item
     * @param userId     The id of the user (TUSER.OID)
     * @param assignment The {@link Assignment}
     * @param fileSizes  The
     *                   {@link main.java.webcat.deveventtracker.models.CurrentFileSize
     *                   CurrentFileSize} for each file seen so far.
     * @param earlyOften The {@link EarlyOften} score and intermediate data for this
     *                   student project
     */
    public Feedback(String id, String userId, Assignment assignment, Map<String, CurrentFileSize> fileSizes,
            EarlyOften earlyOften) {
        this.id = id;
        this.userId = userId;
        this.assignment = assignment;
        this.fileSizes = fileSizes;
        this.earlyOften = earlyOften;
    }

    /**
     * Initialises a project for the specifed student (user) on the given
     * assignment. Current file sizes and early often are initialised with default
     * values.
     * 
     * @param userId     The id of the user (TUSER.OID)
     * @param assignment The {@link Assignment}
     */
    public Feedback(String userId, Assignment assignment) {
        this.id = null;
        this.userId = userId;
        this.assignment = assignment;
        this.fileSizes = new HashMap<String, CurrentFileSize>();
        this.earlyOften = new EarlyOften();
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
     * @return the id
     */
    public String getId() {
        return this.id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
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
}
