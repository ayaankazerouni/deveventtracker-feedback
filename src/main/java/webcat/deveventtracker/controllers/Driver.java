/**
 * 
 */
package main.java.webcat.deveventtracker.controllers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Queries the Web-CAT DB for sensordata and populates a "drop-off point" with
 * incremental development feedback.
 * 
 * This package will run as a regularly scheduled job that updates incremental
 * development scores for all students on a given assignment.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Driver {
    
    private final static Logger log = LogManager.getLogger();
    
    /**
     * Entry point for the application. Calculates incremental development feedback
     * for the specified assignment.
     * 
     * @param args A list of assignment offering ids, that map to rows in the
     *             TASSIGNMENTOFFERING table
     */
    public static void main(String[] args) {
        log.info("Initialised with params " + String.join(", ", args) + ".");
        ApplicationController controller = new ApplicationController(args);
        controller.run();
    }
}
