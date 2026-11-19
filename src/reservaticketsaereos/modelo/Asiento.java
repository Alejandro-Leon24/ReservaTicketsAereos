package reservaticketsaereos.modelo;

public class Asiento {
    private final int numero;
    private boolean ocupado;
    private String cliente;

    public Asiento(int numero) {
        this.numero = numero;
        this.ocupado = false;
        this.cliente = "";
    }

    public int getNumero() {
        return numero;
    }

    public boolean isOcupado() {
        return ocupado;
    }

    public void setOcupado(boolean ocupado) {
        this.ocupado = ocupado;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    @Override
    public String toString() {
        return "Asiento #" + numero + " [" + (ocupado ? "OCUPADO por " + cliente : "LIBRE") + "]";
    }
}
