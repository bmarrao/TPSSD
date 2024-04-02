package kademlia;

import kademlia.server.KademliaServer;
import org.checkerframework.checker.units.qual.K;

import java.util.List;

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
        ipAddress = "localhost";
        port = 5003;
        nodeId = kd.generateNodeId();
        KademliaProtocol protocol = new KademliaProtocol(nodeId);


        KademliaServer server = new KademliaServer(port);
        Thread serverThread = new Thread(server);
        serverThread.start();
        KademliaProtocol kp = new KademliaProtocol(nodeId);

        rtNormal = new KrtNormal(nodeId, protocol, 20);
        rtBootStrap = new KrtBootStrap(nodeId, protocol, 20);

        for (int i  = 0 ; i < 200; i++)
        {
            KademliaNode ins = new KademliaNode("localhost",kd.generateNodeId(),5000);
            rtNormal.insert(ins);
            rtBootStrap.insert(ins);

        }
        rtNormal.printTree();
        System.out.println("PRINT BOOTSTRAP");
        rtBootStrap.printTree();
        // 
    }
}
