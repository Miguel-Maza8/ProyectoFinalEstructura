package structures.graphs;

import java.util.Set;

/**
 * Resultado de una busqueda de ruta (BFS o DFS) sobre el grafo.
 *
 * Contiene dos conjuntos ordenados (LinkedHashSet construido por cada
 * PathFinder) para conservar el orden de insercion:
 *  - visitados: orden en que el algoritmo recorrio los nodos.
 *  - path: secuencia de nodos que forman la ruta final desde el inicio
 *          hasta el destino (vacio si no existe ruta).
 */
public class PathResult<T> {

    private final Set<T> visitados;
    private final Set<T> path;

    public PathResult(Set<T> visitados, Set<T> path) {
        this.visitados = visitados;
        this.path = path;
    }

    public Set<T> getVisitados() {
        return visitados;
    }

    public Set<T> getPath() {
        return path;
    }

    public boolean encontroRuta() {
        return path != null && !path.isEmpty();
    }

    @Override
    public String toString() {
        return "PathResult [visitados=" + visitados + ", path=" + path + "]";
    }
}
