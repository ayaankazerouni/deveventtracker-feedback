/**
 * 
 */
package main.java.webcat.deveventtracker.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import main.java.webcat.deveventtracker.models.Assignment;
import main.java.webcat.deveventtracker.models.CurrentFileSize;
import main.java.webcat.deveventtracker.models.Feedback;
import main.java.webcat.deveventtracker.models.SensorData;
import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Singleton class providing restricted access to the database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-10-03
 */
public class Database {

    /**
     * The singleton instance
     */
    private static Database theInstance;

    private static final Logger log = LogManager.getLogger();

    private Connection connect;

    private Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String user = System.getProperty("mysql.user");
            String pw = System.getProperty("mysql.pw");
            String dbUrl = System.getProperty("mysql.url");
            this.connect = DriverManager.getConnection("jdbc:mysql://" + dbUrl + "?" + "user=" + user + "&"
                    + "password=" + pw + "&" + "serverTimezone=UTC");
        } catch (ClassNotFoundException e) {
            log.error("Could not load JDBC Driver.", e);
        }
    }

    /**
     * @return a singleton instance of this class, creating it if necessary
     */
    public static Database getInstance() {
        if (theInstance == null) {
            try {
                theInstance = new Database();
            } catch (SQLException e) {
                log.error("An error occurred while getting the MySQL connection.", e);
            }
        }

        return theInstance;
    }

    /**
     * Gets the newest {@link SensorData} events for the specified student for the
     * specified assignment. "New" events are those that come after the given
     * {@code afterTime}.
     * 
     * @param feedback  The {@link Feedback} for which we want new events
     * @param afterTime A time stamp in milliseconds
     * @return A List of {@link SensorData} events
     */
    public List<SensorData> getNewEventsForStudentOnAssignment(Feedback feedback, long afterTime) {
        List<SensorData> events = new ArrayList<SensorData>();
        ResultSet result = null;
        String sdQuery = "select sensordata.`time`, sensordataproperty.`value` as 'className', sensordata.currentSize as currentSize "
                + "from sensordata, sensordataproperty, studentproject, studentprojectforassignment, projectforassignment, tassignmentoffering "
                + "where sensordata.projectId = studentproject.OID and sensordataproperty.name = 'Class-Name' "
                + "and sensordataproperty.sensorDataId = sensordata.OID "
                + "and studentproject.OID = studentprojectforassignment.studentProjectId "
                + "and studentprojectforassignment.projectForAssignmentId = projectforassignment.OID "
                + "and projectforassignment.assignmentOfferingId = tassignmentoffering.OID "
                + "and sensordata.userId = ? " + "and tassignmentoffering.OID = ? " + "and sensordata.`time` >= ?;";
        try (PreparedStatement preparedStatement = this.connect.prepareStatement(sdQuery)) {
            preparedStatement.setString(1, feedback.getUserId());
            preparedStatement.setString(2, feedback.getAssignment().getAssignmentId());
            preparedStatement.setTimestamp(3, new Timestamp(afterTime));
            result = preparedStatement.executeQuery();
            while (result.next()) {
                long time = result.getTimestamp("time").getTime();
                String name = result.getString("className");
                if (name != null) {
                    SensorData event = new SensorData(time, result.getInt("currentSize"), name);
                    events.add(event);
                }
            }
        } catch (SQLException e) {
            String message = "An exception occured while retrieving SensorData for " + feedback;
            log.error(message, e);
            return null;
        } finally {
            this.close(result);
        }
        return events;
    }

    /**
     * Get the specified TASSIGNMENTOFFERINGs from Web-CAT.
     * 
     * @param assignmentOfferingIds A list of TASSIGNMENTOFFERING.OID values
     * @return A list of {@link Assignment} objects.
     * @throws IllegalArgumentException if there are no assignments offering with
     *                                  the specified id.
     */
    public List<Assignment> getAssignments(String[] assignmentOfferingIds) {
        StringBuilder query = new StringBuilder(
                "select OID as assignmentId, CDUEDATE as deadline from tassignmentoffering " + "where OID in (");

        for (int i = 0; i < assignmentOfferingIds.length; i++) {
            if (i == 0) {
                query.append("?");
            } else {
                query.append(", ?");
            }
        }

        query.append(");");

        ResultSet result = null;
        try (PreparedStatement preparedStatement = this.connect.prepareStatement(query.toString())) {
            for (int i = 1; i <= assignmentOfferingIds.length; i++) {
                preparedStatement.setInt(i, Integer.parseInt(assignmentOfferingIds[i - 1])); // Prepared statement
                                                                                             // params are 1-indexed
            }
            result = preparedStatement.executeQuery();

            List<Assignment> assignments = new ArrayList<Assignment>();

            if (result.first()) {
                do {
                    long time = result.getTimestamp("deadline").getTime();
                    assignments.add(new Assignment(result.getString("assignmentId"), time));
                } while (result.next());

                return assignments;
            } else {
                log.error("Couldn't find any assignment offerings with specified ids.");
                return new ArrayList<Assignment>();
            }
        } catch (SQLException e) {
            String msg = "An exception occured while retrieving assigments with ids [";
            for (String id : assignmentOfferingIds) {
                msg += id;
            }
            msg += "].";
            log.error(msg, e);
            return null;
        } finally {
            this.close(result);
        }
    }

    /**
     * Gets a list of user ids for students who have {@link SensorData} associated
     * with the specified {@link Assignment}.
     * 
     * @param assignment The specified Assignment
     * @return a list of user ids
     */
    public List<String> getUsersWithSensorData(Assignment assignment) {
        List<String> users = new ArrayList<String>();

        String query = "select distinct sensordata.`userId` "
                + "from sensordata, studentproject, studentprojectforassignment, projectforassignment, tassignmentoffering "
                + "where sensordata.projectId = studentproject.OID and studentproject.`OID` = studentprojectforassignment.`studentProjectId` "
                + "    and studentprojectforassignment.`projectForAssignmentId` = projectforassignment.`OID` "
                + "    and projectforassignment.`assignmentOfferingId` = tassignmentoffering.`OID` "
                + "    and tassignmentoffering.`OID` = ?;";
        ResultSet result = null;
        try (PreparedStatement preparedStatement = this.connect.prepareStatement(query)) {
            preparedStatement.setString(1, assignment.getAssignmentId());
            result = preparedStatement.executeQuery();
            while (result.next()) {
                users.add(result.getString(1));
            }
        } catch (SQLException e) {
            log.error("An error occured while retrieving users with SensorData for " + assignment, e);
        } finally {
            this.close(result);
        }

        return users;
    }

    /**
     * Gets a {@link Feedback} for the specified user on the given
     * {@link Assignment}. If one does not exist, a new one is created and returned.
     * 
     * If a SQLException occurs, returns null
     * 
     * @param userId     The OID of the specified user
     * @param assignment The specified assignment
     * @return Feedback for the student on the assignment, or null if a database
     *         error occurred
     */
    public Feedback getFeedback(String userId, Assignment assignment) {
        String query = "select FileSizeForStudentProject.name as className, FileSizeForStudentProject.size as currentSize, "
                + "IncDevFeedbackForStudentProject.* "
                + "from IncDevFeedbackForStudentProject, FileSizeForStudentProject "
                + "where FileSizeForStudentProject.feedbackId = IncDevFeedbackForStudentProject.id "
                + "and IncDevFeedbackForStudentProject.userId = ? and IncDevFeedbackForStudentProject.assignmentOfferingId = ?";
        ResultSet result = null;
        try (PreparedStatement preparedStatement = this.connect.prepareStatement(query)) {
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, assignment.getAssignmentId());
            result = preparedStatement.executeQuery();
            Map<String, CurrentFileSize> fileSizes = new HashMap<String, CurrentFileSize>();
            if (result.first()) {
                // The early often score will be replicated for each file entry
                long lastUpdatedAt = result.getTimestamp("lastUpdatedAt").getTime();
                Double score = result.getDouble("earlyOftenScore");
                if (result.wasNull()) {
                    // The score could be a NaN if there haven't been any edits yet
                    score = null;
                }
                EarlyOften earlyOften = new EarlyOften(result.getInt("totalEdits"), result.getInt("totalWeightedEdits"),
                        score, lastUpdatedAt);
                String feedbackId = result.getString("id");
                do {
                    // We moved to the first one already, so read it before moving the cursor
                    // further
                    fileSizes.put(result.getString("className"),
                            new CurrentFileSize(result.getString("className"), result.getInt("currentSize")));
                } while (result.next());

                Feedback project = new Feedback(feedbackId, userId, assignment, fileSizes, earlyOften);
                return project;
            } else {
                return new Feedback(userId, assignment); // this has not been saved to the DB yet
            }

        } catch (SQLException e) {
            log.error("Error while retrieving the Feedback for user " + userId + " on " + assignment, e);
            return null;
        } finally {
            this.close(result);
        }
    }

    /**
     * Update or insert the specified {@link Feedback} object. If the (userId,
     * Assignment) unique key finds a match, this method will perform an update. If
     * not it will perform an insertion.
     * 
     * This method updates the IncDevFeedbackForStudentProject table.
     * 
     * @param feedback The Feedback record to be upserted.
     * @return The id of the record if it was a new one inserted.
     */
    public String upsertFeedback(Feedback feedback) {
        String sql = "insert into IncDevFeedbackForStudentProject (assignmentOfferingId, userId, totalEdits, totalWeightedEdits, lastUpdatedAt, earlyOftenScore) "
                + "values (?, ?, ?, ?, ?, ?) on duplicate key update "
                + "totalEdits=values(totalEdits), totalWeightedEdits=values(totalWeightedEdits), earlyOftenScore=values(earlyOftenScore), lastUpdatedAt=values(lastUpdatedAt);";
        EarlyOften earlyOften = feedback.getEarlyOften();
        try (PreparedStatement preparedStatement = this.connect.prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, feedback.getAssignment().getAssignmentId());
            preparedStatement.setString(2, feedback.getUserId());
            preparedStatement.setInt(3, earlyOften.getTotalEdits());
            preparedStatement.setInt(4, earlyOften.getTotalWeightedEdits());
            preparedStatement.setTimestamp(5, new Timestamp(earlyOften.getLastUpdated()));
            Double score = earlyOften.getScore();
            if (Double.isNaN(score)) {
                preparedStatement.setString(6, null);
            } else {
                preparedStatement.setBigDecimal(6, new BigDecimal(score));
            }

            int affectedRows = preparedStatement.executeUpdate();
            ;
            if (affectedRows == 0) {
                throw new SQLException("Updating feedback failed.");
            }

            try (ResultSet keys = preparedStatement.getGeneratedKeys()) {
                if (keys.first()) {
                    return keys.getString(1);
                } else {
                    log.info(feedback + " did not need an update.");
                    return null;
                }
            }
        } catch (SQLException e) {
            log.error("Error while updating " + feedback, e);
            return null;
        }
    }

    /**
     * Update or insert the specified file sizes for the specified feedback record.
     * Note that there must be a record with id feedbackId in the
     * IncDevFeedbackForStudentProject table, to satisfy the foreign key constraint.
     * 
     * @param feedback A {@link Feedback} record
     */
    public void upsertFileSizes(Feedback feedback) {
        if (feedback.getFileSizes().isEmpty()) {
            log.info("No file sizes to update for " + feedback);
            return;
        }

        // Build update statement
        StringBuilder sql = new StringBuilder(
                "insert into FileSizeForStudentProject (feedbackId, name, `size`) values ");
        Map<String, CurrentFileSize> fileSizes = feedback.getFileSizes();
        String feedbackId = feedback.getId();
        for (int i = 0; i < fileSizes.size(); i++) {
            sql.append("(" + feedbackId + ", ?, ?)");
            if (i < fileSizes.size() - 1) {
                sql.append(", ");
            }
        }
        sql.append(" on duplicate key update `size`=values(`size`)");

        try (PreparedStatement preparedStatement = this.connect.prepareStatement(sql.toString())) {
            int i = 0;
            for (CurrentFileSize f : fileSizes.values()) {
                preparedStatement.setString(++i, f.getName());
                preparedStatement.setInt(++i, f.getSize());
            }
            int affectedRows = preparedStatement.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Was not able to update any file sizes.");
            }
        } catch (SQLException e) {
            log.error("Error while updating file sizes for " + feedback + ". FileSizes:\n"
                    + feedback.getFileSizeInformation(), e);
        }
    }

    private void close(ResultSet result) {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            log.error("Error while closing resources.", e);
        }
    }
}
