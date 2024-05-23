package kademlia;
import java.time.LocalDateTime;

public class KademliaNode
{
    public String ipAdress ;
    public byte[] nodeId;

    public int port;
    public byte[] publicKey;
    public LocalDateTime time;
    public static float reputation;


    public KademliaNode(String ipAdress, byte[] nodeId,int port, byte[] publicKey)
    {
        this.ipAdress = ipAdress;
        // Create nodeID this.nodeId= nodeId;
        this.port = port;
        this.nodeId = nodeId;
        this.publicKey = publicKey;
        this.time = LocalDateTime.now();
        reputation = 0;
    }

    public String getIpAdress() {
        return ipAdress;
    }

    public void setIpAdress(String ipAdress) {
        this.ipAdress = ipAdress;
    }

    public byte[] getNodeId() {
        return nodeId;
    }

    public void setNodeId(byte[] nodeId) {
        this.nodeId = nodeId;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public byte[] getPublicKey() { return this.publicKey; }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public LocalDateTime getDateTime()
    {
        return time;
    }

    public void setReputation(float val) { reputation = val; }

    public float getReputation() { return reputation; }

    public void setTime()
    {
        this.time = LocalDateTime.now();
    }
}

