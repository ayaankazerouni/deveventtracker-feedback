/*
 * 
 */
package main.java.webcat.deveventtracker.controllers;

import java.util.List;

import main.java.webcat.deveventtracker.db.Database;
import main.java.webcat.deveventtracker.models.Assignment;

/**
 * Main controller for the application. Triggers calculation of metrics for all
 * students on a given assignment.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-20
 */
public class ApplicationController {
    private List<Assignment> assignments;
    private Database db;
    
    /**
     * Initialises a new assignment controller. 
     * 
     * @param assignmentId A TASSIGNMENTOFFERING.OID
     * @see main.java.webcat.deveventtracker.models.Assignment Assignment
     */
    public ApplicationController(String[] assignmentOfferingIds) {
        this.db = Database.getInstance();
        this.assignments = this.db.getAssignments(assignmentOfferingIds);
    }
    
    public void run() {
        for (Assignment current : this.assignments) {
            current.updateEarlyOftenForAssignment();
        }
    }

}
