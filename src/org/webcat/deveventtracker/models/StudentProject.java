/**
 * 
 */
package org.webcat.deveventtracker.models;

import java.util.HashMap;
import java.util.Map;

import org.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Contains incremental development feedback for
 * the specified student and project.
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
	
	public StudentProject(String userId, String assignmentId, long deadline) {
		this.userId = userId;
		this.assignmentId = assignmentId;
		this.deadline = deadline;
		this.earlyOften = new EarlyOften();
		this.fileSizes = new HashMap<String, CurrentFileSize>();
	}
	
	/**
	 * @return the earlyOften
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
	
	public SensorData[] getEditSizes(SensorData[] events) {
		for (SensorData event : events) {
			event.setEditSize(this.getEditSize(event));
		}
		
		return events;
	}
	
	private int getEditSize(SensorData event) {
		String className = event.getClassName();
		int size = event.getCurrentSize();
		
		// If file was seen before, calculate and update edit size
		int oldSize = 0;
		if (this.fileSizes.containsKey(className)) {
			CurrentFileSize current = this.fileSizes.get(className);
			oldSize = current.getSize();
		}
		this.fileSizes.put(className, new CurrentFileSize(className, size, true)); // Update the last seen file size
		
		return Math.abs(size - oldSize);
	}
	
	public void updateEarlyOften(SensorData[] events) {
		this.earlyOften.update(events, this.deadline);
	}
}
