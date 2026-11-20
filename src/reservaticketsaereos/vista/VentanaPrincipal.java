package reservaticketsaereos.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import reservaticketsaereos.controlador.GestionReservas;
import reservaticketsaereos.controlador.ReservaSegura;
import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.Avion;
import reservaticketsaereos.modelo.ColaEspera;
import reservaticketsaereos.modelo.SolicitudReserva;

/**
 * Modulo: Interfaz Grafica de Usuario (Java Swing)
 * Responsable: Monar Zambrano Nohelya Carolina
 * 
 * Ventana interactiva que permite:
 * - Visualizar la cuadricula de los 30 asientos y su estado (Libre / Ocupado).
 * - Ejecutar operaciones de reserva, cancelacion y cambio de forma asincrona sin congelar la GUI.
 * - Monitorear la cola de espera y el log de operaciones en tiempo real.
 */
public class VentanaPrincipal extends JFrame {

    private final Avion avion;
    private final ReservaSegura gestorSeguro;
    private final ReservaSinControl gestorInseguro;
    private final GestionReservas gestionReservas;
    private final ColaEspera colaEspera;

    // Componentes visuales
    private final JButton[] botonesAsientos = new JButton[Avion.TOTAL_ASIENTOS];
    private JTextField txtCliente;
    private JTextField txtAsiento;
    private JTextField txtNuevoAsiento;
    private JComboBox<String> cmbVentanilla;
    private JComboBox<String> cmbModo;
    private JLabel lblDisponibles;
    private JLabel lblOcupados;
    private JLabel lblColaSize;
    private DefaultListModel<String> modeloCola;
    private JList<String> listaCola;
    private JTextArea txtLog;

    // Colores tematicos
    private static final Color COLOR_LIBRE = new Color(46, 204, 113); // Verde
    private static final Color COLOR_OCUPADO = new Color(231, 76, 60); // Rojo
    private static final Color COLOR_FONDO = new Color(245, 247, 250);

    public VentanaPrincipal(Avion avion, ReservaSegura gestorSeguro, ReservaSinControl gestorInseguro,
                            GestionReservas gestionReservas, ColaEspera colaEspera) {
        this.avion = avion;
        this.gestorSeguro = gestorSeguro;
        this.gestorInseguro = gestorInseguro;
        this.gestionReservas = gestionReservas;
        this.colaEspera = colaEspera;

        initComponents();
        actualizarVistaAsientos();
        actualizarEstadisticas();
    }

    private void initComponents() {
        setTitle("Sistema de Reserva de Tickets Aereos - Concurrencia");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_FONDO);
        setLayout(new BorderLayout(10, 10));

        // 1. BANNER SUPERIOR DE ESTADO
        add(crearPanelSuperior(), BorderLayout.NORTH);

        // 2. PANEL CENTRAL (Distribucion de Asientos + Panel Lateral de Operaciones)
        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                crearPanelAsientos(), crearPanelOperaciones());
        splitCentral.setResizeWeight(0.55);
        splitCentral.setDividerLocation(580);
        add(splitCentral, BorderLayout.CENTER);

        // 3. PANEL INFERIOR (Log de eventos)
        add(crearPanelInferior(), BorderLayout.SOUTH);
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        JLabel titulo = new JLabel("Reserva de Tickets Aéreos - Gestión Concurrente");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titulo.setForeground(Color.WHITE);

        JPanel panelStats = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        panelStats.setOpaque(false);

        lblDisponibles = new JLabel("Libres: 30");
        lblDisponibles.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDisponibles.setForeground(new Color(230, 255, 230));

        lblOcupados = new JLabel("Ocupados: 0");
        lblOcupados.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblOcupados.setForeground(new Color(255, 220, 220));

        lblColaSize = new JLabel("En cola: 0");
        lblColaSize.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblColaSize.setForeground(Color.WHITE);

        panelStats.add(lblDisponibles);
        panelStats.add(lblOcupados);
        panelStats.add(lblColaSize);

        panel.add(titulo, BorderLayout.WEST);
        panel.add(panelStats, BorderLayout.EAST);
        return panel;
    }

    private JPanel crearPanelAsientos() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(10, 10, 10, 10),
                BorderFactory.createTitledBorder("Mapa de Asientos del Avión (30 Asientos)")
        ));

        // Grilla estilo cabina de avion (6 filas x 5 columnas)
        JPanel grid = new JPanel(new GridLayout(6, 5, 8, 8));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < Avion.TOTAL_ASIENTOS; i++) {
            final int num = i + 1;
            JButton btn = new JButton("#" + num);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setBackground(COLOR_LIBRE);
            btn.setForeground(Color.WHITE);

            // Al hacer clic en el asiento, se carga su numero en el campo de texto
            btn.addActionListener(e -> {
                txtAsiento.setText(String.valueOf(num));
                Asiento a = avion.getAsiento(num);
                if (a != null && a.isOcupado()) {
                    txtCliente.setText(a.getCliente());
                }
            });

            botonesAsientos[i] = btn;
            grid.add(btn);
        }

        panel.add(new JScrollPane(grid), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelOperaciones() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Formulario
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        form.setBorder(BorderFactory.createTitledBorder("Datos de la Solicitud"));

        form.add(new JLabel("Cliente:"));
        txtCliente = new JTextField();
        form.add(txtCliente);

        form.add(new JLabel("Asiento Solicitado (1-30):"));
        txtAsiento = new JTextField();
        form.add(txtAsiento);

        form.add(new JLabel("Nuevo Asiento (Para cambio):"));
        txtNuevoAsiento = new JTextField();
        form.add(txtNuevoAsiento);

        form.add(new JLabel("Ventanilla de Atención:"));
        cmbVentanilla = new JComboBox<>(new String[]{"Ventanilla-1", "Ventanilla-2", "Ventanilla-3"});
        form.add(cmbVentanilla);

        form.add(new JLabel("Control de Concurrencia:"));
        cmbModo = new JComboBox<>(new String[]{"Exclusión Mutua (Seguro)", "Sin Control (Inseguro)"});
        form.add(cmbModo);

        panel.add(form);
        panel.add(Box.createVerticalStrut(10));

        // Botonera de acciones
        JPanel botonera = new JPanel(new GridLayout(2, 2, 8, 8));
        JButton btnReservar = new JButton("Reservar Asiento");
        JButton btnCancelar = new JButton("Cancelar Reserva");
        JButton btnCambiar = new JButton("Cambiar Asiento");
        JButton btnEncolar = new JButton("Poner en Cola");

        btnReservar.setBackground(new Color(52, 152, 219));
        btnReservar.setForeground(Color.WHITE);
        btnCancelar.setBackground(new Color(230, 126, 34));
        btnCancelar.setForeground(Color.WHITE);
        btnCambiar.setBackground(new Color(155, 89, 182));
        btnCambiar.setForeground(Color.WHITE);
        btnEncolar.setBackground(new Color(52, 73, 94));
        btnEncolar.setForeground(Color.WHITE);

        btnReservar.addActionListener(e -> accionarReserva());
        btnCancelar.addActionListener(e -> accionarCancelacion());
        btnCambiar.addActionListener(e -> accionarCambio());
        btnEncolar.addActionListener(e -> accionarEncolar());

        botonera.add(btnReservar);
        botonera.add(btnCancelar);
        botonera.add(btnCambiar);
        botonera.add(btnEncolar);

        panel.add(botonera);
        panel.add(Box.createVerticalStrut(10));

        // Lista de Cola de Espera
        JPanel panelCola = new JPanel(new BorderLayout());
        panelCola.setBorder(BorderFactory.createTitledBorder("Cola de Espera (Orden de Llegada)"));
        modeloCola = new DefaultListModel<>();
        listaCola = new JList<>(modeloCola);
        panelCola.add(new JScrollPane(listaCola), BorderLayout.CENTER);
        panelCola.setPreferredSize(new Dimension(300, 160));

        panel.add(panelCola);
        return panel;
    }

    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 10, 10, 10),
                BorderFactory.createTitledBorder("Log de Eventos y Concurrencia en Tiempo Real")
        ));
        panel.setPreferredSize(new Dimension(1000, 160));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(220, 220, 220));

        panel.add(new JScrollPane(txtLog), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // ACCIONES ASINCRONAS (NO BLOQUEAN LA INTERFAZ)
    // =========================================================================

    private void accionarReserva() {
        String cliente = txtCliente.getText().trim();
        String asientoStr = txtAsiento.getText().trim();
        String ventanilla = (String) cmbVentanilla.getSelectedItem();
        boolean seguro = cmbModo.getSelectedIndex() == 0;

        if (cliente.isEmpty() || asientoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el nombre del cliente y el asiento.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numAsiento = Integer.parseInt(asientoStr);
            // Ejecutar en hilo de fondo para no congelar Swing
            new Thread(() -> {
                agregarLog(String.format("[%s] Solicitando reserva del Asiento #%d para '%s'...", ventanilla, numAsiento, cliente));
                boolean exito;
                if (seguro) {
                    exito = gestorSeguro.reservar(numAsiento, cliente, ventanilla);
                } else {
                    exito = gestorInseguro.reservar(numAsiento, cliente, ventanilla);
                }
                agregarLog(String.format("[%s] Resultado de reserva: %s", ventanilla, exito ? "EXITOSA" : "DENEGADA"));
                SwingUtilities.invokeLater(() -> {
                    actualizarVistaAsientos();
                    actualizarEstadisticas();
                });
            }).start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "El número de asiento debe ser un entero válido (1-30).", "Error de formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionarCancelacion() {
        String cliente = txtCliente.getText().trim();
        String asientoStr = txtAsiento.getText().trim();
        String ventanilla = (String) cmbVentanilla.getSelectedItem();

        if (cliente.isEmpty() || asientoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indique el cliente y el asiento a cancelar.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int numAsiento = Integer.parseInt(asientoStr);
            new Thread(() -> {
                boolean exito = gestionReservas.cancelarReserva(numAsiento, cliente, ventanilla);
                agregarLog(String.format("[%s] Cancelacion Asiento #%d: %s", ventanilla, numAsiento, exito ? "LIBERADO" : "FALLIDO"));
                if (exito) {
                    colaEspera.notificarAsientoLiberado();
                }
                SwingUtilities.invokeLater(() -> {
                    actualizarVistaAsientos();
                    actualizarEstadisticas();
                });
            }).start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Numero de asiento invalido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionarCambio() {
        String cliente = txtCliente.getText().trim();
        String actualStr = txtAsiento.getText().trim();
        String nuevoStr = txtNuevoAsiento.getText().trim();
        String ventanilla = (String) cmbVentanilla.getSelectedItem();

        if (cliente.isEmpty() || actualStr.isEmpty() || nuevoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indique el cliente, asiento actual y el nuevo asiento.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int actual = Integer.parseInt(actualStr);
            int nuevo = Integer.parseInt(nuevoStr);

            new Thread(() -> {
                boolean exito = gestionReservas.cambiarAsiento(actual, nuevo, cliente, ventanilla);
                agregarLog(String.format("[%s] Cambio Asiento #%d -> #%d: %s", ventanilla, actual, nuevo, exito ? "EXITOSO" : "FALLIDO"));
                SwingUtilities.invokeLater(() -> {
                    actualizarVistaAsientos();
                    actualizarEstadisticas();
                });
            }).start();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Los numeros de asiento deben ser enteros validos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void accionarEncolar() {
        String cliente = txtCliente.getText().trim();
        String asientoStr = txtAsiento.getText().trim();

        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Ingrese el nombre del cliente para encolar.", "Atención", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int asientoDeseado = 0;
        if (!asientoStr.isEmpty()) {
            try {
                asientoDeseado = Integer.parseInt(asientoStr);
            } catch (NumberFormatException ignored) {}
        }

        SolicitudReserva solicitud = new SolicitudReserva(cliente, asientoDeseado);
        colaEspera.agregarCliente(solicitud);
        agregarLog(String.format("[COLA] Cliente '%s' agregado a la cola de espera.", cliente));
        actualizarCola();
        actualizarEstadisticas();
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    public void actualizarVistaAsientos() {
        for (int i = 0; i < Avion.TOTAL_ASIENTOS; i++) {
            Asiento a = avion.getAsiento(i + 1);
            if (a != null) {
                if (a.isOcupado()) {
                    botonesAsientos[i].setBackground(COLOR_OCUPADO);
                    botonesAsientos[i].setText("#" + a.getNumero() + " (" + a.getCliente() + ")");
                    botonesAsientos[i].setToolTipText("Ocupado por: " + a.getCliente());
                } else {
                    botonesAsientos[i].setBackground(COLOR_LIBRE);
                    botonesAsientos[i].setText("#" + a.getNumero() + " [Libre]");
                    botonesAsientos[i].setToolTipText("Disponible");
                }
            }
        }
    }

    public void actualizarCola() {
        SwingUtilities.invokeLater(() -> {
            modeloCola.clear();
            List<SolicitudReserva> lista = colaEspera.getListaEspera();
            for (SolicitudReserva s : lista) {
                modeloCola.addElement(s.toString());
            }
        });
    }

    public void actualizarEstadisticas() {
        lblDisponibles.setText("Libres: " + avion.getCantidadDisponibles());
        lblOcupados.setText("Ocupados: " + avion.getCantidadOcupados());
        lblColaSize.setText("En cola: " + colaEspera.getTamano());
    }
}
