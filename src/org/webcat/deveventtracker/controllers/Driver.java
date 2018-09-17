/**
 * 
 */
package org.webcat.deveventtracker.controllers;

/**
 * Queries the Web-CAT DB for sensordata and populates a
 * "drop-off point" with incremental development feedback.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Driver {

    /**
     * Entry point for the application. Calculates incremental development
     * feedback for the specified student project.
     * 
     * Command line arguments should include a student id and an
     * assignment id.
     * 
     * @param args Command line arguments, containing a userId and a projectId
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            String message = "Expected parameters: <userId> <projectId>";
            throw new IllegalArgumentException(message);
        }

        String userId = args[0];
        String projectId = args[1];
    }
}
