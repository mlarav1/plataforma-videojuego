package service;

import graph.Conexion;
import graph.GrafoPonderado;
import model.ElementoCatalogo;
import model.ElementoCatalogo.Tipo;
import model.Jugador;
import tree.ArbolGeneral;

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
public class PlataformaVideojuegos {

    private final ArbolGeneral<ElementoCatalogo> catalogo = new ArbolGeneral<>(ElementoCatalogo::getId);
    private final GrafoPonderado<Jugador> red = new GrafoPonderado<>(false);
    private final Map<String, Jugador> jugadores = new LinkedHashMap<>();

    // ================= CATALOGO (ARBOL) =================

    public boolean registrarElemento(String idPadre, String id, String nombre, Tipo tipo) {
        if (catalogo.isEmpty()) return catalogo.setRoot(new ElementoCatalogo(id, nombre, tipo));
        return catalogo.insertar(idPadre, new ElementoCatalogo(id, nombre, tipo));
    }

    public ElementoCatalogo buscarElemento(String id) {
        var nodo = catalogo.buscar(id);
        return nodo == null ? null : nodo.getData();
    }

    public List<ElementoCatalogo> hijosDe(String id) {
        return catalogo.hijos(id);
    }

    public ElementoCatalogo padreDe(String id) {
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
        ElementoCatalogo actual = buscarElemento(id);
        if (actual == null) return false;
        return catalogo.modificar(id, new ElementoCatalogo(id, nuevoNombre, actual.getTipo()));
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

    public List<ElementoCatalogo> recorridoProfundidad() {
        return catalogo.recorridoProfundidad();
    }

    public List<ElementoCatalogo> recorridoPorNiveles() {
        return catalogo.recorridoPorNiveles();
    }

    // ================= RED DE JUGADORES (GRAFO) =================

    public boolean registrarJugador(String id, String nickname, String pais, String idJuegoFavorito) {
        ElementoCatalogo juego = buscarElemento(idJuegoFavorito);
        if (juego == null || juego.getTipo() != Tipo.VIDEOJUEGO) return false;
        Jugador j = new Jugador(id, nickname, pais, idJuegoFavorito);
        if (!red.agregarVertice(j)) return false;
        jugadores.put(id, j);
        return true;
    }

    public Jugador buscarJugador(String id) {
        return jugadores.get(id);
    }

    public boolean eliminarJugador(String id) {
        Jugador j = jugadores.get(id);
        if (j == null) return false;
        red.eliminarVertice(j);
        jugadores.remove(id);
        return true;
    }

    public boolean crearConexion(String id1, String id2, double latencia) {
        Jugador a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.agregarArista(a, b, latencia);
    }

    public boolean eliminarConexion(String id1, String id2) {
        Jugador a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.eliminarArista(a, b);
    }

    public boolean existeConexion(String id1, String id2) {
        Jugador a = jugadores.get(id1), b = jugadores.get(id2);
        return a != null && b != null && red.existeArista(a, b);
    }

    public List<Conexion<Jugador>> conexionesDe(String id) {
        Jugador j = jugadores.get(id);
        return j == null ? new ArrayList<>() : red.vecinos(j);
    }

    public int gradoDe(String id) {
        Jugador j = jugadores.get(id);
        return j == null ? -1 : red.grado(j);
    }

    public Map<Jugador, Integer> bfs(String idInicio) {
        Jugador j = jugadores.get(idInicio);
        return j == null ? new LinkedHashMap<>() : red.bfs(j);
    }

    public List<Jugador> dfs(String idInicio) {
        Jugador j = jugadores.get(idInicio);
        return j == null ? new ArrayList<>() : red.dfs(j);
    }

    public List<Jugador> caminoMinimoSaltos(String id1, String id2) {
        Jugador a = jugadores.get(id1), b = jugadores.get(id2);
        return (a == null || b == null) ? null : red.caminoMinimoSaltos(a, b);
    }

    public GrafoPonderado.Ruta<Jugador> rutaMenorLatencia(String id1, String id2) {
        Jugador a = jugadores.get(id1), b = jugadores.get(id2);
        return (a == null || b == null) ? null : red.rutaMenorCosto(a, b);
    }

    /** Funcionalidad especifica del grafo: comunidades de jugadores (componentes conexos). */
    public List<List<Jugador>> comunidades() {
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
    public List<Jugador> jugadoresDeNodo(String idNodo) {
        List<String> idsJuegos = catalogo.subarbol(idNodo).stream()
                .filter(e -> e.getTipo() == Tipo.VIDEOJUEGO)
                .map(ElementoCatalogo::getId)
                .collect(Collectors.toList());
        return jugadores.values().stream()
                .filter(j -> idsJuegos.contains(j.getIdJuegoFavorito()))
                .collect(Collectors.toList());
    }

    /** Ruta en la jerarquia del nodo: Plataforma > Categoria > Genero ... */
    public String rutaJerarquica(String id) {
        LinkedList<String> partes = new LinkedList<>();
        ElementoCatalogo e = buscarElemento(id);
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

