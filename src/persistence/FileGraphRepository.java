package persistence;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import models.MapPoint;
import structures.graphs.Graph;
import structures.node.Node;


public class FileGraphRepository implements GraphRepository {

    private final String filePath;

    public FileGraphRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public Graph<MapPoint> load() {
        Graph<MapPoint> graph = new Graph<>();
        Path path = Path.of(filePath);

        if (!Files.exists(path)) {
            
            return graph;
        }

        Map<String, MapPoint> puntosPorId = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String linea;
            int numeroLinea = 0;

            while ((linea = reader.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] campos = linea.split(",");

                if (campos[0].equalsIgnoreCase("NODE")) {
                    procesarNodo(campos, numeroLinea, graph, puntosPorId);
                } else if (campos[0].equalsIgnoreCase("EDGE")) {
                    procesarArista(campos, numeroLinea, graph, puntosPorId);
                } else {
                    System.err.println("Linea " + numeroLinea + " ignorada: tipo de registro desconocido.");
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer la configuracion del grafo: " + e.getMessage());
        }

        return graph;
    }

    private void procesarNodo(String[] campos, int numeroLinea, Graph<MapPoint> graph,
            Map<String, MapPoint> puntosPorId) {
        if (campos.length < 4) {
            System.err.println("Linea " + numeroLinea + " invalida: NODE requiere id,x,y.");
            return;
        }

        String id = campos[1].trim();
        if (id.isEmpty()) {
            System.err.println("Linea " + numeroLinea + " invalida: el id del nodo esta vacio.");
            return;
        }
        if (puntosPorId.containsKey(id)) {
            System.err.println("Linea " + numeroLinea + " ignorada: el nodo '" + id + "' ya existe.");
            return;
        }

        try {
            int x = Integer.parseInt(campos[2].trim());
            int y = Integer.parseInt(campos[3].trim());

            MapPoint punto = new MapPoint(id, x, y);
            puntosPorId.put(id, punto);
            graph.add(punto);
        } catch (NumberFormatException e) {
            System.err.println("Linea " + numeroLinea + " invalida: coordenadas x,y deben ser numericas.");
        }
    }

    private void procesarArista(String[] campos, int numeroLinea, Graph<MapPoint> graph,
            Map<String, MapPoint> puntosPorId) {
        if (campos.length < 4) {
            System.err.println("Linea " + numeroLinea + " invalida: EDGE requiere origen,destino,bidireccional.");
            return;
        }

        String idOrigen = campos[1].trim();
        String idDestino = campos[2].trim();
        String bidireccionalTexto = campos[3].trim();

        MapPoint origen = puntosPorId.get(idOrigen);
        MapPoint destino = puntosPorId.get(idDestino);

        if (origen == null || destino == null) {
            System.err.println("Linea " + numeroLinea + " ignorada: referencia a nodo inexistente ("
                    + idOrigen + " o " + idDestino + ").");
            return;
        }

        boolean bidireccional = Boolean.parseBoolean(bidireccionalTexto);
        if (bidireccional) {
            graph.addEdge(origen, destino);
        } else {
            graph.addEdgeUni(origen, destino);
        }
    }

    @Override
    public void save(Graph<MapPoint> graph) {
        crearCarpetaSiNoExiste();

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("# Configuracion del grafo del mapa (nodos y conexiones)");

            for (Node<MapPoint> nodo : graph.getNodes()) {
                MapPoint punto = nodo.getValue();
                writer.println("NODE," + punto.getId() + "," + punto.getX() + "," + punto.getY());
            }

            Set<String> paresEscritos = new HashSet<>();

            for (Map.Entry<Node<MapPoint>, Set<Node<MapPoint>>> entry : graph.getGraph().entrySet()) {
                MapPoint origen = entry.getKey().getValue();

                for (Node<MapPoint> vecinoNode : entry.getValue()) {
                    MapPoint destino = vecinoNode.getValue();

                    String claveDirecta = origen.getId() + "->" + destino.getId();
                    String claveInversa = destino.getId() + "->" + origen.getId();

                    if (paresEscritos.contains(claveDirecta) || paresEscritos.contains(claveInversa)) {
                        continue;
                    }

                    boolean esBidireccional = graph.getVecinos(destino).contains(new Node<>(origen));

                    writer.println("EDGE," + origen.getId() + "," + destino.getId() + "," + esBidireccional);

                    paresEscritos.add(claveDirecta);
                    if (esBidireccional) {
                        paresEscritos.add(claveInversa);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("No se pudo guardar la configuracion del grafo: " + e.getMessage());
        }
    }

    private void crearCarpetaSiNoExiste() {
        Path carpeta = Path.of(filePath).getParent();
        if (carpeta != null && !Files.exists(carpeta)) {
            try {
                Files.createDirectories(carpeta);
            } catch (IOException e) {
                System.err.println("No se pudo crear la carpeta de configuracion: " + e.getMessage());
            }
        }
    }
}
