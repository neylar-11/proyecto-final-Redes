package servidor.udp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.nio.charset.StandardCharsets;

public class ServidorEscuchaUDP2 extends Thread {
    protected DatagramSocket socket; //socket UDP
    protected final int PUERTO_SERVER;

    // Tamaño de buffer razonable
    private static final int MAX_BUFFER = 1024;

    public ServidorEscuchaUDP2(int puertoS)throws Exception {
        PUERTO_SERVER = puertoS;

        // Crear socket UDP
        socket = new DatagramSocket(PUERTO_SERVER);
        // Timeout opcional
        // socket.setSoTimeout(5000);
    }

    @Override
    public void run() {
        try {
            EntradaSalida.mostrarMensaje("Servidor UDP escuchando en puerto "+ PUERTO_SERVER + "...\n");

            // El servidor UDP normalmente nunca termina
            while (true) {
                try {
                    // recibir datagrama
                    Mensaje mensajeObj = recibeMensaje();

                    // procesar lógica
                    procesaMensaje(mensajeObj);
                }
                catch (SocketTimeoutException e) {
                    EntradaSalida.mostrarMensaje("Timeout esperando paquetes...\n");
                }
                catch (Exception e) {
                    EntradaSalida.mostrarMensaje( "Error procesando paquete: "+ e.getMessage() + "\n");
                }
            }

        }
        catch (Exception e) {
            System.err.println("Error servidor UDP: "+ e.getMessage());
        }
        finally {
            if (socket != null && !socket.isClosed()) { //cerrar socket
                socket.close();
            }
        }
    }

    private void procesaMensaje(Mensaje mensajeObj) throws Exception {

        String mensaje = mensajeObj.getMensaje();
        String respuesta;

        // Comparaciones de msg
        if (mensaje.equalsIgnoreCase("hola")) {
            respuesta = "¿Cómo estás?";
        }
        else if (mensaje.equalsIgnoreCase("bien y tú?")) {
            respuesta = "Estoy pal arrastre, aunque gracias por preguntar";
        }
        else if (mensaje.equalsIgnoreCase("fin")) {
            // NO cerrar servidor
            respuesta = "Cliente finalizó comunicación";
        }
        else {
            respuesta = "Servidor no entiende: " + mensaje;
        }

        mensajeObj.setMensaje(respuesta);
        enviaMensaje(mensajeObj);
    }

    /*private Mensaje recibeMensaje() throws Exception {

        Mensaje mensajeObj = new Mensaje();
        // buffer recepción
        byte[] buffer = new byte[MAX_BUFFER];

        //Datagrama
        DatagramPacket paquete = new DatagramPacket( buffer, buffer.length);

        // Se queda bloqueante en espera
        socket.receive(paquete);

        // convertir bytes a cadena String
        String mensaje =  new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8);

        mensajeObj.setMensaje(mensaje);
        // datos cliente
        mensajeObj.setAddressCliente(paquete.getAddress());
        mensajeObj.setPuertoCliente(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje() + "\" del cliente "
                + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente()+ "\n");

        return mensajeObj;
    }*/

    private Mensaje recibeMensaje() throws Exception {
        Mensaje mensajeObj = new Mensaje();
        byte[] buffer = new byte[MAX_BUFFER];
        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length);

        // 1. El servidor se bloquea esperando el paquete
        socket.receive(paquete);

        // 2. Convertir los bytes recibidos a String completo ("Texto|Checksum")
        String cadenaRecibida = new String(paquete.getData(), 0, paquete.getLength(), StandardCharsets.UTF_8);

        // 3. Separar los datos usando el token '|'
        // Se usa "\\|" porque el carácter '|' es especial en expresiones regulares
        String[] partes = cadenaRecibida.split("\\|");

        if (partes.length == 2) {
            String mensajeTexto = partes[0];
            long checksumRecibido = Long.parseLong(partes[1]);

            // 4. Recalcular el CRC32 sobre el texto recibido para verificar integridad
            byte[] bytesParaVerificar = mensajeTexto.getBytes(StandardCharsets.UTF_8);
            java.util.zip.CRC32 crc = new java.util.zip.CRC32();
            crc.update(bytesParaVerificar);
            long checksumCalculado = crc.getValue();

            // 5. Validar si los códigos coinciden (Fase de detección de errores)
            if (checksumCalculado == checksumRecibido) {
                mensajeObj.setMensaje(mensajeTexto);
                EntradaSalida.mostrarMensaje("[INTEGRIDAD OK] ");
            } else {
                // Si los números no coinciden, el mensaje se alteró en la red
                mensajeObj.setMensaje("[ERROR: MENSAJE CORRUPTO] -> " + mensajeTexto);
                EntradaSalida.mostrarMensaje("[¡ALERTA DE ERROR EN RED!] ");
            }
        
            mensajeObj.setcodigoCRC(checksumRecibido);
        } else {
            // Por si se recibe un paquete que no cumpla con el formato
            mensajeObj.setMensaje(cadenaRecibida);
        }

        // 6. Asignar los datos del remitente (Cliente)
        mensajeObj.setAddressCliente(paquete.getAddress());
        mensajeObj.setPuertoCliente(paquete.getPort());

        EntradaSalida.mostrarMensaje("Mensaje recibido \"" + mensajeObj.getMensaje() 
            + "\" del cliente " + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");

        return mensajeObj;
    }

    private void enviaMensaje( Mensaje mensajeObj) throws Exception {

        // String a  bytes UTF-8
        byte[] buffer =  mensajeObj.getMensaje().getBytes(StandardCharsets.UTF_8);

        DatagramPacket paquete = new DatagramPacket(buffer, buffer.length, mensajeObj.getAddressCliente(), mensajeObj.getPuertoCliente());

        // envío UDP
        socket.send(paquete);

        EntradaSalida.mostrarMensaje( "Mensaje enviado \"" + mensajeObj.getMensaje() + "\" al cliente "
                + mensajeObj.getAddressCliente() + ":" + mensajeObj.getPuertoCliente() + "\n");
    }
}