/**
 * 
 */
package org.webcat.deveventtracker.models.metrics;

import java.util.concurrent.TimeUnit;

import org.webcat.deveventtracker.models.SensorData;

/**
 * Handles calculation of the Early/Often index.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class EarlyOften {
	private String id;
	private double score;
	private int totalEdits;
	private int totalWeightedEdits;
	
	public EarlyOften() {
		this.totalEdits = 0;
		this.totalWeightedEdits = 0;
		this.score = Integer.MAX_VALUE;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * @return the score
	 */
	public double getScore() {
		return this.score;
	}
	
	/**
	 * @param score the score to set
	 */
	public void setScore(double score) {
		this.score = score;
	}
	
	/**
	 * @return the totalEdits
	 */
	public int getTotalEdits() {
		return this.totalEdits;
	}
	
	/**
	 * @param totalEdits the totalEdits to set
	 */
	public void setTotalEdits(int totalEdits) {
		this.totalEdits = totalEdits;
	}
	
	/**
	 * @return the totalWeightedEdits
	 */
	public int getTotalWeightedEdits() {
		return this.totalWeightedEdits;
	}
	
	/**
	 * @param totalWeightedEdits the totalWeightedEdits to set
	 */
	public void setTotalWeightedEdits(int totalWeightedEdits) {
		this.totalWeightedEdits = totalWeightedEdits;
	}
	
	/**
	 * Updates this early often score based on a new batch of events.
	 * If there are no new events, does nothing.
	 * 
	 * @param events The batch of events.
	 */
	public void update(SensorData[] events, long deadline) {
		if (events.length == 0) {
			return;
		}
		
		for (SensorData e : events) {
			long time = TimeUnit.MILLISECONDS.toDays(e.getTime());
			long deadlineDate = TimeUnit.MILLISECONDS.toDays(deadline);
			int daysToDeadline = (int) (deadlineDate - time);
			int editSize = e.getEditSize();
			
			// Update edit totals
			this.totalEdits += editSize;
			this.totalWeightedEdits += (editSize * daysToDeadline);
		}
		
		// Calculate new early often score
		this.score = this.totalWeightedEdits / this.totalEdits;
	}
}
