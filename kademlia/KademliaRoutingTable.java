import java.time.LocalDateTime;
import java.lang.Math;
import java.util.ArrayList;
import java.util.Map;
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

    // Implement the routing table logic here
}

