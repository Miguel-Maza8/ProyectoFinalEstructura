
package structures.node;

import java.util.Objects;

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