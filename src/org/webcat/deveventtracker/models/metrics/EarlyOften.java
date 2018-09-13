/**
 * 
 */
package org.webcat.deveventtracker.models.metrics;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.webcat.deveventtracker.models.CurrentFileSize;
import org.webcat.deveventtracker.models.Feedback;
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
	private Feedback feedback;
	private Map<String, CurrentFileSize> fileSizes;
	
	public EarlyOften() {
		this.score = Integer.MAX_VALUE;
		this.fileSizes = new ConcurrentHashMap<String, CurrentFileSize>();
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
	 * @return the feedbackId
	 */
	public Feedback getFeedbackId() {
		return this.feedback;
	}
	
	/**
	 * Updates this early often score based on a new batch of events.
	 * If there are no new events, does nothing.
	 * 
	 * @param events The batch of events.
	 */
	public void updateEarlyOften(SensorData[] events) {
		if (events.length == 0) {
			return;
		}
		
		for (SensorData e : events) {
			String className = e.getClassName();
			int size = e.getCurrentSize();
			long time = TimeUnit.MILLISECONDS.toDays(e.getTime());
			long deadline = TimeUnit.MILLISECONDS.toDays(this.feedback.getDeadline());
			int daysToDeadline = (int) (deadline - time);
			
			// If file was seen before, calculate and update edit size
			int oldSize = 0;
			if (this.fileSizes.containsKey(className)) {
				CurrentFileSize current = this.fileSizes.get(className);
				oldSize = current.getSize();
			}
			this.fileSizes.put(className, new CurrentFileSize(className, size, true)); // Update the last seen file size
			int editSize = Math.abs(size - oldSize);
			
			// Update edit totals
			this.totalEdits += editSize;
			this.totalWeightedEdits += (editSize * daysToDeadline);
		}
		
		// Calculate new early often score
		this.score = this.totalWeightedEdits / this.totalEdits;
	}
}
