import auctions.BrokerService;
import com.google.protobuf.ByteString;
import kademlia.Kademlia;
import kademlia.Node;

public class AuctionTest
{
    public static void main(String[] args)
    {
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
            System.out.println(ins);
            kd1.rt.insert(ins);

        }
        kd1.rt.printTree();
    }


}


class Broker implements Runnable
{
    Kademlia x;
    Broker(BrokerService bs)
    {

    }

    @Override
    public void run()
    {
        /*
        try
        {

        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }


         */
    }

}