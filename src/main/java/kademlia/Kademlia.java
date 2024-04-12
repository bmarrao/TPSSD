package kademlia;

import auctions.Auction;
import kademlia.server.KademliaServer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Kademlia
{
    public static byte[] nodeId;
    public static String ipAddress;

    public static int port;
    public static boolean bootstrapNode;
    public static KademliaRoutingTable rt;
    public static KademliaProtocol protocol;

//    Auction a ;

  //  BlockChain b;
    // TODO Public e private Key - Quando for implementar blockchain
    // TODO Inicialização do no kademlia , contacto com boostrap node - Cristina
    // TODO Criar bootstrap node - Breno

    public Kademlia(String ipAdress, int port, boolean bootstrap, int k, String bootstrapFilePath)
    {
        protocol = new KademliaProtocol(nodeId,ipAdress,port);
        KademliaServer server = new KademliaServer(port, new Auction(protocol));
        Thread serverThread = new Thread(server);
        serverThread.start();

        if (bootstrap)
        {
            rt = new KrtBootStrap(nodeId,protocol,k);
            initiateBootStrap(bootstrapFilePath);

        }
        else
        {
            rt = new KrtNormal(nodeId, protocol,k);
            initiateNormal();
        }
    }

    public void initiateNormal()
    {

    }

    public void initiateBootStrap(String bootstrapFilePath)
    {
        /*
        List<String> bootstrapNodesInfo = getBootstrapNodesInfo(bootstrapFilePath);
        String selectedBootstrap = selectRandomBootstrapNode(bootstrapNodesInfo);
        String[] bootstrapIpPort = selectedBootstrap.split(" ");
        String ipBootstrap = bootstrapIpPort[0];
        int portBootstrap = Integer.parseInt(bootstrapIpPort[1]);
        if (args[3]== null) // its a bootstrap node
        {
            bootstrapNode = true;
            // add ip and port to bootstrap file
            addIpPortBSFile(ipAddress, port, bootstrapFilePath);

            // initialize routing table
            rt = new KrtBootStrap(nodeId,kp,k);
            KademliaFindOpResult res = protocol.findNodeOp(nodeId, ipAddress, port, nodeId,ipBootstrap,portBootstrap);

        }
        else
        {
            bootstrapNode = false;
            // initialize routing table
            rt = new KrtNormal(nodeId,kp,k);

            // read info of available bootstrap nodes and randomly select one


            // send find node operation to selected bootstrap node
            KademliaFindOpResult res = protocol.findNodeOp(nodeId, ipAddress, port, nodeId,ipBootstrap,portBootstrap);

            // add received ids of closest nodes to routing table
            // o metodo addNodes deve verificar se os ids contidos já estao na routing table
            // o metodo retorna boolean (com true se foram adicionados novos nós à rt)
            // rt.addNodes(res.getNodesList());

            // send find node to the closest nodes
            // repeat process if new closest nodes are received
            //TODO perguntar sobre quando parar find Node

        }

         */

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

    public static void main(String[] args) throws NoSuchAlgorithmException {
        byte[] nodeId = sKadGenerateNodeId(8, 8);
        System.out.println("S/Kademlia nodeId: " + Arrays.toString(nodeId));
    }

    public static boolean checkZeroCount(byte[] puzzle, int numOfLeadingZeros) {
        int leadingZeros = 0;

        for (byte b : puzzle) {
            if (b == 0)
            {
                leadingZeros += 8; // If the byte is zero, add 8 to leadingZeros since byte contains 8 bits
            }
            else
            { // for example byte = 00001234
                // Count leading zeros in the byte using bit manipulation
                int byteLeadingZeros = Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                leadingZeros += byteLeadingZeros;
                break; // Stop counting leading zeros once a non-zero byte is encountered
            }
        }
        System.out.println("Leading zeros count: " + leadingZeros);

        if (leadingZeros >= numOfLeadingZeros)
        {
            return true;
        }
        return false;
    }


    public static byte[] sKadGenerateNodeId(int leadingZerosStatic, int leadingZerosDynamic) throws NoSuchAlgorithmException {
        /* static crypto puzzle - against eclipe attacks
             1) generate pair (Spub, Spriv)
             2) P = H(H(Spub))
             3) if preceeding x zero bits => NodeId = H(Spub) generated
             4) otherwise generate pair (Spub, Spriv) again and go back to 1)
        */
        byte[] sKadNodeId = new byte[32];
        boolean generatedId = false;

        while (!generatedId)
        {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            SecureRandom secureRandom = new SecureRandom();
            keyPairGenerator.initialize(2048, secureRandom);

            // Generate key pair and get public key
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            java.security.PublicKey publicKey = keyPair.getPublic();
            System.out.println("Generated public and private key pair");

            // Hash public key twice
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            sKadNodeId = md.digest(publicKey.getEncoded());
            byte[] staticPuzzle = md.digest(sKadNodeId);
            System.out.println("Static puzzle is: " + Arrays.toString(staticPuzzle));

            if (checkZeroCount(staticPuzzle, leadingZerosStatic))
            {
                System.out.println("Static puzzle solved!");
                generatedId = true;
            }
        }


        /* dynamic crypto puzzle - against sybil attacks
            1) NodeId = H(Spub)
            2) choose random X
            3) P = H(NodeId XOR X)
            4) if preceeding y zero bits => puzzle solved
            5) otherwise choose random X and go back to 2)
        */
        boolean solvedDynamic = false;
        while (!solvedDynamic)
        {
            // Generate random byte X
            Random random = new Random();
            byte randomX = (byte) random.nextInt(256);

            System.out.println("Generated random X: " + randomX);

            byte[] xorOp = new byte[sKadNodeId.length];

            // calculate NodeId XOR randomX
            for (int i = 0; i < sKadNodeId.length; i++)
            {
                xorOp[i] = (byte) (sKadNodeId[i] ^ randomX);
            }
            System.out.println("Calculated XOR: " + Arrays.toString(xorOp));

            // H(NodeId XOR randomX)
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] dynamicPuzzle = md.digest(xorOp);
            System.out.println("Hashed XOR value");

            if (checkZeroCount(dynamicPuzzle, leadingZerosDynamic))
            {
                System.out.println("Dynamic puzzle solved!");
                solvedDynamic = true;
            }
        }
        return sKadNodeId;
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
