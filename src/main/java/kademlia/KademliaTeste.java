package kademlia;

import auctions.Auction;
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
         // TODO define ipadress and port
         String ipAddress = "";
        int port = 5000;

        KademliaRoutingTable rtNormal ;
        KademliaRoutingTable rtBootStrap ;

        Kademlia kd = new Kademlia();
        port = 5003;
        nodeId = kd.generateNodeId();
        KademliaProtocol protocol = new KademliaProtocol(nodeId,ipAddress, port);
        System.out.println();

        KademliaServer server = new KademliaServer(port, new Auction(protocol));
        Thread serverThread = new Thread(server);
        serverThread.start();

        rtNormal = new KrtNormal(nodeId, protocol, 20, 20);


        rtBootStrap = new KrtBootStrap(nodeId, protocol, 20, 20);
        Map<KademliaNode, BigInteger> mapa = new HashMap<KademliaNode,BigInteger>();
        for (int i  = 0 ; i < 200; i++)
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp("localhost");
            nd.setPort(5000);
            nodeId = kd.generateNodeId();
            nd.setId(ByteString.copyFrom(nodeId));
            Node ins = nd.build();
            rtNormal.insert(ins);
            rtBootStrap.insert(ins);

        }
        System.out.println("");
        rtNormal.printTree();

        ArrayList<Node> arr = rtNormal.findClosestNode(nodeId, 1);
        System.out.println(rtNormal.printId(rtBootStrap.myNodeId));
        System.out.println(rtNormal.printId(nodeId));
        System.out.println(rtNormal.printId(arr.get(0).getId().toByteArray()));


        rtBootStrap.printTree();
        arr = rtBootStrap.findClosestNode(nodeId, 1);
        System.out.println(rtNormal.printId(rtBootStrap.myNodeId));
        for (Node n : arr)
        {
            System.out.println(rtBootStrap.printId(n.getId().toByteArray()));

        }

        System.out.println(rtNormal.calculateDistance(nodeId,nodeId));

        System.out.println(rtNormal.CheckNodeIsInTree(nodeId,rtNormal.root,""));

        System.out.println(rtNormal.printId(rtBootStrap.myNodeId));

        System.out.println(rtBootStrap.printId(nodeId));

        System.out.println(rtBootStrap.CheckNodeIsInTree(nodeId,rtBootStrap.root,""));

        //
    }
}
