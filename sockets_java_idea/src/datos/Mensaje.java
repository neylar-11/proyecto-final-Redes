package datos;

import java.net.*;

public class Mensaje {
    protected String mensaje; //cadena del usuario
    protected int puertoServidor;
    protected int puertoCliente;
    protected InetAddress addressCliente;
    protected InetAddress addressServidor;
    protected long codigoCRC; // nuevo atributo para el CRC

    public Mensaje() {

    }

    public Mensaje(String mensaje, int puertoServidor, int puertoCliente, InetAddress addressCliente, InetAddress addressServidor) {
        this.mensaje = mensaje;
        this.puertoServidor = puertoServidor;
        this.puertoCliente = puertoCliente;
        this.addressCliente = addressCliente;
        this.addressServidor = addressServidor;
        this.codigoCRC = codigoCRC; // <--- NUEVO: calcular el CRC al crear el mensaje
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public int getPuertoServidor() {
        return puertoServidor;
    }

    public void setPuertoServidor(int puertoServidor) {
        this.puertoServidor = puertoServidor;
    }

    public int getPuertoCliente() {
        return puertoCliente;
    }

    public void setPuertoCliente(int puertoCliente) {
        this.puertoCliente = puertoCliente;
    }

    public InetAddress getAddressCliente() {
        return addressCliente;
    }

    public void setAddressCliente(InetAddress addressCliente) {
        this.addressCliente = addressCliente;
    }

    public InetAddress getAddressServidor() {
        return addressServidor;
    }

    public void setAddressServidor(InetAddress addressServidor) {
        this.addressServidor = addressServidor;
    }

    // NUEVOS: Getters y Setters para el CRC
    public long getcodigoCRC() {
        return codigoCRC;
    }

    public void setcodigoCRC(long codigoCRC) {
        this.codigoCRC = codigoCRC;
    }
}
