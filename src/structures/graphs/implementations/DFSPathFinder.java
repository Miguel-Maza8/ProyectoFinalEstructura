package structures.graphs.implementations;

import java.util.LinkedHashSet;
import java.util.Set;
import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.node.Node;

/**
 * Implementacion de Busqueda en Profundidad (DFS) sobre Graph<T>, mediante
 * recursividad.
 *
 * Se mantiene un conjunto de visitados (para evitar ciclos y llamadas
 * recursivas infinitas) y una ruta actual (path) que se va construyendo
 * mientras la recursion avanza. Cuando una rama no conduce al destino,
 * se aplica retroceso (backtracking) eliminando el ultimo elemento
 * agregado al path antes de regresar false a la llamada anterior.
 */
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

        // Retroceso: esta rama no condujo al destino.
        path.remove(actual);
        return false;
    }
}
