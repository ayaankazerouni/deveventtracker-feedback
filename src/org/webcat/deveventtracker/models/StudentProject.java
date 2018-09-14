package org.webcat.deveventtracker.models;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Represents an assignment state for a given student.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class StudentProject {
	private String userId;
	private String assignmentId;
	private long deadline;

	private Map<String, CurrentFileSize> fileSizes;

	private EarlyOften earlyOften;

	/**
	 * Initialises a project for the specified student (user) on the given
	 * assignment.
	 * 
	 * @param userId A unique identifier for the user
	 * @param assignmentId A unique identifier for the assignment
	 * @param deadline     The assignment due date, a timestamp in milliseconds
	 */
	public StudentProject(String userId, String assignmentId, long deadline) {
		this.userId = userId;
		this.assignmentId = assignmentId;
		this.deadline = deadline;
		this.earlyOften = new EarlyOften();
		this.fileSizes = new HashMap<String, CurrentFileSize>();
	}

	/**
	 * @return the {@code EarlyOften} object, which contains score data and
	 *         intermediate edit sizes.
	 * @see EarlyOften
	 */
	public EarlyOften getEarlyOften() {
		return this.earlyOften;
	}

	/**
	 * @return the userId
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * @return the assignmentId
	 */
	public String getAssignmentId() {
		return this.assignmentId;
	}

	/**
	 * @return the deadline
	 */
	public long getDeadline() {
		return this.deadline;
	}

	/**
	 * @return the fileSizes
	 */
	public Map<String, CurrentFileSize> getFileSizes() {
		return fileSizes;
	}

	/**
	 * Given a set of SensorData events, return the total number of edits and the
	 * total number of weighted edits contained in those events.
	 * 
	 * Edits are weighted by time, i.e., the number of days until the assignment
	 * deadline.
	 * 
	 * @param events An array of SensorData events
	 * @return A {@code HashMap<String, Integer>} containing the keys totalEdits and
	 *         totalWeightedEdits
	 */
	public Map<String, Integer> processBatch(SensorData[] events) {
		HashMap<String, Integer> newBatch = new HashMap<String, Integer>();
		int totalEdits = 0;
		int totalWeightedEdits = 0;

		for (SensorData event : events) {
			String className = event.getClassName();
			int size = event.getCurrentSize();

			long time = TimeUnit.MILLISECONDS.toDays(event.getTime());
			long deadlineDate = TimeUnit.MILLISECONDS.toDays(deadline);
			int daysToDeadline = (int) (deadlineDate - time);

			// If file was seen before, calculate and update edit size
			int oldSize = 0;
			if (this.fileSizes.containsKey(className)) {
				CurrentFileSize current = this.fileSizes.get(className);
				oldSize = current.getSize();
			}

			int editSize = Math.abs(size - oldSize);
			totalEdits += editSize;
			totalWeightedEdits += (editSize * daysToDeadline);

			// Update the last seen file size
			this.fileSizes.put(className, new CurrentFileSize(className, size, true));
		}

		newBatch.put("totalEdits", totalEdits);
		newBatch.put("totalWeightedEdits", totalWeightedEdits);
		return newBatch;
	}

	/**
	 * Updates this project's early often index based on the given additional data.
	 * 
	 * @param events Additional editing events.
	 * @see SensorData
	 * @see EarlyOften
	 */
	public void updateEarlyOften(SensorData[] events) {
		this.earlyOften.update(this.processBatch(events));
	}
}
