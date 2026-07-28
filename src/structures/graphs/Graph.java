package structures.graphs;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import structures.node.Node;


 
public class Graph<T> {

    private Map<Node<T>, Set<Node<T>>> nodes;

    public Graph() {
        this.nodes = new LinkedHashMap<>();
    }

    /**
     * Agrega un nodo al grafo si todavia no existe. No falla si el nodo
     * ya se encuentra registrado (usa putIfAbsent).
     */
    public void add(T value) {
        Node<T> node = new Node<>(value);
        this.nodes.putIfAbsent(node, new LinkedHashSet<>());
    }

    /**
     * Crea una calle de un solo sentido: value1 -> value2.
     * Si alguno de los dos puntos no existe todavia en el grafo, se crea.
     */
    public void addEdgeUni(T value1, T value2) {
        add(value1);
        add(value2);

        Node<T> n1 = new Node<>(value1);
        Node<T> n2 = new Node<>(value2);

        nodes.get(n1).add(n2);
    }

    /**
     * Crea una calle bidireccional entre value1 y value2.
     */
    public void addEdge(T value1, T value2) {
        add(value1);
        add(value2);

        Node<T> n1 = new Node<>(value1);
        Node<T> n2 = new Node<>(value2);

        nodes.get(n1).add(n2);
        nodes.get(n2).add(n1);
    }

    /**
     * Elimina la arista bidireccional entre v1 y v2 (ambos sentidos).
     */
    public void removeEdge(T v1, T v2) {
        Node<T> n1 = new Node<>(v1);
        Node<T> n2 = new Node<>(v2);

        if (nodes.containsKey(n1)) {
            nodes.get(n1).remove(n2);
        }
        if (nodes.containsKey(n2)) {
            nodes.get(n2).remove(n1);
        }
    }

    /**
     * Elimina unicamente la conexion v1 -> v2, dejando intacta v2 -> v1
     * en caso de que exista.
     */
    public void removeEdgeUni(T v1, T v2) {
        Node<T> n1 = new Node<>(v1);
        Node<T> n2 = new Node<>(v2);

        if (nodes.containsKey(n1)) {
            nodes.get(n1).remove(n2);
        }
    }

    /**
     * Elimina un nodo del grafo junto con todas las aristas que apuntan
     * hacia el desde otros nodos.
     */
    public void remove(T value) {
        Node<T> target = new Node<>(value);
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            entry.getValue().remove(target);
        }
        nodes.remove(target);
    }

    /**
     * Devuelve los vecinos directos (nodos alcanzables en un paso) de currente.
     * Si el nodo no existe, devuelve un conjunto vacio en lugar de fallar.
     */
    public Set<Node<T>> getVecinos(T currente) {
        Node<T> currentNode = new Node<>(currente);
        return nodes.getOrDefault(currentNode, new LinkedHashSet<>());
    }

    /**
     * Devuelve todos los nodos registrados en el grafo, en orden de insercion.
     */
    public Set<Node<T>> getNodes() {
        return nodes.keySet();
    }

    /**
     * Devuelve la estructura interna completa (lista de adyacencia).
     * Utilizado por la vista para dibujar todas las aristas del mapa.
     */
    public Map<Node<T>, Set<Node<T>>> getGraph() {
        return nodes;
    }

    /**
     * Indica si un valor ya se encuentra registrado como nodo del grafo.
     */
    public boolean contains(T value) {
        return nodes.containsKey(new Node<>(value));
    }

    public int contarConexiones() {
        return nodes.size();
    }

    public void print() {
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            System.out.print(entry.getKey().getValue() + " -> ");
            for (Node<T> node : entry.getValue()) {
                System.out.print(node.getValue() + " ");
            }
            System.out.println();
        }
    }
}
