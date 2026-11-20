package reservaticketsaereos.modelo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Modulo: Cola de espera sincronizada
 * Responsable: Mero Araujo Jeremy Joao
 * 
 * Estructura concurrente basada en el patron Productor-Consumidor.
 * - Mantiene el orden estricto de llegada (FIFO).
 * - Utiliza wait() y notifyAll() para coordinar los hilos de las ventanillas.
 * - Evita bloqueos indefinidos mediante timeouts configurables.
 */
public class ColaEspera {

    private final Queue<SolicitudReserva> cola;
    private final int capacidadMaxima;
    private boolean cerrada;

    public ColaEspera(int capacidadMaxima) {
        this.cola = new LinkedList<>();
        this.capacidadMaxima = capacidadMaxima;
        this.cerrada = false;
    }

    public ColaEspera() {
        this(50); // Capacidad por defecto
    }

    /**
     * Encola un nuevo cliente. Notifica a las ventanillas dormidas.
     */
    public synchronized boolean agregarCliente(SolicitudReserva solicitud) {
        if (cerrada) {
            return false;
        }

        while (cola.size() >= capacidadMaxima) {
            try {
                wait(); // Espera si la cola esta llena
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        cola.offer(solicitud);
        System.out.printf("[COLA] Llega cliente: %s (En espera: %d)\n", solicitud, cola.size());
        notifyAll(); // Despierta a las ventanillas que esperan clientes
        return true;
    }

    /**
     * Extrae el siguiente cliente. Si la cola esta vacia, el hilo espera hasta timeoutMs.
     */
    public synchronized SolicitudReserva siguienteCliente(long timeoutMs) {
        long tiempoLimite = System.currentTimeMillis() + timeoutMs;

        while (cola.isEmpty() && !cerrada) {
            long tiempoRestante = tiempoLimite - System.currentTimeMillis();
            if (tiempoRestante <= 0) {
                return null; // Timeout alcanzado, evita bloqueo indefinido
            }
            try {
                wait(tiempoRestante);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return null;
            }
        }

        if (cola.isEmpty()) {
            return null;
        }

        SolicitudReserva solicitud = cola.poll();
        notifyAll(); // Notifica por si habia productores esperando espacio
        return solicitud;
    }

    /**
     * Notifica a todos los hilos en espera cuando se libera un asiento.
     */
    public synchronized void notificarAsientoLiberado() {
        System.out.println("[COLA] Notificacion general: Se ha liberado un asiento.");
        notifyAll();
    }

    /**
     * Cierra la cola al finalizar la jornada.
     */
    public synchronized void cerrar() {
        this.cerrada = true;
        notifyAll();
    }

    public synchronized boolean estaVacia() {
        return cola.isEmpty();
    }

    public synchronized int getTamano() {
        return cola.size();
    }

    /**
     * Retorna una copia de la lista en espera para visualizacion en la GUI.
     */
    public synchronized List<SolicitudReserva> getListaEspera() {
        return new ArrayList<>(cola);
    }
}
