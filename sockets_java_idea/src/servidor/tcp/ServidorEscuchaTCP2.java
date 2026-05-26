package servidor.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;

public class ServidorEscuchaTCP2 extends Thread {
    protected ServerSocket socket; //Socket servidor
    protected Socket socket_cli; //Socket de datos cliente
    protected final int PUERTO_SERVER;

    public ServidorEscuchaTCP2(int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        // Primitiva de LISTEN, crea socket con Ip (implícita activa) y puerto
        socket = new ServerSocket(PUERTO_SERVER);
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor TCP escuchando en puerto " + PUERTO_SERVER + "...\n");

            // El servidor queda esperando clientes siempre
            while (true) {
                // Primitiva ACCEPT, acepta conexiones de clientes
                socket_cli = socket.accept();

                EntradaSalida.mostrarMensaje("Cliente conectado " + socket_cli.getInetAddress() + ":" + socket_cli.getPort() + "\n");

                // Crear flujo de entrada de datos del socket para ese cliente
                DataInputStream in = new DataInputStream(socket_cli.getInputStream());

                // Bucle para atender al cliente conectado
                while (true) {
                    try {
                        // ==========================================================
                        // NUEVO CÓDIGO: RECIBIR ARCHIVOS BINARIOS
                        // ==========================================================
                        recibeArchivo(in);

                        /* ==========================================================
                           CÓDIGO ORIGINAL (MENSAJES DE TEXTO) - COMENTADO
                           ==========================================================
                        Mensaje mensajeObj = recibeMensaje(in);

                        if (mensajeObj != null) {
                            EntradaSalida.mostrarMensaje("Mensaje recibido \""+ mensajeObj.getMensaje()
                                    + "\" del cliente "+ mensajeObj.getAddressCliente()+ ":"
                                    + mensajeObj.getPuertoCliente() + "\n");
                        }
                        ========================================================== */
                    }
                    // Cliente cerró conexión normalmente o mandó señal de fin
                    catch (EOFException e) {
                        EntradaSalida.mostrarMensaje("Cliente desconectado o finalizó transferencia.\n");
                        break;
                    }
                    // Error de socket (Pérdida de red)
                    catch (SocketException e) {
                        EntradaSalida.mostrarMensaje("Conexión perdida con cliente.\n");
                        break;
                    }
                }
                // cerrar socket del cliente
                socket_cli.close();
                EntradaSalida.mostrarMensaje("Esperando nuevo cliente...\n");
            }
        } catch (Exception e) {
            System.err.println("Error en servidor: " + e.getMessage());
        }
    }

    // ==========================================================
    // NUEVO MÉTODO: RECEPCIÓN DE ARCHIVOS Y ESCRITURA EN DISCO
    // ==========================================================
    private void recibeArchivo(DataInputStream in) throws Exception {
        // 1. Leer los METADATOS (Nombre del archivo)
        String nombreArchivo = in.readUTF();

        // Si el cliente mandó la señal de salida, lanzamos EOFException para salir del bucle
        if (nombreArchivo.equals("EOF_TRANSFERENCIA")) {
            throw new EOFException("Fin de transferencias");
        }

        // Leer el tamaño del archivo
        long tamanoArchivo = in.readLong();

        EntradaSalida.mostrarMensaje("Preparando para recibir: " + nombreArchivo + " (" + tamanoArchivo + " bytes)...\n");

        // 2. Crear archivo local (se guarda en la raíz del proyecto)
        FileOutputStream fos = new FileOutputStream("recibido_" + nombreArchivo);

        // 3. Preparar la "cubeta" de 8 KB
        byte[] buffer = new byte[8192];
        int bytesLeidos;
        long totalRecibido = 0;

        // 4. Bucle de recepción: lee estrictamente hasta alcanzar el peso total
        while (totalRecibido < tamanoArchivo) {
            // Calcular cuánto falta para no pasarnos y leer basura de la red
            int bytesFaltantes = (int) Math.min(buffer.length, tamanoArchivo - totalRecibido);
            
            bytesLeidos = in.read(buffer, 0, bytesFaltantes);

            if (bytesLeidos == -1) {
                break; // El cliente se desconectó a la mitad de la transferencia
            }

            fos.write(buffer, 0, bytesLeidos);
            totalRecibido += bytesLeidos;
        }

        // 5. Limpiar y cerrar el archivo guardado
        fos.close();
        EntradaSalida.mostrarMensaje("¡Archivo '" + nombreArchivo + "' recibido y guardado con éxito!\n\n");
    }

    /* ==========================================================
       CÓDIGO ORIGINAL (MENSAJES DE TEXTO) - COMENTADO
       ==========================================================
    private Mensaje recibeMensaje(DataInputStream in) throws Exception {
        Mensaje mensajeObj = new Mensaje();

        // Se queda bloqueante en espera de mensajes
        String mensaje = in.readUTF();
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressCliente(socket_cli.getInetAddress());
        mensajeObj.setPuertoCliente(socket_cli.getPort());

        return mensajeObj;
    }
    ========================================================== */
}