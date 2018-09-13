/**
 * 
 */
package org.webcat.deveventtracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Singleton class providing restricted access to
 * the database.
 * 
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class Database {
	
	/**
	 * The singleton instance
	 */
	private static Database theInstance;
	
	private Connection connect;
	
	private Database() throws SQLException {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			this.connect = DriverManager.getConnection("jdbc:mysql://localhost/web-cat-dev?"
					+ "user=root&"
					+ "password=root&"
					+ "serverTimezone=UTC");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.connect.close();
		}
	}
	
	/**
	 * Get the singleton instance, creating it if necessary.
	 * 
	 * @return a Database
	 */
	public static Database getInstance() {
		if (theInstance == null) {
			try {
				theInstance = new Database();
			} catch(SQLException e) {
				System.out.println("An error occurred while getting the MySQL connection:\n" + e.getMessage());
			}
		}
		
		return theInstance;
	}
	
	public Map<String, Integer> getFileSizes(String earlyOftenId) {
		Map<String, Integer> fileSizes = new HashMap<String, Integer>();
		// TODO: database operations to retrieve current file sizes
		return fileSizes;
	}
}
