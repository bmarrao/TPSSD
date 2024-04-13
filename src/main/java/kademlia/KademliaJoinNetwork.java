package kademlia;

import java.util.ArrayList;
import java.util.List;

import static kademlia.Kademlia.rt;


public class KademliaJoinNetwork implements Runnable {
    public byte[] nodeId;
    public String ipAddress;
    public int port;
    public String bootstrapIp;
    public int bootstrapPort;

    KademliaJoinNetwork(byte[] nodeId, String ipAddress, int port, String bootstrapIp, int bootstrapPort) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
    }

    @Override
    public void run() {
        KademliaProtocol protocol = new KademliaProtocol(this.nodeId, this.ipAddress, this.port);

        // send find node operation to selected bootstrap node
        KademliaFindOpResult closestNodes = protocol.findNodeOp(this.nodeId, this.ipAddress, this.port, this.nodeId, this.bootstrapIp, this.bootstrapPort);

        // add received ids of closest nodes to routing table
        // o metodo addNodes deve verificar se os ids contidos já estao na routing table
        // retorna a lista de nós mais próximos que ainda não estavam na rt (para depois os contactar)
        ArrayList<Node> newAddedNodes = rt.addNodes(closestNodes.getNodesList());


        while (!newAddedNodes.isEmpty()) {
            List<Node> nodesToIterate = new ArrayList<>(newAddedNodes);
            newAddedNodes.clear();

            for (Node n : nodesToIterate) {
                closestNodes = protocol.findNodeOp(this.nodeId, this.ipAddress, this.port, n.getId().toByteArray(), n.getIp(), n.getPort());
                List<Node> newNodes = closestNodes.getNodesList();
                newAddedNodes.addAll(rt.addNodes(newNodes));
            }
        }



        /*
        boolean foundNewClosestNodes = true;
        while (res.size() == 0)
        {
            foundNewClosestNodes = false;
            ArrayList<Node> newRes;
            for (Node n : res.getNodesList())
            {
                protocol = new KademliaProtocol(nodeId, n.getIp(), n.getPort());
                KademliaFindOpResult closestNodes = protocol.findNodeOp(nodeId, ipAddress, port, nodeId);
                for (Node n : res.getNodesList())
                {
                    if (!insert(n))
                    {
                        newRes.add(n);
                    }
                }
                res = newRes;
            }
            for (Node n : res.getNodesList())
            {
                KademliaFindOpResult closestNodes = protocol.findNodeOp(nodeId, ipAddress, port, nodeId,n.ipAdress, n.port);
                for (Node j : res.getNodesList())
                {
                    if (rt.insert(new KademliaNode(j.get)))
                    {
                        //Nothing for now
                        protocol = new KademliaProtocol(nodeId, n.ipAdress, n.port);
                    }
                }
            }
        }
        */
    }
}
