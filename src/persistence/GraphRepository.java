package persistence;

import models.MapPoint;
import structures.graphs.Graph;


public interface GraphRepository {

    Graph<MapPoint> load();

    void save(Graph<MapPoint> graph);
}
