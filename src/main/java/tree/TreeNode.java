package tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo de un arbol general: guarda un dato, una lista de hijos y
 * una referencia a su padre (null en la raiz).
 */
public class TreeNode<T> {

    private T data;
    private final List<TreeNode<T>> children = new ArrayList<>();
    private TreeNode<T> parent;

    public TreeNode(T data) {
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }

    public boolean isLeaf() {
        return children.isEmpty();
    }

    public void addChild(TreeNode<T> child) {
        child.setParent(this);
        children.add(child);
    }

    public boolean removeChild(TreeNode<T> child) {
        boolean removed = children.remove(child);
        if (removed) child.setParent(null);
        return removed;
    }
}
