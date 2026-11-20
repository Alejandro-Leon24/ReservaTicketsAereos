package reservaticketsaereos.vista;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import reservaticketsaereos.controlador.GestionReservas;
import reservaticketsaereos.controlador.ReservaSegura;
import reservaticketsaereos.controlador.ReservaSinControl;
import reservaticketsaereos.controlador.Ventanilla;
import reservaticketsaereos.modelo.Asiento;
import reservaticketsaereos.modelo.Avion;
import reservaticketsaereos.modelo.ColaEspera;
import reservaticketsaereos.modelo.SolicitudReserva;

/**
 * Modulo: Interfaz Grafica de Usuario (Java Swing)
 * Responsable: Monar Zambrano Nohelya Carolina
 * 
 * Implementacion exacta segun el bosquejo oficial del docente:
 * - Fila superior: 3 Ventanillas independientes (1, 2, 3) + Boton 'Simular concurrencia'.
 * - Fila intermedia: Mapa del Avion con pasillo central (30 asientos), Lista de Espera y panel de Actividades (Exclusion Mutua).
 * - Fila inferior: Registro de Clientes y Tabla de Clientes registrados (AVIANCA).
 */
public class VentanaPrincipal extends JFrame {

    private final Avion avion;
    private final ReservaSegura gestorSeguro;
    private final ReservaSinControl gestorInseguro;
    private final GestionReservas gestionReservas;
    private final ColaEspera colaEspera;

    // Las 3 ventanillas como instancias controladoras de Jeremy Mero
    private final Ventanilla v1;
    private final Ventanilla v2;
    private final Ventanilla v3;

    // Componentes de Ventanilla 1
    private JTextField txtAsientoV1;
    private JTextField txtClienteV1;

    // Componentes de Ventanilla 2
    private JTextField txtAsientoV2;
    private JTextField txtClienteV2;

    // Componentes de Ventanilla 3
    private JTextField txtAsientoV3;
    private JTextField txtClienteV3;

    // Selector de Concurrencia (Actividades)
    private JRadioButton rbExclusionMutua;
    private JRadioButton rbSinExclusionMutua;

    // Mapa de Asientos (Avion)
    private final JButton[] botonesAsientos = new JButton[Avion.TOTAL_ASIENTOS];

    // Lista de Espera
    private DefaultListModel<String> modeloListaEspera;
    private JList<String> listaEsperaVisual;

    // Registro de Clientes y Tabla AVIANCA
    private JTextField txtCedula;
    private JTextField txtNombreCliente;
    private DefaultTableModel modeloTablaClientes;
    private JTable tablaClientes;
    private int contadorClientes = 1;

    // Consola / Log de actividades en tiempo real
    private JTextArea txtLog;

    // Ventanilla seleccionada actualmente por el usuario (1, 2 o 3)
    private int ventanillaActiva = 1;

    // Estilos de color segun el diagrama
    private static final Color COLOR_HEADER_VENTANILLA = new Color(254, 249, 231); // Amarillo suave
    private static final Color COLOR_HEADER_AVION = new Color(245, 183, 177); // Rosado / Salmon suave
    private static final Color COLOR_HEADER_AVIANCA = new Color(212, 230, 241); // Azul cielo
    private static final Color COLOR_BOTON_AZUL = new Color(0, 102, 204);
    private static final Color COLOR_ASIENTO_LIBRE = Color.WHITE;
    private static final Color COLOR_ASIENTO_OCUPADO = new Color(212, 239, 223); // Verde suave con datos

    public VentanaPrincipal(Avion avion, ReservaSegura gestorSeguro, ReservaSinControl gestorInseguro,
                            GestionReservas gestionReservas, ColaEspera colaEspera) {
        this.avion = avion;
        this.gestorSeguro = gestorSeguro;
        this.gestorInseguro = gestorInseguro;
        this.gestionReservas = gestionReservas;
        this.colaEspera = colaEspera;

        // Inicializar las 3 ventanillas de Jeremy Mero conectadas al sistema
        this.v1 = new Ventanilla("Ventanilla 1", gestorSeguro, gestorInseguro, gestionReservas, colaEspera);
        this.v2 = new Ventanilla("Ventanilla 2", gestorSeguro, gestorInseguro, gestionReservas, colaEspera);
        this.v3 = new Ventanilla("Ventanilla 3", gestorSeguro, gestorInseguro, gestionReservas, colaEspera);
        this.v1.setLogger(this::agregarLog);
        this.v2.setLogger(this::agregarLog);
        this.v3.setLogger(this::agregarLog);
        this.colaEspera.registrarListener(this::actualizarColaVisual);

        initComponents();
        actualizarMapaAvion();
        actualizarColaVisual();
        agregarClientesIniciales();
    }

    private void initComponents() {
        setTitle("Sistema de Reserva de Tickets Aereos - Concurrencia");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 850);
        setMinimumSize(new Dimension(1050, 750));
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(8, 8));

        // PANEL CONTENEDOR PRINCIPAL VERTICAL
        JPanel panelPrincipal = new JPanel();
        panelPrincipal.setLayout(new BoxLayout(panelPrincipal, BoxLayout.Y_AXIS));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. FILA SUPERIOR: 3 VENTANILLAS + SIMULAR CONCURRENCIA
        panelPrincipal.add(crearFilaSuperiorVentanillas());
        panelPrincipal.add(Box.createVerticalStrut(10));

        // 2. FILA INTERMEDIA: AVION + LISTA DE ESPERA + ACTIVIDADES
        panelPrincipal.add(crearFilaIntermedia());
        panelPrincipal.add(Box.createVerticalStrut(10));

        // 3. FILA INFERIOR: REGISTRAR CLIENTE + AVIANCA (CLIENTES)
        panelPrincipal.add(crearFilaInferiorClientes());
        panelPrincipal.add(Box.createVerticalStrut(10));

        // 4. CONSOLA DE LOGS
        panelPrincipal.add(crearPanelLog());

        add(new JScrollPane(panelPrincipal), BorderLayout.CENTER);
    }

    // =========================================================================
    // 1. FILA SUPERIOR: VENTANILLAS Y DISPARADOR CONCURRENTE
    // =========================================================================
    private JPanel crearFilaSuperiorVentanillas() {
        JPanel panelFila = new JPanel(new GridLayout(1, 4, 12, 0));

        // Ventanilla 1
        txtAsientoV1 = new JTextField("2");
        txtClienteV1 = new JTextField("Usuario A");
        panelFila.add(crearCajaVentanilla("Ventanilla 1", txtAsientoV1, txtClienteV1, v1, 1));

        // Ventanilla 2
        txtAsientoV2 = new JTextField("2");
        txtClienteV2 = new JTextField("Usuario B");
        panelFila.add(crearCajaVentanilla("Ventanilla 2", txtAsientoV2, txtClienteV2, v2, 2));

        // Ventanilla 3
        txtAsientoV3 = new JTextField("2");
        txtClienteV3 = new JTextField("Usuario C");
        panelFila.add(crearCajaVentanilla("Ventanilla 3", txtAsientoV3, txtClienteV3, v3, 3));

        // Caja 'RESERVAR ASIENTO' -> Simular concurrencia
        JPanel panelSimular = new JPanel(new BorderLayout(5, 10));
        panelSimular.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblTituloSimular = new JLabel("RESERVAR ASIENTO", SwingConstants.CENTER);
        lblTituloSimular.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panelSimular.add(lblTituloSimular, BorderLayout.NORTH);

        JButton btnSimular = new JButton("Simular concurrencia");
        btnSimular.setBackground(COLOR_BOTON_AZUL);
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSimular.setFocusPainted(false);
        btnSimular.addActionListener(e -> ejecutarSimulacionConcurrente());

        JPanel panelCentro = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelCentro.add(btnSimular);
        panelSimular.add(panelCentro, BorderLayout.CENTER);

        JLabel lblThreadInfo = new JLabel("<html><center>thread (1)<br>thread (2)<br>thread (3)<br>thread.run</center></html>", SwingConstants.CENTER);
        lblThreadInfo.setFont(new Font("Consolas", Font.PLAIN, 11));
        panelSimular.add(lblThreadInfo, BorderLayout.SOUTH);

        panelFila.add(panelSimular);
        return panelFila;
    }

    private JPanel crearCajaVentanilla(String titulo, JTextField txtAsiento, JTextField txtCliente, Ventanilla vent, int numVentanilla) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(new Color(212, 172, 13), 1));

        // Nube / Header simulacion sleep
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_HEADER_VENTANILLA);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel lblNube = new JLabel("thread (sleep) 1 y 3 seg", SwingConstants.CENTER);
        lblNube.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        JLabel lblTitulo = new JLabel(titulo, SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        header.add(lblNube, BorderLayout.NORTH);
        header.add(lblTitulo, BorderLayout.CENTER);
        panel.add(header, BorderLayout.NORTH);

        // Campos: Asiento y Cliente
        JPanel panelCampos = new JPanel(new GridLayout(2, 2, 4, 4));
        panelCampos.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        panelCampos.add(new JLabel("Asiento:"));
        panelCampos.add(txtAsiento);
        panelCampos.add(new JLabel("Cliente:"));
        panelCampos.add(txtCliente);

        // Detectar cuando el usuario hace clic o escribe en esta ventanilla para volverla activa
        java.awt.event.FocusAdapter focoVentanilla = new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                ventanillaActiva = numVentanilla;
            }
        };
        txtAsiento.addFocusListener(focoVentanilla);
        txtCliente.addFocusListener(focoVentanilla);

        panel.add(panelCampos, BorderLayout.CENTER);

        // Botonera de la ventanilla
        JPanel panelBotones = new JPanel(new GridLayout(3, 1, 4, 4));
        panelBotones.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

        JButton btnReservar = new JButton("Reservar asiento");
        JButton btnEliminar = new JButton("Eliminar reservacion");
        JButton btnCambiar = new JButton("Cambiar asiento");

        estilizarBotonAzul(btnReservar);
        estilizarBotonAzul(btnEliminar);
        estilizarBotonAzul(btnCambiar);

        btnReservar.addActionListener(e -> operarReserva(txtAsiento, txtCliente, vent));
        btnEliminar.addActionListener(e -> operarEliminacion(txtAsiento, txtCliente, vent));
        btnCambiar.addActionListener(e -> operarCambio(txtAsiento, txtCliente, vent));

        panelBotones.add(btnReservar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnCambiar);
        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private void estilizarBotonAzul(JButton btn) {
        btn.setBackground(COLOR_BOTON_AZUL);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setFocusPainted(false);
    }

    // =========================================================================
    // 2. FILA INTERMEDIA: AVION + LISTA DE ESPERA + ACTIVIDADES
    // =========================================================================
    private JPanel crearFilaIntermedia() {
        JPanel panelFila = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 4, 0, 4);

        // 2.1 PANEL AVION (Con pasillo central)
        gbc.gridx = 0;
        gbc.weightx = 0.45;
        gbc.weighty = 1.0;
        panelFila.add(crearPanelAvion(), gbc);

        // 2.2 PANEL LISTA DE ESPERA
        gbc.gridx = 1;
        gbc.weightx = 0.30;
        panelFila.add(crearPanelListaEspera(), gbc);

        // 2.3 PANEL ACTIVIDADES (Exclusion Mutua)
        gbc.gridx = 2;
        gbc.weightx = 0.25;
        panelFila.add(crearPanelActividades(), gbc);

        return panelFila;
    }

    private JPanel crearPanelAvion() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblHeader = new JLabel("AVION", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(COLOR_HEADER_AVION);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblHeader, BorderLayout.NORTH);

        // Cuadricula con pasillo central: 6 filas de asientos.
        // Columna 1 y 2 (Izquierda) | PASILLO | Columna 3, 4 y 5 (Derecha) -> 5 asientos por fila x 6 filas = 30 asientos
        JPanel gridCabina = new JPanel(new GridLayout(6, 6, 6, 6));
        gridCabina.setBackground(Color.WHITE);
        gridCabina.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int asientoIdx = 0;
        for (int fila = 0; fila < 6; fila++) {
            // Asientos lado izquierdo (2 asientos)
            for (int colIzq = 0; colIzq < 2; colIzq++) {
                JButton btn = crearBotonAsiento(asientoIdx + 1);
                botonesAsientos[asientoIdx] = btn;
                gridCabina.add(btn);
                asientoIdx++;
            }

            // Pasillo central
            JLabel lblPasillo = new JLabel("||", SwingConstants.CENTER);
            lblPasillo.setForeground(Color.LIGHT_GRAY);
            lblPasillo.setFont(new Font("Consolas", Font.BOLD, 12));
            gridCabina.add(lblPasillo);

            // Asientos lado derecho (3 asientos)
            for (int colDer = 0; colDer < 3; colDer++) {
                JButton btn = crearBotonAsiento(asientoIdx + 1);
                botonesAsientos[asientoIdx] = btn;
                gridCabina.add(btn);
                asientoIdx++;
            }
        }

        panel.add(gridCabina, BorderLayout.CENTER);
        return panel;
    }

    private JButton crearBotonAsiento(int numAsiento) {
        JButton btn = new JButton(String.valueOf(numAsiento));
        btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btn.setBackground(COLOR_ASIENTO_LIBRE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(50, 40));
        btn.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        btn.addActionListener(e -> {
            // Cargar el asiento en la ventanilla actualmente activa (donde el usuario hizo clic)
            if (ventanillaActiva == 1) {
                txtAsientoV1.setText(String.valueOf(numAsiento));
            } else if (ventanillaActiva == 2) {
                txtAsientoV2.setText(String.valueOf(numAsiento));
            } else {
                txtAsientoV3.setText(String.valueOf(numAsiento));
            }
        });
        return btn;
    }

    private JPanel crearPanelListaEspera() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblHeader = new JLabel("Lista de Espera", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(new Color(234, 237, 237));
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblHeader, BorderLayout.NORTH);

        modeloListaEspera = new DefaultListModel<>();
        listaEsperaVisual = new JList<>(modeloListaEspera);
        listaEsperaVisual.setFont(new Font("Consolas", Font.PLAIN, 12));

        panel.add(new JScrollPane(listaEsperaVisual), BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelActividades() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblHeader = new JLabel("ACTIVIDADES", SwingConstants.CENTER);
        lblHeader.setOpaque(true);
        lblHeader.setBackground(COLOR_HEADER_AVION);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lblHeader, BorderLayout.NORTH);

        JPanel panelRadios = new JPanel();
        panelRadios.setLayout(new BoxLayout(panelRadios, BoxLayout.Y_AXIS));
        panelRadios.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        rbExclusionMutua = new JRadioButton("EXCLUSION MUTUA", true);
        rbSinExclusionMutua = new JRadioButton("SIN EXCLUSION MUTUA", false);

        rbExclusionMutua.setFont(new Font("Segoe UI", Font.BOLD, 12));
        rbSinExclusionMutua.setFont(new Font("Segoe UI", Font.BOLD, 12));

        ButtonGroup grupoModo = new ButtonGroup();
        grupoModo.add(rbExclusionMutua);
        grupoModo.add(rbSinExclusionMutua);

        panelRadios.add(rbExclusionMutua);
        panelRadios.add(Box.createVerticalStrut(15));
        panelRadios.add(rbSinExclusionMutua);

        panel.add(panelRadios, BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // 3. FILA INFERIOR: REGISTRAR CLIENTE + TABLA AVIANCA
    // =========================================================================
    private JPanel crearFilaInferiorClientes() {
        JPanel panelFila = new JPanel(new GridLayout(1, 2, 12, 0));
        panelFila.setPreferredSize(new Dimension(1000, 145));
        panelFila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        // 3.1 REGISTRAR CLIENTE
        JPanel panelRegistro = new JPanel(new BorderLayout(5, 5));
        panelRegistro.setBorder(BorderFactory.createLineBorder(new Color(212, 172, 13), 1));

        JLabel lblHeaderReg = new JLabel("Registrar Cliente", SwingConstants.CENTER);
        lblHeaderReg.setOpaque(true);
        lblHeaderReg.setBackground(COLOR_HEADER_VENTANILLA);
        lblHeaderReg.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderReg.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelRegistro.add(lblHeaderReg, BorderLayout.NORTH);

        // Formulario compacto para evitar estiramiento vertical excesivo
        JPanel formReg = new JPanel(new GridLayout(2, 2, 8, 6));
        formReg.setBorder(BorderFactory.createEmptyBorder(8, 20, 4, 20));

        JLabel lblCed = new JLabel("Cedula:");
        lblCed.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formReg.add(lblCed);

        txtCedula = new JTextField();
        txtCedula.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // Evento para admitir unicamente numeros del 0 al 9 y maximo 10 digitos
        txtCedula.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isDigit(c) || txtCedula.getText().length() >= 10) {
                    e.consume();
                }
            }
        });
        formReg.add(txtCedula);

        JLabel lblNom = new JLabel("Nombre:");
        lblNom.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formReg.add(lblNom);

        txtNombreCliente = new JTextField();
        txtNombreCliente.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        // Evento para admitir solo letras y espacios
        txtNombreCliente.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                char c = e.getKeyChar();
                if (!Character.isLetter(c) && !Character.isWhitespace(c) && !Character.isISOControl(c)) {
                    e.consume();
                }
            }
        });
        formReg.add(txtNombreCliente);
        panelRegistro.add(formReg, BorderLayout.CENTER);

        JButton btnRegistrar = new JButton("Registrar");
        estilizarBotonAzul(btnRegistrar);
        btnRegistrar.addActionListener(e -> registrarNuevoCliente());

        JPanel panelBotonReg = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        panelBotonReg.add(btnRegistrar);
        panelRegistro.add(panelBotonReg, BorderLayout.SOUTH);

        panelFila.add(panelRegistro);

        // 3.2 AVIANCA (TABLA DE CLIENTES)
        JPanel panelAvianca = new JPanel(new BorderLayout(5, 5));
        panelAvianca.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        JLabel lblHeaderAvianca = new JLabel("AVIANCA", SwingConstants.CENTER);
        lblHeaderAvianca.setOpaque(true);
        lblHeaderAvianca.setBackground(COLOR_HEADER_AVIANCA);
        lblHeaderAvianca.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHeaderAvianca.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panelAvianca.add(lblHeaderAvianca, BorderLayout.NORTH);

        modeloTablaClientes = new DefaultTableModel(new Object[]{"#", "Cedula", "Clientes"}, 0);
        tablaClientes = new JTable(modeloTablaClientes);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tablaClientes.setRowHeight(20);

        // Al seleccionar un cliente de la tabla, se carga en la ventanilla actualmente activa
        tablaClientes.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = tablaClientes.getSelectedRow();
            if (selectedRow >= 0) {
                String clienteSeleccionado = (String) modeloTablaClientes.getValueAt(selectedRow, 2);
                if (ventanillaActiva == 1) {
                    txtClienteV1.setText(clienteSeleccionado);
                } else if (ventanillaActiva == 2) {
                    txtClienteV2.setText(clienteSeleccionado);
                } else {
                    txtClienteV3.setText(clienteSeleccionado);
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaClientes);
        panelAvianca.add(scrollTabla, BorderLayout.CENTER);
        panelFila.add(panelAvianca);

        return panelFila;
    }

    private JPanel crearPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log de Concurrencia y Eventos"));
        panel.setPreferredSize(new Dimension(1000, 130));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setBackground(new Color(30, 30, 30));
        txtLog.setForeground(new Color(230, 230, 230));
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 11));

        panel.add(new JScrollPane(txtLog), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================================
    // ACCIONES Y LÓGICA DE CONCURRENCIA
    // =========================================================================

    private void agregarClientesIniciales() {
        modeloTablaClientes.addRow(new Object[]{1, "0912345678", "Usuario A"});
        modeloTablaClientes.addRow(new Object[]{2, "0923456789", "Usuario B"});
        modeloTablaClientes.addRow(new Object[]{3, "0934567890", "Usuario C"});
        contadorClientes = 4;
    }

    private void registrarNuevoCliente() {
        String cedula = txtCedula.getText().trim();
        String nombre = txtNombreCliente.getText().trim();

        if (cedula.isEmpty() || nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Por favor, debe rellenar todos los campos (Cedula y Nombre).", 
                    "Campos vacios", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (cedula.length() != 10) {
            JOptionPane.showMessageDialog(this, 
                    "La cedula debe contener exactamente 10 digitos numericos.", 
                    "Cedula incompleta", 
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        modeloTablaClientes.addRow(new Object[]{contadorClientes++, cedula, nombre});
        txtCedula.setText("");
        txtNombreCliente.setText("");
        agregarLog("[CLIENTES] Cliente registrado: " + nombre + " (CI: " + cedula + ")");
    }

    private int parseAsiento(JTextField txt) {
        try {
            return Integer.parseInt(txt.getText().trim());
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * DISPARADOR PRINCIPAL: Ejecuta las 3 ventanillas simultaneamente como hilos concurrentes.
     */
    private void ejecutarSimulacionConcurrente() {
        boolean modoSeguro = rbExclusionMutua.isSelected();
        agregarLog("\n==============================================================");
        agregarLog(">>> DISPARANDO SIMULACION CONCURRENTE EN LAS 3 VENTANILLAS");
        agregarLog("Modo de ejecucion: " + (modoSeguro ? "EXCLUSION MUTUA (Seguro)" : "SIN EXCLUSION MUTUA (Inseguro)"));
        agregarLog("==============================================================");

        int a1 = parseAsiento(txtAsientoV1);
        int a2 = parseAsiento(txtAsientoV2);
        int a3 = parseAsiento(txtAsientoV3);
        String c1 = txtClienteV1.getText().trim();
        String c2 = txtClienteV2.getText().trim();
        String c3 = txtClienteV3.getText().trim();

        Thread t1 = new Thread(() -> {
            v1.procesarReserva(a1, c1, modoSeguro);
            SwingUtilities.invokeLater(this::actualizarMapaAvion);
        });
        Thread t2 = new Thread(() -> {
            v2.procesarReserva(a2, c2, modoSeguro);
            SwingUtilities.invokeLater(this::actualizarMapaAvion);
        });
        Thread t3 = new Thread(() -> {
            v3.procesarReserva(a3, c3, modoSeguro);
            SwingUtilities.invokeLater(this::actualizarMapaAvion);
        });

        t1.start();
        t2.start();
        t3.start();
    }

    private void operarReserva(JTextField txtAsiento, JTextField txtCliente, Ventanilla vent) {
        String asientoStr = txtAsiento.getText().trim();
        String cliente = txtCliente.getText().trim();

        if (asientoStr.isEmpty() || cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete el asiento y cliente.", "Atencion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int num = Integer.parseInt(asientoStr);
            boolean modoSeguro = rbExclusionMutua.isSelected();
            new Thread(() -> {
                vent.procesarReserva(num, cliente, modoSeguro);
                SwingUtilities.invokeLater(this::actualizarMapaAvion);
            }).start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Asiento invalido (1-30).", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void operarEliminacion(JTextField txtAsiento, JTextField txtCliente, Ventanilla vent) {
        String asientoStr = txtAsiento.getText().trim();
        String cliente = txtCliente.getText().trim();

        if (asientoStr.isEmpty() || cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Indique el asiento y cliente para eliminar.", "Atencion", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int num = Integer.parseInt(asientoStr);
            new Thread(() -> {
                vent.procesarEliminacion(num, cliente);
                SwingUtilities.invokeLater(this::actualizarMapaAvion);
            }).start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Asiento invalido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void operarCambio(JTextField txtAsiento, JTextField txtCliente, Ventanilla vent) {
        String actualStr = txtAsiento.getText().trim();
        String cliente = txtCliente.getText().trim();
        String nuevoStr = JOptionPane.showInputDialog(this, "Ingrese el nuevo asiento para '" + cliente + "':");

        if (nuevoStr == null || nuevoStr.trim().isEmpty()) {
            return;
        }

        try {
            int actual = Integer.parseInt(actualStr);
            int nuevo = Integer.parseInt(nuevoStr.trim());
            new Thread(() -> {
                vent.procesarCambio(actual, nuevo, cliente);
                SwingUtilities.invokeLater(this::actualizarMapaAvion);
            }).start();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Numero de asiento invalido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void actualizarColaVisual() {
        SwingUtilities.invokeLater(() -> {
            modeloListaEspera.clear();
            for (String item : colaEspera.getListaEsperaFormateada()) {
                modeloListaEspera.addElement(item);
            }
            actualizarMapaAvion();
        });
    }

    public void actualizarMapaAvion() {
        for (int i = 0; i < Avion.TOTAL_ASIENTOS; i++) {
            Asiento a = avion.getAsiento(i + 1);
            if (a != null) {
                if (a.isOcupado()) {
                    botonesAsientos[i].setBackground(COLOR_ASIENTO_OCUPADO);
                    // Mostrar código o nombre corto del cliente como en el bosquejo (ej: B1, A5, o iniciales)
                    String cli = a.getCliente();
                    String display = cli.length() > 6 ? cli.substring(0, 5) + ".." : cli;
                    botonesAsientos[i].setText("<html><center>#" + a.getNumero() + "<br><b>" + display + "</b></center></html>");
                    botonesAsientos[i].setToolTipText("Ocupado por: " + a.getCliente());
                } else {
                    botonesAsientos[i].setBackground(COLOR_ASIENTO_LIBRE);
                    botonesAsientos[i].setText(String.valueOf(a.getNumero()));
                    botonesAsientos[i].setToolTipText("Asiento #" + a.getNumero() + " Disponible");
                }
            }
        }
    }

    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            txtLog.append(mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
}
