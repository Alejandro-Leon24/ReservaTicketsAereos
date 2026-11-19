package reservaticketsaereos.modelo;

public class Avion {

    // Capacidad fija del avión
    public static final int TOTAL_ASIENTOS = 30;

    // Estructura compartida para los 30 asientos
    private final Asiento[] asientos;

    public Avion() {
        asientos = new Asiento[TOTAL_ASIENTOS];
        // Crear la estructura numerada del 1 al 30
        for (int i = 0; i < TOTAL_ASIENTOS; i++) {
            asientos[i] = new Asiento(i + 1);
        }
    }

    // Operación básica de consulta de disponibilidad
    public boolean isAsientoDisponible(int numeroAsiento) {
        if (numeroAsiento >= 1 && numeroAsiento <= TOTAL_ASIENTOS) {
            return !asientos[numeroAsiento - 1].isOcupado();
        }
        return false; // Retorna falso si el número está fuera de rango
    }

    // Método para obtener la referencia de un asiento específico
    public Asiento getAsiento(int numeroAsiento) {
        if (numeroAsiento >= 1 && numeroAsiento <= TOTAL_ASIENTOS) {
            return asientos[numeroAsiento - 1];
        }
        return null;
    }

    // Operaciones de modificación de estado
    public void ocuparAsiento(int numeroAsiento, String nombreCliente) {
        Asiento asiento = getAsiento(numeroAsiento);
        if (asiento != null) {
            asiento.setOcupado(true);
            asiento.setCliente(nombreCliente);
        }
    }

    public void liberarAsiento(int numeroAsiento) {
        Asiento asiento = getAsiento(numeroAsiento);
        if (asiento != null) {
            asiento.setOcupado(false);
            asiento.setCliente("");
        }
    }

    // Retorna todos los asientos (útil para la vista Swing)
    public Asiento[] getTodosLosAsientos() {
        return asientos;
    }

    // Métodos auxiliares de consulta
    public int getCantidadDisponibles() {
        int libres = 0;
        for (Asiento a : asientos) {
            if (!a.isOcupado()) {
                libres++;
            }
        }
        return libres;
    }

    public int getCantidadOcupados() {
        return TOTAL_ASIENTOS - getCantidadDisponibles();
    }
}
