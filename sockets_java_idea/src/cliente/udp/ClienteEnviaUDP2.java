package cliente.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public class ClienteEnviaUDP2 extends Thread {
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    protected DatagramSocket socket; //socket de cliente
    protected InetAddress addressServer;
    private volatile boolean ejecutando = true;

    public ClienteEnviaUDP2(DatagramSocket nuevoSocket,String servidor,int puertoServidor) throws Exception {
        socket = nuevoSocket;
        SERVER = servidor;
        PUERTO_SERVER = puertoServidor;

        // resolver dirección
        addressServer = InetAddress.getByName(SERVER);
    }

    @Override
    public void run() {
        BufferedReader teclado = null;

        try {
            EntradaSalida.mostrarMensaje("Cliente UDP listo para mandar...\n");

            // crear teclado
            teclado = new BufferedReader(new InputStreamReader(System.in));

            while (ejecutando) {
                Mensaje mensajeObj = enviaMensaje(teclado);

                // salir elegantemente
                if (mensajeObj.getMensaje().equalsIgnoreCase("fin")) {
                    EntradaSalida.mostrarMensaje("Finalizando envío UDP...\n");
                    ejecutando = false;
                }
            }
        }
        catch (Exception e) {
            System.err.println("Error cliente UDP envío: "+ e.getMessage());
        }
        finally {
            try {
                if (teclado != null) {
                    teclado.close(); //cerrar flujo teclado
                }
                if (socket != null && !socket.isClosed()) {
                    socket.close();  //cerrar socket
                }
            }
            catch (Exception e) {
                System.err.println("Error cerrando recursos: "+ e.getMessage());
            }
        }
    }

    /*private Mensaje enviaMensaje(BufferedReader teclado) throws Exception {
        Mensaje mensajeObj =new Mensaje();

        // leer mensaje teclado
        String mensaje = teclado.readLine();

        // String -> bytes UTF-8
        byte[] buffer = mensaje.getBytes(StandardCharsets.UTF_8);

        // crear paquete UDP
        DatagramPacket paquete = new DatagramPacket(buffer,buffer.length,addressServer,PUERTO_SERVER);

        // enviar paquete
        socket.send(paquete);

        // llenar objeto mensaje
        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \""+ mensajeObj.getMensaje()
                + "\" enviado a servidor " + mensajeObj.getAddressServidor()
                + ":" + mensajeObj.getPuertoServidor() + "\n");
        return mensajeObj;
    }*/
    private Mensaje enviaMensaje(BufferedReader teclado) throws Exception {
        Mensaje mensajeObj = new Mensaje();

        // 1. Leer el mensaje del teclado
        String mensajeOriginal = teclado.readLine();

        // 2. Calcular el CRC32 del mensaje original
        byte[] bytesOriginales = mensajeOriginal.getBytes(StandardCharsets.UTF_8);
        java.util.zip.CRC32 crc = new java.util.zip.CRC32();
        crc.update(bytesOriginales);
        long valorCRC = crc.getValue(); // Este es nuestro Crc

        // 3. Crear el paquete definitivo con nuestro protocolo: "Texto|crc"
        String paqueteCompleto = mensajeOriginal + "|" + valorCRC;
        byte[] bufferEnvio = paqueteCompleto.getBytes(StandardCharsets.UTF_8);

        // 4. Crear y enviar el paquete UDP
        DatagramPacket paquete = new DatagramPacket(bufferEnvio, bufferEnvio.length, addressServer, PUERTO_SERVER);
        socket.send(paquete);

        // 5. Llenar el objeto mensaje local para el log de la consola
        mensajeObj.setMensaje(mensajeOriginal);
        mensajeObj.setcodigoCRC(valorCRC);
        mensajeObj.setAddressServidor(paquete.getAddress());
        mensajeObj.setPuertoServidor(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \"" + mensajeObj.getMensaje() 
            + "\" [CRC: " + mensajeObj.getcodigoCRC() + "] enviado a servidor " 
            + mensajeObj.getAddressServidor() + ":" + mensajeObj.getPuertoServidor() + "\n");
            
        return mensajeObj;
    }

    // detener hilo elegantemente
    public void detener() {
        ejecutando = false;
        if (socket != null  && !socket.isClosed()) {
            socket.close();
        }
    }
}