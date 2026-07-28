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


    public void add(T value) {
        Node<T> node = new Node<>(value);
        this.nodes.putIfAbsent(node, new LinkedHashSet<>());
    }

    public void addEdgeUni(T value1, T value2) {
        add(value1);
        add(value2);

        Node<T> n1 = new Node<>(value1);
        Node<T> n2 = new Node<>(value2);

        nodes.get(n1).add(n2);
    }

    public void addEdge(T value1, T value2) {
        add(value1);
        add(value2);

        Node<T> n1 = new Node<>(value1);
        Node<T> n2 = new Node<>(value2);

        nodes.get(n1).add(n2);
        nodes.get(n2).add(n1);
    }

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

    public void removeEdgeUni(T v1, T v2) {
        Node<T> n1 = new Node<>(v1);
        Node<T> n2 = new Node<>(v2);

        if (nodes.containsKey(n1)) {
            nodes.get(n1).remove(n2);
        }
    }

    public void remove(T value) {
        Node<T> target = new Node<>(value);
        for (Map.Entry<Node<T>, Set<Node<T>>> entry : nodes.entrySet()) {
            entry.getValue().remove(target);
        }
        nodes.remove(target);
    }

    public Set<Node<T>> getVecinos(T currente) {
        Node<T> currentNode = new Node<>(currente);
        return nodes.getOrDefault(currentNode, new LinkedHashSet<>());
    }

    public Set<Node<T>> getNodes() {
        return nodes.keySet();
    }


    public Map<Node<T>, Set<Node<T>>> getGraph() {
        return nodes;
    }

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