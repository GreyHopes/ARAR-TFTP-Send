import java.io.File;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Client
{
    static String ipServer = "127.0.0.1";
    static int portServer = 69;

    public static void sendFile(String nomFichier)
    {
        //File fichier = new File(nomFichier);

        System.out.println("Fichier à envoyer");
        //System.out.println("Nom : "+fichier.getName());
        System.out.println();

        try
        {
            DatagramSocket ds = new DatagramSocket();

            //Envoi du WRQ
            InetAddress adresse = InetAddress.getByName(ipServer);
            byte[] requestPacket = createRequestPacket(nomFichier);

            DatagramPacket dp = new DatagramPacket(requestPacket,requestPacket.length,adresse,portServer);
            ds.send(dp);

            //Reception premier ACK
            ds.receive(dp);
            byte[] receivedData = dp.getData();
            portServer = dp.getPort();

            //On vérifie si on a bien reçu un ACK
            if(receivedData[0] == 0 && receivedData[1] == 4)
            {
                System.out.println("ACK");
            }
            else if(receivedData[0] == 0 && receivedData[1] == 5)
            {
                System.out.println("Erreur");
            }

        }
        catch(Exception e)
        {
            System.out.println(e);
        }

    }

    public static byte[] createRequestPacket(String nomFichier)
    {
        String mode = "octet";
        int tailleTab = 2 + nomFichier.length()+ 1 + mode.length() + 1;
        byte[] packet = new byte[tailleTab];
        int posTab = 0;

        //On insère l'entête
        packet[0] = (byte) 0;
        packet[1] = (byte) 2; //WRQ
        posTab += 2;

        //On met le nom du fichier
        for(int i =0;i< nomFichier.length();i++)
        {
            packet[i+2] = (byte) nomFichier.charAt(i);
            posTab++;
        }

        //Le zero
        packet[posTab] = (byte) 0;
        posTab++;

        //Le mode
        for(int i =0;i< mode.length();i++)
        {
            packet[posTab] = (byte) mode.charAt(i);
            posTab++;
        }

        //Le zero
        packet[posTab] = (byte) 0;

        return packet;
    }


    public static void main(String[] args)
    {
        sendFile("test.txt");
    }
}
