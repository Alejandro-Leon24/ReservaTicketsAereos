package reservaticketsaereos.controlador;

import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.ColaEspera;
import reservaticketsaereos.modelo.SolicitudReserva;

/**
 * Modulo: Ventanilla de atencion concurrente
 * Responsable: Mero Araujo Jeremy Joao
 * 
 * Cada ventanilla actua como un hilo trabajador (Worker Thread) independiente.
 * Toma clientes de la ColaEspera y ejecuta las reservas a traves del gestor seguro.
 */
public class Ventanilla implements Runnable {

    private final String nombre;
    private final ColaEspera cola;
    private final ReservaSegura gestorSeguro;
    private volatile boolean activa;
    private int clientesAtendidos;

    public Ventanilla(String nombre, ColaEspera cola, ReservaSegura gestorSeguro) {
        this.nombre = nombre;
        this.cola = cola;
        this.gestorSeguro = gestorSeguro;
        this.activa = true;
        this.clientesAtendidos = 0;
    }

    @Override
    public void run() {
        System.out.printf("[%s] Inicia jornada laboral y espera clientes...\n", nombre);

        while (activa) {
            // Espera al siguiente cliente con un timeout de 800ms para evitar bloqueos
            SolicitudReserva solicitud = cola.siguienteCliente(800);

            if (solicitud != null) {
                atenderCliente(solicitud);
            }
        }

        System.out.printf("[%s] Jornada finalizada. Total clientes atendidos: %d\n", 
                nombre, clientesAtendidos);
    }

    private void atenderCliente(SolicitudReserva solicitud) {
        System.out.printf("[%s] Atendiendo a '%s' (Solicita %s)...\n",
                nombre, solicitud.getCliente(), 
                solicitud.getAsientoDeseado() > 0 ? "Asiento #" + solicitud.getAsientoDeseado() : "Cualquier asiento");

        // Simula tiempo de digitacion y atencion al cliente (80 a 150 ms)
        try {
            Thread.sleep((long) (Math.random() * 70 + 80));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        int asientoDeseado = solicitud.getAsientoDeseado();
        boolean exito = false;

        if (asientoDeseado > 0) {
            // Intenta reservar el asiento especifico
            exito = gestorSeguro.reservar(asientoDeseado, solicitud.getCliente(), nombre);
        } else {
            // Asignar el primer asiento libre disponible
            for (Asiento a : gestorSeguro.getAvion().getTodosLosAsientos()) {
                if (gestorSeguro.reservar(a.getNumero(), solicitud.getCliente(), nombre)) {
                    exito = true;
                    break;
                }
            }
        }

        if (!exito) {
            System.out.printf("[%s] No fue posible asignar el asiento deseado a '%s'.\n", 
                    nombre, solicitud.getCliente());
        }

        clientesAtendidos++;
    }

    public void detener() {
        this.activa = false;
    }

    public String getNombre() {
        return nombre;
    }

    public int getClientesAtendidos() {
        return clientesAtendidos;
    }
}
