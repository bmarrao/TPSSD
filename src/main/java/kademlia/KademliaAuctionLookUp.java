package kademlia;

import java.math.BigInteger;
import java.util.*;

public class KademliaAuctionLookUp implements Runnable
{

    public static KademliaRoutingTable rt;
    public static KademliaProtocol protocol;
    Transaction t; ;
    byte[] key ;
    int a ;
    Node n;
    KademliaAuctionLookUp(KademliaProtocol protocol, KademliaRoutingTable rt, Transaction t, byte[] key, int a, Node n)
    {
        this.rt = rt;
        this.protocol = protocol;
        this.t= t;
        this.key = key ;
        this.a = a ;
        this.n = n;
    }

    @Override
    public void run() {
        // Initialize a set to keep track of visited nodes to avoid loops
        Set<Node> visitedNodes = new HashSet<>();
        // TODO FIX THIS COMPARATOR
        PriorityQueue<Node> closestNodesQueue = new PriorityQueue<>((node1, node2) -> {
            BigInteger distance1 = rt.calculateDistance(node1.getId().toByteArray(), key);
            BigInteger distance2 = rt.calculateDistance(node2.getId().toByteArray(), key);
            return distance1.compareTo(distance2);
        });
        // Start by finding the closest nodes in the routing table to the target key
        FindAuctionResponse response = protocol.findAuctionOp(key, n.getIp(), n.getPort());

        if (response.getHasTransaction())
        {
            this.t = response.getT();
        } else
        {
            closestNodesQueue.addAll(response.getNodesList());
            while (!closestNodesQueue.isEmpty()) {
                // Take the closest node from the queue
                Node currentNode = closestNodesQueue.poll();

                // Check if the current node has already been visited
                if (!visitedNodes.contains(currentNode)) {
                    // Mark the current node as visited
                    visitedNodes.add(currentNode);

                    response = protocol.findAuctionOp(key, currentNode.getIp(),currentNode.getPort());

                    if (response.getHasTransaction())
                    {
                        this.t = response.getT();
                        break;
                    }
                    List<Node> additionalClosestNodes = response.getNodesList();

                    for (Node node : additionalClosestNodes) {
                        if (!visitedNodes.contains(node)) {
                            closestNodesQueue.add(node);
                        }
                    }

                    // Add the additional closest nodes found to the queue
                    closestNodesQueue.addAll(additionalClosestNodes);


                }
            }
        }


    }

}
