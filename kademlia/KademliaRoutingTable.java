import java.time.LocalDateTime;
import java.lang.Math;
import java.util.*;
import java.math.BigInteger;
public class KademliaRoutingTable
{
    private BigInteger nodeId;
    private int k;
    private Map<Integer, Map<BigInteger,KademliaNode>> rt;

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
                    /*
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

