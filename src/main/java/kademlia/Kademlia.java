package kademlia;

import kademlia.server.KademliaServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

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

        KademliaServer server = new KademliaServer(port);
        Thread serverThread = new Thread(server);
        serverThread.start();
        KademliaProtocol kp = new KademliaProtocol(nodeId);

        int k = 20;
        //rt = new KademliaRoutingTable(nodeId, protocol, k);

        String bootstrapFilePath = args[2];

        // args[2] pode ser um ficheiro .txt que contem info dos bootstrap nodes na rede
        if (Boolean.parseBoolean(args[3])) // its a bootstrap node
        {
            // add ip and port to bootstrap file
            addIpPortBSFile(ipAddress, port, bootstrapFilePath);

            // initialize routing table
            rt = new KrtBootStrap(nodeId,kp,k);
        }
        else
        {
            // initialize routing table
            rt = new KrtNormal(nodeId,kp,k);

            // read info of available bootstrap nodes and randomly select one
            List<String> bootstrapNodesInfo = getBootstrapNodesInfo(bootstrapFilePath);
            String selectedBootstrap = selectRandomBootstrapNode(bootstrapNodesInfo);

            String[] bootstrapIpPort = selectedBootstrap.split(" ");
            String ipBootstrap = bootstrapIpPort[0];
            int portBootstrap = Integer.parseInt(bootstrapIpPort[1]);

            // send find node operation to selected bootstrap node
            KademliaFindOpResult res = protocol.findNodeOp(nodeId, ipAddress, port, nodeId,ipBootstrap,portBootstrap);

            // add received ids of closest nodes to routing table
            // o metodo addNodes deve verificar se os ids contidos já estao na routing table
            // o metodo retorna boolean (com true se foram adicionados novos nós à rt)
            // rt.addNodes(res.getNodesList());

            // send find node to the closest nodes
            // repeat process if new closest nodes are received
            //TODO perguntar sobre quando parar find Node
            boolean foundNewClosestNodes = true;
            /*
            while (res.size() == 0)
            {
                foundNewClosestNodes = false;
                ArrayList<Node> newRes;
                for (Node n : res.getNodesList()) 
                {
                    protocol = new KademliaProtocol(nodeId, n.getIp(), n.getPort());
                    KademliaFindOpResult closestNodes = protocol.findNodeOp(nodeId, ipAddress, port, nodeId);
                     for (Node n : res.getNodesList()){
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
                            //Nothing for now                    protocol = new KademliaProtocol(nodeId, n.ipAdress, n.port);

                         }
                    }
                }
                            */

        }

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

    public static void addIpPortBSFile(String ipAddress, int port, String bootstrapFilePath) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(bootstrapFilePath, true));
            writer.write(ipAddress + " " + port + "\n");
            writer.close();
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

    private static List<String> getBootstrapNodesInfo(String filepath) {
        List<String> ipPortPairs = new ArrayList<>();
        try {
            ipPortPairs = Files.readAllLines(Paths.get(filepath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipPortPairs;
    }


    private static String selectRandomBootstrapNode(List<String> bootstrapNodesInfo) {
        Random random = new Random();
        int randomIndex = random.nextInt(bootstrapNodesInfo.size());
        return bootstrapNodesInfo.get(randomIndex);
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
