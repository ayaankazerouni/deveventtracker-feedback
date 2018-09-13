package org.webcat.deveventtracker.models;

public class SensorData {
	private long time;
	private int currentSize;
	private String className;
	
	public SensorData(long time, int currentSize, String className) {
		this.time = time;
		this.currentSize = currentSize;
		this.className = className;
	}
	
	/**
	 * @return the time
	 */
	public long getTime() {
		return this.time;
	}
	
	/**
	 * @return the currentSize
	 */
	public Integer getCurrentSize() {
		return this.currentSize;
	}
	
	/**
	 * @return the className
	 */
	public String getClassName() {
		return this.className;
	}
}
