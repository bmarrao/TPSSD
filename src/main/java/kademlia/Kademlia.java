package kademlia;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import kademlia.server.KademliaServer;
public class Kademlia
{
    String nodeId;
    String ipAdress;
    int port ;
    KademliaRoutingTable rt ;
    public static void main(String[] args)
    {
        // TODO: Get args from variable "args" and set node, ipAdress, VERY IMPORTANT GENERATE node ID
        // rt = new KademliaRoutingTable(.......)
        // Talvez passar rt como argumento para server ?
        // KademliaServer(port);
        // TODO: keep sending requests to test the definitions of kademlia
        // KademliaClient()
    }

    public Kademlia()
    {

    }
    public String generateNodeId()
    {
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomKey = new byte[20]; // 160 bits for SHA-1
        secureRandom.nextBytes(randomKey);
        StringBuilder binaryStringBuilder = new StringBuilder(8 * randomKey.length);
        for (byte b : randomKey) {
            // Convert each byte to a binary string with leading zeros
            binaryStringBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
        }
        return binaryStringBuilder.toString();
    }
}
