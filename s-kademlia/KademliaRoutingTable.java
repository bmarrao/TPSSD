import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;

public class KademliaRoutingTable
{
    private String nodeId;

    private int k;
    private Map<Integer, ArrayList<NodeInfo>> kbucket;

    public KademliaRoutingTable(int k , String nodeId)
    {
        this.k = k;
        this.nodeId = nodeId;
    }

    public newACtivity(string nodeId)
    {
        
    }

    // Implement the routing table logic here
}

class NodeInfo
{
    public String ipAdress ;
    public String nodeId;
    public int port;
    public LocalDateTime time;


    public NodeInfo(String ipAdress, String nodeId,int port)
    {
        this.ipAdress = ipAdress;
        this.nodeId= nodeId;
        this.port = port;
        this.time = LocalDateTime.now();
    }

    public void setTime()
    {
        this.time = LocalDateTime.now();
    }
}