

public interface RoutableEdge<E> {

	double getWeight();

	double setWeight(double weight);

	RoutableVertex<E> getFirstVertex();

	RoutableVertex<E> getSecondVertex();

	RoutableVertex<E> getOtherVertex(RoutableVertex<E> vertex);
//	default RoutableVertex<E> getOhterVertex(RoutableVertex<E> vertex) {
//		if (getFirstVertex().equals(vertex)) {
//			return getSecondVertex();
//		} else if (getSecondVertex().equals(vertex)) {
//			return getFirstVertex();
//		} else {
//			throw new IllegalArgumentException("the edge contains not the given vertex");
//		}
//	}

}
