import java.awt.Dimension;
import java.awt.Point;
import java.util.Map;
import java.util.Set;

/**
 * A sample rover to explore the features of the simulation.
 * 
 * @author Mark Broderius
 */
public class SampleRover extends Creature {

	public static final String AUTHOR = "Mark Broderius";
	public static final String DESCRIPTION = "A sample rover to explore the features of the simulation.";

	private static GameMap map;

	private Trace trace;

	@Override
	public void run() {
		setUp();
		while (true) {
			System.out.println(map.print());
			Observation[] observations = observe();
			map.updateObservations(observations);
			if (trace.isVisited(observations[1].position)) {
				turnLeft();
			}
			if (!moveForwardAdvanced()) {
				attack();
				turnLeft();
			}
		}
	}

	boolean moveForwardAdvanced() {
		boolean returnVal = moveForward();
		if (returnVal) {
			trace.setVisited(getPosition());
			map.updateObservation(new Observation(trace.getLastPosition(), getGameTime()));
		}
		return returnVal;
	}

	private void setUp() {
		if (map == null) {
			map = new GameMap(getMapDimensions());
		}
		trace = new Trace(getMapDimensions(), getId());
		trace.setVisited(getPosition());
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

		public void updateObservation(Observation observation) {
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
		private Symbol symbol;

		public GameField() {
			symbol = Symbol.EMPTY;
		}

		private void updateObservation(Observation observation) {
			this.observation = observation;
			switch (observation.type) {
			case CREATURE:
				setSymbol(Symbol.CREATURE);
				break;
			case EMPTY:
				setSymbol(Symbol.EMPTY);
				break;
			case HAZARD:
				setSymbol(Symbol.HAZARD);
				break;
			case WALL:
				setSymbol(Symbol.WALL);
				break;
			default:
				throw new NullPointerException("cannot identify type");
			}
		}

		private Observation getObservation() {
			return observation;
		}

		public char print() {
			return symbol.getSymbol();
		}

		public void setSymbol(Symbol symbol) {
			this.symbol = symbol;
		}

	}

	public static enum Symbol {

		CREATURE('c'), EMPTY(' '), HAZARD('h'), WALL('X');

		private char symbol;

		private Symbol(char symbol) {
			this.symbol = symbol;
		}

		public char getSymbol() {
			return symbol;
		}
	}

	public static class Trace {

		int id;
		private boolean[][] fields;
		private Point currentPosition;
		private Point lastPosition;

		public Trace(Dimension dimension, int id) {
			this.id = id;
			int height = (int) dimension.getHeight();
			int width = (int) dimension.getWidth();
			fields = new boolean[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					fields[i][j] = false;
				}
			}
		}

		public boolean isVisited(Point position) {
			return fields[position.y - 1][position.x - 1];
		}

		public void setVisited(Point position) {
			fields[position.y - 1][position.x - 1] = true;
			lastPosition = currentPosition;
			currentPosition = position;
		}

		public Point getCurrentPosition() {
			return currentPosition;
		}

		public Point getLastPosition() {
			return lastPosition;
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
