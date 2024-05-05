package kademlia;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

import static kademlia.Kademlia.rtn;


public class KademliaJoinNetwork implements Runnable {
    public byte[] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public String bootstrapIp;
    public int bootstrapPort;
    public byte[] cryptoPuzzleSol;

    KademliaJoinNetwork(byte[] nodeId, String ipAddress, int port, PublicKey publicKey, PrivateKey privateKey, String bootstrapIp, int bootstrapPort, byte[] cryptoPuzzleSol) {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.bootstrapIp = bootstrapIp;
        this.bootstrapPort = bootstrapPort;
        this.cryptoPuzzleSol = cryptoPuzzleSol;
    }

    @Override
    public void run() {
        KademliaProtocol protocol = new KademliaProtocol(this.nodeId, this.ipAddress, this.port, this.publicKey, this.privateKey, this.cryptoPuzzleSol);

        // send find node operation to selected bootstrap node
        List<Node> closestNodes;
        try {
            closestNodes = protocol.findNodeOp(this.nodeId, this.nodeId, this.bootstrapIp, this.bootstrapPort).getNodesList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("Closest nodes are: " + closestNodes);

        // add received ids of closest nodes to routing table
        ArrayList<Node> newAddedNodes = new ArrayList<>();
        for (Node n : closestNodes)
        {
            if (rtn.insert(n, 1))
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
                    closestNodes = protocol.findNodeOp(this.nodeId, n.getId().toByteArray(), n.getIp(), n.getPort()).getNodesList();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                for (Node m : closestNodes)
                {
                    if (rtn.insert(m, 1))
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