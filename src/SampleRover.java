import java.awt.Dimension;
import java.awt.Point;
import java.util.Random;

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
			Observation[] observations = observe();
			map.updateObservations(observations);
			System.out.println(map.print());

			// new version
			GameField forwardField = front();
			if (trace.isVisited(forwardField.getPosition()) || forwardField.isWall() || forwardField.isHazard()) {
				// should decide
				GameField leftField = left();
				GameField rightField = right();
				GameField backField = back();
				if (!turnToNextUnknown(leftField, rightField, backField)) {
					// no unknown field left
					if (!turnToLeastVisitedField(forwardField, leftField, rightField, backField)) {
						// front is least visited field
						if (forwardField.isCreature()) {
							attack();
						}
						moveForwardAdvanced();
					}
				}
				// GameField leftField = left();
				// if (leftField.isUnknown() || !trace.isVisited(leftField.getPosition())) {
				// turnLeft();
				// } else {
				// GameField rightField = right();
				// if (rightField.isUnknown() || !trace.isVisited(rightField.getPosition())) {
				// turnRight();
				// } else {
				// boolean turned = false;
				// // TODO add functionality
				// if (forwardField.isWall() || forwardField.isHazard()) {
				// boolean rightFieldUnknown = rightField.isUnknown();
				// boolean leftFieldUnknown = leftField.isUnknown();
				// if (rightFieldUnknown && leftFieldUnknown) {
				// turnLeft(); //TODO
				// } else if (rightFieldUnknown) {
				// turnRight();
				// turned = true;
				// } else if (leftFieldUnknown) {
				// turnLeft();
				// turned = true;
				// } else {
				//
				// }
				// } else {
				//
				// }
				// // if (turnToLessVisitedField(forwardField, leftField, rightField, back())) {
				// // moveForwardAdvanced();
				// // }
				// // old code
				// // if (forwardField.isWall() || forwardField.isHazard()) {
				// // turnLeft();
				// // } else {
				// // moveForwardAdvanced();
				// // }
				// }
				// }
			} else {
				// can go forward
				if (forwardField.isCreature()) {
					attack();
				}
				moveForwardAdvanced();
			}

			// // simple version
			// if (observations.length >= 1 && trace.isVisited(observations[1].position)) {
			// turnLeft();
			// }
			// if (!moveForwardAdvanced()) {
			// attack();
			// turnLeft();
			// }
		}
	}

	private boolean turnToLeastVisitedField(GameField forwardField, GameField leftField, GameField rightField,
			GameField backField) {
		boolean forwardFieldPossible = forwardField.isEmpty() || forwardField.isCreature();
		boolean leftFieldPossible = leftField.isEmpty() || leftField.isCreature();
		boolean rightFieldPossible = rightField.isEmpty() || rightField.isCreature();
		boolean backFieldPossible = backField.isEmpty() || backField.isCreature();
		boolean turned = false;
		if (forwardFieldPossible && leftFieldPossible && rightFieldPossible && backFieldPossible) {

		} else if (forwardFieldPossible && leftFieldPossible && rightFieldPossible) {

		} else if (leftFieldPossible && rightFieldPossible && backFieldPossible) {

		} else if (forwardFieldPossible && leftFieldPossible && backFieldPossible) {

		} else if (forwardFieldPossible && rightFieldPossible && backFieldPossible) {

		} else if (forwardFieldPossible && leftFieldPossible) {

		} else if (leftFieldPossible && rightFieldPossible) {

		} else if (rightFieldPossible && backFieldPossible) {

		} else if (backFieldPossible && forwardFieldPossible) {

		} else if (forwardFieldPossible && rightFieldPossible) {

		} else if (leftFieldPossible && backFieldPossible) {

		} else if (forwardFieldPossible) {

		} else if (leftFieldPossible) {

		} else if (rightFieldPossible) {

		} else if (backFieldPossible) {

		} else {
			throw new IllegalStateException("should not be possible");
		}
	}

	private boolean turnToNextUnknown(GameField leftField, GameField rightField, GameField backField) {
		if (leftField.isUnknown()) {
			if (rightField.isUnknown()) {
				turnLeft(); // TODO
			} else if (backField.isUnknown()) {
				turnLeft(); // TODO
			} else {
				turnLeft();
			}
			return true;
		} else if (rightField.isUnknown()) {
			if (backField.isUnknown()) {
				turnRight(); // TODO
			} else {
				turnRight();
			}
			return true;
		} else if (backField.isUnknown()) {
			if (rightField.isUnknown()) {
				turnRight(); // TODO
			} else {
				turnRight();
				turnRight();
			}
			return true;
		}
		return false;
	}

	// returns the game field of the map of the current position
	public GameField getCurrentGameField() {
		return map.getField(getPosition());
	}

	// returns the game field of the map of the last position
	public GameField getLastGameField() {
		return trace.getLastPosition() != null ? map.getField(trace.getLastPosition()) : null;
	}

	// updates the trace with the current position
	public void updateTrace() {
		trace.addVisit(getPosition());
	}

	// returns the neighbour GameField of the given direction and the current
	// position
	public GameField neighbour(Direction targetDirection) {
		return map.neighbour(getPosition(), targetDirection);
	}

	public boolean rotate(Direction targetDirection) {
		Direction direction = getDirection();
		if (direction.left().equals(targetDirection)) {
			turnLeft();
			return true;
		} else if (direction.right().equals(targetDirection)) {
			turnRight();
			return true;
		} else if (direction.opposite().equals(targetDirection)) {
			turnLeft();
			turnLeft();
			return true;
		}
		return false;
	}

	// returns the GameField in the front of the rover
	public GameField front() {
		return neighbour(getDirection());
	}

	// returns the GameField to the left of the rover
	public GameField left() {
		return neighbour(getDirection().left());
	}

	// returns the GameField to the right of the rover
	public GameField right() {
		return neighbour(getDirection().right());
	}

	// returns the GameField in the back of the rover
	public GameField back() {
		return neighbour(getDirection().opposite());
	}

	boolean moveForwardAdvanced() {
		boolean returnVal = moveForward();
		if (returnVal) {
			updateTrace();
			map.updateObservation(new Observation(trace.getLastPosition(), getGameTime()));
		}
		return returnVal;
	}

	// initializes a new map if necessary, creates a new trace for this creature and
	// updates it the first time
	private void setUp() {
		if (map == null) {
			map = new GameMap(getMapDimensions());
		}
		trace = new Trace(getMapDimensions(), getId());
		updateTrace();
	}

	public static class NeighbourField {

		private Point position;
		private Direction direction;

		private NeighbourField(Point position, Direction direction) {
			this.position = position;
			this.direction = direction;
		}

		Point getPosition() {
			return position;
		}

		Direction getDirection() {
			return direction;
		}

	}

	public static class GameMap {

		private int height;
		private int width;
		private GameField[][] fields;

		// constructs a new game map: initializes the game fields
		public GameMap(Dimension dimension) {
			height = (int) dimension.getHeight();
			width = (int) dimension.getWidth();
			fields = new GameField[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					fields[i][j] = new GameField(new Point(i, j));
				}
			}
		}

		public GameField neighbour(Point position, Direction targetDirection) {
			switch (targetDirection) {
			case EAST:
				return east(position);
			case NORTH:
				return north(position);
			case SOUTH:
				return south(position);
			case WEST:
				return west(position);
			default:
				throw new NullPointerException("should not be the case");
			}
		}

		// updates matching fields with the new observations
		public void updateObservations(Observation[] observations) {
			for (Observation observation : observations) {
				updateObservation(observation);
			}
		}

		// updates the field of the position given by the observation with the
		// observation
		public void updateObservation(Observation observation) {
			GameField field = getField(observation.position);
			field.updateObservation(observation);
		}

		public GameField getField(Point position) {
			return fields[position.x][position.y];
		}

		// returns the field above the given position or null: decrements the
		// x-coordinate if possible
		public GameField north(Point position) {
			return position.y > 0 ? fields[position.x][position.y - 1] : null;
		}

		// returns the field under the given position or null: increments the
		// x-coordinate if possible
		public GameField south(Point position) {
			return position.y < height - 1 ? fields[position.x][position.y + 1] : null;
		}

		// returns the field on the left side of the given position or null: decrements
		// the y-coordinate if possible
		public GameField west(Point position) {
			return position.x > 0 ? fields[position.x - 1][position.y] : null;
		}

		// return the field right to the given position or null: increments the
		// y-coordinate if possible
		public GameField east(Point position) {
			return position.x < width - 1 ? fields[position.x + 1][position.y] : null;
		}

		// prints the map: returns a Strings
		public String print() {
			StringBuilder builder = new StringBuilder();
			// for (int i = 0; i < width; i++) {
			// builder.append(i);
			// }
			// builder.append('\n');
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					builder.append(fields[j][i].print());
				}
				builder.append("  " + i);
				builder.append('\n');
			}
			return builder.toString();
		}

	}

	public static class GameField {

		private Observation observation;
		private Point position;
		private Symbol symbol;

		// constructs a new game field with the symbol UNKNOWN
		public GameField(Point position) {
			this.position = position;
			symbol = Symbol.UNKOWN;
		}

		// sets the observation of the field and updates the field symbol depending on
		// the typ of the observation
		private void updateObservation(Observation observation) {
			if (!observation.position.equals(position)) {
				throw new IllegalArgumentException("position does not match");
			}
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

		// observation != null type == WALL
		public boolean isWall() {
			return observation != null ? observation.type.equals(Type.WALL) : false;
		}

		// observation != null type == CREATURE
		public boolean isCreature() {
			return observation != null ? observation.type.equals(Type.CREATURE) : false;
		}

		// observation != null type == HAZARD
		public boolean isHazard() {
			return observation != null ? observation.type.equals(Type.HAZARD) : false;
		}

		// observation != null && type == EMPTY
		public boolean isEmpty() {
			return observation != null ? observation.type.equals(Type.EMPTY) : false;
		}

		// observation != null
		public boolean isUnknown() {
			return observation == null;
		}

		private Observation getObservation() {
			return observation;
		}

		// returns the symbol determinded by updateObservation(..)
		public char print() {
			return symbol.getSymbol();
		}

		// sets the symbol of this field
		private void setSymbol(Symbol symbol) {
			this.symbol = symbol;
		}

		// returns the position of the observation
		public Point getPosition() {
			return position;
		}

	}

	public static enum Symbol {

		UNKOWN('?'), CREATURE('C'), EMPTY(' '), HAZARD('H'), WALL('#');

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
		private TraceField[][] fields;
		// private boolean[][] fields;
		private Point currentPosition;
		private Point lastPosition;

		// constructs and initalizes a new trace for a creature id
		public Trace(Dimension dimension, int id) {
			this.id = id;
			int height = (int) dimension.getHeight();
			int width = (int) dimension.getWidth();
			fields = new TraceField[width][height];
			// fields = new boolean[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					// fields[i][j] = false;
					fields[i][j] = new TraceField();
				}
			}
		}

		public int getVisitedTimes(GameField field) {
			return getVisitedTimes(field.getPosition());
		}

		public int getVisitedTimes(Point position) {
			return fields[position.x][position.y].getVisitedTimes();
		}

		public boolean isVisited(GameField field) {
			return isVisited(field.getPosition());
		}

		// returns the value of the field of the given position
		public boolean isVisited(Point position) {
			return fields[position.x][position.y].isVisited();
		}

		// sets the field of the given position to true, updates the current position to
		// the given one and sets the old position to the field "lastPosition"
		public void addVisit(Point position) {
			fields[position.x][position.y].setVisited();
			lastPosition = currentPosition;
			currentPosition = position;
		}

		public void addVisit(GameField field) {
			addVisit(field.getPosition());
		}

		// returns the current position, this means the last position added by
		// "setCurrentPosition()"
		public Point getCurrentPosition() {
			return currentPosition;
		}

		// returns the last position, this means the last before position before the
		// last position added by "setCurrentPosition()"
		public Point getLastPosition() {
			return lastPosition;
		}

	}

	public static class TraceField {

		private boolean visited;
		private int visitedTimes;

		public TraceField() {
			visited = false;
			visitedTimes = 0;
		}

		void setVisited() {
			this.visited = true;
			visitedTimes++;
		}

		boolean isVisited() {
			return visited;
		}

		int getVisitedTimes() {
			return visitedTimes;
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
