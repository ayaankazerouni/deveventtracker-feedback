/**
 * 
 */
package org.webcat.deveventtracker.models;

import org.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * Contains incremental development feedback for
 * the specified student and project.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Feedback {
	private String userId;
	private String assignmentId;
	private long deadline;
	private EarlyOften earlyOften;
	
	public Feedback(String userId, String assignmentId, long deadline, EarlyOften earlyOften) {
		this.userId = userId;
		this.assignmentId = assignmentId;
		this.deadline = deadline;
		this.earlyOften = earlyOften;
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
}
