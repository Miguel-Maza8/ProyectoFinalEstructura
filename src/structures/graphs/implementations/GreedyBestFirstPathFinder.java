package structures.graphs.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.Heuristic;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;


public class GreedyBestFirstPathFinder<T> implements PathFinder<T> {

    private final Heuristic<T> heuristica;

    public GreedyBestFirstPathFinder(Heuristic<T> heuristica) {
        this.heuristica = heuristica;
    }

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {
        PriorityQueue<T> frontera = new PriorityQueue<>(
                (a, b) -> Double.compare(heuristica.estimar(a, end), heuristica.estimar(b, end)));

        Set<T> visitados = new HashSet<>();
        Set<T> ordenVisita = new LinkedHashSet<>();
        Map<T, T> predecesores = new HashMap<>();

        frontera.add(start);
        visitados.add(start);
        predecesores.put(start, null);

        while (!frontera.isEmpty()) {
            T actual = frontera.poll();
            ordenVisita.add(actual);

            if (actual.equals(end)) {
                return new PathResult<>(ordenVisita, buildPath(predecesores, end));
            }

            for (Node<T> vecino : graph.getVecinos(actual)) {
                T valorVecino = vecino.getValue();
                if (!visitados.contains(valorVecino)) {
                    visitados.add(valorVecino);
                    predecesores.put(valorVecino, actual);
                    frontera.add(valorVecino);
                }
            }
        }

        return new PathResult<>(ordenVisita, new LinkedHashSet<>());
    }

    private Set<T> buildPath(Map<T, T> predecesores, T end) {
        java.util.LinkedList<T> pathInverso = new java.util.LinkedList<>();
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
