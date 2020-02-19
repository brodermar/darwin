
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RoutableGraphImpl<E> implements RoutableGraph<E> {

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
