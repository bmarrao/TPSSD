package kademlia;

import java.util.List;

public class KademliaFindOpResult {
    byte[] nodeId;
    String val;
    List<Node> nodesList;

    public KademliaFindOpResult(byte[] nodeId, String val, List<Node> nodesList)
    {
        this.nodeId = nodeId;
        this.val = val;
        this.nodesList = nodesList;
    }

    public byte[] getNodeId() {
        return this.nodeId;
    }

    public String getVal() {
        return this.val;
    }

    public List<Node> getNodesList() {
        return this.nodesList;
    }
}