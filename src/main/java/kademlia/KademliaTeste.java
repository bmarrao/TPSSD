package kademlia;

import auctions.Auction;
import com.google.protobuf.ByteString;
import kademlia.server.KademliaServer;
import org.checkerframework.checker.units.qual.K;

import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class KademliaTeste
{
                    /*

    public static void main(String[] args)
    {
         byte[] nodeId;
         // TODO define ipadress and port
         String ipAddress = "";
        int port;
        PublicKey publicKey = null;
        PrivateKey privateKey = null;

        KademliaRoutingTable rtNormal ;
        KademliaRoutingTable rtBootStrap ;

        byte[] cryptoPuzzleSol = null;
        int leadingZeros = 8;

        //Kademlia kd = new Kademlia();
        port = 5003;
        nodeId = Kademlia.generateNodeId();
        System.out.println("Generated node ID: \n" + Arrays.toString(nodeId) + "\n");
        KademliaProtocol protocol = new KademliaProtocol(nodeId,ipAddress, port, publicKey, privateKey, cryptoPuzzleSol);
        System.out.println();

        rtNormal = new KrtNormal(nodeId, protocol, 20, 20);


        KademliaServer server = new KademliaServer(port, new Auction(protocol), leadingZeros);
        Thread serverThread = new Thread(server);
        serverThread.start();

        rtBootStrap = new KrtBootStrap(nodeId, protocol, 20, 20);
        Map<KademliaNode, BigInteger> mapa = new HashMap<KademliaNode,BigInteger>();
        for (int i  = 0 ; i < 200; i++)
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp("localhost");
            nd.setPort(5000);
            nodeId = Kademlia.generateNodeId();
            assert nodeId != null;
            nd.setId(ByteString.copyFrom(nodeId));
            Node ins = nd.build();
            rtNormal.insert(ins, 0);
            rtBootStrap.insert(ins, 0);

        }

        System.out.println();
        rtNormal.printTree();

        System.out.println(rtNormal.printId(rtBootStrap.myNodeId));
        System.out.println(rtNormal.printId(nodeId));
        System.out.println(rtNormal.printId(arr.get(0).getId().toByteArray()));


        System.out.println(rtNormal.printId(rtBootStrap.myNodeId));
        for (Node n : arr)
        {
            System.out.println(rtBootStrap.printId(n.getId().toByteArray()));

        }


        System.out.println();
        ArrayList<Node> arr = rtBootStrap.findClosestNode(nodeId, 1);
        for (Node n : arr)
        {
            System.out.println(rtBootStrap.printId(n.getId().toByteArray()));

        }
        System.out.println(rtBootStrap.printId(nodeId));
        System.out.println(rtBootStrap.printId(rtBootStrap.myNodeId));


        System.out.println(rtBootStrap.CheckNodeIsInTree(nodeId,rtBootStrap.root,""));

        //
    }
             */

}
