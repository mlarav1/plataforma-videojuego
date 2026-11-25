package model;

import java.util.Objects;

/**
 * Jugador de la plataforma. Es un vertice del grafo de conexiones.
 * Su juego favorito referencia un videojuego (nodo del arbol), lo que
 * integra las dos estructuras en una sola aplicacion.
 */
public class Jugador {

    private final String id;
    private String nickname;
    private String pais;
    private String idJuegoFavorito;

    public Jugador(String id, String nickname, String pais, String idJuegoFavorito) {
        this.id = id;
        this.nickname = nickname;
        this.pais = pais;
        this.idJuegoFavorito = idJuegoFavorito;
    }

    public String getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getIdJuegoFavorito() {
        return idJuegoFavorito;
    }

    public void setIdJuegoFavorito(String idJuegoFavorito) {
        this.idJuegoFavorito = idJuegoFavorito;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jugador)) return false;
        return id.equals(((Jugador) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nickname + " (" + id + ", " + pais + ")";
    }
}
