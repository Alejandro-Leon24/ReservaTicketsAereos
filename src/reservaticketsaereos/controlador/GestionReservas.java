package reservaticketsaereos.controlador;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.Avion;

/**
 * Modulo: Cancelacion y cambio de asiento
 * Responsable: Mehler Castro Natasha Victoria
 * 
 * Gestiona el ciclo de vida de las reservas ya realizadas:
 * - Validacion de titularidad previa a cualquier modificacion.
 * - Cancelacion de reservas y liberacion controlada de asientos.
 * - Reasignacion y cambio seguro a otro asiento disponible.
 * - Mantenimiento de la consistencia de los datos.
 */
public class GestionReservas {

    private final Avion avion;
    private final Object monitorConsistencia = new Object();

    private int totalCancelaciones;
    private int totalCambios;
    private Consumer<String> logger;

    public GestionReservas(Avion avion) {
        this.avion = avion;
        this.totalCancelaciones = 0;
        this.totalCambios = 0;
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
     * Cancela una reserva validando que el cliente realmente posea el asiento.
     */
    public boolean cancelarReserva(int numeroAsiento, String cliente, String ventanilla) {
        synchronized (monitorConsistencia) {
            emitirLog(String.format("[%s] [GESTION] Solicitud de cancelacion: Asiento #%d por cliente '%s'...",
                    ventanilla, numeroAsiento, cliente));

            Asiento asiento = avion.getAsiento(numeroAsiento);

            // Validacion 1: Existencia del asiento
            if (asiento == null) {
                emitirLog(String.format("[%s] [ERROR CANCELACION] El Asiento #%d no existe.", ventanilla, numeroAsiento));
                return false;
            }

            // Validacion 2: Comprobar si esta ocupado
            if (!asiento.isOcupado()) {
                emitirLog(String.format("[%s] [ERROR CANCELACION] El Asiento #%d ya se encuentra LIBRE.", ventanilla, numeroAsiento));
                return false;
            }

            // Validacion 3: Comprobar titularidad del cliente
            if (!asiento.getCliente().equalsIgnoreCase(cliente.trim())) {
                emitirLog(String.format("[%s] [ERROR CANCELACION] El Asiento #%d pertenece a '%s', no a '%s'. Operacion rechazada.",
                        ventanilla, numeroAsiento, asiento.getCliente(), cliente));
                return false;
            }

            // Operacion: Liberar asiento manteniendo consistencia
            avion.liberarAsiento(numeroAsiento);
            totalCancelaciones++;
            emitirLog(String.format("[%s] [EXITO] Asiento #%d cancelado y liberado correctamente para '%s'.",
                    ventanilla, numeroAsiento, cliente));
            return true;
        }
    }

    /**
     * Cambia el asiento de un cliente a otro disponible garantizando consistencia atomica.
     */
    public boolean cambiarAsiento(int asientoActual, int nuevoAsiento, String cliente, String ventanilla) {
        synchronized (monitorConsistencia) {
            emitirLog(String.format("[%s] [GESTION] Solicitud de cambio: Mover a '%s' del Asiento #%d al Asiento #%d...",
                    ventanilla, cliente, asientoActual, nuevoAsiento));

            if (asientoActual == nuevoAsiento) {
                emitirLog(String.format("[%s] [ERROR CAMBIO] El nuevo asiento debe ser diferente al actual (#%d).",
                        ventanilla, asientoActual));
                return false;
            }

            Asiento origen = avion.getAsiento(asientoActual);
            Asiento destino = avion.getAsiento(nuevoAsiento);

            // Validacion 1: Existencia de ambos asientos
            if (origen == null || destino == null) {
                emitirLog(String.format("[%s] [ERROR CAMBIO] Asiento(s) invalido(s). Rango valido: 1 a %d.",
                        ventanilla, Avion.TOTAL_ASIENTOS));
                return false;
            }

            // Validacion 2: Validar que el cliente tenga el asiento origen
            if (!origen.isOcupado() || !origen.getCliente().equalsIgnoreCase(cliente.trim())) {
                emitirLog(String.format("[%s] [ERROR CAMBIO] El cliente '%s' no posee la reserva del Asiento #%d.",
                        ventanilla, cliente, asientoActual));
                return false;
            }

            // Validacion 3: Validar que el asiento destino este disponible
            if (destino.isOcupado()) {
                emitirLog(String.format("[%s] [ERROR CAMBIO] El nuevo Asiento #%d ya esta ocupado por '%s'. Cambio denegado.",
                        ventanilla, nuevoAsiento, destino.getCliente()));
                return false;
            }

            // Operacion atomica: Liberar origen y ocupar destino
            avion.liberarAsiento(asientoActual);
            avion.ocuparAsiento(nuevoAsiento, cliente.trim());
            totalCambios++;

            emitirLog(String.format("[%s] [EXITO] Asiento cambiado con exito. '%s' ahora ocupa el Asiento #%d (Asiento #%d quedo libre).",
                    ventanilla, cliente, nuevoAsiento, asientoActual));
            return true;
        }
    }

    /**
     * Consulta que asientos tiene reservados un cliente especifico.
     */
    public List<Integer> consultarAsientosDeCliente(String cliente) {
        synchronized (monitorConsistencia) {
            List<Integer> reservados = new ArrayList<>();
            for (Asiento a : avion.getTodosLosAsientos()) {
                if (a.isOcupado() && a.getCliente().equalsIgnoreCase(cliente.trim())) {
                    reservados.add(a.getNumero());
                }
            }
            return reservados;
        }
    }

    public int getTotalCancelaciones() {
        return totalCancelaciones;
    }

    public int getTotalCambios() {
        return totalCambios;
    }

    public Avion getAvion() {
        return avion;
    }
}
