package gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class VentanaPrincipal extends JFrame {
    // Componentes de Conexión
    private JTextField campoIPServidor;

    // Componentes UDP
    private JTextArea areaChatUDP;
    private JTextField campoMensajeUDP;
    private JLabel lblCRC;

    // Componentes TCP
    private JTextField campoRutaArchivo;
    private JProgressBar barraProgreso;
    private JLabel lblMetricas;

    public VentanaPrincipal() {
        setTitle("Sistema de Red Profesional - UDP & TCP");
        setSize(800, 650);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // NUEVO: Panel de Configuración de Red Superior
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelNorte.setBackground(new Color(40, 40, 40));
        
        JLabel lblIP = new JLabel("IP del Servidor:");
        lblIP.setForeground(Color.WHITE);
        lblIP.setFont(new Font("Arial", Font.BOLD, 14));
        
        campoIPServidor = new JTextField("192.168.1.x", 15);
        campoIPServidor.setFont(new Font("Arial", Font.PLAIN, 14));
        campoIPServidor.setHorizontalAlignment(JTextField.CENTER);

        panelNorte.add(lblIP);
        panelNorte.add(campoIPServidor);
        
        add(panelNorte, BorderLayout.NORTH);

        // Crear pestañas
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.addTab("Chat UDP (Integridad)", crearPanelUDP());
        pestañas.addTab("Archivos TCP (Métricas)", crearPanelTCP());

        add(pestañas, BorderLayout.CENTER);
    }

    private JPanel crearPanelUDP() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        areaChatUDP = new JTextArea();
        areaChatUDP.setEditable(false);
        panel.add(new JScrollPane(areaChatUDP), BorderLayout.CENTER);

        JPanel inferior = new JPanel(new BorderLayout(5, 5));
        campoMensajeUDP = new JTextField();
        JButton btnEnviarUDP = new JButton("Enviar Mensaje");
        lblCRC = new JLabel("Último CRC: ---");

        inferior.add(campoMensajeUDP, BorderLayout.CENTER);
        inferior.add(btnEnviarUDP, BorderLayout.EAST);
        inferior.add(lblCRC, BorderLayout.SOUTH);

        panel.add(inferior, BorderLayout.SOUTH);

        btnEnviarUDP.addActionListener(e -> {
            String ip = campoIPServidor.getText();
            String texto = campoMensajeUDP.getText();
            if(!texto.isEmpty()) {
                areaChatUDP.append("A (" + ip + "): " + texto + "\n");
                campoMensajeUDP.setText("");
            }
        });

        return panel;
    }

    private JPanel crearPanelTCP() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        JLabel titulo = new JLabel("Transferencia de Archivos de Alto Nivel", SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 18));
        
        campoRutaArchivo = new JTextField();
        JButton btnSeleccionar = new JButton("Seleccionar Archivo...");
        barraProgreso = new JProgressBar(0, 100);
        barraProgreso.setStringPainted(true);
        
        JButton btnEnviarTCP = new JButton("Iniciar Transferencia");
        lblMetricas = new JLabel("BPS: 0 | Tiempo: 0s | Restante: 0s", SwingConstants.CENTER);

        panel.add(titulo);
        panel.add(btnSeleccionar);
        panel.add(campoRutaArchivo);
        panel.add(barraProgreso);
        panel.add(btnEnviarTCP);
        panel.add(lblMetricas);

        btnSeleccionar.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            int resultado = selector.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                campoRutaArchivo.setText(selector.getSelectedFile().getAbsolutePath());
            }
        });

        btnEnviarTCP.addActionListener(e -> {
            String ip = campoIPServidor.getText();
            String ruta = campoRutaArchivo.getText();
            // Lógica de validación rápida
            if(ruta.isEmpty() || ip.isEmpty() || ip.equals("192.168.1.x")) {
                JOptionPane.showMessageDialog(this, "Por favor verifica la IP y selecciona un archivo.");
            } else {
                System.out.println("Iniciando transferencia a: " + ip);
                barraProgreso.setValue(50); // Visual de prueba
            }
        });

        return panel;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}