package kademlia;

import java.math.BigInteger;
import java.util.*;

public class KademliaBlockLookUp implements Runnable
{
    public static KademliaRoutingTable rt;
    public static KademliaProtocol protocol;
    grpcBlock b;
    byte[] key ;
    int a ;
    Node n;
    KademliaBlockLookUp(KademliaProtocol protocol, KademliaRoutingTable rt, grpcBlock b, byte[] key, int a, Node n)
    {
        this.rt = rt;
        this.protocol = protocol;
        this.b= b;
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
        FindBlockResponse response = protocol.findBlockOp(key, n.getIp(), n.getPort());

        if (response.getHasBlock())
        {
            this.b = response.getB();
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

                    response = protocol.findBlockOp(key, currentNode.getIp(),currentNode.getPort());

                    if (response.getHasBlock())
                    {
                        this.b = response.getB();
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
