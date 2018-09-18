/**
 * 
 */
package main.java.webcat.deveventtracker.controllers;

import main.java.webcat.deveventtracker.db.Database;
import main.java.webcat.deveventtracker.models.Assignment;
import main.java.webcat.deveventtracker.models.SensorData;
import main.java.webcat.deveventtracker.models.StudentProject;

/**
 * Main controller for the application. Triggers calculation of metrics for all
 * students on a given assignment.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-17
 */
public class ApplicationController {
    private Assignment assignment;
    private Database db;
    
    /**
     * Initialises a new assignment controller. 
     * 
     * @param assignmentId A TASSIGNMENTOFFERING.OID
     * @see main.java.webcat.deveventtracker.models.Assignment Assignment
     */
    public ApplicationController(String assignmentId) {
        this.db = Database.getInstance();
        this.assignment = this.db.getAssignment(assignmentId);
    }
    
    public void updateEarlyOftenForStudent(String userId) {
        StudentProject studentProject = this.db.getStudentProject(userId, this.assignment);
        SensorData[] events = this.db.getNewEventsForStudentOnAssignment(studentProject, studentProject.getEarlyOften().getLastUpdated());
        studentProject.updateEarlyOften(events);
        
        // TODO: Update the DB
    }
}
