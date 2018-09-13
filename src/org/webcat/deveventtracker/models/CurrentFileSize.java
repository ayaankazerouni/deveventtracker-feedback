/**
 * 
 */
package org.webcat.deveventtracker.models;

/**
 * The last seen size of a file.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-03-13
 */
public class CurrentFileSize {
	private String name;
	private int size;
	private boolean dirty;
	
	public CurrentFileSize(String name, int size) {
		this.name = name;
		this.size = size;
	}
	
	public CurrentFileSize(String name, int size, boolean dirty) {
		this(name, size);
		this.dirty = true;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @param size the size to set
	 */
	public void setSize(int size) {
		this.size = size;
	}

	/**
	 * @return the dirty
	 */
	public boolean isDirty() {
		return this.dirty;
	}

	/**
	 * @param dirty the dirty to set
	 */
	public void setDirty() {
		this.dirty = true;
	}
}
