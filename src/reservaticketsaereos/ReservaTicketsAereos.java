package reservaticketsaereos;

import java.util.List;
import javax.swing.SwingUtilities;
import reservaticketsaereos.controlador.GestionReservas;
import reservaticketsaereos.controlador.ReservaSegura;
import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.controlador.Ventanilla;
import reservaticketsaereos.modelo.Avion;
import reservaticketsaereos.modelo.ColaEspera;
import reservaticketsaereos.modelo.SolicitudReserva;
import reservaticketsaereos.vista.VentanaPrincipal;

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

        gestorSeguro.reservar(12, "Natasha Mehler", "Ventanilla-1");
        gestorSeguro.reservar(15, "Roberto Gomez", "Ventanilla-2");

        gestion.cancelarReserva(20, "Natasha Mehler", "Ventanilla-1");
        gestion.cancelarReserva(15, "Natasha Mehler", "Ventanilla-1");
        gestion.cambiarAsiento(12, 14, "Natasha Mehler", "Ventanilla-1");
        gestion.cancelarReserva(14, "Natasha Mehler", "Ventanilla-1");

        List<Integer> reservasNatasha = gestion.consultarAsientosDeCliente("Natasha Mehler");
        System.out.println("\n[RESUMEN CASO 3 - CANCELACION Y CAMBIO]");
        System.out.printf("Total cancelaciones exitosas: %d | Total cambios exitosos: %d\n",
                gestion.getTotalCancelaciones(), gestion.getTotalCambios());
        System.out.printf("Asientos actuales de Natasha Mehler: %s\n\n", reservasNatasha);

        // ------------------------------------------------------------------
        // CASO 4: HILOS, VENTANILLAS Y COLA DE ESPERA (Jeremy Mero)
        // ------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 4: 3 VENTANILLAS CONCURRENTES CONSUMIENDO COLA DE ESPERA");
        Avion avionGeneral = new Avion();
        ReservaSegura gestorGeneralSeguro = new ReservaSegura(avionGeneral);
        ReservaSinControl gestorGeneralInseguro = new ReservaSinControl(avionGeneral);
        GestionReservas gestionGeneral = new GestionReservas(avionGeneral);
        ColaEspera colaGeneral = new ColaEspera(30);

        // Instanciar 3 ventanillas concurrentes
        Ventanilla v1 = new Ventanilla("Ventanilla-1", colaGeneral, gestorGeneralSeguro);
        Ventanilla v2 = new Ventanilla("Ventanilla-2", colaGeneral, gestorGeneralSeguro);
        Ventanilla v3 = new Ventanilla("Ventanilla-3", colaGeneral, gestorGeneralSeguro);

        Thread tv1 = new Thread(v1);
        Thread tv2 = new Thread(v2);
        Thread tv3 = new Thread(v3);

        tv1.start();
        tv2.start();
        tv3.start();

        // Encolar algunas reservas iniciales de prueba
        colaGeneral.agregarCliente(new SolicitudReserva("Laura Paez", 1));
        colaGeneral.agregarCliente(new SolicitudReserva("David Ruiz", 2));
        colaGeneral.agregarCliente(new SolicitudReserva("Elena Moran", 3));

        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 5: LANZANDO INTERFAZ GRAFICA SWING (Nohelya Monar)");
        System.out.println("===============================================================");

        // Iniciar interfaz grafica Swing con su propio avion e instancias limpias (sin hilos previos)
        SwingUtilities.invokeLater(() -> {
            Avion avionGUI = new Avion();
            ReservaSegura gestorSeguroGUI = new ReservaSegura(avionGUI);
            ReservaSinControl gestorInseguroGUI = new ReservaSinControl(avionGUI);
            GestionReservas gestionGUI = new GestionReservas(avionGUI);
            ColaEspera colaGUI = new ColaEspera(30);

            VentanaPrincipal ventana = new VentanaPrincipal(
                    avionGUI, gestorSeguroGUI, gestorInseguroGUI,
                    gestionGUI, colaGUI);
            ventana.setVisible(true);
        });
    }
}
