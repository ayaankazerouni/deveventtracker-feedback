/**
 * 
 */
package main.java.webcat.deveventtracker.models;

import java.util.List;

import main.java.webcat.deveventtracker.db.Database;

/**
 * Light representation of the TASSIGNMENTOFFERING table in the Web-CAT
 * database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-25
 */
public class Assignment {

    private String assignmentId;
    private long deadline;

    /**
     * Initialise an Assignment.
     * 
     * @param assignmentId Maps to: TASSIGNMENTOFFERING.OID
     * @param deadline     A time stamp in milliseconds. Maps to:
     *                     TASSIGNMENTOFFERING.CDUEDATE
     */
    public Assignment(String assignmentId, long deadline) {
        this.assignmentId = assignmentId;
        this.deadline = deadline;
    }

    /**
     * @return the assignmentId
     */
    public String getAssignmentId() {
        return this.assignmentId;
    }

    /**
     * @param assignmentId the assignmentId to set
     */
    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    /**
     * @return the deadline
     */
    public long getDeadline() {
        return this.deadline;
    }

    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    /**
     * Updates {@link main.java.webcat.deveventtracker.models.metrics.EarlyOften
     * EarlyOften} scores for all students who have {@link SensorData} associated
     * with this Assignment.
     */
    public void updateEarlyOften() {
        Database db = Database.getInstance();
        List<String> studentIds = db.getUsersWithSensorData(this);
        studentIds.parallelStream().forEach(s -> {
            Feedback feedback = db.getFeedback(s, this);
            List<SensorData> events = db.getNewEventsForStudentOnAssignment(feedback,
                    feedback.getEarlyOften().getLastUpdated());
            feedback.updateEarlyOften(events);
            String id = db.upsertFeedback(feedback);
            if (id != null) {
                db.upsertFileSizes(feedback.getFileSizes(), feedback.getId());
            }
        });
    }
}
