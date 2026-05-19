package cliente.udp;

public class PruebaClienteUDP{
    public static void main(String args[]) throws Exception{
        ClienteUDP clienteUDP =new ClienteUDP("10.10.28.129",50000);
        
        clienteUDP.inicia();
    }
}
