package kademlia;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static kademlia.Kademlia.rt;


public class KademliaJoinNetwork implements Runnable {
    public byte[] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public byte randomX;
    public String bootstrapIp;
    public int bootstrapPort;


    KademliaJoinNetwork(byte[] nodeId, String ipAddress, int port, PublicKey publicKey, PrivateKey privateKey, byte randomX, String bootstrapIp, int bootstrapPort) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.randomX = randomX;
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
    }

    @Override
    public void run() {
        KademliaProtocol protocol = new KademliaProtocol(this.nodeId, this.ipAddress, this.port, this.publicKey, this.privateKey, this.randomX);

        // send find node operation to selected bootstrap node
        List<Node> closestNodes;
        try {
            closestNodes = protocol.findNodeOp(this.nodeId, this.bootstrapIp, this.bootstrapPort);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Closest nodes are: " + closestNodes);

        // add received ids of closest nodes to routing table
        ArrayList<Node> newAddedNodes = new ArrayList<>();
        for (Node n : closestNodes)
        {
            if (rt.insert(n, 1))
            {
                System.out.println("Got new closest node to contact: " + n.getId());
                newAddedNodes.add(n);
            }
        }

        while (!newAddedNodes.isEmpty()) {
            List<Node> nodesToIterate = new ArrayList<>(newAddedNodes);
            newAddedNodes.clear();

            for (Node n : nodesToIterate) {
                try {
                    closestNodes = protocol.findNodeOp(this.nodeId, n.getIp(), n.getPort());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (Node m : closestNodes)
                {
                    if (rt.insert(m, 1))
                    {
                        System.out.println("Got new closest node to contact: " + n.getId());
                        newAddedNodes.add(m);
                    }
                }
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