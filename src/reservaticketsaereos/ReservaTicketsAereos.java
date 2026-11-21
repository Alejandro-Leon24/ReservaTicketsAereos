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

/**
 * Modulo: Integracion General, Orquestacion y Suite de Pruebas
 * Responsable: Jhon Hector Molina Sarco
 * 
 * Integra los modulos desarrollados por los integrantes del equipo:
 * - Modelo (Arianna Esteves)
 * - Concurrencia sin control (Xavier Leon)
 * - Exclusion mutua con ReentrantLock (Juan Marcillo)
 * - Cancelacion y cambio de reservas (Natasha Mehler)
 * - Ventanillas concurrentes y cola sincronizada (Jeremy Mero)
 * - Interfaz grafica Swing (Nohelya Monar)
 */
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
        System.out.println(">>> CASO 4: HILOS, VENTANILLAS Y ASIGNACION AUTOMATICA (Jeremy Mero)");
        System.out.println("Demostracion de: 3 ventanillas concurrentes, bloqueo en espera (wait)");
        System.out.println("y reasignacion automatica al liberarse el asiento (notifyAll).\n");

        Avion avionJeremy = new Avion();
        ReservaSegura gestorSeguroJeremy = new ReservaSegura(avionJeremy);
        ReservaSinControl gestorInseguroJeremy = new ReservaSinControl(avionJeremy);
        GestionReservas gestionJeremy = new GestionReservas(avionJeremy);
        ColaEspera colaJeremy = new ColaEspera(30);

        Ventanilla v1 = new Ventanilla("Ventanilla-1", gestorSeguroJeremy, gestorInseguroJeremy, gestionJeremy, colaJeremy);
        Ventanilla v2 = new Ventanilla("Ventanilla-2", gestorSeguroJeremy, gestorInseguroJeremy, gestionJeremy, colaJeremy);
        Ventanilla v3 = new Ventanilla("Ventanilla-3", gestorSeguroJeremy, gestorInseguroJeremy, gestionJeremy, colaJeremy);

        // 4.1: Ventanilla-1 reserva el Asiento 2 para "Usuario A"
        System.out.println("-- Paso 4.1: Ventanilla-1 aparta el Asiento #2 para 'Usuario A' --");
        v1.procesarReserva(2, "Usuario A", true);

        // 4.2: Ventanilla-2 intenta en hilo paralelo reservar el mismo Asiento 2 para "Usuario B"
        // Como esta ocupado, se bloquea y entra a la ColaEspera
        System.out.println("\n-- Paso 4.2: Ventanilla-2 solicita Asiento #2 (quedara bloqueada en espera) --");
        Thread hiloV2 = new Thread(() -> {
            v2.procesarReserva(2, "Usuario B", true);
        });
        hiloV2.start();

        // Breve pausa para asegurar que el hilo de Ventanilla-2 entre a la cola y se suspenda con wait()
        try {
            Thread.sleep(600);
        } catch (InterruptedException ignored) {}

        System.out.println("\nEstado actual de la Lista de Espera:");
        for (String item : colaJeremy.getListaEsperaFormateada()) {
            System.out.println("  " + item);
        }

        // 4.3: Ventanilla-1 cancela/elimina su reserva del Asiento 2
        System.out.println("\n-- Paso 4.3: Ventanilla-1 libera el Asiento #2 --");
        v1.procesarEliminacion(2, "Usuario A");

        // Esperar a que el hilo de Ventanilla-2 despierte y finalice
        try {
            hiloV2.join();
        } catch (InterruptedException ignored) {}

        System.out.println("\n[RESUMEN CASO 4 - HILOS Y COLA]");
        System.out.printf("Titular final en Asiento #2: '%s' (Asignado automaticamente sin intervencion manual)\n",
                avionJeremy.getAsiento(2).getCliente());
        System.out.printf("Clientes restantes en cola de espera: %d\n\n", colaJeremy.getTotalEnEspera());

        // ------------------------------------------------------------------
        // CASO 5: INTEGRACION GENERAL Y VISTA (Jhon Molina / Nohelya Monar)
        // ------------------------------------------------------------------
        System.out.println("---------------------------------------------------------------");
        System.out.println(">>> CASO 5: LANZANDO INTERFAZ GRAFICA SWING (Nohelya Monar / Jhon Molina)");
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
