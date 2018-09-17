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

    public SensorData[] getEventsinTimeRange(String userId, String projectId, long startTime, long endTime) {
        List<SensorData> events = new ArrayList<SensorData>();
        
        String stmt = "select SensorData.userId, SensorData.projectId, " +
            "SensorData.time,  TASSIGNMENTOFFERING.`CDUEDATE` as deadline, " +
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
            PreparedStatement preparedStatement = this.connect.prepareStatement(stmt);
            preparedStatement.setString(1, userId);
            preparedStatement.setString(2, projectId);
            preparedStatement.setDate(3, new Date(startTime));
            preparedStatement.setDate(4, new Date(endTime));
            ResultSet result = preparedStatement.executeQuery();
        } catch(SQLException e) {
            System.out.println("An error occured while executing the query.");
        }

        return events.toArray(new SensorData[events.size()]);
    }
}
