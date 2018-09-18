package org.webcat.deveventtracker.models;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Represents an assignment state for a given student.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class StudentProject {
    private String userId;
    private Assignment assignment;
    private Map<String, CurrentFileSize> fileSizes;

    private EarlyOften earlyOften;

    /**
     * Initialises a project for the specified student (user) on the given
     * assignment.
     * 
     * @param userId A unique identifier for the user
     * @param assignment The {@link Assignment} for this student project
     */
    public StudentProject(String userId, Assignment assignment) {
        this.userId = userId;
        this.earlyOften = new EarlyOften();
        this.fileSizes = new HashMap<String, CurrentFileSize>();
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
    public Map<String, Long> processBatch(SensorData[] events) {
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
    public void updateEarlyOften(SensorData[] events) {
        this.earlyOften.update(this.processBatch(events));
    }
}
