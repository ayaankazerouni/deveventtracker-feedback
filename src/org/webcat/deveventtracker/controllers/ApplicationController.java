/**
 * 
 */
package org.webcat.deveventtracker.controllers;

import org.webcat.deveventtracker.db.Database;
import org.webcat.deveventtracker.models.Assignment;

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
     * @see org.webcat.deveventtracker.models.Assignment Assignment
     */
    public ApplicationController(String assignmentId) {
        this.db = Database.getInstance();
        this.assignment = this.db.getAssignment(assignmentId);
    }
}
