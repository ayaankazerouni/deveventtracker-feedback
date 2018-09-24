package test.java.webcat.deveventtracker.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import main.java.webcat.deveventtracker.models.Assignment;
import main.java.webcat.deveventtracker.models.CurrentFileSize;
import main.java.webcat.deveventtracker.models.SensorData;
import main.java.webcat.deveventtracker.models.Feedback;
import main.java.webcat.deveventtracker.models.metrics.EarlyOften;

/**
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class FeedbackTest {
	private Feedback studentProject;
	private List<SensorData> events;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		// Mock deadline: September 15
	    Assignment assignment = new Assignment("123", 1537055940000L);
	    Map<String, CurrentFileSize> fileSizes = new HashMap<String, CurrentFileSize>();
		this.studentProject = new Feedback("123", assignment, fileSizes, new EarlyOften());
		this.events = new ArrayList<SensorData>();
		this.events.add(new SensorData(1536428003000L, 300, "FirstClass")); // September 8
		this.events.add(new SensorData(1536690345000L, 275, "FirstClass")); // September 11
		this.events.add(new SensorData(1536610046000L, 50, "SecondClass")); // September 10
		this.events.add(new SensorData(1536782783000L, 62, "SecondClass")); // September 12
	}

	@Test
	@DisplayName("processBatch; 1 file, 1 event")
	public void testProcessBatch1File1Event() {
		List<SensorData> events = this.events.subList(0, 1);
		Map<String, Long> processed = this.studentProject.processBatch(events);
		assertEquals(300, (long) processed.get("totalEdits"));
		assertEquals(2100, (long) processed.get("totalWeightedEdits"));
	}

	@Test
	@DisplayName("processBatch; 1 file, 2 events")
	public void testProcessBatch1File2Events() {
		List<SensorData> events = this.events.subList(0, 2);
		Map<String, Long> processed = this.studentProject.processBatch(events);
		assertEquals(325, (long) processed.get("totalEdits"));
		assertEquals(2200, (long) processed.get("totalWeightedEdits"));
	}

	@Test
	@DisplayName("processBatch; 2 files, 2 events each")
	public void testProcessBatch2Files2Events() {
		Map<String, Long> processed = this.studentProject.processBatch(this.events);
		assertEquals(387, (long) processed.get("totalEdits"));
		assertEquals(2486, (long) processed.get("totalWeightedEdits"));
	}

	@Test
	@DisplayName("updateEarlyOften; integration test; without existing data")
	public void testInitialEarlyOften() {
		this.studentProject.updateEarlyOften(this.events);
		EarlyOften earlyOften = this.studentProject.getEarlyOften();
		assertEquals(387, earlyOften.getTotalEdits());
		assertEquals(2486, earlyOften.getTotalWeightedEdits());
		assertEquals(6.42, earlyOften.getScore(), 0.01);
	}
}
