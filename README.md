# Sistema Concurrente de Reserva de Tickets Aéreos

**Materia:** Aplicaciones Distribuidas  
**Semestre:** Noveno Semestre  
**Tecnología:** Java 21 / Swing / NetBeans IDE  
**Patrón de Arquitectura:** Modelo - Vista - Controlador (MVC)  

---

## 📌 Descripción General

El proyecto consiste en una simulación de venta y administración de tickets aéreos para un avión con capacidad para **30 pasajeros**. El entorno es **concurrente**, modelando tres ventanillas de atención simultánea operadas por hilos independientes (`Thread` / `Runnable`).

El objetivo principal es evidenciar el problema clásico de las **condiciones de carrera (*Race Conditions*)** en el acceso a recursos compartidos y contrastarlo con una solución robusta basada en **exclusión mutua (`ReentrantLock`)**, control de consistencia transaccional y sincronización de hilos en cola de espera mediante el mecanismo monitor (`wait()` / `notifyAll()`).

---

## 👥 Distribución de Módulos, Roles y Responsabilidades

| Integrante | Rol / Módulo | Clases Desarrolladas | Responsabilidad Principal |
| :--- | :--- | :--- | :--- |
| **Arianna Lisbeth Esteves Valero** | Gestión de Asientos | `Asiento.java`<br>`Avion.java` | Estructura de datos de los 30 asientos, control de estado (libre/ocupado) y consultas de disponibilidad. |
| **Xavier Alejandro León Salazar** | Reservas Sin Exclusión Mutua | `ReservaSinControl.java` | Simulación de la condición de carrera (*Check-Then-Act*) con pausas artificiales (`Thread.sleep`) y registro de colisiones. |
| **Juan Sebastián Marcillo Pico** | Exclusión Mutua | `ReservaSegura.java` | Protección con `ReentrantLock(true)` (política equitativa FIFO) para garantizar atomicidad y consistencia. |
| **Natasha Victoria Mehler Castro** | Cancelación y Cambio | `GestionReservas.java` | Lógica de negocio: validación de titularidad previa, liberación y reasignación atómica de asientos. |
| **Jeremy Joao Mero Araujo** | Hilos y Cola de Espera | `Ventanilla.java`<br>`ColaEspera.java`<br>`SolicitudReserva.java` | Hilos concurrentes de ventanilla (retardo 1-3s), bloqueo con `wait()` y reasignación automática al liberar con `notifyAll()`. |
| **Nohelya Carolina Monar Zambrano** | Interfaz Gráfica | `VentanaPrincipal.java` | Diseño en Java Swing: 3 ventanillas, avión con pasillo, lista de espera, actividades y clientes. |
| **Jhon Héctor Molina Sarco** | Integración y Documentación | `ReservaTicketsAereos.java`<br>`README.md` | Orquestador general, coordinación de hilos y documentación técnica. |

---

## 🏗️ Arquitectura del Sistema (Patrón MVC)

El proyecto sigue una estricta separación de responsabilidades para desacoplar el modelo de datos de la lógica de negocio y la interfaz visual:

```text
src/reservaticketsaereos/
│
├── modelo/                               <-- MODELO (Estructuras de datos y entidades)
│   ├── Asiento.java                      [Arianna Esteves] Entidad de asiento individual
│   ├── Avion.java                        [Arianna Esteves] Colección y estado de los 30 asientos
│   ├── SolicitudReserva.java             [Jeremy Mero] Datos de la petición de un cliente
│   └── ColaEspera.java                   [Jeremy Mero] Monitor concurrente (wait / notifyAll)
│
├── controlador/                          <-- CONTROLADOR (Lógica concurrente y transacciones)
│   ├── ReservaSinControl.java            [Xavier León] Generador de condiciones de carrera
│   ├── ReservaSegura.java                [Juan Marcillo] Exclusión mutua con ReentrantLock
│   ├── GestionReservas.java              [Natasha Mehler] Cancelaciones y cambios atómicos
│   └── Ventanilla.java                   [Jeremy Mero] Hilo trabajador con simulación de sleep (1-3s)
│
├── vista/                                <-- VISTA (Interfaz Gráfica Swing)
│   └── VentanaPrincipal.java             [Nohelya Monar] Formulario interactivo
│
└── ReservaTicketsAereos.java             [Jhon Molina] Main: pruebas de módulos y unión de toda la logica
```

---

## ⚙️ Conceptos Teóricos Demostrados

### 1. Condición de Carrera (*Race Condition*) - Sin Exclusión Mutua
Ocurre debido al patrón defectuoso **Check-Then-Act**:
1. El Hilo A verifica si el Asiento #2 está libre (`isAsientoDisponible` `true`).
2. El Hilo A entra en una pausa de procesamiento (`Thread.sleep`).
3. En ese intervalo, el Hilo B consulta el mismo Asiento #2 y también lo ve libre.
4. Ambos hilos proceden a escribir en memoria (`ocuparAsiento`), produciendo **sobreventa** y sobreescritura de datos.

### 2. Exclusión Mutua (*Mutual Exclusion*) - Con ReentrantLock
Se implementa un cerrojo reentrante equitativo:
```java
lock.lock();
try {
    // Sección Crítica: Consulta y Asignación atómica
} finally {
    lock.unlock();
}
```
Solo un hilo a la vez puede ejecutar la sección crítica. Los hilos competidores son serializados en estricto orden de llegada (FIFO), garantizando **cero sobreventas**.

### 3. Bloqueo y Asignación Automática (*Monitor Pattern*)
Si una ventanilla solicita un asiento que ya está ocupado en modo seguro:
* El hilo se suspende mediante `wait()` y queda registrado en la `ColaEspera` para ese asiento específico.
* En cuanto otra ventanilla cancela o cambia dicho asiento, el sistema invoca `notifyAll()`, despierta al hilo en espera y **asigna automáticamente el boleto al primer cliente en la fila**.

---

## 🖥️ Componentes de la Interfaz Gráfica (`VentanaPrincipal`)

1. **Fila Superior (Ventanillas y Concurrencia):**
   * **Ventanilla 1, 2 y 3:** Cajas independientes con campos de *Asiento*, *Cliente* y botones individuales para *Reservar asiento*, *Eliminar reservación* y *Cambiar asiento*.
   * **RESERVAR ASIENTO (`Simular concurrencia`):** Botón principal que extrae la información de las 3 ventanillas y dispara 3 hilos simultáneos para competir por los asientos.
2. **Fila Intermedia (Avión, Lista y Modo):**
   * **AVIÓN (30 Asientos):** Cuadrícula con pasillo central (`2 asientos | PASILLO | 3 asientos`). Cambia de color al ocuparse y muestra el nombre del pasajero.
   * **Lista de Espera:** Monitor en tiempo real de los hilos/usuarios suspendidos esperando asientos ocupados.
   * **ACTIVIDADES:** Selector de radio buttons para alternar entre `EXCLUSIÓN MUTUA` y `SIN EXCLUSIÓN MUTUA`.
3. **Fila Inferior (Base de Pasajeros):**
   * **Registrar Cliente:** Formulario validado (cédula numérica de exactamente 10 dígitos y nombre en letras).
   * **AVIANCA:** Tabla con el listado de clientes registrados. Al hacer doble clic en un cliente, se carga en la ventanilla activa.
4. **Log de Concurrencia en Tiempo Real:**
   * Consola inferior que registra con marcas de tiempo las acciones de cada hilo, los tiempos de *sleep*, colisiones detectadas y resúmenes de simulación sin bloquear la ventana.

---

## 🚀 Instrucciones de Ejecución

### Prerrequisitos
* **Java Development Kit (JDK):** Versión 17 o superior (desarrollado y probado en JDK 21).
* **IDE:** Apache NetBeans IDE (versión 17 o superior) o cualquier entorno compatible con proyectos Ant / Maven.

### Pasos para compilar y ejecutar
1. Abrir **Apache NetBeans IDE**.
2. Ir a `File` `Open Project...` y seleccionar la carpeta del repositorio (`ReservaTicketsAereos`).
3. Hacer clic derecho sobre el proyecto y seleccionar **Clean and Build**.
4. Abrir la clase principal: `src/reservaticketsaereos/ReservaTicketsAereos.java`.
5. Presionar **Shift + F6** (o clic derecho **Run File**).

---

## 🧪 Casos de Prueba para Demostración (Guion para el Video)

| Caso | Qué se debe probar | Resultado Esperado |
| :--- | :--- | :--- |
| **Prueba 1: Condiciones de Carrera** | 1. Seleccionar `SIN EXCLUSIÓN MUTUA`.<br>2. Poner Asiento `2` en las 3 ventanillas.<br>3. Clic en `Simular concurrencia`. | El log reporta **colisiones y sobreescrituras**. Dos o tres hilos se sobreescriben el Asiento #2. |
| **Prueba 2: Exclusión Mutua** | 1. Seleccionar `EXCLUSIÓN MUTUA`.<br>2. Poner Asiento `2` en las 3 ventanillas.<br>3. Clic en `Simular concurrencia`. | Solo una ventanilla reserva exitosamente. Las otras son rechazadas sin corromper los datos. |
| **Prueba 3: Bloqueo y Asignación Automática** | 1. Ventanilla 1 aparta Asiento `2` (Usuario A).<br>2. Ventanilla 2 intenta pedir Asiento `2` (Usuario B).<br>3. Ventanilla 1 presiona `Eliminar reservación`. | Usuario B entra a la `Lista de Espera`. Al presionar eliminar en Ventanilla 1, el asiento **se asigna automáticamente a Usuario B**. |
| **Prueba 4: Cambio de Asiento** | Con un cliente que posea un asiento, presionar `Cambiar asiento` e ingresar un nuevo número disponible. | El asiento anterior queda libre y el nuevo pasa a estar ocupado por el cliente. |
| **Prueba 5: Registro Validado** | Ingresar letras en cédula o dejar campos vacíos al dar clic en `Registrar`. | El sistema rechaza letras, restringe a 10 dígitos y emite advertencia si faltan datos. |
