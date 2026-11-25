package graph;

/**
 * Arista de la lista de adyacencia: destino y peso.
 * En esta plataforma el peso es la latencia (ms) entre dos jugadores.
 */
public class Conexion<T> {

    private final T destination;
    private double weight;

    public Conexion(T destination, double weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public T getDestination() {
        return destination;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        return destination + " (" + weight + " ms)";
    }
}
