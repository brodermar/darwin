import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * A sample rover to explore the features of the simulation.
 * 
 * @author Mark Broderius
 */
public class SampleRover extends Creature {

	public static final String AUTHOR = "Mark Broderius";
	public static final String DESCRIPTION = "A sample rover to explore the features of the simulation.";

	// the game map, which will be revealed during the game by various instances of
	// the rover
	private static GameMap map;

	// the path get passed to the map by the rover for calculations of the dijkstra
	private Path<GameField> path;

	// the neighbors of the rover
	private GameField frontField;
	private GameField leftField;
	private GameField rightField;
	private GameField backField;

	// the trace of the rover
	private Trace trace;

	@Override
	public void run() {
		setUp();
		while (true) {
			map.updateObservations(observe());
			if (map.isTreasureKnown()) {
				moveToField(map.getTreasure());
			}
			System.out.println(map.print());
			uncover();
			if (trace.isVisited(frontField.getPosition()) || frontField.isNotVisitable()
					|| frontField.isFriend(getClassId())) {
				if (!moveToNextUnknown()) {
					turnToLeastVisitedField();
					if(!moveForwardAdvanced()) {
						rotateAround();
					}
				}
			} else {
				if(!moveForwardAdvanced()) {
					rotateAround();
				}
			}
		}
	}

	public Direction uncover() {
		Direction direction = getDirection();
		if (!rotateToNextUnknown()) {
			return direction;
		}
		while (true) {
			map.updateObservations(observe());
			if (map.isTreasureKnown()) {
				moveToField(map.getTreasure());
				return direction;
			}
			if (!rotateToNextUnknown()) {
				return direction;
			}
		}
	}

	private void moveToField(GameField target) {
		map.calcPathToTarget(path, getCurrentField(), target);
		List<GameField> elements = path.getElements();
		if (elements.size() < 2) {
			return;
		}
		for (int i = 1; i < elements.size(); i++) {
			GameField curField = elements.get(i);
			rotate(curField);
			map.updateObservations(observe());
			if (map.isTreasureKnown() && !target.equals(map.getTreasure())) {
				return;
			}
			if (!moveForwardAdvanced()) {
				return;
			}
		}
	}

	private boolean moveToNextUnknown() {
		if (!rotateToNextUnknown()) {
			map.calcPathToNextUnknown(path, map.getField(getPosition()));
			List<GameField> elements = path.getElements();
			if (elements.size() < 2) {
				return false;
			}
			for (int i = 1; i < elements.size(); i++) {
				rotate(elements.get(i));
				map.updateObservations(observe());
				if (map.isTreasureKnown()) {
					moveToField(map.getTreasure());
					return true;
				}
				if (!moveForwardAdvanced()) {
					return false;
				}
			}
			rotateToNextUnknown();
		}
		return true;
	}

	private boolean turnToLeastVisitedField() {
		GameField selected = null;
		if (frontField != null) {
			if (selected == null || (frontField.isVisitable() && !frontField.isFriend(getClassId())
					&& trace.getVisitedTimes(frontField) < trace.getVisitedTimes(selected))) {
				selected = frontField;
			}
		}
		if (leftField != null) {
			if (selected == null || (leftField.isVisitable() && !frontField.isFriend(getClassId())
					&& trace.getVisitedTimes(leftField) < trace.getVisitedTimes(selected))) {
				selected = leftField;
			}
		}
		if (rightField != null) {
			if (selected == null || (rightField.isVisitable() && !frontField.isFriend(getClassId())
					&& trace.getVisitedTimes(rightField) < trace.getVisitedTimes(selected))) {
				selected = rightField;
			}
		}
		if (backField != null) {
			if (selected == null || (backField.isVisitable() && !frontField.isFriend(getClassId())
					&& trace.getVisitedTimes(backField) < trace.getVisitedTimes(selected))) {
				selected = backField;
			}
		}
		return rotate(selected);
	}

	public boolean rotateToNextUnknown() {
		if (leftField.isUnknown()) {
			rotateLeft();
			return true;
		} else if (rightField.isUnknown()) {
			rotateRight();
			return true;
		} else if (backField.isUnknown()) {
			rotateAround();
			return true;
		}
		return false;
	}

	public void rotateLeft() {
		turnLeft();
		determineNeighbours();
	}

	public void rotateRight() {
		turnRight();
		determineNeighbours();
	}

	public void rotateAround() {
		turnLeft();
		turnLeft();
		determineNeighbours();
	}

	// returns the game field of the map of the current position
	public GameField getCurrentField() {
		return map.getField(getPosition());
	}

	// returns the game field of the map of the last position
	public GameField getLastField() {
		return trace.getLastPosition() != null ? map.getField(trace.getLastPosition()) : null;
	}

	// returns the neighbour GameField of the given direction and the current
	// position
	private GameField neighbour(Direction targetDirection) {
		return map.neighbour(getPosition(), targetDirection);
	}

	public boolean rotate(Direction targetDirection) {
		if (getDirection().left().equals(targetDirection)) {
			rotateLeft();
			return true;
		} else if (getDirection().right().equals(targetDirection)) {
			rotateRight();
			return true;
		} else if (getDirection().opposite().equals(targetDirection)) {
			rotateAround();
			return true;
		}
		return false;
	}

	public boolean rotate(GameField field) {
		return rotate(Direction.fromTo(getPosition(), field.getPosition()));
	}

	public boolean isNeighbour(GameField field) {
		return field.equals(frontField) || field.equals(leftField) || field.equals(backField)
				|| field.equals(rightField);
	}

	// returns the GameField in the front of the rover
	private GameField frontField() {
		return neighbour(getDirection());
	}

	// returns the GameField to the left of the rover
	private GameField leftField() {
		return neighbour(getDirection().left());
	}

	// returns the GameField to the right of the rover
	private GameField rightField() {
		return neighbour(getDirection().right());
	}

	// returns the GameField in the back of the rover
	private GameField backField() {
		return neighbour(getDirection().opposite());
	}

	boolean moveForwardAdvanced() {
		if (frontField.isEnemy(getClassId())) {
			attack();
			return false;
		}
		if (frontField.isFriend(getClassId())) {
			return false;
		}
//		boolean returnVal = moveForward();
		if (moveForward()) {
			updateWithCurrentPosition();
			return true;
		}
		return false;
	}

	private void updateWithCurrentPosition() {
		trace.addVisit(getPosition());
		determineNeighbours();
		map.updateObservation(new Observation(trace.getLastPosition(), getGameTime()));
	}

	// initializes a new map if necessary, creates a new trace for this creature and
	// updates it the first time
	private void setUp() {
		if (map == null) {
			map = new GameMap(getMapDimensions());
		}
		trace = new Trace(getMapDimensions(), getId());
		path = new Path<GameField>();
		trace.addVisit(getPosition());
		determineNeighbours();
	}

	private void determineNeighbours() {
		frontField = frontField();
		leftField = leftField();
		rightField = rightField();
		backField = backField();
	}

	public static class GameMap {

		public final UnknownNeighboursPredicate hasUnknownNeighboursPredicate;
		public final EqualityPredicate equalityPredicate;

		private RoutableGraph<GameField> graph;
		private Dijkstra<GameField> dijkstra;
		private int height;
		private int width;
		private GameField[][] fields;
		private GameField treasure;
		private Map<Point, RoutableVertex<GameField>> vertices;

		// constructs a new game map: initializes the game fields
		public GameMap(Dimension dimension) {
			height = (int) dimension.getHeight();
			width = (int) dimension.getWidth();
			fields = new GameField[width][height];
			vertices = new HashMap<>();
			graph = new RoutableGraphImpl<GameField>();
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					Point position = new Point(i, j);
					fields[i][j] = new GameField(position);
					vertices.put(position, graph.addVertex(fields[i][j]));
				}
			}
			dijkstra = new Dijkstra<>();
			dijkstra.setGraph(graph);
			hasUnknownNeighboursPredicate = new UnknownNeighboursPredicate();
			equalityPredicate = new EqualityPredicate(vertices.get(fields[0][0].getPosition()));
		}

		public Path<GameField> calcPathToTarget(Path<GameField> path, GameField startPosition,
				GameField targetPosition) {
			equalityPredicate.setTargetPosition(getVertex(targetPosition));
			dijkstra.setEscapeCondition(equalityPredicate);
			dijkstra.setStartPosition(getVertex(startPosition));
			path.load(dijkstra.calc());
			return path;
		}

		public Path<GameField> calcPathToNextUnknown(Path<GameField> path, GameField startPosition) {
			dijkstra.setEscapeCondition(hasUnknownNeighboursPredicate);
			dijkstra.setStartPosition(getVertex(startPosition));
			path.load(dijkstra.calc());
			return path;
		}

		public RoutableVertex<GameField> getVertex(GameField field) {
			return getVertex(field.getPosition());
		}

		public RoutableVertex<GameField> getVertex(Point position) {
			return vertices.get(position);
		}

		public boolean isTreasureKnown() {
			return treasure != null;
		}

		public GameField getTreasure() {
			return treasure;
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

		public GameField neighbour(GameField field, Direction targetDirection) {
			return neighbour(field.getPosition(), targetDirection);
		}

		// updates matching fields with the new observations
		public void updateObservations(Observation[] observations) {
			GameField lastField = null;
			for (Observation observation : observations) {
				GameField curField = updateObservation(observation);
				if (lastField != null && lastField.isVisitable() && curField.isVisitable()) {
					addEdge(lastField, curField, 1.0);
				}
				lastField = curField;
			}
		}

		private void addEdge(GameField field1, GameField field2, double weight) {
			RoutableVertex<GameField> first = vertices.get(field1.getPosition());
			RoutableVertex<GameField> second = vertices.get(field2.getPosition());
			if (!graph.containsEdge(first, second, weight)) {
				graph.addEdge(first, second, weight);
			}
		}

		// updates the field of the position given by the observation with the
		// observation
		public GameField updateObservation(Observation observation) {
			GameField field = getField(observation.position);
			field.updateObservation(observation);
			if (field.isTreasure()) {
				treasure = field;
			}
			return field;
		}

		public GameField getField(Point position) {
			return fields[position.x][position.y];
		}

		// returns the field above the given position or null: decrements the
		// x-coordinate if possible
		public GameField north(Point position) {
			return position.y > 0 ? fields[position.x][position.y - 1] : null;
		}

		public GameField north(GameField field) {
			return north(field.getPosition());
		}

		public boolean isNorthUnknown(GameField field) {
			GameField north = north(field);
			return north != null && north.isUnknown();
		}

		// returns the field under the given position or null: increments the
		// x-coordinate if possible
		public GameField south(Point position) {
			return position.y < height - 1 ? fields[position.x][position.y + 1] : null;
		}

		public GameField south(GameField field) {
			return south(field.getPosition());
		}

		public boolean isSouthUnknown(GameField field) {
			GameField south = south(field);
			return south != null && south.isUnknown();
		}

		// returns the field on the left side of the given position or null: decrements
		// the y-coordinate if possible
		public GameField west(Point position) {
			return position.x > 0 ? fields[position.x - 1][position.y] : null;
		}

		public GameField west(GameField field) {
			return west(field.getPosition());
		}

		public boolean isWestUnknown(GameField field) {
			GameField west = west(field);
			return west != null && west.isUnknown();
		}

		// return the field right to the given position or null: increments the
		// y-coordinate if possible
		public GameField east(Point position) {
			return position.x < width - 1 ? fields[position.x + 1][position.y] : null;
		}

		public GameField east(GameField field) {
			return east(field.getPosition());
		}

		public boolean isEastUnknown(GameField field) {
			GameField east = east(field);
			return east != null && east.isUnknown();
		}

		public RoutableGraph<GameField> getGraph() {
			return graph;
		}

		public boolean bordersUnknown(GameField field) {
			return isEastUnknown(field) || isNorthUnknown(field) || isSouthUnknown(field) || isWestUnknown(field);
		}

		// prints the map: returns a Strings
		public String print() {
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < height; i++) {
				for (int j = 0; j < width; j++) {
					builder.append(fields[j][i].print());
				}
				builder.append("  " + i);
				builder.append('\n');
			}
			return builder.toString();
		}

		private class UnknownNeighboursPredicate implements Predicate<RoutableVertex<GameField>> {

			@Override
			public boolean test(RoutableVertex<GameField> element) {
				return bordersUnknown(element.getElement());
			}

		}

		private class EqualityPredicate implements Predicate<RoutableVertex<GameField>> {

			private RoutableVertex<GameField> targetPosition;

			private EqualityPredicate(RoutableVertex<GameField> targetPosition) {
				if (targetPosition == null) {
					throw new NullPointerException("target position is not allowed to be null");
				}
				this.targetPosition = targetPosition;
			}

			@Override
			public boolean test(RoutableVertex<GameField> position) {
				return targetPosition.equals(position);
			}

			private RoutableVertex<GameField> setTargetPosition(RoutableVertex<GameField> targetPosition) {
				if (targetPosition == null) {
					throw new NullPointerException("target position is not allowed to be null");
				}
				RoutableVertex<GameField> old = this.targetPosition;
				this.targetPosition = targetPosition;
				return old;
			}

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
				this.symbol = Symbol.CREATURE;
				break;
			case EMPTY:
				this.symbol = Symbol.EMPTY;
				break;
			case HAZARD:
				this.symbol = Symbol.HAZARD;
				break;
			case WALL:
				this.symbol = Symbol.WALL;
				break;
			default:
				throw new NullPointerException("cannot identify type");
			}
		}

		public Observation getObservation() {
			return observation;
		}

		public boolean isEnemy(int referenceClassId) {
			Observation observation = getObservation();
			return observation != null
					? (observation.type == Type.CREATURE) && (observation.classId != referenceClassId)
					: false;
		}

		public boolean isFriend(int referenceClassId) {
			Observation observation = getObservation();
			return observation != null
					? (observation.type == Type.CREATURE) && (observation.classId == referenceClassId)
					: false;
		}

		public boolean isVisitable() {
			return !isHazard() && !isWall();
		}

		public boolean isNotVisitable() {
			return isHazard() || isWall();
		}

		// observation != null type == WALL
		public boolean isWall() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.WALL) : false;
		}

		// observation != null type == CREATURE
		public boolean isCreature() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.CREATURE) : false;
		}

		// observation != null type == HAZARD
		public boolean isHazard() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.HAZARD) : false;
		}

		public boolean isTreasure() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.CREATURE) && observation.classId == 10 : false;
		}

		public boolean isApple() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.CREATURE) && observation.classId == 11 : false;
		}

		public boolean isFlytrap() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.CREATURE) && observation.classId == 12 : false;
		}

		// observation != null && type == EMPTY
		public boolean isEmpty() {
			Observation observation = getObservation();
			return observation != null ? observation.type.equals(Type.EMPTY) : false;
		}

		// observation != null
		public boolean isUnknown() {
			Observation observation = getObservation();
			return observation == null;
		}

		// returns the symbol determinded by updateObservation(..)
		public char print() {
			return symbol.getSymbol();
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
		private Point currentPosition;
		private Point lastPosition;

		// constructs and initalizes a new trace for a creature id
		public Trace(Dimension dimension, int id) {
			this.id = id;
			int height = (int) dimension.getHeight();
			int width = (int) dimension.getWidth();
			fields = new TraceField[width][height];
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
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

	public interface RoutableVertex<E> {

		E getElement();

		boolean isVisited();

		void setVisited(boolean visited);

		Double getDistance();

		void setDistance(Double distance);

		RoutableVertex<E> getPredecessor();

		void setPredecessor(RoutableVertex<E> vertex);

	}

	public interface RoutableGraph<E> {

		RoutableVertex<E> addVertex(E element);

		void removeVertex(RoutableVertex<E> vertex);

		Set<RoutableVertex<E>> getVertices();

		boolean contains(RoutableVertex<E> vertex);

		boolean contains(RoutableEdge<E> edge);

		RoutableEdge<E> addEdge(RoutableVertex<E> firstVertex, RoutableVertex<E> secondVertex, double weight);

		boolean containsEdge(RoutableVertex<E> firstVertex, RoutableVertex<E> secondVertex, double weight);

		void removeEdge(RoutableEdge<E> edge);

		List<RoutableEdge<E>> getEdges(RoutableVertex<E> vertex);

		Set<RoutableEdge<E>> getEdges();

	}

	public interface RoutableEdge<E> {

		Double getWeight();

		void setWeight(Double weight);

		RoutableVertex<E> getFirstVertex();

		RoutableVertex<E> getSecondVertex();

		RoutableVertex<E> getOtherVertex(RoutableVertex<E> vertex);

	}

	public static class RoutableGraphImpl<E> implements RoutableGraph<E> {

		private Map<RoutableVertexImpl<E>, List<RoutableEdgeImpl<E>>> vertices;

		public RoutableGraphImpl() {
			vertices = new HashMap<>();
		}

		@Override
		public RoutableVertex<E> addVertex(E element) {
			RoutableVertexImpl<E> vertex = new RoutableVertexImpl<>(element);
			vertices.put(vertex, new ArrayList<RoutableEdgeImpl<E>>());
			return vertex;
		}

		@Override
		public void removeVertex(RoutableVertex<E> vertex) {
			RoutableVertexImpl<E> vertexImpl = validate(vertex);
			List<RoutableEdgeImpl<E>> edges = vertices.remove(vertexImpl);
			for (RoutableEdgeImpl<E> edge : edges) {
				RoutableVertexImpl<E> neighbourVertex = edge.getOtherVertexImpl(vertexImpl);
				vertices.get(neighbourVertex).remove(edge);
			}
//			return vertexImpl;
		}

		@Override
		public Set<RoutableVertex<E>> getVertices() {
			Set<RoutableVertex<E>> set = new HashSet<>();
			set.addAll(vertices.keySet());
			return set;
		}

		@Override
		public RoutableEdge<E> addEdge(RoutableVertex<E> firstVertex, RoutableVertex<E> secondVertex, double weight) {
			RoutableVertexImpl<E> first = validate(firstVertex);
			RoutableVertexImpl<E> second = validate(secondVertex);
			RoutableEdgeImpl<E> newEdge = new RoutableEdgeImpl<E>(first, second, weight);
			vertices.get(first).add(newEdge);
			vertices.get(second).add(newEdge);
			return newEdge;
		}

		public boolean containsEdge(RoutableVertex<E> firstVertex, RoutableVertex<E> secondVertex, double weight) {
			RoutableVertexImpl<E> first = validate(firstVertex);
			RoutableVertexImpl<E> second = validate(secondVertex);
			for (RoutableEdgeImpl<E> edge : vertices.get(first)) {
				if (edge.getOtherVertexImpl(first).equals(second) && edge.getWeight() == weight) {
					return true;
				}
			}
			return false;
		}

		@Override
		public void removeEdge(RoutableEdge<E> edge) {
			RoutableEdgeImpl<E> edgeImpl = validate(edge);
//			RoutableVertexImpl<E> firstVertex = edgeImpl.getFirstVertexImpl();
//			RoutableVertexImpl<E> secondVertex = edgeImpl.getSecondVertexImpl();
			vertices.get(edgeImpl.getFirstVertexImpl()).remove(edgeImpl);
			vertices.get(edgeImpl.getSecondVertexImpl()).remove(edgeImpl);
//			return edgeImpl;
		}

		@Override
		public Set<RoutableEdge<E>> getEdges() {
			Set<RoutableEdge<E>> set = new HashSet<>();
			for (List<RoutableEdgeImpl<E>> edges : vertices.values()) {
				set.addAll(edges);
			}
			return set;
		}

		@Override
		public List<RoutableEdge<E>> getEdges(RoutableVertex<E> vertex) {
			RoutableVertexImpl<E> vertexImpl = validate(vertex);
			return new ArrayList<RoutableEdge<E>>(vertices.get(vertexImpl));
		}

		@Override
		public boolean contains(RoutableVertex<E> vertex) {
			if (vertex instanceof RoutableVertexImpl) {
				return vertices.containsKey(vertex);
			}
			return false;
		}

		@Override
		public boolean contains(RoutableEdge<E> edge) {
			if (edge instanceof RoutableEdgeImpl && contains(edge.getFirstVertex())
					&& contains(edge.getSecondVertex())) {
				return vertices.get(edge.getFirstVertex()).contains(edge);
			}
			return false;
		}

		private RoutableVertexImpl<E> validate(RoutableVertex<E> vertex) {
			if (vertex instanceof RoutableVertexImpl) {
				return (RoutableVertexImpl<E>) vertex;
			} else {
				throw new IllegalArgumentException("the given vertex has no valid type");
			}
		}

		private RoutableEdgeImpl<E> validate(RoutableEdge<E> edge) {
			if (edge instanceof RoutableEdgeImpl) {
				return (RoutableEdgeImpl<E>) edge;
			} else {
				throw new IllegalArgumentException("the given edge has no valid type");
			}
		}

		private class RoutableEdgeImpl<T> implements RoutableEdge<T> {

			private Double weight;
			private RoutableVertexImpl<T> firstVertex;
			private RoutableVertexImpl<T> secondVertex;

			private RoutableEdgeImpl(RoutableVertexImpl<T> firstVertex, RoutableVertexImpl<T> secondVertex,
					double weight) {
				this.firstVertex = firstVertex;
				this.secondVertex = secondVertex;
				this.weight = weight;
			}

			@Override
			public Double getWeight() {
				return weight;
			}

			@Override
			public void setWeight(Double weight) {
//				double old = this.weight;
				this.weight = weight;
//				return old;
			}

			private RoutableVertexImpl<T> getOtherVertexImpl(RoutableVertexImpl<T> vertex) {
				if (firstVertex.equals(vertex)) {
					return secondVertex;
				} else if (secondVertex.equals(vertex)) {
					return firstVertex;
				} else {
					throw new IllegalArgumentException("the edge contains not the given vertex");
				}
			}

			private RoutableVertexImpl<T> getFirstVertexImpl() {
				return firstVertex;
			}

			private RoutableVertexImpl<T> getSecondVertexImpl() {
				return secondVertex;
			}

			@Override
			public RoutableVertex<T> getFirstVertex() {
				return firstVertex;
			}

			@Override
			public RoutableVertex<T> getSecondVertex() {
				return secondVertex;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("RoutableEdgeImpl [weight=");
				builder.append(weight);
				builder.append(", firstVertex=");
				builder.append(firstVertex);
				builder.append(", secondVertex=");
				builder.append(secondVertex);
				builder.append("]");
				return builder.toString();
			}

			@Override
			public RoutableVertex<T> getOtherVertex(RoutableVertex<T> vertex) {
				if (getFirstVertex().equals(vertex)) {
					return secondVertex;
				} else if (getSecondVertex().equals(vertex)) {
					return firstVertex;
				} else {
					throw new IllegalArgumentException("the edge contains not the given vertex");
				}
			}

		}

		private class RoutableVertexImpl<T> implements RoutableVertex<T> {

			private RoutableVertex<T> predecessor;
			private Double distance;
			private boolean visited;
			private T element;

			private RoutableVertexImpl(T element) {
				this.element = element;
				this.distance = Double.MAX_VALUE;
				this.visited = false;
			}

			@Override
			public T getElement() {
				return element;
			}

			@Override
			public boolean isVisited() {
				return visited;
			}

			@Override
			public void setVisited(boolean visited) {
				this.visited = visited;
			}

			@Override
			public Double getDistance() {
				return distance;
			}

			@Override
			public void setDistance(Double distance) {
//				double old = this.distance;
				this.distance = distance;
//				return old;
			}

			@Override
			public RoutableVertex<T> getPredecessor() {
				return predecessor;
			}

			@Override
			public void setPredecessor(RoutableVertex<T> vertex) {
//				RoutableVertex<T> old = predecessor;
				predecessor = vertex;
//				return old;
			}

			@Override
			public String toString() {
				StringBuilder builder = new StringBuilder();
				builder.append("RoutableVertexImpl [distance=");
				builder.append(distance);
				builder.append(", visited=");
				builder.append(visited);
				builder.append(", element=");
				builder.append(element);
				builder.append("]");
				return builder.toString();
			}

		}

	}

	public static class Path<E> {

		private List<E> elements;

		public Path() {
			elements = new ArrayList<>();
		}

		public void clear() {
			elements.clear();
		}

		public List<E> getElements() {
			return elements;
		}

		public void load(RoutableVertex<E> vertex) {
			elements.clear();
			elements.add(vertex.getElement());
			RoutableVertex<E> current = vertex.getPredecessor();
			while (current != null) {
				elements.add(0, current.getElement());
				current = current.getPredecessor();
			}
		}

	}

	public static class Dijkstra<E> {

		private RoutableGraph<E> graph;
		private RoutableVertex<E> startPosition;
		private RoutableVertex<E> currentPosition;
		private Predicate<RoutableVertex<E>> escapeCondition;
		private PriorityQueue<RoutableVertex<E>> uncalculatedPositions;
		private Double distance;

		public Dijkstra() {
			uncalculatedPositions = new PriorityQueue<RoutableVertex<E>>(new RoutableVertexComparator());
		}

		public RoutableVertex<E> calc() {
//			if (!isInitialized()) {
//				throw new IllegalArgumentException("the dijkstra was not fully initialised");
//			}
			for (RoutableVertex<E> vertex : graph.getVertices()) {
				vertex.setDistance(Double.MAX_VALUE);
				vertex.setVisited(false);
				vertex.setPredecessor(null);
			}
			startPosition.setDistance(0.0);
			currentPosition = null;
			uncalculatedPositions.clear();
			uncalculatedPositions.add(startPosition);
			while (!uncalculatedPositions.isEmpty()) {
				currentPosition = uncalculatedPositions.poll();
				if (escapeCondition.test(currentPosition)) {
					return currentPosition;
				}
				currentPosition.setVisited(true);
				List<RoutableEdge<E>> edges = graph.getEdges(currentPosition);
				RoutableVertex<E> neighbourVertex = null;
				for (RoutableEdge<E> edge : edges) {
					neighbourVertex = edge.getOtherVertex(currentPosition);
					if (!neighbourVertex.isVisited()) {
						distance = currentPosition.getDistance() + edge.getWeight();
						if (distance < neighbourVertex.getDistance()) {
							neighbourVertex.setDistance(distance);
							neighbourVertex.setPredecessor(currentPosition);
						}
						uncalculatedPositions.add(neighbourVertex);
					}
				}
			}
			return currentPosition;
		}

		public RoutableVertex<E> calc(RoutableGraph<E> graph, RoutableVertex<E> startPosition,
				Predicate<RoutableVertex<E>> escapeCondition) {
			this.graph = graph;
			this.startPosition = startPosition;
			this.escapeCondition = escapeCondition;
			return calc();
		}

		public void setEscapeCondition(Predicate<RoutableVertex<E>> escapeCondition) {
//			Predicate<RoutableVertex<E>> old = this.escapeCondition;
			this.escapeCondition = escapeCondition;
//			return old;
		}

		public void setStartPosition(RoutableVertex<E> startPosition) {
//			RoutableVertex<E> old = this.startPosition;
			this.startPosition = startPosition;
//			return old;
		}

		public void setGraph(RoutableGraph<E> graph) {
//			RoutableGraph<E> old = this.graph;
			this.graph = graph;
//			return old;
		}

//		public boolean isInitialized() {
//			return startPosition != null && graph != null && graph.contains(startPosition) && escapeCondition != null;
//		}

		private class RoutableVertexComparator implements Comparator<RoutableVertex<E>> {

			@Override
			public int compare(RoutableVertex<E> o1, RoutableVertex<E> o2) {
				return Double.compare(o1.getDistance(), o2.getDistance());
			}

		}

	}

	@FunctionalInterface
	public static interface Predicate<E> {

		public boolean test(E element);

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
