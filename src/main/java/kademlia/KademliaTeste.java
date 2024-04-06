package kademlia;

import com.google.protobuf.ByteString;
import kademlia.server.KademliaServer;
import org.checkerframework.checker.units.qual.K;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KademliaTeste
{
    public static void main(String[] args)
    {
         byte[] nodeId;
         String ipAddress;

        int port;
        KademliaRoutingTable rtNormal ;
        KademliaRoutingTable rtBootStrap ;
        Kademlia kd = new Kademlia();
        port = 5003;
        nodeId = kd.generateNodeId();
        KademliaProtocol protocol = new KademliaProtocol(nodeId);
        System.out.println();

        KademliaServer server = new KademliaServer(port);
        Thread serverThread = new Thread(server);
        serverThread.start();
        KademliaProtocol kp = new KademliaProtocol(nodeId);

        rtNormal = new KrtNormal(nodeId, protocol, 20);



        rtBootStrap = new KrtBootStrap(nodeId, protocol, 20);
        Map<KademliaNode, BigInteger> mapa = new HashMap<KademliaNode,BigInteger>();
        for (int i  = 0 ; i < 200; i++)
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp("localhost");
            nd.setPort(5000);
            nd.setId(ByteString.copyFrom(kd.generateNodeId()));
            Node ins = nd.build();
            rtNormal.insert(ins);
            rtBootStrap.insert(ins);

        }
        rtNormal.printTree();

        nodeId = kd.generateNodeId();

        ArrayList<Node> arr = rtNormal.findClosestNode(nodeId, 50);
        System.out.println(arr.size());


        arr = rtBootStrap.findClosestNode(nodeId, 50);
        System.out.println(arr.size());

        // 
    }
}
