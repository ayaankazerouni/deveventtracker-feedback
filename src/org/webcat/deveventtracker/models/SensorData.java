package org.webcat.deveventtracker.models;

public class SensorData {
	private long time;
	private int currentSize;
	private String className;
	private int editSize;
	
	public SensorData(long time, int currentSize, String className) {
		this.time = time;
		this.currentSize = currentSize;
		this.className = className;
		this.editSize = -1;
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
	
	/**
	 * @param editSize The size of the change made in this event
	 */
	public void setEditSize(int editSize) {
		this.editSize = editSize;
	}
	
	/**
	 * @return the editSize
	 */
	public int getEditSize() {
		return this.editSize;
	}
}
