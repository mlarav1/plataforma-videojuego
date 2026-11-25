package graph;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 * Grafo implementado con lista de adyacencia: Map&lt;T, List&lt;Conexion&lt;T&gt;&gt;&gt;.
 * Puede ser dirigido o no dirigido; las aristas siempre tienen peso.
 */
public class GrafoPonderado<T> {

    /** Resultado de una busqueda de ruta: camino y costo total. */
    public static class Ruta<T> {
        public final List<T> camino;
        public final double costo;

        public Ruta(List<T> camino, double costo) {
            this.camino = camino;
            this.costo = costo;
        }
    }

    private final Map<T, List<Conexion<T>>> adyacencias = new LinkedHashMap<>();
    private final boolean dirigido;

    public GrafoPonderado(boolean dirigido) {
        this.dirigido = dirigido;
    }

    public boolean isDirigido() {
        return dirigido;
    }

    // ---------- Vertices ----------

    public boolean agregarVertice(T v) {
        if (adyacencias.containsKey(v)) return false;
        adyacencias.put(v, new ArrayList<>());
        return true;
    }

    public boolean contieneVertice(T v) {
        return adyacencias.containsKey(v);
    }

    /** Elimina el vertice y todas las aristas que entran o salen de el. */
    public boolean eliminarVertice(T v) {
        if (!adyacencias.containsKey(v)) return false;
        adyacencias.remove(v);
        for (List<Conexion<T>> lista : adyacencias.values()) {
            lista.removeIf(e -> e.getDestination().equals(v));
        }
        return true;
    }

    public List<T> vertices() {
        return new ArrayList<>(adyacencias.keySet());
    }

    public int cantidadVertices() {
        return adyacencias.size();
    }

    // ---------- Aristas ----------

    /** Crea la conexion; si el grafo no es dirigido tambien crea la inversa. */
    public boolean agregarArista(T origen, T destino, double peso) {
        if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino)) return false;
        if (origen.equals(destino) || existeArista(origen, destino)) return false;
        adyacencias.get(origen).add(new Conexion<>(destino, peso));
        if (!dirigido) adyacencias.get(destino).add(new Conexion<>(origen, peso));
        return true;
    }

    public boolean eliminarArista(T origen, T destino) {
        if (!existeArista(origen, destino)) return false;
        adyacencias.get(origen).removeIf(e -> e.getDestination().equals(destino));
        if (!dirigido) adyacencias.get(destino).removeIf(e -> e.getDestination().equals(origen));
        return true;
    }

    /** Conexion directa origen -> destino. */
    public boolean existeArista(T origen, T destino) {
        List<Conexion<T>> lista = adyacencias.get(origen);
        if (lista == null) return false;
        for (Conexion<T> e : lista) {
            if (e.getDestination().equals(destino)) return true;
        }
        return false;
    }

    public List<Conexion<T>> vecinos(T v) {
        List<Conexion<T>> lista = adyacencias.get(v);
        return lista == null ? new ArrayList<>() : new ArrayList<>(lista);
    }

    /** Cantidad de aristas (en no dirigido cada conexion se cuenta una vez). */
    public int cantidadAristas() {
        int total = 0;
        for (List<Conexion<T>> l : adyacencias.values()) total += l.size();
        return dirigido ? total : total / 2;
    }

    // ---------- Grados ----------

    public int gradoSalida(T v) {
        List<Conexion<T>> l = adyacencias.get(v);
        return l == null ? -1 : l.size();
    }

    public int gradoEntrada(T v) {
        if (!adyacencias.containsKey(v)) return -1;
        int n = 0;
        for (List<Conexion<T>> l : adyacencias.values()) {
            for (Conexion<T> e : l) if (e.getDestination().equals(v)) n++;
        }
        return n;
    }

    /** En no dirigido es la cantidad de conexiones; en dirigido entrada + salida. */
    public int grado(T v) {
        if (!adyacencias.containsKey(v)) return -1;
        return dirigido ? gradoEntrada(v) + gradoSalida(v) : gradoSalida(v);
    }

    // ---------- Recorridos ----------

    /**
     * BFS con cola. Devuelve cada vertice alcanzado con su nivel
     * (distancia en saltos desde el inicio), en orden de visita.
     */
    public Map<T, Integer> bfs(T inicio) {
        Map<T, Integer> niveles = new LinkedHashMap<>();
        if (!adyacencias.containsKey(inicio)) return niveles;
        Queue<T> cola = new ArrayDeque<>();
        niveles.put(inicio, 0);
        cola.add(inicio);
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            for (Conexion<T> e : adyacencias.get(actual)) {
                if (!niveles.containsKey(e.getDestination())) {
                    niveles.put(e.getDestination(), niveles.get(actual) + 1);
                    cola.add(e.getDestination());
                }
            }
        }
        return niveles;
    }

    /** DFS iterativo con pila explicita. Devuelve el orden de visita. */
    public List<T> dfs(T inicio) {
        List<T> orden = new ArrayList<>();
        if (!adyacencias.containsKey(inicio)) return orden;
        Set<T> visitados = new HashSet<>();
        Deque<T> pila = new ArrayDeque<>();
        pila.push(inicio);
        while (!pila.isEmpty()) {
            T actual = pila.pop();
            if (!visitados.add(actual)) continue;
            orden.add(actual);
            List<Conexion<T>> lista = adyacencias.get(actual);
            // se apila al reves para visitar primero al primer vecino
            for (int i = lista.size() - 1; i >= 0; i--) {
                if (!visitados.contains(lista.get(i).getDestination())) {
                    pila.push(lista.get(i).getDestination());
                }
            }
        }
        return orden;
    }

    // ---------- Problemas sobre el grafo ----------

    public boolean existeCamino(T origen, T destino) {
        return bfs(origen).containsKey(destino);
    }

    /** Camino con menos conexiones (BFS con mapa de predecesores). Null si no existe. */
    public List<T> caminoMinimoSaltos(T origen, T destino) {
        if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino)) return null;
        Map<T, T> previo = new HashMap<>();
        Set<T> visitados = new HashSet<>();
        Queue<T> cola = new ArrayDeque<>();
        visitados.add(origen);
        cola.add(origen);
        while (!cola.isEmpty()) {
            T actual = cola.poll();
            if (actual.equals(destino)) break;
            for (Conexion<T> e : adyacencias.get(actual)) {
                if (visitados.add(e.getDestination())) {
                    previo.put(e.getDestination(), actual);
                    cola.add(e.getDestination());
                }
            }
        }
        return reconstruir(previo, origen, destino, visitados);
    }

    /** Dijkstra: ruta de menor peso total. Null si no existe. */
    public Ruta<T> rutaMenorCosto(T origen, T destino) {
        if (!adyacencias.containsKey(origen) || !adyacencias.containsKey(destino)) return null;
        Map<T, Double> dist = new HashMap<>();
        Map<T, T> previo = new HashMap<>();
        Set<T> cerrados = new HashSet<>();
        PriorityQueue<Map.Entry<T, Double>> cola =
                new PriorityQueue<>((a, b) -> Double.compare(a.getValue(), b.getValue()));
        dist.put(origen, 0.0);
        cola.add(Map.entry(origen, 0.0));
        while (!cola.isEmpty()) {
            T actual = cola.poll().getKey();
            if (!cerrados.add(actual)) continue;
            if (actual.equals(destino)) break;
            for (Conexion<T> e : adyacencias.get(actual)) {
                double nueva = dist.get(actual) + e.getWeight();
                if (nueva < dist.getOrDefault(e.getDestination(), Double.POSITIVE_INFINITY)) {
                    dist.put(e.getDestination(), nueva);
                    previo.put(e.getDestination(), actual);
                    cola.add(Map.entry(e.getDestination(), nueva));
                }
            }
        }
        if (!dist.containsKey(destino)) return null;
        List<T> camino = reconstruir(previo, origen, destino, dist.keySet());
        return new Ruta<>(camino, dist.get(destino));
    }

    private List<T> reconstruir(Map<T, T> previo, T origen, T destino, Set<T> alcanzados) {
        if (!alcanzados.contains(destino)) return null;
        List<T> camino = new ArrayList<>();
        for (T v = destino; v != null; v = previo.get(v)) camino.add(v);
        Collections.reverse(camino);
        return camino.get(0).equals(origen) ? camino : null;
    }

    /**
     * Componentes conexos (comunidades de jugadores). Se asume grafo no dirigido:
     * cada BFS desde un vertice sin visitar descubre un grupo aislado.
     */
    public List<List<T>> componentesConexos() {
        List<List<T>> componentes = new ArrayList<>();
        Set<T> visitados = new LinkedHashSet<>();
        for (T v : adyacencias.keySet()) {
            if (visitados.contains(v)) continue;
            List<T> grupo = new ArrayList<>(bfs(v).keySet());
            visitados.addAll(grupo);
            componentes.add(grupo);
        }
        return componentes;
    }
}
