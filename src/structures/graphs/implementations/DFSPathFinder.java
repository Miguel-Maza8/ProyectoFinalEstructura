package structures.graphs.implementations;

import java.util.LinkedHashSet;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;


public class DFSPathFinder<T> implements PathFinder<T> {

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {
        Set<T> visitados = new LinkedHashSet<>();
        LinkedHashSet<T> path = new LinkedHashSet<>();

        boolean encontrado = dfs(graph, start, end, visitados, path);

        if (encontrado) {
            return new PathResult<>(visitados, path);
        }
        return new PathResult<>(visitados, new LinkedHashSet<>());
    }

    private boolean dfs(Graph<T> graph, T actual, T end, Set<T> visitados, LinkedHashSet<T> path) {
        visitados.add(actual);
        path.add(actual);

        if (actual.equals(end)) {
            return true;
        }

        for (Node<T> vecino : graph.getVecinos(actual)) {
            T valorVecino = vecino.getValue();
            if (!visitados.contains(valorVecino)) {
                boolean encontrado = dfs(graph, valorVecino, end, visitados, path);
                if (encontrado) {
                    return true;
                }
            }
        }

        path.remove(actual);
        return false;
    }
}
