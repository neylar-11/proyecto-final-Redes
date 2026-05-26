package cliente.tcp;

import datos.EntradaSalida;
import datos.Mensaje;

import java.net.*;
import java.io.*;

public class ClienteEnviaTCP2 extends Thread {
    protected Socket socket; //socket cliente
    protected final int PUERTO_SERVER;
    protected final String SERVER;
    
    // NUEVO: Variable para controlar la ejecución del hilo
    private volatile boolean ejecutando = true; 

    public ClienteEnviaTCP2(String servidor,int puertoS) throws Exception {
        PUERTO_SERVER = puertoS;
        SERVER = servidor;

        // Se hace una invocación a primitva CONNECT
        socket = new Socket(SERVER,PUERTO_SERVER);
    }

    @Override
    public void run() {
        BufferedReader teclado = null;
        DataOutputStream out = null;

        try {
            EntradaSalida.mostrarMensaje( "Cliente conectado con servidor "+ socket.getInetAddress()
                                            + ":" + socket.getPort() + "\n");

            /* ==========================================================
               CÓDIGO ORIGINAL (MENSAJES DE TEXTO) - COMENTADO
               ==========================================================
            EntradaSalida.mostrarMensaje( "Escribe mensajes (fin para salir)\n");

            // Crear flujo de teclado
            teclado = new BufferedReader( new InputStreamReader(System.in));

            //Crea flujo de salida de red al socket
            out = new DataOutputStream(socket.getOutputStream());

            while (true) {
                //Invocamos a mètodo que envìa datos por la red
                Mensaje mensajeObj =  enviaMensaje(teclado, out);

                // condición de salida
                if (mensajeObj.getMensaje().equalsIgnoreCase("fin")) {
                    EntradaSalida.mostrarMensaje("Cerrando conexión...\n");
                    break;
                }
            }
            */

            // NUEVO CÓDIGO TRANSFERENCIA DE ARCHIVOS
            
            
            // Crear flujo de teclado
            teclado = new BufferedReader(new InputStreamReader(System.in));
            // Crea flujo de salida binario de red al socket
            out = new DataOutputStream(socket.getOutputStream());

            while (ejecutando) {
                EntradaSalida.mostrarMensaje("Escribe la ruta completa del archivo a enviar (o escribe 'fin' para salir):\n");
                String rutaArchivo = teclado.readLine();

                // condición de salida elegante
                if (rutaArchivo.equalsIgnoreCase("fin")) {
                    EntradaSalida.mostrarMensaje("Cerrando conexión TCP...\n");
                    // Le avisamos al servidor que ya no mandaremos archivos
                    out.writeUTF("EOF_TRANSFERENCIA");
                    break;
                }

                // Invocamos al método que envía el archivo físico
                enviaArchivo(rutaArchivo, out);
            }

        }
        catch (Exception e) {
            System.err.println("Error cliente: "+ e.getMessage());
        }
        finally {
            // cerrar recursos
            try {
                if (out != null) //flujos de socket
                    out.close();

                if (teclado != null) //flujos entrada de teclado
                    teclado.close();

                if (socket != null && !socket.isClosed()) { //cerramos socket
                    socket.close();
                }
            }
            catch (Exception e) {
                System.err.println("Error cerrando recursos: "+ e.getMessage());
            }
        }
    }


    // NUEVO MÉTODO: ENVÍO DE ARCHIVOS  Y CÁLCULO 

    private void enviaArchivo(String rutaArchivo, DataOutputStream out) {
        FileInputStream fis = null;
        try {
            File archivo = new File(rutaArchivo);

            if (!archivo.exists() || archivo.isDirectory()) {
                EntradaSalida.mostrarMensaje("[ERROR] El archivo no existe o la ruta es inválida.\n\n");
                return;
            }

            String nombreArchivo = archivo.getName();
            long tamanoTotalBytes = archivo.length();

            // 1. Enviar los METADATOS al servidor (Nombre y Peso)
            out.writeUTF(nombreArchivo);
            out.writeLong(tamanoTotalBytes);
            out.flush();

            // 2. Abrir archivo local
            fis = new FileInputStream(archivo);

            // Preparar la "cubeta" de 8 KB
            byte[] buffer = new byte[8192];
            int bytesLeidos;
            long bytesEnviadosAcumulados = 0;

            EntradaSalida.mostrarMensaje("Transmitiendo '" + nombreArchivo + "' (" + tamanoTotalBytes + " bytes)...\n");

            // Iniciar cronómetro
            long tiempoInicioMs = System.currentTimeMillis();

            // 3. Bucle de transferencia
            while ((bytesLeidos = fis.read(buffer)) != -1) {
                out.write(buffer, 0, bytesLeidos);
                bytesEnviadosAcumulados += bytesLeidos;

                // --- MATEMÁTICA EN TIEMPO REAL ---
                long tiempoActualMs = System.currentTimeMillis();
                long tiempoTranscurridoMs = tiempoActualMs - tiempoInicioMs;
                
                double segundosTranscurridos = tiempoTranscurridoMs / 1000.0;
                if (segundosTranscurridos == 0) segundosTranscurridos = 0.001; 

                // Tasa de transferencia en bps (Bits per second)
                double bps = (bytesEnviadosAcumulados * 8.0) / segundosTranscurridos;

                // Tiempo restante
                long bytesRestantes = tamanoTotalBytes - bytesEnviadosAcumulados;
                double tiempoRestanteSegundos = (bps > 0) ? (bytesRestantes * 8.0) / bps : 0;

                double porcentaje = ((double) bytesEnviadosAcumulados / tamanoTotalBytes) * 100.0;

                // Imprimir métricas dinámicamente (\r sobreescribe la línea actual)
                System.out.printf("Progreso: %.2f%% | Tasa: %.2f bps | Transcurrido: %.2f s | Restante: %.2f s\r",
                        porcentaje, bps, segundosTranscurridos, tiempoRestanteSegundos);
            }
            
            out.flush();
            long tiempoTotalMs = System.currentTimeMillis() - tiempoInicioMs;
            double tiempoTotalSegundos = tiempoTotalMs / 1000.0;
            double bpsPromedio = (tamanoTotalBytes * 8.0) / (tiempoTotalSegundos > 0 ? tiempoTotalSegundos : 0.001);

            // 4. Reporte Final (Latencia)
            EntradaSalida.mostrarMensaje("\n ---TRANSFERENCIA FINALIZADA:---\n");
            EntradaSalida.mostrarMensaje("\nArchivo enviado: " + nombreArchivo);
            EntradaSalida.mostrarMensaje("\nTiempo total (Latencia): " + tiempoTotalSegundos + " segundos");
            EntradaSalida.mostrarMensaje("\nTasa promedio: " + bpsPromedio + " bps\n\n");

        } catch (Exception e) {
            EntradaSalida.mostrarMensaje("\n[ERROR] Transferencia: " + e.getMessage() + "\n\n");
        } finally {
            try {
                if (fis != null) fis.close();
            } catch (Exception e) {
                System.err.println("Error cerrando archivo local: " + e.getMessage());
            }
        }
    }

    // NUEVO: Método para detener el hilo limpiamente
    public void detener() {
        ejecutando = false;
    }

    /*

    private Mensaje enviaMensaje(BufferedReader teclado, DataOutputStream out) throws Exception {

        Mensaje mensajeObj = new Mensaje();
        // leer teclado
        String mensaje = teclado.readLine();

        // enviar UTF por el socket
        out.writeUTF(mensaje);

        // forzar envío
        out.flush();

        mensajeObj.setMensaje(mensaje);
        mensajeObj.setAddressServidor(socket.getInetAddress());
        mensajeObj.setPuertoServidor(socket.getPort());

        EntradaSalida.mostrarMensaje("Mensaje \""+ mensajeObj.getMensaje()+ "\" enviado a servidor "
                + mensajeObj.getAddressServidor() + ":"+ mensajeObj.getPuertoServidor()+ "\n");

        return mensajeObj;
    }
    */
}