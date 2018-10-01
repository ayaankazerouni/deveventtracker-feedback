package test.java.webcat.deveventtracker.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class EarlyOftenTest {
	private EarlyOften earlyOften;
	private Map<String, Long> batchProcessed;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		this.earlyOften = new EarlyOften();
		this.batchProcessed = new HashMap<String, Long>();
		this.batchProcessed.put("totalEdits", 400L);
		this.batchProcessed.put("totalWeightedEdits", 1200L);
		this.batchProcessed.put("lastUpdated", 123L);
	}
	
	@Test
	@DisplayName("update; simple test")
	public void testSimple() {
		this.earlyOften.update(this.batchProcessed);
		assertEquals(new Double(3), this.earlyOften.getScore());
	}
}
