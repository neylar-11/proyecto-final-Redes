package cliente.udp;

public class PruebaClienteUDP{
    public static void main(String args[]) throws Exception{
        ClienteUDP clienteUDP =new ClienteUDP("192.168.1.107",50000);
        
        clienteUDP.inicia();
    }
}
