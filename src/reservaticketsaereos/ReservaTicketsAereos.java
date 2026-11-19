package reservaticketsaereos;

import reservaticketsaereos.controlador.ReservaSegura;
import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.modelo.Avion;

public class ReservaTicketsAereos {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   SISTEMA DE RESERVA DE TICKETS AÉREOS - COMPARACIÓN CONCURRENTE");
        System.out.println("===============================================================\n");

        // ------------------------------------------------------------------
        // PRUEBA 1: RESERVAS SIN EXCLUSIÓN MUTUA (Xavier León)
        // ------------------------------------------------------------------
        System.out.println(">>> CASO 1: SIMULACIÓN SIN CONTROL (CONDICIONES DE CARRERA)");
        Avion avionInseguro = new Avion();
        ReservaSinControl gestorInseguro = new ReservaSinControl(avionInseguro);
        final int asientoInseguro = 7;

        Thread h1 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "Carlos Mendoza", "Ventanilla-1"));
        Thread h2 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "María López", "Ventanilla-2"));
        Thread h3 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "Andrés Torres", "Ventanilla-3"));

        h1.start();
        h2.start();
        h3.start();

        try {
            h1.join();
            h2.join();
            h3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n[RESUMEN CASO 1 - SIN CONTROL]");
        System.out.printf("Colisiones detectadas: %d\n", gestorInseguro.getColisionesDetectadas());
        System.out.printf("Titular final registrado en Asiento #%d: '%s'\n\n",
                asientoInseguro, avionInseguro.getAsiento(asientoInseguro).getCliente());

        // ------------------------------------------------------------------
        // PRUEBA 2: RESERVAS CON EXCLUSIÓN MUTUA (Juan Marcillo)
        // ------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 2: SIMULACIÓN CON EXCLUSIÓN MUTUA (REENTRANTLOCK)");
        Avion avionSeguro = new Avion();
        ReservaSegura gestorSeguro = new ReservaSegura(avionSeguro);
        final int asientoSeguro = 7;

        Thread s1 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "Carlos Mendoza", "Ventanilla-1"));
        Thread s2 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "María López", "Ventanilla-2"));
        Thread s3 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "Andrés Torres", "Ventanilla-3"));

        s1.start();
        s2.start();
        s3.start();

        try {
            s1.join();
            s2.join();
            s3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("\n[RESUMEN CASO 2 - SEGURO]");
        System.out.printf("Reservas exitosas: %d | Reservas rechazadas: %d\n", 
                gestorSeguro.getReservasExitosas(), gestorSeguro.getReservasRechazadas());
        System.out.printf("Titular final y único en Asiento #%d: '%s'\n",
                asientoSeguro, avionSeguro.getAsiento(asientoSeguro).getCliente());
        System.out.println("Conclusión: La exclusión mutua garantiza que NUNCA ocurra sobreventa.");
        System.out.println("===============================================================");
    }
}
