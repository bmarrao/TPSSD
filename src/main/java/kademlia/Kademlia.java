package kademlia;

import com.google.protobuf.ByteString;
import kademlia.server.KademliaServer;
import kademlia.client.KademliaClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.Arrays;

public class Kademlia
{
    public static byte[] nodeId;
    public static String ipAddress;

    public static int port;
    public boolean bootstrapNode;
    public static KademliaRoutingTable rt ;
    public static KademliaProtocol protocol;

    // TODO Public e private Key - Quando for implementar blockchain
    // TODO Inicialização do no kademlia , contacto com boostrap node - Cristina
    // TODO Criar bootstrap node - Breno
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ipAddress = args[0];

        port = Integer.parseInt(args[1]);

        nodeId = generateNodeId();

        new KademliaServer(port);

        protocol = new KademliaProtocol(nodeId, ipAddress, port);

        int k = 20;
        rt = new KademliaRoutingTable(nodeId, protocol, k);


        /*
        if (args[2] != null)
        {
            rt = new KademliaRoutingTableBootStrap(this.nodeId)
        }
        else
        {
            rt = new KademliaRoutingTableNormal(this.nodeId);
        }



        */
        System.out.println("Generated nodeId: " + Arrays.toString(nodeId));

        //callOps(protocol, nodeId, ipAddress, port);
    }


    // node id is 160-bit and is based on SHA-1
    public static byte[] generateNodeId()
    {
        byte[] array = new byte[20];
        // SecureRandom() assures that random generated word is safe for crypto purposes
        new SecureRandom().nextBytes(array);

        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(array);
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static KademliaProtocol getKdProtocol()
    {
        return protocol;
    }


    /*
    public static void callOps(KademliaProtocol protocol, byte []nodeId, String ip, int port)
    {
        String key = "key123";
        String val = "val123";

        System.out.println("Response for ping: " + Arrays.toString(protocol.pingOp(nodeId)));
        System.out.println("Response for store: " + protocol.storeOp(nodeId, key, val, ip, port));

        System.out.println("Response for find node:");
        KademliaFindOpResult findNodeRes = protocol.findNodeOp(nodeId, ip, port, key);
        System.out.println("   id: " + Arrays.toString(findNodeRes.getNodeId()));

        System.out.println("   nodes: ");
        for (Node n : findNodeRes.getNodesList()) {
            System.out.println(n);
        }

        System.out.println("Response for find value:");
        KademliaFindOpResult findValueRes = protocol.findValueOp(nodeId, ip, port, key);
        System.out.println("   id: " + Arrays.toString(findNodeRes.getNodeId()));
        System.out.println("   value: " + findNodeRes.getVal());
        System.out.println("   nodes: ");
        for (Node n : findValueRes.getNodesList()) {
            System.out.println(n);
        }
    }
     */

}
