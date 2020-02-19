

public interface RoutableVertex<E> {

	E getElement();
	
	boolean isVisited();

	void setVisited(boolean value);

	double getDistance();

	double setDistance(double distance);

	RoutableVertex<E> getPredecessor();

	RoutableVertex<E> setPredecessor(RoutableVertex<E> vertex);

}
