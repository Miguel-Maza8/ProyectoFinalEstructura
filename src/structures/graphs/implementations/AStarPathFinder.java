package structures.graphs.implementations;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import structures.graphs.Graph;
import structures.graphs.Heuristic;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;

public class AStarPathFinder<T> implements PathFinder<T> {

    private final Heuristic<T> heuristica;

    public AStarPathFinder(Heuristic<T> heuristica) {
        this.heuristica = heuristica;
    }

    @Override
    public PathResult<T> find(Graph<T> graph, T start, T end) {
        Map<T, Double> costoAcumulado = new HashMap<>();
        Map<T, T> predecesores = new HashMap<>();
        Set<T> ordenVisita = new LinkedHashSet<>();
        Set<T> cerrados = new HashSet<>();

        PriorityQueue<T> frontera = new PriorityQueue<>(
                (a, b) -> Double.compare(f(a, costoAcumulado, end), f(b, costoAcumulado, end)));

        costoAcumulado.put(start, 0.0);
        predecesores.put(start, null);
        frontera.add(start);

        while (!frontera.isEmpty()) {
            T actual = frontera.poll();

            if (cerrados.contains(actual)) {
                continue;
            }
            cerrados.add(actual);
            ordenVisita.add(actual);

            if (actual.equals(end)) {
                return new PathResult<>(ordenVisita, buildPath(predecesores, end));
            }

            for (Node<T> vecino : graph.getVecinos(actual)) {
                T valorVecino = vecino.getValue();
                if (cerrados.contains(valorVecino)) {
                    continue;
                }

                double nuevoCosto = costoAcumulado.get(actual) + 1.0;
                Double costoPrevio = costoAcumulado.get(valorVecino);

                if (costoPrevio == null || nuevoCosto < costoPrevio) {
                    costoAcumulado.put(valorVecino, nuevoCosto);
                    predecesores.put(valorVecino, actual);
                    frontera.add(valorVecino);
                }
            }
        }

        return new PathResult<>(ordenVisita, new LinkedHashSet<>());
    }

    private double f(T nodo, Map<T, Double> costoAcumulado, T end) {
        double g = costoAcumulado.getOrDefault(nodo, Double.MAX_VALUE);
        double h = heuristica.estimar(nodo, end);
        return g + h;
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