/**
 * 
 */
package org.webcat.deveventtracker.db;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.webcat.deveventtracker.models.SensorData;

/**
 * Singleton class providing restricted access to
 * the database.
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
    
    private Database() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
    
            String user = System.getProperty("mysql.user");
            String pw = System.getProperty("mysql.pw");
            this.connect = DriverManager.getConnection("jdbc:mysql://localhost/web-cat-dev?"
                    + "user=" + user + "&"
                    + "password=" + pw + "&"
                    + "serverTimezone=UTC");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            this.connect.close();
        }
    }
    
    /** 
     * @return a singleton instance of this class, creating it if necessary
     */
    public static Database getInstance() {
        if (theInstance == null) {
            try {
                theInstance = new Database();
            } catch(SQLException e) {
                System.out.println("An error occurred while getting the MySQL connection:\n" + e.getMessage());
            }
        }
        
        return theInstance;
    }

    /**
     * Gets SensorData events from within a certain time range for the give
     * student project.
     * 
     * @param userId The student's ID
     * @param projectId The student project ID
     * @param startTime The range start time, inclusive, in milliseconds
     * @param endTime The range end time, inclusive, in milliseconds
     * @return An array of {@link Sensordata} objects
     */
    public SensorData[] getEventsinTimeRange(String userId, String projectId, long startTime, long endTime) {
        List<SensorData> events = new ArrayList<SensorData>();

        String sdQuery = "select SensorData.time, " +
        "TASSIGNMENTOFFERING.`CDUEDATE` as deadline, " +
        "SensorData.`currentSize`, SensorDataProperty.value as 'className'" +
        "from `SensorData`, `SensorDataProperty`, `ProjectForAssignment`, " +
            "`ProjectForAssignmentStudent`,`TASSIGNMENTOFFERING`" +
        "where SensorData.`projectId` = ProjectForAssignment.`OID`" +
            "and ProjectForAssignment.`assignmentOfferingId` = TASSIGNMENTOFFERING.`OID`" +
            "and ProjectForAssignmentStudent.`projectId` = ProjectForAssignment.`OID`" +
            "and SensorData.`userId` = ProjectForAssignmentStudent.`studentId`" +
            "and SensorData.`userId` = ?" +
            "and SensorData.`projectId` = ?" +
            "and SensorData.`OID` = SensorDataProperty.`sensorDataId`" +
            "and SensorDataProperty.`name` = 'Class-Name'" +
            "and SensorData.time >= ?" + 
            "and SensorData.time <= ?;";
        try  {
            PreparedStatement preparedStatement = this.connect.prepareStatement(sdQuery);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, projectId);
            preparedStatement.setDate(3, new Date(startTime));
            preparedStatement.setDate(4, new Date(endTime));
            ResultSet result = preparedStatement.executeQuery();

            while(result.next()) {
                long time = result.getDate("time").getTime();
                String className = result.getString("className");
                int currentSize = result.getInt("currentSize");
    
                SensorData event = new SensorData(time, currentSize, className);
                events.add(event);
            }
        } catch(SQLException e) {
            System.out.println("An error occured while retrieving SensorData.");
        }

        return events.toArray(new SensorData[events.size()]);
    }

    /**
     * Get the assignment deadline for a given project.
     *
     * @param projectId The ID of the student project
     * @return The assignment deadline in milliseconds
     * @throws IllegalArgumentException if an assignment was not found 
     *      for this project 
     */
    public long getProjectDeadline(String projectId) {
        String deadlineQuery = "select TASSIGNMENTOFFERING.CDUEDATE as deadline " +
            "from SensorData, ProjectForAssignment, TASSIGNMENTOFFERING " +
            "where SensorData.projectId = ProjectForAssignment.OID " +
                "and ProjectForAssignment.assignmentOfferingId = TASSIGNMENTOFFERING.OID " +
                "and projectId = ? " +
            "limit 1;";
        try {
            PreparedStatement preparedStatement = this.connect.prepareStatement(deadlineQuery);
            preparedStatement.setString(1, projectId);
            ResultSet result = preparedStatement.executeQuery();
            if (result.first()) {
                return result.getDate("deadline").getTime();
            } else {
                throw new IllegalArgumentException("Could not find an assignment offering for the project id " + projectId + ".");
            }
        } catch(SQLException e) {
            System.out.println("An error occurred while getting the project deadline");
            return -1;
        }
    }
}
