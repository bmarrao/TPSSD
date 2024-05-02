package kademlia;

import auctions.Auction;
import kademlia.server.KademliaServer;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.util.*;

public class Kademlia
{
    public static byte[] nodeId;
    public static PublicKey generatedPk;
    public static PrivateKey generatedSk;

    // public static String ipAddress;
    // public static int port;

    public static KademliaRoutingTable rt;
    public static KademliaProtocol protocol;
    public static String bootstrapFilePath = "src/main/java/kademlia/BootstrapNodes.txt";
    // Auction a;
    // BlockChain b;

    // TODO Public e private Key - Quando for implementar blockchain
    // TODO Inicialização do no kademlia , contacto com boostrap node - Cristina
    // TODO Criar bootstrap node - Breno

    public Kademlia()
    {

    }

    public Kademlia(byte[] nodeId, String ipAddress, int port, boolean bootstrap, int k, int s)
    {
        this.nodeId = nodeId;
        protocol = new KademliaProtocol(nodeId,ipAddress,port,generatedPk,generatedSk);

        if (bootstrap)
        {
            rt = new KrtBootStrap(nodeId,protocol,k,s);
            addIpPortBSFile(ipAddress, port, bootstrapFilePath);
        }
        else
        {
            rt = new KrtNormal(nodeId, protocol, k, s);

            // Randomly select one bootstrap node from BootstrapNodes.txt to contact
            List<String> bootstrapNodesInfo = getBootstrapNodesInfo(bootstrapFilePath);
            String selectedBootstrap = selectRandomBootstrapNode(bootstrapNodesInfo);
            String[] bootstrapIpPort = selectedBootstrap.split(" ");

            // Start thread for joining network
            Thread joinNetThread = new Thread(new KademliaJoinNetwork(nodeId, ipAddress, port, generatedPk, generatedSk, bootstrapIpPort[0], Integer.parseInt(bootstrapIpPort[1])));
            joinNetThread.start();
        }

        KademliaServer server = new KademliaServer(port, new Auction(protocol,rt));
        Thread serverThread = new Thread(server);
        serverThread.start();
    }


    public static void main(String[] args) throws NoSuchAlgorithmException {
        byte[] sKadNodeId = solveStaticPuzzle(8);
        solveDynamicPuzzle(sKadNodeId, 8);
        System.out.println("S/Kademlia nodeId: " + Arrays.toString(sKadNodeId));

        if (args[0].equals("bootstrap"))
        {
            protocol = new KademliaProtocol(sKadNodeId,"127.0.0.1",5000, generatedPk, generatedSk);
            rt = new KrtBootStrap(sKadNodeId,protocol,20,20);

            KademliaServer server = new KademliaServer(5000, new Auction(protocol,rt));
            Thread serverThread = new Thread(server);
            serverThread.start();

            addIpPortBSFile("127.0.0.1", 5000, bootstrapFilePath);
        }
        else {
            protocol = new KademliaProtocol(sKadNodeId,"localhost",Integer.parseInt(args[1]), generatedPk, generatedSk);

            rt = new KrtNormal(sKadNodeId, protocol, 20, 20);

            KademliaServer server = new KademliaServer(Integer.parseInt(args[1]), new Auction(protocol,rt));
            Thread serverThread = new Thread(server);
            serverThread.start();

            // Randomly select one bootstrap node from BootstrapNodes.txt to contact
            List<String> bootstrapNodesInfo = getBootstrapNodesInfo(bootstrapFilePath);
            String selectedBootstrap = selectRandomBootstrapNode(bootstrapNodesInfo);
            String[] bootstrapIpPort = selectedBootstrap.split(" ");

            // Start thread for joining network
            Thread joinNetThread = new Thread(new KademliaJoinNetwork(sKadNodeId, "localhost", Integer.parseInt(args[1]), generatedPk, generatedSk, bootstrapIpPort[0], Integer.parseInt(bootstrapIpPort[1])));
            joinNetThread.start();

        }
    }


    public static boolean checkZeroCount(byte[] puzzle, int numOfLeadingZeros) {
        int leadingZeros = 0;

        for (byte b : puzzle) {
            // If byte is zero -> add 8 since byte contains 8 bits
            if (b == 0)
            {
                leadingZeros += 8;
            }
            else
            {
                // Count leading zeros in the byte using bit manipulation
                int byteLeadingZeros = Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                leadingZeros += byteLeadingZeros;
                break;
            }
        }
        System.out.println("Leading zeros count: " + leadingZeros);

        return leadingZeros >= numOfLeadingZeros;
    }

    public static byte[] solveStaticPuzzle(int leadingZerosStatic) throws NoSuchAlgorithmException {

    //public static byte[] solveStaticPuzzle(int leadingZerosStatic) throws NoSuchAlgorithmException {
        /* against eclipe attacks
             1) generate pair (Spub, Spriv)
             2) P = H(H(Spub))
             3) if preceeding x zero bits => NodeId = H(Spub) generated
             4) otherwise go back to 1)
        */
        while (true)
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
            byte[] sKadNodeId = md.digest(publicKey.getEncoded());
            byte[] staticPuzzle = md.digest(sKadNodeId);
            System.out.println("Static puzzle is: " + Arrays.toString(staticPuzzle));

            if (checkZeroCount(staticPuzzle, leadingZerosStatic))
            {
                System.out.println("Static puzzle solved!");
                generatedPk = publicKey;
                generatedSk = keyPair.getPrivate();
                return sKadNodeId;
            }
        }
    }


    public static void solveDynamicPuzzle(byte[] sKadNodeId, int leadingZerosDynamic) throws NoSuchAlgorithmException {
        /* against sybil attacks
            1) NodeId = H(Spub)
            2) choose random X
            3) P = H(NodeId XOR X)
            4) if preceeding y zero bits => puzzle solved
            5) otherwise go back to 2)
        */
        while (true)
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
                return;
            }
        }
    }

    public static ArrayList<Node> lookup(byte[] nodeId) {
        // Initialize a set to keep track of visited nodes to avoid loops
        Set<Node> visitedNodes = new HashSet<>();
        // TODO FIX THIS COMPARATOR
        PriorityQueue<Node> closestNodesQueue = new PriorityQueue<>((node1, node2) -> {
            BigInteger distance1 = rt.calculateDistance(node1.getId().toByteArray(), nodeId);
            BigInteger distance2 = rt.calculateDistance(node2.getId().toByteArray(), nodeId);
            return distance1.compareTo(distance2);
        });
        // Start by finding the closest nodes in the routing table to the target key
        List<Node> closestNodes = rt.findClosestNode(nodeId,3);
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

                List<Node> additionalClosestNodes = protocol.findNodeOp(nodeId,nodeId, currentNode.getIp(),currentNode.getPort()).getNodesList();

                for (Node node : additionalClosestNodes) {
                    if (!visitedNodes.contains(node)) {
                        closestNodesQueue.add(node);
                    }
                }

                // Add the additional closest nodes found to the queue
                closestNodesQueue.addAll(additionalClosestNodes);

            }

            // Check if termination condition is met (e.g., desired number of closest nodes found)
            // Not implemented in this example, you can add your termination condition here
        }

        // Convert the closest nodes priority queue to a list
        ArrayList<Node> resultNodes = new ArrayList<>(closestNodesQueue);
// Sort the result nodes list by their distance to the target key
        resultNodes.sort((node1, node2) -> {
            BigInteger distance1 = rt.calculateDistance(node1.getId().toByteArray(), nodeId);
            BigInteger distance2 = rt.calculateDistance(node2.getId().toByteArray(), nodeId);
            return distance1.compareTo(distance2);
        });
        return resultNodes;
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
