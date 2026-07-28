package structures.node;

import java.util.Objects;

/**
 * Nodo generico utilizado internamente por Graph<T> para representar
 * un vertice del grafo. El nodo unicamente envuelve el valor almacenado;
 * la identidad logica del nodo depende exclusivamente de ese valor.
 *
 * Se utilizan equals()/hashCode() basados en el valor porque Graph crea
 * nuevas instancias de Node<T> para consultar las claves del HashMap
 * (por ejemplo al buscar vecinos o al eliminar aristas), tal como se
 * trabajo en clase con la clase Nodes<T>.
 */
public class Node<T> {

    private T value;

    public Node(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "N(" + value + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Node<?> other = (Node<?>) obj;
        return Objects.equals(value, other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
}
