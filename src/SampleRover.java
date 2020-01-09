import java.awt.Dimension;
import java.awt.Point;

/**
 * A sample rover to explore the features of the simulation.
 * 
 * @author Mark Broderius
 */
public class SampleRover extends Creature {

	public static final String AUTHOR = "Mark Broderius";
	public static final String DESCRIPTION = "A sample rover to explore the features of the simulation.";

	private GameMap map;

	@Override
	public void run() {
		setUp();
		while (true) {
			map.updateObservations(observe());
			if (!moveForward()) {
				attack();
				turnLeft();
			}
		}
	}

	private void setUp() {
		map = new GameMap(getMapDimensions());
	}

	public static class GameMap {

		private int height;
		private int width;
		private int size;
		private GameField[] fields;

		public GameMap(Dimension dimension) {
			height = (int) dimension.getHeight();
			width = (int) dimension.getWidth();
			size = height * width;
			fields = new GameField[size];
			for (int i = 0; i < fields.length; i++) {
				fields[i] = new GameField();
			}
		}

		public void updateObservations(Observation[] observations) {
			for (Observation observation : observations) {
				updateObservation(observation);
			}
		}

		private void updateObservation(Observation observation) {
			GameField field = getField(observation.position);
			field.setObservation(observation);
		}

		public GameField getField(Point position) {
			return fields[position(position.x, position.y)];
		}

		private int position(int xCoord, int yCoord) {
			return xCoord * width + yCoord;
		}

		private int xCoord(int position) {
			return position / width;
		}

		private int yCoord(int position) {
			return position % width;
		}

	}

	public static class GameField {

		private Observation observation;

		public GameField() {

		}

		private void setObservation(Observation observation) {
			this.observation = observation;
		}

		private Observation getObservation() {
			return observation;
		}

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
