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
    public void createKBucket()
    {
        this.kbucket = new HashMap<String, KademliaNode>();
        this.kc = 1;
    }

}

class BinaryTree
{
    Node root;
    String myNodeId;

    int k;
    public BinaryTree(String nodeId, int k )
    {
        this.root = new Node();
        this.root.createKBucket();
        // Colocar o proprio node this.insert(OWN NODE)
        this.myNodeId = nodeId;
        this.k = k;

    }

    public void insert(KademliaNode node)
    {
        Node curr = root;
        String otherId = node.nodeId;
        insertRec(node, curr,0, 'e');
    }
    public void insertRec(KademliaNode node, Node curr,int i, char prevDir )
    {
        if (i < 160)
        {
            if (curr.kc >= 1)
            {
                if (curr.kc == this.k)
                {
                    if (prevDir =='e')
                    {
                        curr.kc = 0;
                        curr.left = new Node();
                        curr.left.createKBucket();
                        curr.right = new Node();
                        curr.right.createKBucket();
                        this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i++);
                        curr.kbucket = null;
                        //Creates new kbuckets
                    }
                    else
                    {
                        testLeastRecentlySeen(curr.kbucket, node);
                    }
                }
                else
                {
                    curr.kc++;
                    curr.kbucket.put(node.nodeId,node);
                }
            }
            else
            {
                if (myNodeId.charAt(i) == node.nodeId.charAt(0))
                {
                    insertRec(node, curr.left,i++,'e');
                }
                else
                {
                    insertRec(node, curr.right,i++,'d');
                }
            }
        }
    }


    private void addToBuckets(Node left, Node right, Map<String,KademliaNode> kbucket,KademliaNode node, int i )
    {
        int count_right = 0;
        for (KademliaNode BNode: kbucket.values())
        {
            if(BNode.nodeId.charAt(i) == this.myNodeId.charAt(i))
            {
                left.kbucket.put(BNode.nodeId, BNode);
            }
            else
            {
                right.kbucket.put(BNode.nodeId, BNode);
                count_right++;
            }
        }
        if (count_right == this.k)
        {
            testLeastRecentlySeen(right.kbucket,node);
        }
        else if (count_right == 0)
        {
            testLeastRecentlySeen(left.kbucket, node);

        }
        else
        {
            if(node.nodeId.charAt(i) == this.myNodeId.charAt(i))
            {
                left.kbucket.put(node.nodeId, node);
            }
            else
            {
                right.kbucket.put(node.nodeId, node);
            }
        }
        //Adds the previous kbuckets to the newly created ones, and also the new one
    }

    private void testLeastRecentlySeen(Map<String,KademliaNode> kbucket, KademliaNode node)
    {
        KademliaNode testPing = leastRecentlySeen(kbucket);
        /*
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

         */
    }

    private KademliaNode findClosestNode(String nodeId)
    {
        return findClosestNodeRec(this.root,nodeId, 0);
    }

    private KademliaNode findClosestNodeRec(Node curr, String nodeId, int i)
    {
        if (i < 160)
        {
            if (curr.kc >= 1)
            {
                return searchMapClosest(curr.kbucket, nodeId);
            }
            else
            {
                if (nodeId.charAt(i) == this.myNodeId.charAt(i))
                {
                    return findClosestNodeRec(curr.left,nodeId, i++);

                }
                else
                {
                    return findClosestNodeRec(curr.right,nodeId, i++);
                }
            }
        }
        return null;
    }


    private KademliaNode searchMapClosest(Map<String,KademliaNode> kbucket,String nodeId)
    {
        int distance = -1;
        KademliaNode node = null;
        for (KademliaNode bnode : kbucket.values())
        {
            int new_distance = calculateDistance(nodeId,bnode.nodeId);
            if (new_distance< distance)
            {
                distance = new_distance;
                node = bnode;
            }
        }
        return node;

    }

    private int calculateDistance (String node1, String node2)
    {
        return 0;
    }
    private KademliaNode leastRecentlySeen(Map<String,KademliaNode> kbucket)
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
}


public class KademliaRoutingTable
{
    private String nodeId;
    private int k;
    private BinaryTree bt;

    public KademliaRoutingTable(int k, String nodeId)
    {
        this.k = k;
        this.nodeId = nodeId;
        this.bt = new BinaryTree(nodeId,k);
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