package service;

import graph.Edge;
import graph.Graph;
import model.EntidadPrincipal;
import model.EntidadPrincipal.Tipo;
import model.EntidadRelacionada;
import tree.Tree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Logica de la plataforma de videojuegos. Integra:
 *  - Arbol general: catalogo (plataforma > categoria > genero > subgenero > videojuego).
 *  - Grafo no dirigido y ponderado: red de jugadores (peso = latencia en ms).
 * La integracion: cada jugador referencia un videojuego del arbol como favorito.
 */
public class Sistema {

    private final Tree<EntidadPrincipal> catalogo = new Tree<>(EntidadPrincipal::getId);
    private final Graph<EntidadRelacionada> red = new Graph<>(false);
    private final Map<String, EntidadRelacionada> jugadores = new LinkedHashMap<>();

    // ================= CATALOGO (ARBOL) =================

    public boolean registrarElemento(String idPadre, String id, String nombre, Tipo tipo) {
        if (catalogo.isEmpty()) return catalogo.setRoot(new EntidadPrincipal(id, nombre, tipo));
        return catalogo.insertar(idPadre, new EntidadPrincipal(id, nombre, tipo));
    }

    public EntidadPrincipal buscarElemento(String id) {
        var nodo = catalogo.buscar(id);
        return nodo == null ? null : nodo.getData();
    }

    public List<EntidadPrincipal> hijosDe(String id) {
        return catalogo.hijos(id);
    }

    public EntidadPrincipal padreDe(String id) {
        return catalogo.padre(id);
    }

    public int nivelDe(String id) {
        return catalogo.nivel(id);
    }

    public int alturaCatalogo() {
        return catalogo.altura();
    }

    public int alturaSubarbol(String id) {
        return catalogo.altura(id);
    }

    public int totalElementos() {
        return catalogo.tamanio();
    }

    public boolean modificarNombre(String id, String nuevoNombre) {
        EntidadPrincipal actual = buscarElemento(id);
        if (actual == null) return false;
        return catalogo.modificar(id, new EntidadPrincipal(id, nuevoNombre, actual.getTipo()));
    }

    /** Elimina solo si es hoja. */
    public boolean eliminarElemento(String id) {
        return catalogo.eliminar(id);
    }

    public boolean eliminarElementoConDescendientes(String id) {
        return catalogo.eliminarConDescendientes(id);
    }

    public String mostrarCatalogo() {
        return catalogo.representacion(e -> e.getNombre() + " (" + e.getTipo() + ")");
    }

    public List<EntidadPrincipal> recorridoProfundidad() {
        return catalogo.recorridoProfundidad();
    }

    public List<EntidadPrincipal> recorridoPorNiveles() {
        return catalogo.recorridoPorNiveles();
    }

    // ================= RED DE JUGADORES (GRAFO) =================

    public boolean registrarJugador(String id, String nickname, String pais, String idJuegoFavorito) {
        EntidadPrincipal juego = buscarElemento(idJuegoFavorito);
        if (juego == null || juego.getTipo() != Tipo.VIDEOJUEGO) return false;
        EntidadRelacionada j = new EntidadRelacionada(id, nickname, pais, idJuegoFavorito);
        if (!red.agregarVertice(j)) return false;
        jugadores.put(id, j);
        return true;
    }

    public EntidadRelacionada buscarJugador(String id) {
        return jugadores.get(id);
    }

    public boolean eliminarJugador(String id) {
        EntidadRelacionada j = jugadores.get(id);
        if (j == null) return false;
        red.eliminarVertice(j);
        jugadores.remove(id);
        return true;
    }

    public boolean crearConexion(String id1, String id2, double latencia) {
        EntidadRelacionada a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.agregarArista(a, b, latencia);
    }

    public boolean eliminarConexion(String id1, String id2) {
        EntidadRelacionada a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.eliminarArista(a, b);
    }

    public boolean existeConexion(String id1, String id2) {
        EntidadRelacionada a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.existeArista(a, b);
    }

    public List<Edge<EntidadRelacionada>> conexionesDe(String id) {
        EntidadRelacionada j = jugadores.get(id);
        return j == null ? new ArrayList<>() : red.vecinos(j);
    }

    public int gradoDe(String id) {
        EntidadRelacionada j = jugadores.get(id);
        return j == null ? -1 : red.grado(j);
    }

    public Map<EntidadRelacionada, Integer> bfs(String idInicio) {
        EntidadRelacionada j = jugadores.get(idInicio);
        return j == null ? new LinkedHashMap<>() : red.bfs(j);
    }

    public List<EntidadRelacionada> dfs(String idInicio) {
        EntidadRelacionada j = jugadores.get(idInicio);
        return j == null ? new ArrayList<>() : red.dfs(j);
    }

    public List<EntidadRelacionada> caminoMinimoSaltos(String id1, String id2) {
        EntidadRelacionada a = jugadores.get(id1), b = jugadores.get(id2);
        return (a == null || b == null) ? null : red.caminoMinimoSaltos(a, b);
    }

    public Graph.Ruta<EntidadRelacionada> rutaMenorLatencia(String id1, String id2) {
        EntidadRelacionada a = jugadores.get(id1), b = jugadores.get(id2);
        return (a == null || b == null) ? null : red.rutaMenorCosto(a, b);
    }

    /** Funcionalidad especifica del grafo: comunidades de jugadores (componentes conexos). */
    public List<List<EntidadRelacionada>> comunidades() {
        return red.componentesConexos();
    }

    public int totalJugadores() {
        return red.cantidadVertices();
    }

    public int totalConexiones() {
        return red.cantidadAristas();
    }

    // ================= INTEGRACION ARBOL + GRAFO =================

    /** Jugadores cuyo juego favorito pertenece al subarbol del nodo indicado (genero, categoria...). */
    public List<EntidadRelacionada> jugadoresDeNodo(String idNodo) {
        List<String> idsJuegos = catalogo.subarbol(idNodo).stream()
                .filter(e -> e.getTipo() == Tipo.VIDEOJUEGO)
                .map(EntidadPrincipal::getId)
                .collect(Collectors.toList());
        return jugadores.values().stream()
                .filter(j -> idsJuegos.contains(j.getIdJuegoFavorito()))
                .collect(Collectors.toList());
    }

    /** Ruta en la jerarquia del nodo: Plataforma > Categoria > Genero ... */
    public String rutaJerarquica(String id) {
        LinkedList<String> partes = new LinkedList<>();
        EntidadPrincipal e = buscarElemento(id);
        while (e != null) {
            partes.addFirst(e.getNombre());
            e = catalogo.padre(e.getId());
        }
        return String.join(" > ", partes);
    }

    // ================= CARGA DE DATOS DE PRUEBA =================

    public void cargarDatosDePrueba() {
        Tipo C = Tipo.CATEGORIA, G = Tipo.GENERO, S = Tipo.SUBGENERO, V = Tipo.VIDEOJUEGO;
        registrarElemento(null, "PLAT", "Plataforma de Videojuegos", Tipo.PLATAFORMA);
        registrarElemento("PLAT", "CAT-PC", "PC", C);
        registrarElemento("PLAT", "CAT-CON", "Consola", C);
        registrarElemento("PLAT", "CAT-MOV", "Movil", C);
        registrarElemento("CAT-PC", "GEN-ACC", "Accion", G);
        registrarElemento("CAT-PC", "GEN-EST", "Estrategia", G);
        registrarElemento("CAT-CON", "GEN-DEP", "Deportes", G);
        registrarElemento("CAT-CON", "GEN-AVE", "Aventura", G);
        registrarElemento("CAT-MOV", "GEN-PUZ", "Puzzle", G);
        registrarElemento("GEN-ACC", "SUB-FPS", "Shooter en primera persona", S);
        registrarElemento("GEN-ACC", "SUB-BR", "Battle Royale", S);
        registrarElemento("GEN-EST", "SUB-MOBA", "MOBA", S);
        registrarElemento("GEN-DEP", "SUB-FUT", "Futbol", S);
        registrarElemento("GEN-AVE", "SUB-RPG", "RPG de mundo abierto", S);
        registrarElemento("SUB-FPS", "JUE-CS2", "Counter-Strike 2", V);
        registrarElemento("SUB-FPS", "JUE-VAL", "Valorant", V);
        registrarElemento("SUB-BR", "JUE-FN", "Fortnite", V);
        registrarElemento("SUB-MOBA", "JUE-LOL", "League of Legends", V);
        registrarElemento("SUB-FUT", "JUE-FC", "EA FC 25", V);
        registrarElemento("SUB-RPG", "JUE-ZEL", "Zelda Tears of the Kingdom", V);
        registrarElemento("GEN-PUZ", "JUE-CAN", "Candy Crush", V);

        String[][] js = {
            {"J01", "Shadow", "Colombia", "JUE-CS2"}, {"J02", "Nova", "Mexico", "JUE-VAL"},
            {"J03", "Blaze", "Colombia", "JUE-FN"}, {"J04", "Frost", "Chile", "JUE-LOL"},
            {"J05", "Viper", "Peru", "JUE-VAL"}, {"J06", "Titan", "Argentina", "JUE-CS2"},
            {"J07", "Echo", "Espana", "JUE-FC"}, {"J08", "Pixel", "Colombia", "JUE-CAN"},
            {"J09", "Rogue", "Mexico", "JUE-LOL"}, {"J10", "Zen", "Chile", "JUE-ZEL"},
            {"J11", "Orion", "Peru", "JUE-FC"}, {"J12", "Luna", "Brasil", "JUE-ZEL"},
            {"J13", "Kira", "Brasil", "JUE-CAN"}
        };
        for (String[] j : js) registrarJugador(j[0], j[1], j[2], j[3]);

        Object[][] cs = {
            {"J01", "J02", 20}, {"J01", "J03", 35}, {"J01", "J04", 50}, {"J02", "J03", 15},
            {"J02", "J05", 40}, {"J03", "J06", 25}, {"J04", "J06", 30}, {"J04", "J07", 60},
            {"J05", "J08", 45}, {"J05", "J06", 55}, {"J06", "J09", 20}, {"J07", "J09", 35},
            {"J07", "J10", 25}, {"J08", "J10", 70}, {"J08", "J11", 30}, {"J09", "J11", 40},
            {"J10", "J11", 15}, {"J06", "J08", 90}, {"J12", "J13", 10}
        };
        for (Object[] c : cs) crearConexion((String) c[0], (String) c[1], ((Integer) c[2]).doubleValue());
    }
}

