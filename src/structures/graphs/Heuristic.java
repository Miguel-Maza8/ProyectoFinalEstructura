package structures.graphs;

@FunctionalInterface
public interface Heuristic<T> {

    double estimar(T actual, T destino);
    
}
