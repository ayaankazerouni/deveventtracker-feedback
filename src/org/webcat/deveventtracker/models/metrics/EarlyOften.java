package org.webcat.deveventtracker.models.metrics;

import java.util.Map;

/**
 * Handles calculation of the Early/Often index and storage of intermediate
 * states for it.
 * 
 * The Early/Often index is a quantification of procrastination on a software
 * project.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class EarlyOften {
    private String id;
    private double score;
    private int totalEdits;
    private int totalWeightedEdits;
    private long lastUpdated;

    /**
     * Initialises an EarlyOften object, with 0 edits.
     */
    public EarlyOften() {
        this.totalEdits = 0;
        this.totalWeightedEdits = 0;
        this.score = Integer.MAX_VALUE;
        this.lastUpdated = -1;
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
     * @return the score
     */
    public double getScore() {
        return this.score;
    }

    /**
     * @param score the score to set
     */
    public void setScore(double score) {
        this.score = score;
    }

    /**
     * @return the total number of edits
     */
    public int getTotalEdits() {
        return this.totalEdits;
    }

    /**
     * @param totalEdits the totalEdits to set
     */
    public void setTotalEdits(int totalEdits) {
        this.totalEdits = totalEdits;
    }

    /**
     * Edits are weighted by time, i.e., the number of days until the assignment
     * deadline.
     * 
     * @see org.webcat.deveventtracker.models.StudentProject StudentProject
     * @return the total number of weighted edits.
     */
    public int getTotalWeightedEdits() {
        return this.totalWeightedEdits;
    }

    /**
     * @param totalWeightedEdits the totalWeightedEdits to set
     */
    public void setTotalWeightedEdits(int totalWeightedEdits) {
        this.totalWeightedEdits = totalWeightedEdits;
    }

    /**
     * @return The timestamp in milliseconds of the most recent {@link org.webcat.deveventtracker.models.SensorData SensorData}
     *         event seen by this EarlyOften object
     */
    public long getLastUpdated() {
        return this.lastUpdated;
    }

    /**
     * Updates this early often score based on a newly processed batch of events.
     * 
     * @see org.webcat.deveventtracker.models.StudentProject#processBatch(SensorData[])
     *      StudentProject.processBatch(SensorData[])
     * @param batchProcessed A map containing the information needed to update the
     *                       index
     * @throws IllegalArgumentException unless {@code batchProcessed} contains BOTH
     *                                  keys the following: totalEdits,
     *                                  totalWeightedEdits
     */
    public void update(Map<String, Long> batchProcessed) {
        if (!batchProcessed.containsKey("totalEdits") || !batchProcessed.containsKey("totalWeightedEdits")
                || !batchProcessed.containsKey("lastUpdated")) {
            throw new IllegalArgumentException(
                    "processedEvents must contain keys totalEdits and totalWeightedEdits and lastUpdated");
        }

        this.totalEdits += batchProcessed.get("totalEdits");
        this.totalWeightedEdits += batchProcessed.get("totalWeightedEdits");
        this.lastUpdated = batchProcessed.get("lastUpdated");

        // Calculate new early often score
        this.score = (double) this.totalWeightedEdits / this.totalEdits;
    }
}
