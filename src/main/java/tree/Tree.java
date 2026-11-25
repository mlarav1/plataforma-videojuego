package tree;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;

/**
 * Arbol general (cada nodo puede tener N hijos).
 * Se eligio un arbol general porque una categoria tiene cantidad variable
 * de generos, y un genero cantidad variable de subgeneros o juegos.
 *
 * Politica de eliminacion: eliminar(id) se rechaza si el nodo tiene hijos;
 * eliminarConDescendientes(id) borra todo el subarbol.
 */
public class Tree<T> {

    private TreeNode<T> root;
    private final Function<T, String> idExtractor;

    public Tree(Function<T, String> idExtractor) {
        this.idExtractor = idExtractor;
    }

    public TreeNode<T> getRoot() {
        return root;
    }

    public boolean isEmpty() {
        return root == null;
    }

    /** Define la raiz. Solo es posible si el arbol esta vacio. */
    public boolean setRoot(T data) {
        if (root != null) return false;
        root = new TreeNode<>(data);
        return true;
    }

    /** Inserta un dato como hijo del nodo con id idPadre. */
    public boolean insertar(String idPadre, T data) {
        TreeNode<T> padre = buscar(idPadre);
        if (padre == null || buscar(idExtractor.apply(data)) != null) return false;
        padre.addChild(new TreeNode<>(data));
        return true;
    }

    /** Busca un nodo por id con recorrido en profundidad. */
    public TreeNode<T> buscar(String id) {
        return buscar(root, id);
    }

    private TreeNode<T> buscar(TreeNode<T> nodo, String id) {
        if (nodo == null) return null;
        if (idExtractor.apply(nodo.getData()).equals(id)) return nodo;
        for (TreeNode<T> hijo : nodo.getChildren()) {
            TreeNode<T> r = buscar(hijo, id);
            if (r != null) return r;
        }
        return null;
    }

    /** Reemplaza el dato de un nodo sin tocar sus relaciones. El id debe conservarse. */
    public boolean modificar(String id, T nuevoDato) {
        TreeNode<T> nodo = buscar(id);
        if (nodo == null || !idExtractor.apply(nuevoDato).equals(id)) return false;
        nodo.setData(nuevoDato);
        return true;
    }

    /** Elimina un nodo solo si es hoja (y no es la raiz). */
    public boolean eliminar(String id) {
        TreeNode<T> nodo = buscar(id);
        if (nodo == null || nodo == root || !nodo.isLeaf()) return false;
        return nodo.getParent().removeChild(nodo);
    }

    /** Elimina un nodo junto con todo su subarbol. */
    public boolean eliminarConDescendientes(String id) {
        TreeNode<T> nodo = buscar(id);
        if (nodo == null || nodo == root) return false;
        return nodo.getParent().removeChild(nodo);
    }

    public List<T> hijos(String id) {
        List<T> r = new ArrayList<>();
        TreeNode<T> nodo = buscar(id);
        if (nodo != null) {
            for (TreeNode<T> h : nodo.getChildren()) r.add(h.getData());
        }
        return r;
    }

    /** Devuelve el padre directo o null si es la raiz o no existe. */
    public T padre(String id) {
        TreeNode<T> nodo = buscar(id);
        return (nodo == null || nodo.getParent() == null) ? null : nodo.getParent().getData();
    }

    /** Nivel del nodo: raiz = 0. Devuelve -1 si no existe. */
    public int nivel(String id) {
        TreeNode<T> nodo = buscar(id);
        if (nodo == null) return -1;
        int nivel = 0;
        while (nodo.getParent() != null) {
            nivel++;
            nodo = nodo.getParent();
        }
        return nivel;
    }

    /** Altura del arbol completo (raiz sola = 0, vacio = -1). */
    public int altura() {
        return altura(root);
    }

    /** Altura del subarbol que inicia en id. -1 si no existe. */
    public int altura(String id) {
        return altura(buscar(id));
    }

    private int altura(TreeNode<T> nodo) {
        if (nodo == null) return -1;
        int max = -1;
        for (TreeNode<T> h : nodo.getChildren()) max = Math.max(max, altura(h));
        return max + 1;
    }

    /** Cantidad total de nodos. */
    public int tamanio() {
        return recorridoProfundidad().size();
    }

    /** Recorrido en profundidad (preorden): primero el padre, luego cada hijo completo. */
    public List<T> recorridoProfundidad() {
        List<T> r = new ArrayList<>();
        preorden(root, r);
        return r;
    }

    private void preorden(TreeNode<T> nodo, List<T> r) {
        if (nodo == null) return;
        r.add(nodo.getData());
        for (TreeNode<T> h : nodo.getChildren()) preorden(h, r);
    }

    /** Recorrido por niveles: usa una cola, primero los nodos cercanos a la raiz. */
    public List<T> recorridoPorNiveles() {
        List<T> r = new ArrayList<>();
        if (root == null) return r;
        Queue<TreeNode<T>> cola = new ArrayDeque<>();
        cola.add(root);
        while (!cola.isEmpty()) {
            TreeNode<T> n = cola.poll();
            r.add(n.getData());
            cola.addAll(n.getChildren());
        }
        return r;
    }

    /** Datos del subarbol que inicia en id (incluido), en preorden. */
    public List<T> subarbol(String id) {
        List<T> r = new ArrayList<>();
        preorden(buscar(id), r);
        return r;
    }

    /** Representacion textual de la jerarquia con ramas. */
    public String representacion(Function<T, String> etiqueta) {
        StringBuilder sb = new StringBuilder();
        if (root != null) {
            sb.append(etiqueta.apply(root.getData())).append('\n');
            dibujar(root, "", sb, etiqueta);
        }
        return sb.toString();
    }

    private void dibujar(TreeNode<T> nodo, String prefijo, StringBuilder sb, Function<T, String> etiqueta) {
        List<TreeNode<T>> hijos = nodo.getChildren();
        for (int i = 0; i < hijos.size(); i++) {
            boolean ultimo = i == hijos.size() - 1;
            sb.append(prefijo).append(ultimo ? "└── " : "├── ")
              .append(etiqueta.apply(hijos.get(i).getData())).append('\n');
            dibujar(hijos.get(i), prefijo + (ultimo ? "    " : "│   "), sb, etiqueta);
        }
    }
}
