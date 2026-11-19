package reservaticketsaereos;

import reservaticketsaereos.modelo.Avion;

public class ReservaTicketsAereos {

    public static void main(String[] args) {
        System.out.println("--- INICIANDO PRUEBA DEL MÓDULO DE ASIENTOS ---");
        
        // 1. Instanciar tu estructura
        Avion miAvion = new Avion();
        
        // 2. Comprobar que los 30 asientos se crearon correctamente
        System.out.println("Total de asientos generados: " + miAvion.getTodosLosAsientos().length);
        
        // 3. Verificar el estado inicial de un asiento al azar (Ejemplo: Asiento 5)
        System.out.println("¿El asiento 5 está libre?: " + miAvion.isAsientoDisponible(5));
        
        // 4. Simular que una ventanilla ocupa el asiento
        System.out.println("\nOcupando el asiento 5 para 'Arianna Esteves'...");
        miAvion.ocuparAsiento(5, "Arianna Esteves");
        
        // 5. Verificar que los datos cambiaron correctamente
        System.out.println("¿El asiento 5 está libre ahora?: " + miAvion.isAsientoDisponible(5));
        System.out.println("El asiento 5 pertenece a: " + miAvion.getAsiento(5).getCliente());
        
        // 6. Probar la función de liberación (la que usará Natasha)
        System.out.println("\nLiberando el asiento 5...");
        miAvion.liberarAsiento(5);
        System.out.println("¿El asiento 5 está libre de nuevo?: " + miAvion.isAsientoDisponible(5));
        System.out.println("Cliente actual del asiento 5: '" + miAvion.getAsiento(5).getCliente() + "'");
        
        System.out.println("--- PRUEBA FINALIZADA CON ÉXITO ---");
    }
}