package reservaticketsaereos;

import java.util.List;
import reservaticketsaereos.controlador.GestionReservas;
import reservaticketsaereos.controlador.ReservaSegura;
import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.modelo.Avion;

public class ReservaTicketsAereos {

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   SISTEMA DE RESERVA DE TICKETS AEREOS - PRUEBAS DE MODULOS");
        System.out.println("===============================================================\n");

        // ------------------------------------------------------------------
        // CASO 1: RESERVAS SIN EXCLUSION MUTUA (Xavier Leon)
        // ------------------------------------------------------------------
        System.out.println(">>> CASO 1: SIMULACION SIN CONTROL (CONDICIONES DE CARRERA)");
        Avion avionInseguro = new Avion();
        ReservaSinControl gestorInseguro = new ReservaSinControl(avionInseguro);
        final int asientoInseguro = 7;

        Thread h1 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "Carlos Mendoza", "Ventanilla-1"));
        Thread h2 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "Maria Lopez", "Ventanilla-2"));
        Thread h3 = new Thread(() -> gestorInseguro.reservar(asientoInseguro, "Andres Torres", "Ventanilla-3"));

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
        // CASO 2: RESERVAS CON EXCLUSION MUTUA (Juan Marcillo)
        // ------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 2: SIMULACION CON EXCLUSION MUTUA (REENTRANTLOCK)");
        Avion avionSeguro = new Avion();
        ReservaSegura gestorSeguro = new ReservaSegura(avionSeguro);
        final int asientoSeguro = 7;

        Thread s1 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "Carlos Mendoza", "Ventanilla-1"));
        Thread s2 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "Maria Lopez", "Ventanilla-2"));
        Thread s3 = new Thread(() -> gestorSeguro.reservar(asientoSeguro, "Andres Torres", "Ventanilla-3"));

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
        System.out.printf("Titular final y unico en Asiento #%d: '%s'\n\n",
                asientoSeguro, avionSeguro.getAsiento(asientoSeguro).getCliente());

        // ------------------------------------------------------------------
        // CASO 3: CANCELACION Y CAMBIO DE ASIENTO (Natasha Mehler)
        // ------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 3: GESTION DE RESERVAS (CANCELACION Y CAMBIO)");
        GestionReservas gestion = new GestionReservas(avionSeguro);

        // Preparamos reservas iniciales para probar cancelaciones y cambios
        gestorSeguro.reservar(12, "Natasha Mehler", "Ventanilla-1");
        gestorSeguro.reservar(15, "Roberto Gomez", "Ventanilla-2");

        System.out.println("\n-- Prueba 3.1: Intento de cancelar asiento libre (Asiento #20) --");
        gestion.cancelarReserva(20, "Natasha Mehler", "Ventanilla-1");

        System.out.println("\n-- Prueba 3.2: Intento de cancelar asiento ajeno (Asiento #15 de Roberto) --");
        gestion.cancelarReserva(15, "Natasha Mehler", "Ventanilla-1");

        System.out.println("\n-- Prueba 3.3: Cambio exitoso de asiento (Natasha pasa del #12 al #14 libre) --");
        gestion.cambiarAsiento(12, 14, "Natasha Mehler", "Ventanilla-1");

        System.out.println("\n-- Prueba 3.4: Cancelacion exitosa de la reserva (Natasha cancela su Asiento #14) --");
        gestion.cancelarReserva(14, "Natasha Mehler", "Ventanilla-1");

        List<Integer> reservasNatasha = gestion.consultarAsientosDeCliente("Natasha Mehler");
        System.out.println("\n[RESUMEN CASO 3 - CANCELACION Y CAMBIO]");
        System.out.printf("Total cancelaciones exitosas: %d | Total cambios exitosos: %d\n",
                gestion.getTotalCancelaciones(), gestion.getTotalCambios());
        System.out.printf("Asientos actuales de Natasha Mehler: %s\n", reservasNatasha);
        System.out.println("===============================================================");
    }
}
