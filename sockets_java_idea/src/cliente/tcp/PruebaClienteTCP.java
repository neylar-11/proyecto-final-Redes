package cliente.tcp;

public class PruebaClienteTCP{
    public static void main(String args[])throws Exception{
        ClienteTCP clienteTCP =new ClienteTCP("10.10.28.20",60000);
             
        clienteTCP.inicia();
    }
}
