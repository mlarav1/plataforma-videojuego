package tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo de un arbol general: guarda un dato, una lista de hijos y
 * una referencia a su padre (null en la raiz).
 */
public class NodoArbol<T> {

    private T data;
    private final List<NodoArbol<T>> children = new ArrayList<>();
    private NodoArbol<T> parent;

    public NodoArbol(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<NodoArbol<T>> getChildren() {
        return children;
    }

    public NodoArbol<T> getParent() {
        return parent;
    }

    public void setParent(NodoArbol<T> parent) {
        this.parent = parent;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void addChild(NodoArbol<T> child) {
        child.setParent(this);
        children.add(child);
    }

    public boolean removeChild(NodoArbol<T> child) {
        boolean removed = children.remove(child);
        if (removed) child.setParent(null);
        return removed;
    }
}
