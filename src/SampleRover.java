import java.awt.Dimension;

/**
 * A sample rover to explore the features of the simulation.
 * 
 * @author Mark Broderius
 */
public class SampleRover extends Creature {

	public static final String AUTHOR = "Mark Broderius";
	public static final String DESCRIPTION = "A sample rover to explore the features of the simulation.";

	private int height;
	private int width;
	private int size;
	private Observation[] observations;
	private boolean[] visitedPositions;

	@Override
	public void run() {
		
		setUp();
		while (true) {
			updateObservations(observe());
			if (!moveForward()) {
				attack();
				turnLeft();
			}
		}
	}
	
	private void updateObservations(Observation[] observations) {
		
	}

	private void setUp() {
		Dimension dimension = getMapDimensions();
		height = (int) dimension.getHeight();
		width = (int) dimension.getWidth();
		size = height * width;
		observations = new Observation[size];
		visitedPositions = new boolean[size];
	}

	@Override
	public String getAuthorName() {
		return AUTHOR;
	}

	@Override
	public String getDescription() {
		return DESCRIPTION;
	}
}
