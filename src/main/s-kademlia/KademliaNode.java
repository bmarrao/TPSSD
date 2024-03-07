import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class KademliaNode
{
    //Para prevenir um no de ter varios id's , o id ser em base do ipAdress
    private String nodeId;
    private String ipAddress;
    private int port;



    public KademliaNode(String ipAddress, int port)
    {
        this.nodeId = generateNodeIdFromIPAddress(ipAddress);
    }



    private String generateNodeIdFromIPAddress(String ipAddress2) {
        return "";
    }


    // Constructor, getters, setters, etc.
}