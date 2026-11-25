import graph.Conexion;
import graph.GrafoPonderado;
import model.ElementoCatalogo;
import model.ElementoCatalogo.Tipo;
import model.Jugador;
import service.PlataformaVideojuegos;

import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/** Interaccion con el usuario: menu de la plataforma de videojuegos. */
public class Main {

    private static final Scanner in = new Scanner(System.in);
    private static final PlataformaVideojuegos sistema = new PlataformaVideojuegos();

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(System.out, true, "UTF-8"));
        sistema.cargarDatosDePrueba();
        int op;
        do {
            menu();
            op = leerEntero("Opcion: ");
            try {
                ejecutar(op);
            } catch (RuntimeException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (op != 0);
        System.out.println("Hasta pronto.");
    }

    private static void menu() {
        System.out.println("\n===== PLATAFORMA DE VIDEOJUEGOS =====");
        System.out.println(" 1. Registrar elemento del catalogo (categoria/genero/subgenero/juego)");
        System.out.println(" 2. Consultar catalogo de videojuegos");
        System.out.println(" 3. Buscar elemento del catalogo");
        System.out.println(" 4. Mostrar recorrido del catalogo");
        System.out.println(" 5. Consultar nivel y altura");
        System.out.println(" 6. Renombrar elemento del catalogo");
        System.out.println(" 7. Eliminar elemento del catalogo");
        System.out.println(" 8. Registrar jugador");
        System.out.println(" 9. Crear conexion entre jugadores");
        System.out.println("10. Eliminar conexion entre jugadores");
        System.out.println("11. Mostrar conexiones de un jugador");
        System.out.println("12. Ejecutar BFS (amigos por niveles)");
        System.out.println("13. Ejecutar DFS");
        System.out.println("14. Buscar camino entre dos jugadores");
        System.out.println("15. Detectar comunidades de jugadores");
        System.out.println("16. Jugadores por genero/categoria (integracion)");
        System.out.println("17. Eliminar jugador");
        System.out.println("18. Mostrar informacion completa del sistema");
        System.out.println(" 0. Salir");
    }

    private static void ejecutar(int op) {
        switch (op) {
            case 1 -> registrarElemento();
            case 2 -> System.out.print(sistema.mostrarCatalogo());
            case 3 -> buscarElemento();
            case 4 -> recorridos();
            case 5 -> nivelYAltura();
            case 6 -> {
                String id = leer("Id del elemento: ");
                System.out.println(sistema.modificarNombre(id, leer("Nuevo nombre: "))
                        ? "Elemento renombrado." : "No existe el elemento.");
            }
            case 7 -> eliminarElemento();
            case 8 -> {
                String id = leer("Id del jugador (ej. J14): ");
                boolean ok = sistema.registrarJugador(id, leer("Nickname: "), leer("Pais: "),
                        leer("Id del videojuego favorito (ej. JUE-CS2): "));
                System.out.println(ok ? "Jugador registrado."
                        : "No se pudo: id repetido o el juego favorito no existe.");
            }
            case 9 -> {
                String a = leer("Id jugador 1: "), b = leer("Id jugador 2: ");
                double lat = leerEntero("Latencia (ms): ");
                System.out.println(sistema.crearConexion(a, b, lat) ? "Conexion creada."
                        : "No se pudo: jugador inexistente, mismo jugador o conexion ya existente.");
            }
            case 10 -> {
                String a = leer("Id jugador 1: "), b = leer("Id jugador 2: ");
                System.out.println(sistema.eliminarConexion(a, b) ? "Conexion eliminada."
                        : "La conexion no existe.");
            }
            case 11 -> conexiones();
            case 12 -> bfs();
            case 13 -> dfs();
            case 14 -> camino();
            case 15 -> comunidades();
            case 16 -> {
                String id = leer("Id del genero/categoria/subgenero: ");
                ElementoCatalogo e = sistema.buscarElemento(id);
                if (e == null) { System.out.println("No existe en el catalogo."); break; }
                System.out.println("Jugadores cuyo juego favorito esta en '" + e.getNombre() + "':");
                for (Jugador j : sistema.jugadoresDeNodo(id)) {
                    System.out.println("  - " + j + " -> " + sistema.rutaJerarquica(j.getIdJuegoFavorito()));
                }
            }
            case 17 -> {
                String id = leer("Id del jugador: ");
                int grado = sistema.gradoDe(id);
                System.out.println(sistema.eliminarJugador(id)
                        ? "Jugador eliminado junto con sus " + grado + " conexiones."
                        : "No existe el jugador.");
            }
            case 18 -> informacion();
            case 0 -> { }
            default -> System.out.println("Opcion invalida.");
        }
    }

    private static void registrarElemento() {
        System.out.println("Tipos: CATEGORIA, GENERO, SUBGENERO, VIDEOJUEGO");
        String idPadre = leer("Id del padre: ");
        String id = leer("Id nuevo: ");
        String nombre = leer("Nombre: ");
        Tipo tipo = Tipo.valueOf(leer("Tipo: ").toUpperCase());
        boolean ok = sistema.registrarElemento(idPadre, id, nombre, tipo);
        System.out.println(ok ? "Registrado en: " + sistema.rutaJerarquica(id)
                : "No se pudo: padre inexistente o id repetido.");
    }

    private static void buscarElemento() {
        String id = leer("Id a buscar: ");
        ElementoCatalogo e = sistema.buscarElemento(id);
        if (e == null) { System.out.println("No se encontro '" + id + "'."); return; }
        System.out.println("Encontrado: " + e);
        System.out.println("Ruta: " + sistema.rutaJerarquica(id));
        ElementoCatalogo padre = sistema.padreDe(id);
        System.out.println("Padre: " + (padre == null ? "ninguno (es la raiz)" : padre));
        System.out.println("Hijos directos: " + sistema.hijosDe(id));
    }

    private static void recorridos() {
        System.out.println("En profundidad (preorden: padre y luego cada rama completa):");
        imprimirLista(sistema.recorridoProfundidad());
        System.out.println("Por niveles (cola: primero lo cercano a la raiz):");
        imprimirLista(sistema.recorridoPorNiveles());
    }

    private static void nivelYAltura() {
        System.out.println("Altura del catalogo: " + sistema.alturaCatalogo()
                + " | Total de elementos: " + sistema.totalElementos());
        String id = leer("Id del elemento (nivel y altura de su subarbol): ");
        if (sistema.nivelDe(id) < 0) { System.out.println("No existe."); return; }
        System.out.println("Nivel: " + sistema.nivelDe(id) + " | Altura del subarbol: " + sistema.alturaSubarbol(id));
    }

    private static void eliminarElemento() {
        String id = leer("Id a eliminar: ");
        if (sistema.eliminarElemento(id)) { System.out.println("Eliminado."); return; }
        if (sistema.buscarElemento(id) == null) { System.out.println("No existe."); return; }
        if (sistema.hijosDe(id).isEmpty()) { System.out.println("La raiz no se puede eliminar."); return; }
        System.out.println("Tiene hijos: " + sistema.hijosDe(id));
        if (leer("Eliminar tambien todos sus descendientes? (s/n): ").equalsIgnoreCase("s")) {
            System.out.println(sistema.eliminarElementoConDescendientes(id) ? "Subarbol eliminado." : "No se pudo.");
        } else {
            System.out.println("Operacion cancelada.");
        }
    }

    private static void conexiones() {
        String id = leer("Id del jugador: ");
        Jugador j = sistema.buscarJugador(id);
        if (j == null) { System.out.println("No existe el jugador."); return; }
        System.out.println("Conexiones de " + j + " (grado " + sistema.gradoDe(id) + "):");
        for (Conexion<Jugador> e : sistema.conexionesDe(id)) System.out.println("  - " + e);
        String otro = leer("Id de otro jugador para verificar conexion directa (Enter para omitir): ");
        if (!otro.isEmpty()) {
            System.out.println(sistema.existeConexion(id, otro) ? "Si hay conexion directa." : "No hay conexion directa.");
        }
    }

    private static void bfs() {
        String id = leer("Id del jugador inicial: ");
        Map<Jugador, Integer> r = sistema.bfs(id);
        if (r.isEmpty()) { System.out.println("No existe el jugador."); return; }
        int nivel = -1;
        for (Map.Entry<Jugador, Integer> e : r.entrySet()) {
            if (e.getValue() != nivel) {
                nivel = e.getValue();
                System.out.println("Nivel " + nivel + ":");
            }
            System.out.println("   " + e.getKey());
        }
    }

    private static void dfs() {
        String id = leer("Id del jugador inicial: ");
        List<Jugador> r = sistema.dfs(id);
        if (r.isEmpty()) { System.out.println("No existe el jugador."); return; }
        System.out.println("Orden DFS:");
        imprimirLista(r);
    }

    private static void camino() {
        String a = leer("Jugador origen: "), b = leer("Jugador destino: ");
        List<Jugador> saltos = sistema.caminoMinimoSaltos(a, b);
        if (saltos == null) { System.out.println("No existe camino (o algun jugador no existe)."); return; }
        System.out.println("Menos conexiones (BFS, " + (saltos.size() - 1) + " saltos): " + nombres(saltos));
        GrafoPonderado.Ruta<Jugador> r = sistema.rutaMenorLatencia(a, b);
        System.out.println("Menor latencia (Dijkstra, " + r.costo + " ms): " + nombres(r.camino));
    }

    private static void comunidades() {
        List<List<Jugador>> cs = sistema.comunidades();
        System.out.println("Comunidades encontradas: " + cs.size());
        for (int i = 0; i < cs.size(); i++) {
            System.out.println("  Comunidad " + (i + 1) + " (" + cs.get(i).size() + "): " + nombres(cs.get(i)));
        }
    }

    private static void informacion() {
        System.out.println("--- CATALOGO ---");
        System.out.print(sistema.mostrarCatalogo());
        System.out.println("Elementos: " + sistema.totalElementos() + " | Altura: " + sistema.alturaCatalogo());
        System.out.println("--- RED DE JUGADORES ---");
        System.out.println("Jugadores: " + sistema.totalJugadores() + " | Conexiones: " + sistema.totalConexiones());
        for (List<Jugador> grupo : sistema.comunidades()) {
            for (Jugador j : grupo) {
                StringBuilder sb = new StringBuilder();
                for (Conexion<Jugador> e : sistema.conexionesDe(j.getId())) {
                    sb.append(e.getDestination().getNickname()).append('(').append((int) e.getWeight()).append(") ");
                }
                System.out.println("  " + j.getNickname() + " [" + sistema.rutaJerarquica(j.getIdJuegoFavorito()).replace("Plataforma de Videojuegos > ", "") + "] -> " + sb);
            }
        }
    }

    // ---------- utilidades ----------

    private static String nombres(List<Jugador> l) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < l.size(); i++) {
            if (i > 0) sb.append(" -> ");
            sb.append(l.get(i).getNickname());
        }
        return sb.toString();
    }

    private static <T> void imprimirLista(List<T> l) {
        for (int i = 0; i < l.size(); i++) System.out.println("  " + (i + 1) + ". " + l.get(i));
    }

    private static String leer(String msg) {
        System.out.print(msg);
        return in.nextLine().trim();
    }

    private static int leerEntero(String msg) {
        while (true) {
            try {
                return Integer.parseInt(leer(msg));
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un numero valido.");
            }
        }
    }
}
