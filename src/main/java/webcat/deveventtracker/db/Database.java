/**
 * 
 */
package main.java.webcat.deveventtracker.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import main.java.webcat.deveventtracker.models.Assignment;
import main.java.webcat.deveventtracker.models.SensorData;

/**
 * Singleton class providing restricted access to the database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Database {

    /**
     * The singleton instance
     */
    private static Database theInstance;

    private Connection connect;
    private ResultSet result;

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
     * @param userId A user id, maps to TUSER.OID
     * @param assignmentId An assignment id, maps to TASSIGNMENTOFFERING.OID
     * @param afterTime, A time stamp in milliseconds
     * @return An array of {@link SensorData} events
     */
    public SensorData[] getNewEventsForStudentOnAssignment(String userId, String assignmentId, long afterTime) {
        List<SensorData> events = new ArrayList<SensorData>();
        String sdQuery = "select SensorData.`time`, SensorDataProperty.value as 'className', SensorData.currentSize "
                + "from SensorData, SensorDataProperty, StudentProject, StudentProjectForAssignment, ProjectForAssignment, TASSIGNMENTOFFERING "
                + "where SensorData.projectId = StudentProject.OID " + "and SensorDataProperty.name = 'Class-Name' "
                + "and SensorDataProperty.sensorDataId = SensorData.OID "
                + "and StudentProject.OID = StudentProjectForAssignment.studentProjectId "
                + "and StudentProjectForAssignment.projectForAssignmentId = ProjectForAssignment.OID "
                + "and ProjectForAssignment.assignmentOfferingId = TASSIGNMENTOFFERING.OID "
                + "and SensorData.userId = ? "
                + "and TASSIGNMENTOFFERING.OID = ? "
                + "and SensorData.`time` >= ?;";
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(sdQuery);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, assignmentId);
            preparedStatement.setDate(3, new Date(afterTime));
        } catch (SQLException e) {
            System.out.println("An exception occured while retrieving SensorData.");
            return null;
        }
        return events.toArray(new SensorData[events.size()]);
    }

    /**
     * Get the specified TASSIGNMENTOFFERING from Web-CAT.
     * 
     * @param assignmentOfferingId The TASSIGNMENTOFFERING.OID
     * @return An {@link Assignment}
     * @throws IllegalArgumentException if there is no assignment offering with the
     *                                  specified id.
     */
    public Assignment getAssignment(String assignmentOfferingId) {
        String query = "select OID as assignmentId, CDUEDATE as deadline " + "from TASSIGNMENTOFFERING "
                + "where OID = ?";
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(query);
            preparedStatement.setString(1, assignmentOfferingId);
            this.result = preparedStatement.executeQuery();
            if (this.result.first()) {
                return new Assignment(this.result.getString("assignmentId"), this.result.getLong("deadline"));
            } else {
                throw new IllegalArgumentException(
                        "Couldn't find an assignment offering with id=" + assignmentOfferingId);
            }
        } catch (SQLException e) {
            System.out.println("An exception occured while retrieving the assigment.");
            return null;
        } finally {
            this.close();
        }
    }

    private void close() {
        try {
            if (this.result != null) {
                this.result.close();
            }
        } catch (SQLException e) {
            System.out.println("An error occured while closing resources.");
        }
    }
}
