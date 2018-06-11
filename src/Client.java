import java.io.File;
import java.io.FileInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Scanner;

public class Client
{
    static String ipServer = "192.168.43.16";
    //static String ipServer = "127.0.0.1";
    static int portServer = 69;

    public static void sendFile(String nomFichier)
    {
        File f = new File(nomFichier);

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
            byte[] receivedAck= dp.getData();
            portServer = dp.getPort();

            //On vérifie si on a bien reçu un ACK
            if(receivedAck[0] == 0 && receivedAck[1] == 4)
            {
                System.out.println("ACK");
                System.out.println("Sending : " + nomFichier);

                FileInputStream fichier = null;
                byte[] ack = new byte[2];

                try
                {
                    fichier = new FileInputStream(f);
                }
                catch (Exception e)
                {
                    System.out.println(e);
                    return;
                }

                ack[0] = (byte) 0;
                ack[1] = (byte) 1;

                boolean eof = false;
                int nbLus = 512;

                do
                {
                    int byteLus = 0;
                    //Lecture fichier par bloc de 512
                    byte[] dataToSend = new byte[512];

                    for(int i =0;i<512 && byteLus != -1;i++)
                    {
                        byteLus = fichier.read();
                        if(byteLus != -1)
                        {
                            dataToSend[i] = (byte) byteLus;
                        }
                        else
                        {
                            eof = true;
                            nbLus = i;
                        }
                    }

                    byte[] toSend = Arrays.copyOf(dataToSend,nbLus);

                    //Création packet data
                    int taille = 2+2+toSend.length;
                    byte[] packet = new byte[taille];

                    //Code op
                    packet[0] = (byte) 0;
                    packet[1] = (byte) 3;

                    //Ack
                    packet[2] = ack[0];
                    packet[3] = ack[1];
                    System.arraycopy(toSend,0,packet,4,toSend.length);
                    dp = new DatagramPacket(packet,packet.length,adresse,portServer);

                    //Envoi
                    ds.send(dp);
                    System.out.println("Envoi : " + toSend.length + " bytes");

                    //Reception ACK
                    ds.receive(dp);

                    byte[] receivedData = dp.getData();

                    if(receivedData[0] == 0 && receivedData[1] == 4)
                    {
                        byte[] received = {receivedData[2],receivedData[3]};
                        ack[1] = (byte)(received[1] + 1);
                        if(ack[1] == 0)
                        {
                            ack[0]++;
                        }
                        else if(receivedAck[0] == 0 && receivedAck[1] == 5)
                        {
                            eof = true;
                            afficherErreur(receivedAck[3]);
                        }

                    }
                }while(!eof);

                if(fichier != null)
                {
                    System.out.println("Fichier envoyé avec succès !");
                    fichier.close();
                }

            }
            else if(receivedAck[0] == 0 && receivedAck[1] == 5)
            {
                afficherErreur(receivedAck[3]);
            }

        }
        catch(Exception e)
        {
            System.out.println(e);
        }

    }

    public static void afficherErreur(byte codeErreur)
    {
        System.out.println("-- Error --");
        //Code
        switch(codeErreur)
        {
            case 1 :
                System.out.println("File not found");
            break;

            case 2 :
                System.out.println("Access violation");
            break;

            case 3 :
                System.out.println("Disk full");
            break;

            case 4 :
                System.out.println("Illegal TFTP operation");
            break;

            case 6:
                System.out.println("File already exists");
            break;
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
        Scanner sc = new Scanner(System.in);
        System.out.println("Entrez le nom du fichier à envoyer :");
        String nomfichier = sc.nextLine();
        sendFile(nomfichier);
    }
}
