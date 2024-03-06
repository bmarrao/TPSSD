import java.time.LocalDateTime;
import java.lang.Math;
import java.util.*;
import java.math.BigInteger;

class Node
{
    public Map<String,KademliaNode> kbucket;
    public int kc;
    public Node left, right;

    public Node()
    {
        left = right = null;
        this.kc = 0;
    }
    public void createKBucket(String nodeId, KademliaNode node)
    {
        this.kbucket = new HashMap<String, KademliaNode>();
        this.kbucket.put(nodeId, node);
        this.kc = 1;
    }
}

class BinaryTree
{
    Node root;
    String myNodeId;
    public BinaryTree(String nodeId)
    {
        this.root = new Node();
        this.root.createKBucket(nodeId, null);
        this.myNodeId = nodeId;

    }

    public void insert(KademliaNode node, int k )
    {
        Node curr = root;
        String otherId = node.nodeId;
        for (int i = 0 ; i < 160; i++)
        {
            if (curr.kc >= 1)
            {
                break;
            }
            else
            {
                if (myNodeId.charAt(i) == otherId.charAt(0))
                {
                    curr = curr.left;
                }
                else
                {
                    curr = curr.right;
                }
            }
        }
    }

    /*
    private Node insertRec(Node root, int data) {
        if (root == null) {
            root = new Node(data);
            return root;
        }

        if (data < root.data) {
            root.left = insertRec(root.left, data);
        } else if (data > root.data) {
            root.right = insertRec(root.right, data);
        }

        return root;
    }
    */
    
}


public class KademliaRoutingTable
{
    private String nodeId;
    private int k;
    private BinaryTree bt;

    public KademliaRoutingTable(int k, String nodeId) {
        this.k = k;
        this.nodeId = nodeId;
        this.bt = new BinaryTree(nodeId);
    }
}
/*

public class KademliaRoutingTable
{
    private BigInteger nodeId;
    private int k;
    private Map<Integer, Map<BigInteger,KademliaNode>> BinaryTree;

    public KademliaRoutingTable(int k , BigInteger nodeId)
    {
        this.k = k;
        this.nodeId = nodeId;
        this.rt = new HashMap<Integer, Map<BigInteger, KademliaNode>>();
    }

    public void newActivity(KademliaNode node)
    {
        //Calculate the distance of the nodes
        BigInteger distance = this.nodeId.xor(node.nodeId);
        //Calculate the order of the distance to put in the map
        int order = this.calculateOrderOfDistance(distance);
        // Ver se a ordem ja tem alguma representação na routing table
        if (rt.containsKey(order))
        {
            //Fazer get do kbucket da ordem da distancia
            Map<BigInteger,KademliaNode> kbucket = rt.get(order);
            // Ver se esse ja no ja se encontra no kbucket
            if (kbucket.containsKey(node.nodeId))
            {
                //Fazer get no kbucket do no
                KademliaNode newNode = kbucket.get(node.nodeId);
                //Atualizando o tempo do no
                newNode.time = LocalDateTime.now();
            }
            else
            {
                // Testa se já tem K membros no kbucket
                if (kbucket.size() == k)
                {
                    //KademliaNode testPing = leastRecentlySeen(kbucket);
                    //boolean notActive =  ping(testPing)
                    // Ping least recently active node
                    if (!notActive)
                    {
                        kbucket.remove(testPing.nodeID);
                        kbucket.put(node.nodeId, node);
                    }
                    else
                    {
                        testPing.setTime();
                    }


                }
                //Caso contrario adiciona no kbucket
                else
                {
                    kbucket.put(node.nodeId, node);
                }
            }
        }
        else
        {
            Map<BigInteger,KademliaNode> kbucket = new HashMap<BigInteger, KademliaNode>();
            kbucket.put(nodeId, node);
            this.rt.put(order,kbucket);
        }


    }
    private int calculateOrderOfDistance(BigInteger distance)
    {
        int i = 0;
        for (; i < 160 ; i++)
        {
            int comparisonResult = distance.compareTo(BigInteger.valueOf((long) Math.pow(2, i + 1)));
            if (comparisonResult < 0)
            {
                return i;
            }
        }
        return i;
    }

    private KademliaNode leastRecentlySeen(Map<BigInteger,KademliaNode> kbucket)
    {

        Collection<KademliaNode> nodes = kbucket.values();
        KademliaNode menor = null;
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 5, 12, 30);

        for (KademliaNode node : nodes)
        {
            if (dateTime.isAfter(node.time))
            {
                menor = node;
                dateTime = node.time;
            }
        }
        return menor;
    }


    // Implement the routing table logic here
}

*/