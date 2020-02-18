

import java.util.List;
import java.util.Set;

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
