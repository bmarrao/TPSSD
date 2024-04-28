import auctions.BrokerService;
import com.google.protobuf.ByteString;
import kademlia.Kademlia;
import kademlia.Node;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;

public class AuctionTest
{
    public static void main(String[] args)
    {
        byte[] bkId = generateNodeId();
        Node.Builder nd = Node.newBuilder();
        nd.setIp("localhost");
        nd.setPort(5010);
        nd.setId(ByteString.copyFrom(bkId));
        Node self = nd.build();
        ArrayList<Node> nodes = new ArrayList<>();
        nodes.add(self);

        Bidder bd = new Bidder(generateNodeId(),bkId,nodes);
        Broker bk = new Broker(bkId,nodes);
        Thread BidderThread = new Thread(bd);
        Thread BrokerThread = new Thread(bk);
        BrokerThread.start();
        try
        {
            Thread.sleep(1000);
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        BidderThread.start();
        /*
        Kademlia kd1 = new Kademlia("127.0.0.1",5000,true, 10,10);
        byte[] nodeId;
        for (int i  = 0 ; i < 200; i++)
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp("localhost");
            nd.setPort(5000);
            nodeId = Kademlia.generateNodeId();
            assert nodeId != null;
            nd.setId(ByteString.copyFrom(nodeId));
            Node ins = nd.build();
            kd1.rt.insert(ins);

        }
        kd1.rt.printTree();

         */
    }

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

}


class Broker implements Runnable
{
    Kademlia x;
    ArrayList<Node> nodes;
    Broker(byte[] nodeId,ArrayList<Node> nodes)
    {
        x = new Kademlia(nodeId,"127.0.0.1",5010,true, 10,10);
        this.nodes = nodes;
    }

    @Override
    public void run()
    {
        x.protocol.initiateService(nodes.get(0),x.nodeId,nodes,7500);

    }

}

class Bidder implements Runnable
{
    Kademlia x;
    byte[] serviceId;
    ArrayList<Node> brokers;
    Bidder(byte[] nodeId, byte[] serviceId,ArrayList<Node> brokers)
    {
        x = new Kademlia(nodeId,"127.0.0.1",5011,true, 10,10);
        this.serviceId = serviceId;
        this.brokers= brokers;
    }

    @Override
    public void run()
    {
        for (int i = 1 ; i < 10;i++ )
        {
            x.protocol.sendPrice(brokers, i,serviceId);
            System.out.println(x.protocol.getPrice(brokers, serviceId));
        }
    }

}