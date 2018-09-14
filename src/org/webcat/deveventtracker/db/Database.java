/**
 * 
 */
package org.webcat.deveventtracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.webcat.deveventtracker.models.SensorData;
import org.webcat.deveventtracker.models.StudentProject;

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
	
			String user = System.getProperty("mysql.user");
			String pw = System.getProperty("mysql.pw");
			this.connect = DriverManager.getConnection("jdbc:mysql://localhost/web-cat-dev?"
					+ "user=" + user + "&"
					+ "password=" + pw + "&"
					+ "serverTimezone=UTC");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.connect.close();
		}
	}
	
	/** 
	 * @return a singleton instance of this class, creating it if necessary
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
}
