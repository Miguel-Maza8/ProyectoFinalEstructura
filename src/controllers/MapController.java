package controllers;

import java.util.ArrayList;
import java.util.List;

import models.MapPoint;
import persistence.GraphRepository;
import structures.graphs.Graph;
import structures.graphs.Heuristic;
import structures.graphs.PathFinder;
import structures.graphs.PathResult;
import structures.graphs.implementations.AStarPathFinder;
import structures.graphs.implementations.BFSPathFinder;
import structures.graphs.implementations.DFSPathFinder;
import structures.graphs.implementations.GreedyBestFirstPathFinder;
import structures.node.Node;

public class MapController {

    private final Graph<MapPoint> graph;
    private final GraphRepository repository;

    private final PathFinder<MapPoint> bfsPathFinder = new BFSPathFinder<>();
    private final PathFinder<MapPoint> dfsPathFinder = new DFSPathFinder<>();

 
    private final Heuristic<MapPoint> heuristicaEuclidiana = (actual, destino) -> {
        double dx = actual.getX() - destino.getX();
        double dy = actual.getY() - destino.getY();
        return Math.sqrt(dx * dx + dy * dy);
    };

    private final PathFinder<MapPoint> greedyPathFinder = new GreedyBestFirstPathFinder<>(heuristicaEuclidiana);
    private final PathFinder<MapPoint> aStarPathFinder = new AStarPathFinder<>(heuristicaEuclidiana);

    public MapController(GraphRepository repository) {
        this.repository = repository;
        this.graph = repository.load();
    }

    public Graph<MapPoint> getGraph() {
        return graph;
    }

  
    public MapPoint findPoint(String id) {
        for (Node<MapPoint> node : graph.getNodes()) {
            if (node.getValue().getId().equals(id)) {
                return node.getValue();
            }
        }
        return null;
    }

  
    public List<String> getNodeIds() {
        List<String> ids = new ArrayList<>();
        for (Node<MapPoint> node : graph.getNodes()) {
            ids.add(node.getValue().getId());
        }
        return ids;
    }

    
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


    public boolean removeNode(String id) {
        MapPoint punto = findPoint(id);
        if (punto == null) {
            return false;
        }
        graph.remove(punto);
        persist();
        return true;
    }

 
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


    public boolean removeEdge(String idOrigen, String idDestino, boolean bidireccional) {
        MapPoint origen = findPoint(idOrigen);
        MapPoint destino = findPoint(idDestino);

        if (origen == null || destino == null) {
            return false;
        }

        if (bidireccional) {
            graph.removeEdge(origen, destino);
        } else {
            graph.removeEdgeUni(origen, destino);
        }
        persist();
        return true;
    }

    public PathResult<MapPoint> ejecutarBusqueda(String algoritmo, String startId, String endId) {
        MapPoint start = findPoint(startId);
        MapPoint end = findPoint(endId);

        if (start == null || end == null) {
            return null;
        }

        PathFinder<MapPoint> finder = seleccionarFinder(algoritmo);
        return finder.find(graph, start, end);
    }

    private PathFinder<MapPoint> seleccionarFinder(String algoritmo) {
        String clave = algoritmo == null ? "" : algoritmo.toUpperCase();

        if (clave.contains("DFS")) {
            return dfsPathFinder;
        }
        if (clave.contains("GREEDY") || clave.contains("VORAZ")) {
            return greedyPathFinder;
        }
        if (clave.contains("ASTAR") || clave.contains("A*")) {
            return aStarPathFinder;
        }
        return bfsPathFinder;
    }

    private void persist() {
        repository.save(graph);
    }
}
