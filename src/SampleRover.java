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

	private static GameMap map;
	private GameField frontField;
	private GameField leftField;
	private GameField rightField;
	private GameField backField;

	private Trace trace;

	@Override
	public void run() {
		setUp();
		while (true) {
			Observation[] observations = observe();
			map.updateObservations(observations);
			System.out.println(map.print());

			// new version
			if (trace.isVisited(frontField.getPosition()) || frontField.isWall() || frontField.isHazard()) {
				// should decide
				if (!rotateToNextUnknown()) {
					// no unknown field left
					if (!turnToLeastVisitedField()) {
						// front is least visited field
						moveForwardAdvanced();
					}
				}
			} else {
				// can go forward
				moveForwardAdvanced();
			}
		}
	}

	private boolean turnToLeastVisitedField() {
		int leastVisitedCount = frontField != null && frontField.isVisitable() ? trace.getVisitedTimes(frontField)
				: Integer.MAX_VALUE;
		Direction turnDirection = getDirection();
		int visitedTimes = 0;
		if (leftField != null && leftField.isVisitable()) {
			visitedTimes = trace.getVisitedTimes(leftField);
			if (visitedTimes < leastVisitedCount) {
				leastVisitedCount = visitedTimes;
				turnDirection = getDirection().left();
			}
		}
		if (rightField != null && rightField.isVisitable()) {
			visitedTimes = trace.getVisitedTimes(rightField);
			if (visitedTimes < leastVisitedCount) {
				leastVisitedCount = visitedTimes;
				turnDirection = getDirection().right();
			}
		}
		if (backField != null && backField.isVisitable()) {
			visitedTimes = trace.getVisitedTimes(backField);
			if (visitedTimes < leastVisitedCount) {
				turnDirection = getDirection().opposite();
			}
		}
		return rotate(turnDirection);
	}

	public boolean rotateToNextUnknown() {
		if (leftField.isUnknown()) {
			rotateLeft();
			return true;
		} else if (rightField.isUnknown()) {
			rotateRight();
			return true;
		} else if (backField.isUnknown()) {
			rotateBack();
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

	public void rotateBack() {
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

	// updates the trace with the current position
	private void updateTrace() {
		trace.addVisit(getPosition());
	}

	// returns the neighbour GameField of the given direction and the current
	// position
	private GameField neighbour(Direction targetDirection) {
		return map.neighbour(getPosition(), targetDirection);
	}

	public boolean rotate(Direction targetDirection) {
		Direction direction = getDirection();
		if (direction.left().equals(targetDirection)) {
			rotateLeft();
			return true;
		} else if (direction.right().equals(targetDirection)) {
			rotateRight();
			return true;
		} else if (direction.opposite().equals(targetDirection)) {
			rotateBack();
			return true;
		}
		return false;
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
		if (frontField.isCreature()) {
			attack();
		}
		boolean returnVal = moveForward();
		if (returnVal) {
			updateTrace();
			determineNeighbours();
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
		determineNeighbours();
	}

	private void determineNeighbours() {
		frontField = frontField();
		leftField = leftField();
		rightField = rightField();
		backField = backField();
	}

	public static class GameMap {

		private RoutableGraph<GameField> graph;
		private Map<Point, RoutableVertex<GameField>> vertices;
		private int height;
		private int width;
		private GameField[][] fields;

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

		public boolean isVisitable() {
			return !isHazard() && !isWall();
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

		double getDistance();

		double setDistance(double distance);

		RoutableVertex<E> getPredecessor();

		RoutableVertex<E> setPredecessor(RoutableVertex<E> vertex);
		
	}
	
	public interface RoutableGraph<E> {

		RoutableVertex<E> addVertex(E element);

		RoutableVertex<E> removeVertex(RoutableVertex<E> vertex);

		Set<RoutableVertex<E>> getVertices();

		boolean contains(RoutableVertex<E> vertex);

		boolean contains(RoutableEdge<E> edge);

		RoutableEdge<E> addEdge(RoutableVertex<E> firstVertex, RoutableVertex<E> secondVertex, double weight);

		RoutableEdge<E> removeEdge(RoutableEdge<E> edge);

		List<RoutableEdge<E>> getEdges(RoutableVertex<E> vertex);

		Set<RoutableEdge<E>> getEdges();

	}
	
	public interface RoutableEdge<E> {

		double getWeight();

		double setWeight(double weight);

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
		public RoutableVertex<E> removeVertex(RoutableVertex<E> vertex) {
			RoutableVertexImpl<E> vertexImpl = validate(vertex);
			List<RoutableEdgeImpl<E>> edges = vertices.remove(vertexImpl);
			for (RoutableEdgeImpl<E> edge : edges) {
				RoutableVertexImpl<E> neighbourVertex = edge.getOtherVertexImpl(vertexImpl);
				vertices.get(neighbourVertex).remove(edge);
			}
			return vertexImpl;
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

		@Override
		public RoutableEdge<E> removeEdge(RoutableEdge<E> edge) {
			RoutableEdgeImpl<E> edgeImpl = validate(edge);
			RoutableVertexImpl<E> firstVertex = edgeImpl.getFirstVertexImpl();
			RoutableVertexImpl<E> secondVertex = edgeImpl.getSecondVertexImpl();
			vertices.get(firstVertex).remove(edgeImpl);
			vertices.get(secondVertex).remove(edgeImpl);
			return edgeImpl;
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
			return vertex instanceof RoutableVertexImpl;
		}

		@Override
		public boolean contains(RoutableEdge<E> edge) {
			return edge instanceof RoutableEdgeImpl;
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

			private double weight;
			private RoutableVertexImpl<T> firstVertex;
			private RoutableVertexImpl<T> secondVertex;

			private RoutableEdgeImpl(RoutableVertexImpl<T> firstVertex, RoutableVertexImpl<T> secondVertex, double weight) {
				this.firstVertex = firstVertex;
				this.secondVertex = secondVertex;
				this.weight = weight;
			}

			@Override
			public double getWeight() {
				return weight;
			}

			@Override
			public double setWeight(double weight) {
				double old = this.weight;
				this.weight = weight;
				return old;
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
			private double distance;
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
			public double getDistance() {
				return distance;
			}

			@Override
			public double setDistance(double distance) {
				double old = this.distance;
				this.distance = distance;
				return old;
			}

			@Override
			public RoutableVertex<T> getPredecessor() {
				return predecessor;
			}

			@Override
			public RoutableVertex<T> setPredecessor(RoutableVertex<T> vertex) {
				RoutableVertex<T> old = predecessor;
				predecessor = vertex;
				return old;
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
	
	public static class Dijkstra<E> {

		private RoutableGraph<E> graph;
		private RoutableVertex<E> start;
		private RoutableVertex<E> target;
		private PriorityQueue<RoutableVertex<E>> queue;

		public Dijkstra() {
			queue = new PriorityQueue<RoutableVertex<E>>(new RoutableVertexComparator());
		}

		private void calcShortestPath() {
			for (RoutableVertex<E> vertex : graph.getVertices()) {
				vertex.setDistance(Double.MAX_VALUE);
				vertex.setVisited(false);
			}
			start.setDistance(0.0);
			queue.clear();
			queue.add(start);
			double distance;
			while (!queue.isEmpty()) {
				RoutableVertex<E> curVertex = queue.poll();
				if (target != null && target.equals(curVertex)) {
					break;
				}
				curVertex.setVisited(true);
				List<RoutableEdge<E>> edges = graph.getEdges(curVertex);
				for (RoutableEdge<E> edge : edges) {
					RoutableVertex<E> neighbourVertex = edge.getOtherVertex(curVertex);
					if (!neighbourVertex.isVisited()) {
						distance = curVertex.getDistance() + edge.getWeight();
						if (distance < neighbourVertex.getDistance()) {
							neighbourVertex.setDistance(distance);
							neighbourVertex.setPredecessor(curVertex);
						}
						queue.add(neighbourVertex);
					}
				}
			}
		}

		public RoutableVertex<E> calcShortestPath(RoutableGraph<E> graph, RoutableVertex<E> start,
				RoutableVertex<E> target) {
			initialize(graph, start, target);
			calcShortestPath();
			return target;
		}

		private void initialize(RoutableGraph<E> graph, RoutableVertex<E> start, RoutableVertex<E> target) {
			if (start == null) {
				throw new NullPointerException("start is null");
			}
			if (target == null) {
				throw new NullPointerException("target is null");
			}
			if (!graph.contains(start) || !graph.contains(target)) {
				throw new IllegalArgumentException("graph does not contain start or target");
			}
			this.graph = graph;
			this.start = start;
			this.target = target;
		}

		private class RoutableVertexComparator implements Comparator<RoutableVertex<E>> {

			@Override
			public int compare(RoutableVertex<E> o1, RoutableVertex<E> o2) {
				return Double.compare(o1.getDistance(), o2.getDistance());
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
