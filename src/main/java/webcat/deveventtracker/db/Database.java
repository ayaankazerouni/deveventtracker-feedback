/**
 * 
 */
package main.java.webcat.deveventtracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.java.webcat.deveventtracker.models.Assignment;
import main.java.webcat.deveventtracker.models.CurrentFileSize;
import main.java.webcat.deveventtracker.models.Feedback;
import main.java.webcat.deveventtracker.models.SensorData;
import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Singleton class providing restricted access to the database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-24
 */
public class Database {

    /**
     * The singleton instance
     */
    private static Database theInstance;

    private Connection connect;

    private Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            String user = System.getProperty("mysql.user");
            String pw = System.getProperty("mysql.pw");
            this.connect = DriverManager.getConnection("jdbc:mysql://localhost/web-cat-dev?" + "user=" + user + "&"
                    + "password=" + pw + "&" + "serverTimezone=UTC");
        } catch (Exception e) {
            e.printStackTrace();
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
                System.out.println("An error occurred while getting the MySQL connection:\n" + e.getMessage());
            }
        }

        return theInstance;
    }

    /**
     * Gets the newest {@link SensorData} events for the specified student for the
     * specified assignment. "New" events are those that come after the given
     * {@code afterTime}.
     * 
     * @param project   The {@link Feedback} for which we want new events
     * @param afterTime A time stamp in milliseconds
     * @return An array of {@link SensorData} events
     */
    public List<SensorData> getNewEventsForStudentOnAssignment(Feedback project, long afterTime) {
        List<SensorData> events = new ArrayList<SensorData>();
        ResultSet result = null;
        String sdQuery = "select SensorData.`time`, SensorDataProperty.value as 'className', SensorData.currentSize as currentSize"
                + "from SensorData, SensorDataProperty, StudentProject, StudentProjectForAssignment, ProjectForAssignment, TASSIGNMENTOFFERING "
                + "where SensorData.projectId = StudentProject.OID " + "and SensorDataProperty.name = 'Class-Name' "
                + "and SensorDataProperty.sensorDataId = SensorData.OID "
                + "and StudentProject.OID = StudentProjectForAssignment.studentProjectId "
                + "and StudentProjectForAssignment.projectForAssignmentId = ProjectForAssignment.OID "
                + "and ProjectForAssignment.assignmentOfferingId = TASSIGNMENTOFFERING.OID "
                + "and SensorData.userId = ? " + "and TASSIGNMENTOFFERING.OID = ? " + "and SensorData.`time` >= ?;";
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(sdQuery);
            preparedStatement.setString(1, project.getUserId());
            preparedStatement.setString(2, project.getAssignment().getAssignmentId());
            preparedStatement.setTimestamp(3, new Timestamp(afterTime));
            result = preparedStatement.executeQuery();
            while (result.next()) {
                SensorData event = new SensorData(result.getLong("time"), result.getInt("currentSize"),
                        result.getString("className"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.out.println("An exception occured while retrieving SensorData.");
            return null;
        } finally {
            this.close(result);
        }
        return events;
    }

    /**
     * Get the specified TASSIGNMENTOFFERINGs from Web-CAT.
     * 
     * @param assignmentOfferingId A list of TASSIGNMENTOFFERING.OID values
     * @return A list of {@link Assignment} objects
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
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(query.toString());
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
                throw new IllegalArgumentException("Couldn't find any assignment offerings with specified ids.");
            }
        } catch (SQLException e) {
            System.out.println("An exception occured while retrieving the assigment.");
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
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(query);
            preparedStatement.setString(1, assignment.getAssignmentId());
            result = preparedStatement.executeQuery();
            while (result.next()) {
                users.add(result.getString(1));
            }
        } catch (SQLException e) {
            System.out.println("An error occured while retrieving users with SensorData.");
            e.printStackTrace();
        } finally {
            this.close(result);
        }

        return users;
    }

    /**
     * Gets a {@link Feedback} for the specified user on the given
     * {@link Assignment}. If one does not exist, a new one is created and returned.
     * 
     * @param studentProject     The OID of the specified user
     * @param assignment the specified assignment
     * @return
     */
    public Feedback getFeedback(String userId, Assignment assignment) {
        String query = "select FileSizeForStudentProject.name as className, FileSizeForStudentProject.size as currentSize, "
                + "IncDevFeedbackFromStudentProject.* "
                + "from IncDevFeedbackForStudentProject, FileSizeForStudentProject "
                + "where FileSizeForStudentProject.feedbackId = IncDevFeedbackForStudentProject.id "
                + "and IncDevFeedbackForStudentProject.userId = ? and IncDevFeedbackForStudentProject.assignmentOfferingId = ?";
        ResultSet result = null;
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(query);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, assignment.getAssignmentId());
            result = preparedStatement.executeQuery();
            Map<String, CurrentFileSize> fileSizes = new HashMap<String, CurrentFileSize>();
            if (result.first()) {
                // The early often score will be replicated for each file entry
                EarlyOften earlyOften = new EarlyOften(result.getInt("totalEdits"), result.getInt("totalWeightedEdits"),
                        result.getDouble("earlyOftenScore"), result.getLong("lastUpdated"));
                do {
                    // We moved to the first one already, so read it before moving the cursor
                    // further
                    fileSizes.put(result.getString("className"),
                            new CurrentFileSize(result.getString("className"), result.getInt("currentSize")));
                } while (result.next());

                Feedback project = new Feedback(userId, assignment, fileSizes,
                        earlyOften);
                return project;
            } else {
                return new Feedback(userId, assignment, new HashMap<String, CurrentFileSize>(),
                        new EarlyOften());
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving the feedback object.");
            return null;
        } finally {
            this.close(result);
        }
    }

    private void close(ResultSet result) {
        try {
            if (result != null) {
                result.close();
            }
        } catch (SQLException e) {
            System.out.println("An error occured while closing resources.");
        }
    }
}
