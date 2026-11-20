package reservaticketsaereos.controlador;

import java.util.function.Consumer;
import reservaticketsaereos.modelo.ColaEspera;
import reservaticketsaereos.modelo.SolicitudReserva;

/**
 * Modulo: Ventanilla de atencion concurrente (Hilo de procesamiento)
 * Responsable: Mero Araujo Jeremy Joao
 * 
 * Modela cada ventanilla física como un hilo independiente:
 * - Genera retardos aleatorios de procesamiento de 1 a 3 segundos (Thread.sleep).
 * - En modo seguro: si el asiento esta ocupado, se bloquea y entra a la ColaEspera.
 * - En cuanto el asiento se libera, se asigna automaticamente al hilo en espera.
 * - En modo inseguro: simula colisiones de carrera simultaneas.
 */
public class Ventanilla {

    private final String nombre;
    private final ReservaSegura gestorSeguro;
    private final ReservaSinControl gestorInseguro;
    private final GestionReservas gestionReservas;
    private final ColaEspera colaEspera;
    private Consumer<String> logger;

    public Ventanilla(String nombre, ReservaSegura gestorSeguro, ReservaSinControl gestorInseguro,
                      GestionReservas gestionReservas, ColaEspera colaEspera) {
        this.nombre = nombre;
        this.gestorSeguro = gestorSeguro;
        this.gestorInseguro = gestorInseguro;
        this.gestionReservas = gestionReservas;
        this.colaEspera = colaEspera;
        this.logger = System.out::println;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    private void log(String msg) {
        if (logger != null) {
            logger.accept(msg);
        }
    }

    /**
     * Ejecuta una operacion de reserva simulando tiempo de atencion de 1 a 3 segundos.
     * Si el asiento esta ocupado y el modo es seguro, bloquea el hilo y espera liberacion.
     */
    public boolean procesarReserva(int numeroAsiento, String cliente, boolean modoSeguro) {
        try {
            // Retardo aleatorio de 1 a 3 segundos como especifica el diagrama del docente
            long retardo = (long) (Math.random() * 2000 + 1000);
            log(String.format("[%s] Atendiendo a '%s' para Asiento #%d (procesando en %d ms)...",
                    nombre, cliente, numeroAsiento, retardo));
            Thread.sleep(retardo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }

        if (!modoSeguro) {
            // Modo Sin Exclusion Mutua: genera condiciones de carrera directamente
            return gestorInseguro.reservar(numeroAsiento, cliente, nombre);
        }

        // Modo Con Exclusion Mutua
        boolean exito = gestorSeguro.reservar(numeroAsiento, cliente, nombre);

        if (!exito) {
            // El asiento ya estaba ocupado: requerimiento de bloqueo y cola de espera
            log(String.format(">>> [%s] Asiento #%d OCUPADO. '%s' entra a Lista de Espera y queda bloqueado.",
                    nombre, numeroAsiento, cliente));
            colaEspera.agregarClienteAEspera(new SolicitudReserva(cliente, numeroAsiento, nombre));

            try {
                // Hilo bloqueado esperando a que el asiento se libere
                colaEspera.esperarLiberacionAsiento(numeroAsiento);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return exito;
    }

    /**
     * Elimina una reservacion y notifica a la cola de espera para asignacion automatica.
     */
    public boolean procesarEliminacion(int numeroAsiento, String cliente) {
        try {
            long retardo = (long) (Math.random() * 1000 + 500);
            Thread.sleep(retardo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean exito = gestionReservas.cancelarReserva(numeroAsiento, cliente, nombre);
        if (exito) {
            log(String.format("[%s] Asiento #%d liberado por '%s'. Verificando cola de espera...",
                    nombre, numeroAsiento, cliente));

            // ASIGNACION AUTOMATICA: si hay un hilo esperando por este asiento, se le asigna inmediatamente
            SolicitudReserva esperando = colaEspera.notificarAsientoLiberado(numeroAsiento);
            if (esperando != null) {
                gestorSeguro.reservar(numeroAsiento, esperando.getCliente(), "AutoAsignacion(" + esperando.getVentanilla() + ")");
                log(String.format(">>> [ASIGNACION AUTOMATICA] Asiento #%d otorgado inmediatamente a '%s' (orden de llegada).",
                        numeroAsiento, esperando.getCliente()));
            }
        }
        return exito;
    }

    /**
     * Procesa el cambio de asiento de un cliente y revisa si el asiento anterior tenia espera.
     */
    public boolean procesarCambio(int asientoActual, int nuevoAsiento, String cliente) {
        try {
            long retardo = (long) (Math.random() * 1000 + 500);
            Thread.sleep(retardo);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean exito = gestionReservas.cambiarAsiento(asientoActual, nuevoAsiento, cliente, nombre);
        if (exito) {
            log(String.format("[%s] Cambio exitoso. Asiento anterior #%d liberado. Revisando cola...",
                    nombre, asientoActual));

            // Si habia alguien esperando por el asiento anterior que acaba de quedar libre
            SolicitudReserva esperando = colaEspera.notificarAsientoLiberado(asientoActual);
            if (esperando != null) {
                gestorSeguro.reservar(asientoActual, esperando.getCliente(), "AutoAsignacion(" + esperando.getVentanilla() + ")");
                log(String.format(">>> [ASIGNACION AUTOMATICA] Asiento anterior #%d otorgado a '%s' de la cola.",
                        asientoActual, esperando.getCliente()));
            }
        }
        return exito;
    }

    public String getNombre() {
        return nombre;
    }
}
