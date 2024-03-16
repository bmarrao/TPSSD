package kademlia;

import kademlia.server.KademliaServer;
import kademlia.client.KademliaClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.MessageDigest;

public class Kademlia
{
    public static String nodeId;
    public static String ipAddress;

    public static int port;

    public boolean bootstrapNode;
    public static KademliaRoutingTable rt ;
    // TODO Public e private Key - Quando for implementar blockchain
    // TODO Inicialização do no kademlia , contacto com boostrap node - Cristina
    // TODO Criar bootstrap node - Breno
    public static void main(String[] args) throws IOException, InterruptedException
    {
        ipAddress = args[0];

        port = Integer.parseInt(args[1]);

        nodeId = generateNodeId();

        new KademliaServer(port);


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
        System.out.println("Generated nodeId: " + nodeId);

        new KademliaServer(port);

        KademliaClient client = new KademliaClient(nodeId, ipAddress, port, rt);

        callOps(client, nodeId, ipAddress, port);
    }


    // node id is 160-bit and is based on SHA-1
    public static String generateNodeId()
    {
        byte[] array = new byte[20];
        // SecureRandom() assures that random generated word is safe for crypto purposes
        new SecureRandom().nextBytes(array);
        String randomString = new String(array, StandardCharsets.UTF_8);

        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] hash = md.digest(randomString.getBytes());

            StringBuilder sb = new StringBuilder();
            for (byte b : hash)
            {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void callOps(KademliaClient client, String nodeId, String ip, int port)
    {
        String key = "key123";
        String val = "val123";

        System.out.println("Response for ping: " + client.pingOp(nodeId));
        System.out.println("Response for store: " + client.storeOp(nodeId, key, val, ip, port));

        System.out.println("Response for find node:");
        KademliaFindOpResult findNodeRes = client.findNodeOp(nodeId, ip, port, key);
        System.out.println("   id: " + findNodeRes.getNodeId());

        System.out.println("   nodes: ");
        for (Node n : findNodeRes.getNodesList()) {
            System.out.println(n);
        }

        System.out.println("Response for find value:");
        KademliaFindOpResult findValueRes = client.findValueOp(nodeId, ip, port, key);
        System.out.println("   id: " + findNodeRes.getNodeId());
        System.out.println("   value: " + findNodeRes.getVal());
        System.out.println("   nodes: ");
        for (Node n : findValueRes.getNodesList()) {
            System.out.println(n);
        }
    }
}
