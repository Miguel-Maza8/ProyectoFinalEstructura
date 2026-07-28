package structures.graphs;

/**
 * Contrato comun para los algoritmos de busqueda de rutas sobre un Graph<T>.
 * Tanto BFSPathFinder como DFSPathFinder implementan esta interfaz, de modo
 * que el controlador pueda ejecutar cualquiera de los dos sin conocer los
 * detalles internos del algoritmo (polimorfismo).
 */
public interface PathFinder<T> {

    /**
     * Busca una ruta entre start y end dentro de graph.
     *
     * @param graph grafo sobre el cual se realiza la busqueda.
     * @param start punto de inicio (A).
     * @param end   punto de destino (B).
     * @return un PathResult con el orden de nodos visitados y la ruta
     *         encontrada (vacia si no existe conexion entre start y end).
     */
    PathResult<T> find(Graph<T> graph, T start, T end);
}