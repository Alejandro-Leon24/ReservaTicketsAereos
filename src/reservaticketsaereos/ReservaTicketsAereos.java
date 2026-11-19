package reservaticketsaereos;

import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.modelo.Avion;

public class ReservaTicketsAereos {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   SISTEMA DE RESERVA DE TICKETS AÉREOS - SIMULACIÓN CONCURRENTE");
        System.out.println("   Módulo: Reservas sin exclusión mutua (Xavier León)");
        System.out.println("===============================================================\n");

        Avion avion = new Avion();
        ReservaSinControl gestorInseguro = new ReservaSinControl(avion);

        // Simularemos 3 ventanillas (hilos) intentando reservar el mismo asiento (Asiento 7) al mismo tiempo
        final int asientoObjetivo = 7;

        Runnable tareaVentanilla1 = () -> {
            gestorInseguro.reservar(asientoObjetivo, "Carlos Mendoza", "Ventanilla-1");
        };

        Runnable tareaVentanilla2 = () -> {
            gestorInseguro.reservar(asientoObjetivo, "María López", "Ventanilla-2");
        };

        Runnable tareaVentanilla3 = () -> {
            gestorInseguro.reservar(asientoObjetivo, "Andrés Torres", "Ventanilla-3");
        };

        Thread h1 = new Thread(tareaVentanilla1);
        Thread h2 = new Thread(tareaVentanilla2);
        Thread h3 = new Thread(tareaVentanilla3);

        System.out.printf("Iniciando 3 hilos concurrentes compitiendo por el Asiento #%d...\n\n", asientoObjetivo);

        // Disparamos los 3 hilos en simultáneo
        h1.start();
        h2.start();
        h3.start();

        try {
            // Esperamos que los hilos terminen
            h1.join();
            h2.join();
            h3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n----------------- REPORTE FINAL DEL ESTADO -----------------");
        System.out.printf("Total colisiones/errores de concurrencia detectados: %d\n", gestorInseguro.getColisionesDetectadas());
        System.out.printf("Titular final registrado en Asiento #%d: '%s'\n", 
                asientoObjetivo, avion.getAsiento(asientoObjetivo).getCliente());
        System.out.println("Nota: En una reserva segura, solo UNA ventanilla debió tener éxito");
        System.out.println("y las demás debieron ser rechazadas o puestas en cola de espera.");
        System.out.println("------------------------------------------------------------\n");
    }
}
