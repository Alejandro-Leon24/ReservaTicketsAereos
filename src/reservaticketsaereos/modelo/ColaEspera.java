package reservaticketsaereos.modelo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * Modulo: Cola de espera sincronizada por asiento
 * Responsable: Mero Araujo Jeremy Joao
 * 
 * Gestiona el bloqueo y espera de hilos cuando un asiento solicitado ya esta ocupado.
 * Implementa:
 * - Registro ordenado (FIFO) de solicitudes por cada numero de asiento.
 * - Bloqueo coordinado mediante wait().
 * - Despertar y asignacion automatica al liberarse el asiento con notifyAll().
 */
public class ColaEspera {

    // Mapa ordenado: Asiento -> Cola de solicitudes en espera para ese asiento
    private final Map<Integer, Queue<SolicitudReserva>> colaPorAsiento;
    private final List<Runnable> listenersCambio;

    public ColaEspera(int capacidadIgnorada) {
        this.colaPorAsiento = new LinkedHashMap<>();
        this.listenersCambio = new ArrayList<>();
    }

    public ColaEspera() {
        this(50);
    }

    /**
     * Registra un cliente en la cola de espera de un asiento especifico.
     */
    public synchronized void agregarClienteAEspera(SolicitudReserva solicitud) {
        int asiento = solicitud.getAsientoDeseado();
        colaPorAsiento.putIfAbsent(asiento, new LinkedList<>());
        colaPorAsiento.get(asiento).offer(solicitud);
        System.out.printf("[COLA] Cliente '%s' en espera del Asiento #%d desde '%s'.\n",
                solicitud.getCliente(), asiento, solicitud.getVentanilla());
        notificarCambioVisual();
    }

    /**
     * Bloquea el hilo actual hasta que el asiento solicitado sea liberado.
     */
    public synchronized void esperarLiberacionAsiento(int numeroAsiento) throws InterruptedException {
        while (tieneClientesEnEspera(numeroAsiento)) {
            wait(); // El hilo queda suspendido esperando que se libere el asiento
            break;
        }
    }

    /**
     * Cuando se elimina una reservacion o se libera un asiento, extrae al primer cliente
     * que estaba esperando por ese asiento y despierta a los hilos con notifyAll().
     * 
     * @param numeroAsiento Asiento que acaba de quedar libre
     * @return El cliente en espera que tiene derecho al asiento, o null si nadie esperaba
     */
    public synchronized SolicitudReserva notificarAsientoLiberado(int numeroAsiento) {
        Queue<SolicitudReserva> cola = colaPorAsiento.get(numeroAsiento);
        if (cola != null && !cola.isEmpty()) {
            SolicitudReserva siguiente = cola.poll();
            if (cola.isEmpty()) {
                colaPorAsiento.remove(numeroAsiento);
            }
            System.out.printf("[COLA] Asiento #%d liberado. Asignando automaticamente a '%s' (orden de llegada).\n",
                    numeroAsiento, siguiente.getCliente());
            notifyAll(); // Despierta a los hilos esperando
            notificarCambioVisual();
            return siguiente;
        }
        notifyAll();
        return null;
    }

    public synchronized boolean tieneClientesEnEspera(int numeroAsiento) {
        Queue<SolicitudReserva> cola = colaPorAsiento.get(numeroAsiento);
        return cola != null && !cola.isEmpty();
    }

    public synchronized int getTotalEnEspera() {
        int total = 0;
        for (Queue<SolicitudReserva> q : colaPorAsiento.values()) {
            total += q.size();
        }
        return total;
    }

    /**
     * Genera la lista formateada para mostrar en la interfaz grafica:
     * Asiento X
     * Usuario Y
     */
    public synchronized List<String> getListaEsperaFormateada() {
        List<String> items = new ArrayList<>();
        for (Map.Entry<Integer, Queue<SolicitudReserva>> entry : colaPorAsiento.entrySet()) {
            int numAsiento = entry.getKey();
            items.add("Asiento " + numAsiento);
            for (SolicitudReserva sol : entry.getValue()) {
                items.add("  " + sol.getCliente() + " (" + sol.getVentanilla() + ")");
            }
        }
        return items;
    }

    public synchronized void registrarListener(Runnable listener) {
        listenersCambio.add(listener);
    }

    private void notificarCambioVisual() {
        for (Runnable r : listenersCambio) {
            try {
                r.run();
            } catch (Exception ignored) {}
        }
    }
}
