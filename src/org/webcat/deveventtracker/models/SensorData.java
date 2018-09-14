package org.webcat.deveventtracker.models;

/**
 * Represents a single programming event.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class SensorData {
	private long time;
	private int currentSize;
	private String className;
	
	/**
	 * Initialises a SensorData event with the given fields.
	 * 
	 * @param time The time of this event, in milliseconds
	 * @param currentSize The size of the file this event touched, if any, in bytes
	 * @param className The name of the class this event touched, if any
	 */
	public SensorData(long time, int currentSize, String className) {
		this.time = time;
		this.currentSize = currentSize;
		this.className = className;
	}
	
	/**
	 * @return the event timestamp in milliseconds
	 */
	public long getTime() {
		return this.time;
	}
	
	/**
	 * @return the currentSize of the file at the time of this event, in bytes
	 */
	public int getCurrentSize() {
		return this.currentSize;
	}
	
	/**
	 * @return the name of the class this event took place on (can be null)
	 */
	public String getClassName() {
		return this.className;
	}
}
