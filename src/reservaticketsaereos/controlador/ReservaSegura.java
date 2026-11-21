package reservaticketsaereos.controlador;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.Avion;

/**
 * Módulo: Exclusión mutua (Control de concurrencia seguro)
 * Responsable: Marcillo Pico Juan Sebastián
 * 
 * Implementa la versión protegida utilizando ReentrantLock con política equitativa (fairness=true).
 * Garantiza atomicidad en las operaciones de consulta y modificación (Check-Then-Act),
 * evitando sobreventa o inconsistencia entre múltiples ventanillas.
 */
public class ReservaSegura {

    private final Avion avion;
    // Lock con política FIFO (fairness = true) para atender en estricto orden de solicitud
    private final ReentrantLock lock;

    private int reservasExitosas;
    private int reservasRechazadas;
    private Consumer<String> logger;

    public ReservaSegura(Avion avion) {
        this.avion = avion;
        this.lock = new ReentrantLock(true);
        this.reservasExitosas = 0;
        this.reservasRechazadas = 0;
        this.logger = System.out::println;
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    private void emitirLog(String mensaje) {
        System.out.println(mensaje);
        if (logger != null) {
            logger.accept(mensaje);
        }
    }

    /**
     * Reserva un asiento garantizando exclusión mutua.
     */
    public boolean reservar(int numeroAsiento, String cliente, String ventanilla) {
        lock.lock(); // ENTRADA A SECCIÓN CRÍTICA
        try {
            emitirLog(String.format("[%s] [SEGURO] Evaluando Asiento #%d para '%s'...",
                    ventanilla, numeroAsiento, cliente));

            if (avion.isAsientoDisponible(numeroAsiento)) {
                // Simulación de tiempo de procesamiento seguro dentro de la sección crítica (1 a 2 seg)
                try {
                    Thread.sleep((long) (Math.random() * 1000 + 1000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                avion.ocuparAsiento(numeroAsiento, cliente);
                reservasExitosas++;
                emitirLog(String.format("[%s] [SEGURO] EXITO: Asiento #%d asignado exitosamente a '%s'.",
                        ventanilla, numeroAsiento, cliente));
                return true;
            } else {
                reservasRechazadas++;
                Asiento ocupadoPor = avion.getAsiento(numeroAsiento);
                String titular = (ocupadoPor != null) ? ocupadoPor.getCliente() : "Desconocido";
                emitirLog(String.format("[%s] [SEGURO] DENEGADO: Asiento #%d ya fue ocupado previamente por '%s'.",
                        ventanilla, numeroAsiento, titular));
                return false;
            }
        } finally {
            lock.unlock(); // SALIDA DE SECCIÓN CRÍTICA
        }
    }

    /**
     * Cancela una reserva existente de forma atómica y segura.
     */
    public boolean cancelar(int numeroAsiento, String cliente, String ventanilla) {
        lock.lock();
        try {
            Asiento asiento = avion.getAsiento(numeroAsiento);
            if (asiento != null && asiento.isOcupado() && asiento.getCliente().equalsIgnoreCase(cliente)) {
                avion.liberarAsiento(numeroAsiento);
                emitirLog(String.format("[%s] [SEGURO] CANCELACION EXITOSA: Asiento #%d liberado por '%s'.",
                        ventanilla, numeroAsiento, cliente));
                return true;
            } else {
                emitirLog(String.format("[%s] [SEGURO] ERROR CANCELACION: Asiento #%d no pertenece a '%s' o no esta ocupado.",
                        ventanilla, numeroAsiento, cliente));
                return false;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Cambia un asiento por otro de forma atómica (libera uno y ocupa el otro sin riesgo de estado intermedio).
     */
    public boolean cambiar(int asientoActual, int nuevoAsiento, String cliente, String ventanilla) {
        lock.lock();
        try {
            Asiento origen = avion.getAsiento(asientoActual);
            // 1. Validar que el asiento origen sea del cliente
            if (origen == null || !origen.isOcupado() || !origen.getCliente().equalsIgnoreCase(cliente)) {
                emitirLog(String.format("[%s] [SEGURO] CAMBIO FALLIDO: El cliente '%s' no posee el Asiento #%d.",
                        ventanilla, cliente, asientoActual));
                return false;
            }

            // 2. Validar que el nuevo asiento esté disponible
            if (!avion.isAsientoDisponible(nuevoAsiento)) {
                emitirLog(String.format("[%s] [SEGURO] CAMBIO FALLIDO: El nuevo Asiento #%d no esta disponible.",
                        ventanilla, nuevoAsiento));
                return false;
            }

            // 3. Ejecución atómica del cambio
            avion.liberarAsiento(asientoActual);
            avion.ocuparAsiento(nuevoAsiento, cliente);
            emitirLog(String.format("[%s] [SEGURO] CAMBIO EXITOSO: Cliente '%s' movido del Asiento #%d al Asiento #%d.",
                    ventanilla, cliente, asientoActual, nuevoAsiento));
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int getReservasExitosas() {
        return reservasExitosas;
    }

    public int getReservasRechazadas() {
        return reservasRechazadas;
    }

    public Avion getAvion() {
        return avion;
    }
}
