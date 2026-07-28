package controllers;

import java.util.ArrayList;
import java.util.List;

import models.MapPoint;
import persistence.GraphRepository;
import structures.graphs.Graph;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.graphs.implementations.BFSPathFinder;
import structures.graphs.implementations.DFSPathFinder;
import structures.node.Node;

/**
 * Controlador de la aplicacion (patron MVC).
 *
 * Es el unico responsable de:
 *  - Recibir la interaccion del usuario proveniente de la vista.
 *  - Modificar el modelo (Graph<MapPoint>) al agregar/eliminar nodos y aristas.
 *  - Ejecutar los algoritmos de busqueda (BFS/DFS) a traves de PathFinder.
 *  - Persistir los cambios mediante GraphRepository.
 *
 * La vista (MainFrame/MapPanel) solo dibuja lo que el controlador le indica;
 * no conoce la implementacion de BFS/DFS ni el formato de persistencia.
 */
public class MapController {

    private final Graph<MapPoint> graph;
    private final GraphRepository repository;

    private final PathFinder<MapPoint> bfsPathFinder = new BFSPathFinder<>();
    private final PathFinder<MapPoint> dfsPathFinder = new DFSPathFinder<>();

    public MapController(GraphRepository repository) {
        this.repository = repository;
        this.graph = repository.load();
    }

    public Graph<MapPoint> getGraph() {
        return graph;
    }

    /**
     * Busca un punto existente por su identificador.
     * @return el MapPoint correspondiente, o null si no existe.
     */
    public MapPoint findPoint(String id) {
        for (Node<MapPoint> node : graph.getNodes()) {
            if (node.getValue().getId().equals(id)) {
                return node.getValue();
            }
        }
        return null;
    }

    /**
     * Devuelve los identificadores de todos los nodos registrados,
     * en el mismo orden en que fueron agregados al grafo.
     */
    public List<String> getNodeIds() {
        List<String> ids = new ArrayList<>();
        for (Node<MapPoint> node : graph.getNodes()) {
            ids.add(node.getValue().getId());
        }
        return ids;
    }

    /**
     * Agrega un nuevo punto (interseccion) al grafo en la posicion indicada.
     *
     * @return true si se agrego correctamente, false si el id ya existia,
     *         estaba vacio, o las coordenadas eran invalidas.
     */
    public boolean addNode(String id, int x, int y) {
        if (id == null || id.trim().isEmpty()) {
            return false;
        }
        if (findPoint(id) != null) {
            return false;
        }

        graph.add(new MapPoint(id, x, y));
        persist();
        return true;
    }

    /**
     * Elimina un nodo del grafo junto con todas sus conexiones.
     */
    public boolean removeNode(String id) {
        MapPoint punto = findPoint(id);
        if (punto == null) {
            return false;
        }
        graph.remove(punto);
        persist();
        return true;
    }

    /**
     * Crea una calle entre dos puntos existentes.
     *
     * @param bidireccional true para calle en ambos sentidos, false para
     *                      calle de un solo sentido (origen -> destino).
     * @return true si se creo la conexion, false si alguno de los puntos
     *         no existe o si origen y destino son el mismo punto.
     */
    public boolean addEdge(String idOrigen, String idDestino, boolean bidireccional) {
        MapPoint origen = findPoint(idOrigen);
        MapPoint destino = findPoint(idDestino);

        if (origen == null || destino == null || origen.equals(destino)) {
            return false;
        }

        if (bidireccional) {
            graph.addEdge(origen, destino);
        } else {
            graph.addEdgeUni(origen, destino);
        }
        persist();
        return true;
    }

    /**
     * Elimina la conexion (en ambos sentidos) entre dos puntos.
     */
    public boolean removeEdge(String idOrigen, String idDestino) {
        MapPoint origen = findPoint(idOrigen);
        MapPoint destino = findPoint(idDestino);

        if (origen == null || destino == null) {
            return false;
        }

        graph.removeEdge(origen, destino);
        persist();
        return true;
    }

    /**
     * Ejecuta el algoritmo indicado (BFS o DFS) entre startId y endId.
     * La logica de busqueda es identica sin importar el modo de
     * visualizacion elegido en la vista: eso solo afecta como se dibuja
     * el resultado, no como se calcula.
     *
     * @param algoritmo "BFS" o "DFS".
     */
    public PathResult<MapPoint> ejecutarBusqueda(String algoritmo, String startId, String endId) {
        MapPoint start = findPoint(startId);
        MapPoint end = findPoint(endId);

        if (start == null || end == null) {
            return null;
        }

        PathFinder<MapPoint> finder = "DFS".equalsIgnoreCase(algoritmo) ? dfsPathFinder : bfsPathFinder;
        return finder.find(graph, start, end);
    }

    private void persist() {
        repository.save(graph);
    }
}
