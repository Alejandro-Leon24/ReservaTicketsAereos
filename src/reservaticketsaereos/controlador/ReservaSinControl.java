package reservaticketsaereos.controlador;

import java.util.function.Consumer;
import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.Avion;

/**
 * Módulo: Reservas sin exclusión mutua (Simulación de condiciones de carrera)
 * Responsable: León Salazar Xavier Alejandro
 * 
 * En esta clase se ejecutan reservas concurrentes SIN protección (sin synchronized ni Lock).
 * Se introduce un retardo artificial (Thread.sleep) entre la verificación de disponibilidad
 * y la asignación del asiento para evidenciar el problema clásico de Check-Then-Act,
 * provocando sobreventa o sobreescritura de asientos (Race Condition).
 */
public class ReservaSinControl {

    private final Avion avion;
    private int colisionesDetectadas;
    private Consumer<String> logger;

    public ReservaSinControl(Avion avion) {
        this.avion = avion;
        this.colisionesDetectadas = 0;
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
     * Intenta reservar un asiento sin control de concurrencia.
     * 
     * @param numeroAsiento Asiento solicitado (1 al 30)
     * @param cliente Nombre del cliente que solicita la reserva
     * @param ventanilla Identificador del hilo/ventanilla que atiende
     * @return true si la ventanilla creyó haber reservado con éxito
     */
    public boolean reservar(int numeroAsiento, String cliente, String ventanilla) {
        emitirLog(String.format("[%s] Consulta disponibilidad del Asiento #%d para '%s'...",
                ventanilla, numeroAsiento, cliente));

        // 1. Lectura del estado (Check)
        if (avion.isAsientoDisponible(numeroAsiento)) {
            emitirLog(String.format("[%s] Asiento #%d visible como LIBRE para '%s'. Procesando transaccion...",
                    ventanilla, numeroAsiento, cliente));

            try {
                // 2. Simulación de retardo concurrente (1 a 2 segundos)
                // Durante esta pausa critica, los otros hilos consultan y tambien ven el asiento LIBRE.
                long retardo = (long) (Math.random() * 1000 + 1000);
                Thread.sleep(retardo);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("Hilo interrumpido: " + e.getMessage());
            }

            // 3. Modificación del estado (Act) - SIN EXCLUSIÓN MUTUA
            Asiento asientoActual = avion.getAsiento(numeroAsiento);
            if (asientoActual != null && asientoActual.isOcupado()) {
                // Si ya estaba ocupado al despertar, ¡ocurrió una colisión de carrera!
                colisionesDetectadas++;
                emitirLog(String.format(">>> [ERROR CONCURRENCIA] [%s] SOBREESCRIBE Asiento #%d. Estaba reservado por '%s' y se reasigna a '%s' (Condicion de carrera)",
                        ventanilla, numeroAsiento, asientoActual.getCliente(), cliente));
            }

            avion.ocuparAsiento(numeroAsiento, cliente);
            emitirLog(String.format("[%s] Reserva COMPLETADA del Asiento #%d para '%s'.",
                    ventanilla, numeroAsiento, cliente));
            return true;
        } else {
            emitirLog(String.format("[%s] Asiento #%d YA ESTA OCUPADO. Reserva denegada para '%s'.",
                    ventanilla, numeroAsiento, cliente));
            return false;
        }
    }

    public int getColisionesDetectadas() {
        return colisionesDetectadas;
    }

    public Avion getAvion() {
        return avion;
    }
}
