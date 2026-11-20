package reservaticketsaereos.modelo;

/**
 * Representa una peticion de compra o atencion en ventanilla.
 */
public class SolicitudReserva {
    private final String cliente;
    private final int asientoDeseado; // 1 a 30, o 0 si acepta cualquiera disponible

    public SolicitudReserva(String cliente, int asientoDeseado) {
        this.cliente = cliente;
        this.asientoDeseado = asientoDeseado;
    }

    public String getCliente() {
        return cliente;
    }

    public int getAsientoDeseado() {
        return asientoDeseado;
    }

    @Override
    public String toString() {
        String asientoStr = (asientoDeseado > 0) ? ("Asiento #" + asientoDeseado) : "Cualquier asiento libre";
        return String.format("[%s -> %s]", cliente, asientoStr);
    }
}
