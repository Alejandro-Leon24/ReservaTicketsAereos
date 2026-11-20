package reservaticketsaereos.modelo;

/**
 * Representa una peticion de compra o atencion en ventanilla.
 * Responsable: Mero Araujo Jeremy Joao
 */
public class SolicitudReserva {
    private final String cliente;
    private final int asientoDeseado; // 1 a 30
    private final String ventanilla;

    public SolicitudReserva(String cliente, int asientoDeseado, String ventanilla) {
        this.cliente = cliente;
        this.asientoDeseado = asientoDeseado;
        this.ventanilla = ventanilla;
    }

    public SolicitudReserva(String cliente, int asientoDeseado) {
        this(cliente, asientoDeseado, "General");
    }

    public String getCliente() {
        return cliente;
    }

    public int getAsientoDeseado() {
        return asientoDeseado;
    }

    public String getVentanilla() {
        return ventanilla;
    }

    @Override
    public String toString() {
        return String.format("%s (desde %s)", cliente, ventanilla);
    }
}
