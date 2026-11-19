package reservaticketsaereos.controlador;

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

    public ReservaSinControl(Avion avion) {
        this.avion = avion;
        this.colisionesDetectadas = 0;
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
        System.out.printf("[%s] Consulta disponibilidad del Asiento #%d para '%s'...\n", 
                ventanilla, numeroAsiento, cliente);

        // 1. Lectura del estado (Check)
        if (avion.isAsientoDisponible(numeroAsiento)) {
            System.out.printf("[%s] Asiento #%d visible como LIBRE para '%s'. Procesando pago...\n", 
                    ventanilla, numeroAsiento, cliente);

            try {
                // 2. Simulación de retardo de red o procesamiento (50 a 150 ms)
                // Durante esta pausa, otro hilo puede leer el asiento también como LIBRE.
                long retardo = (long) (Math.random() * 100 + 50);
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
                System.err.printf(">>> [¡ERROR DE CONCURRENCIA!] [%s] Sobreescribe Asiento #%d. Estaba reservado por '%s' y ahora se asigna a '%s'\n",
                        ventanilla, numeroAsiento, asientoActual.getCliente(), cliente);
            }

            avion.ocuparAsiento(numeroAsiento, cliente);
            System.out.printf("[%s] Reserva COMPLETADA del Asiento #%d para '%s'.\n", 
                    ventanilla, numeroAsiento, cliente);
            return true;
        } else {
            System.out.printf("[%s] Asiento #%d YA ESTÁ OCUPADO. Reserva denegada para '%s'.\n", 
                    ventanilla, numeroAsiento, cliente);
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
