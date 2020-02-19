
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Dijkstra<E> {

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
