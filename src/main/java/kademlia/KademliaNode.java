package kademlia;
import java.math.BigInteger;
import java.time.LocalDateTime;

public class KademliaNode
{
    public String ipAdress ;
    public byte[] nodeId;

    public int port;
    public LocalDateTime time;


    public KademliaNode(String ipAdress, byte[] nodeId,int port)
    {
        this.ipAdress = ipAdress;
        // Create nodeID this.nodeId= nodeId;
        this.port = port;
        this.nodeId = nodeId;
        this.time = LocalDateTime.now();
    }

    public void setTime()
    {
        this.time = LocalDateTime.now();
    }
}

