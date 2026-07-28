package persistence;

import models.MapPoint;
import structures.graphs.Graph;

/**
 * Abstrae el mecanismo de persistencia del grafo del mapa.
 *
 * El controlador y los algoritmos de busqueda nunca leen o escriben
 * archivos directamente: siempre lo hacen a traves de esta interfaz,
 * de modo que la forma de almacenamiento (CSV, base de datos, etc.)
 * pueda cambiar sin afectar al resto de la aplicacion.
 */
public interface GraphRepository {

    /**
     * Reconstruye el grafo a partir de la configuracion almacenada.
     * Si no existe configuracion previa, devuelve un grafo vacio.
     */
    Graph<MapPoint> load();

    /**
     * Guarda el estado actual del grafo (nodos, coordenadas y conexiones)
     * de forma que pueda reconstruirse identicamente la proxima vez que
     * se llame a load().
     */
    void save(Graph<MapPoint> graph);
}
