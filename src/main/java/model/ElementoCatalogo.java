package model;

import java.util.Objects;

/**
 * Elemento del catalogo jerarquico de la plataforma de videojuegos.
 * Cada nodo del arbol es una ElementoCatalogo: plataforma, categoria,
 * genero, subgenero o videojuego.
 */
public class ElementoCatalogo {

    public enum Tipo {
        PLATAFORMA, CATEGORIA, GENERO, SUBGENERO, VIDEOJUEGO
    }

    private final String id;
    private String nombre;
    private Tipo tipo;

    public ElementoCatalogo(String id, String nombre, Tipo tipo) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ElementoCatalogo)) return false;
        return id.equals(((ElementoCatalogo) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return nombre + " [" + tipo + ", " + id + "]";
    }
}
