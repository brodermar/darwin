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

	private static GameMap map;

	@Override
	public void run() {
		setUp();
		while (true) {
			map.updateObservations(observe());
			System.out.println(map.print());
			if (!moveForward()) {
				attack();
				turnLeft();
			}
		}
	}

	private void setUp() {
		if (map == null) {
			map = new GameMap(getMapDimensions());
		}
	}

	public static class GameMap {

		private int height;
		private int width;
		private GameField[][] fields;

		public GameMap(Dimension dimension) {
			height = (int) dimension.getHeight();
			width = (int) dimension.getWidth();
			fields = new GameField[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					fields[i][j] = new GameField();
				}
			}
		}

		public void updateObservations(Observation[] observations) {
			for (Observation observation : observations) {
				updateObservation(observation);
			}
		}

		private void updateObservation(Observation observation) {
			GameField field = getField(observation.position);
			field.updateObservation(observation);
		}

		public GameField getField(Point position) {
			return fields[position.y - 1][position.x - 1];
		}

		public String print() {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					builder.append(fields[i][j].print());
				}
				builder.append('\n');
			}
			return builder.toString();
		}

	}

	public static class GameField {

		private Observation observation;

		public GameField() {

		}

		private void updateObservation(Observation observation) {
			this.observation = observation;
		}

		private Observation getObservation() {
			return observation;
		}

		public char print() {
			if (observation == null) {
				return ' ';
			}
			switch (observation.type) {
			case CREATURE:
				return 'c';
			case EMPTY:
				return ' ';
			case HAZARD:
				return 'h';
			case WALL:
				return 'X';
			default:
				throw new NullPointerException("cannot identify type");
			}
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
