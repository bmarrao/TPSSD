package kademlia;

import java.math.BigInteger;
import java.util.*;

public class KademliaLookUp implements Runnable
{
    public static KademliaRoutingTable rt;
    public static KademliaProtocol protocol;
    ArrayList<Node> nodes ;
    byte[] nodeId ;
    int a ;
    Node n;
    KademliaLookUp(KademliaProtocol protocol, KademliaRoutingTable rt, ArrayList<Node> nodes,byte[] nodeId, int a, Node n)
    {
        this.rt = rt;
        this.protocol = protocol;
        this.nodes = nodes;
        this.nodeId = nodeId ;
        this.a = a ;
        this.n = n;
    }

    @Override
    public void run()
    {
        // Initialize a set to keep track of visited nodes to avoid loops
        Set<Node> visitedNodes = new HashSet<>();

        PriorityQueue<Node> closestNodesQueue = new PriorityQueue<>((node1, node2) -> {
            BigInteger distance1 = rt.calculateDistance(node1.getId().toByteArray(), nodeId);
            BigInteger distance2 = rt.calculateDistance(node2.getId().toByteArray(), nodeId);
            return distance1.compareTo(distance2);
        });
        // Start by finding the closest nodes in the routing table to the target key
        List<Node> closestNodes = protocol.findNodeOp(nodeId, n.getIp(),n.getPort());

        closestNodesQueue.addAll(closestNodes);

        // Iterate until the closest nodes queue is empty or a termination condition is met
        while (!closestNodesQueue.isEmpty()) {
            // Take the closest node from the queue
            Node currentNode = closestNodesQueue.poll();

            // Check if the current node has already been visited
            if (!visitedNodes.contains(currentNode))
            {
                // Mark the current node as visited
                visitedNodes.add(currentNode);

                List<Node> additionalClosestNodes = protocol.findNodeOp(nodeId, currentNode.getIp(),currentNode.getPort());

                for (Node node : additionalClosestNodes) {
                    if (!visitedNodes.contains(node)) {
                        closestNodesQueue.add(node);
                    }
                }

                // Add the additional closest nodes found to the queue
                closestNodesQueue.addAll(additionalClosestNodes);
            }
        }

        // Initialize a list to store the result nodes
        ArrayList<Node> resultNodes = new ArrayList<>();

        // Transfer elements from priority queue to resultNodes list
        while (resultNodes.size() < a || !closestNodesQueue.isEmpty()) {
            resultNodes.add(closestNodesQueue.poll());
        }

        resultNodes.sort((node1, node2) -> {
            BigInteger distance1 = rt.calculateDistance(node1.getId().toByteArray(), nodeId);
            BigInteger distance2 = rt.calculateDistance(node2.getId().toByteArray(), nodeId);
            return distance1.compareTo(distance2);
        });
        this.nodes = resultNodes;
    }
}
