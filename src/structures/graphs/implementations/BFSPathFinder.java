package structures.graphs.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;


public class BFSPathFinder<T> implements PathFinder<T> {

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {
        Queue<T> queue = new LinkedList<>();
        Set<T> visitados = new HashSet<>();
        Set<T> ordenVisita = new LinkedHashSet<>();
        Map<T, T> predecesores = new HashMap<>();

        queue.add(start);
        visitados.add(start);
        predecesores.put(start, null);

        while (!queue.isEmpty()) {
            T actual = queue.poll();
            ordenVisita.add(actual);

            if (actual.equals(end)) {
                return new PathResult<>(ordenVisita, buildPath(predecesores, end));
            }

            for (Node<T> vecino : graph.getVecinos(actual)) {
                T valorVecino = vecino.getValue();
                if (!visitados.contains(valorVecino)) {
                    visitados.add(valorVecino);
                    predecesores.put(valorVecino, actual);
                    queue.add(valorVecino);
                }
            }
        }

        
        return new PathResult<>(ordenVisita, new LinkedHashSet<>());
    }

    private Set<T> buildPath(Map<T, T> predecesores, T end) {
        LinkedList<T> pathInverso = new LinkedList<>();
        T actual = end;
        while (actual != null) {
            pathInverso.addFirst(actual);
            actual = predecesores.get(actual);
        }

        Set<T> path = new LinkedHashSet<>();
        path.addAll(pathInverso);
        return path;
    }
}
