package gui;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

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

        // Panel de Configuración de Red Superior
        JPanel panelNorte = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        panelNorte.setBackground(new Color(40, 40, 40));
        
        JLabel lblIP = new JLabel("IP del Servidor (Compañero):");
        lblIP.setForeground(Color.WHITE);
        lblIP.setFont(new Font("Arial", Font.BOLD, 14));
        
        campoIPServidor = new JTextField("192.168.1.", 15);
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
        lblCRC = new JLabel("Último CRC enviado: ---");

        inferior.add(campoMensajeUDP, BorderLayout.CENTER);
        inferior.add(btnEnviarUDP, BorderLayout.EAST);
        inferior.add(lblCRC, BorderLayout.SOUTH);

        panel.add(inferior, BorderLayout.SOUTH);

        // ==========================================================
        // LÓGICA DE ENVÍO UDP CON CRC32 CONECTADA AL BOTÓN
        // ==========================================================
        btnEnviarUDP.addActionListener(e -> {
            String ipTarget = campoIPServidor.getText().trim();
            String texto = campoMensajeUDP.getText().trim();

            if (texto.isEmpty() || ipTarget.isEmpty() || ipTarget.endsWith(".")) {
                JOptionPane.showMessageDialog(this, "Por favor, escribe una IP válida y un mensaje.");
                return;
            }

            try (DatagramSocket socketUDP = new DatagramSocket()) {
                InetAddress destino = InetAddress.getByName(ipTarget);

                // Calcular el CRC32 del texto
                byte[] bytesOriginales = texto.getBytes(StandardCharsets.UTF_8);
                java.util.zip.CRC32 crc = new java.util.zip.CRC32();
                crc.update(bytesOriginales);
                long valorCRC = crc.getValue();

                // Construir el paquete bajo nuestro protocolo: "Texto|CRC"
                String paqueteCompleto = texto + "|" + valorCRC;
                byte[] bufferEnvio = paqueteCompleto.getBytes(StandardCharsets.UTF_8);

                DatagramPacket paquete = new DatagramPacket(bufferEnvio, bufferEnvio.length, destino, 50000);
                socketUDP.send(paquete);

                // Actualizar la interfaz gráfica
                areaChatUDP.append("Tú -> " + texto + " [CRC: " + valorCRC + "]\n");
                lblCRC.setText("Último CRC enviado: " + valorCRC);
                campoMensajeUDP.setText("");

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error UDP al enviar: " + ex.getMessage());
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

        // Selector nativo de archivos de Windows
        btnSeleccionar.addActionListener(e -> {
            JFileChooser selector = new JFileChooser();
            int resultado = selector.showOpenDialog(this);
            if (resultado == JFileChooser.APPROVE_OPTION) {
                campoRutaArchivo.setText(selector.getSelectedFile().getAbsolutePath());
            }
        });

        // ==========================================================
        // LÓGICA DE ENVIAR ARCHIVO TCP CON METRICAS EN TIEMPO REAL
        // ==========================================================
        btnEnviarTCP.addActionListener(e -> {
            String ipTarget = campoIPServidor.getText().trim();
            String ruta = campoRutaArchivo.getText().trim().replace("\"", "");

            if (ruta.isEmpty() || ipTarget.isEmpty() || ipTarget.endsWith(".")) {
                JOptionPane.showMessageDialog(this, "Verifica la IP y selecciona un archivo válido.");
                return;
            }

            File archivo = new File(ruta);
            if (!archivo.exists()) {
                JOptionPane.showMessageDialog(this, "El archivo seleccionado no existe en tu disco duro.");
                return;
            }

            // Deshabilitar el botón temporalmente para evitar clics dobles
            btnEnviarTCP.setEnabled(false);

            // Creamos un Thread secundario para que la ventana no se congele durante el envío pesado
            new Thread(() -> {
                try (Socket socketTCP = new Socket(ipTarget, 60000);
                     DataOutputStream out = new DataOutputStream(socketTCP.getOutputStream());
                     FileInputStream fis = new FileInputStream(archivo)) {

                    // 1. Enviar metadatos
                    out.writeUTF(archivo.getName());
                    out.writeLong(archivo.length());
                    out.flush();

                    byte[] buffer = new byte[8192];
                    int bytesLeidos;
                    long acumulados = 0;
                    long tamanoTotal = archivo.length();
                    long tiempoInicio = System.currentTimeMillis();

                    // 2. Bucle de envío por bloques
                    while ((bytesLeidos = fis.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesLeidos);
                        acumulados += bytesLeidos;

                        // Cálculos matemáticos de red
                        long tiempoActual = System.currentTimeMillis();
                        double segundos = (tiempoActual - tiempoInicio) / 1000.0;
                        if (segundos == 0) segundos = 0.001;

                        double bps = (acumulados * 8.0) / segundos;
                        long bytesRestantes = tamanoTotal - acumulados;
                        double eta = (bps > 0) ? (bytesRestantes * 8.0) / bps : 0;
                        int porcentaje = (int) (((double) acumulados / tamanoTotal) * 100);

                        // Actualizar componentes gráficos de forma segura mediante el hilo de Swing
                        final double bpsFinal = bps;
                        final double segFinal = segundos;
                        final double etaFinal = eta;
                        final int porcFinal = porcentaje;

                        SwingUtilities.invokeLater(() -> {
                            barraProgreso.setValue(porcFinal);
                            lblMetricas.setText(String.format("BPS: %.2f | Transcurrido: %.2f s | Restante: %.2f s", 
                                    bpsFinal, segFinal, etaFinal));
                        });
                    }
                    out.flush();

                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "¡Archivo '" + archivo.getName() + "' enviado con éxito!");
                        barraProgreso.setValue(100);
                        btnEnviarTCP.setEnabled(true);
                    });

                } catch (Exception ex) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Error de red TCP: " + ex.getMessage());
                        btnEnviarTCP.setEnabled(true);
                    });
                }
            }).start();
        });

        return panel;
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception e) {}
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}