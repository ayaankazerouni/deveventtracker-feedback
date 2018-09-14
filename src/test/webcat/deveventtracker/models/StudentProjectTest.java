package test.webcat.deveventtracker.models;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.webcat.deveventtracker.models.SensorData;
import org.webcat.deveventtracker.models.StudentProject;

/**
 * @author Ayaan Kazerouni
 * @version 2018-09-13
 */
public class StudentProjectTest {
	private StudentProject studentProject;
	private SensorData[] events;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeEach
	public void setUp() throws Exception {
		this.studentProject = new StudentProject("123", "123", 1537055940000L);
		this.events = new SensorData[] {
				new SensorData(1536428003000L, 300, "FirstClass"), // September 8
				new SensorData(1536690345000L, 275, "FirstClass"), // September 11
				new SensorData(1536610046000L, 50, "SecondClass"), // September 10
				new SensorData(1536782783000L, 62, "SecondClass")  // September 12
		};
	}

	@Test
	public void testGetEditSizeForNewFile() {
		SensorData[] events = this.studentProject.getEditSizes(this.events);
		assertEquals(300, events[0].getEditSize());
		assertEquals(25, events[1].getEditSize());
	}
}
