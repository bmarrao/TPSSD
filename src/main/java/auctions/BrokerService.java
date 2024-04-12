package auctions;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.KademliaProtocol;
import kademlia.Node;
import kademlia.Offer;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

// open auction
// bid
// transaction
public class BrokerService
{
    public ReentrantLock l = new ReentrantLock();


    byte[] serviceId ;
    byte[] owner;
    ArrayList<Node>brokerSet;
    Offer highestOffer;
    ArrayList <Node> subscribed;
    int time;
    BrokerService(byte[] serviceId, byte[] owner, int time, ArrayList<Node> brokerSet)
    {
        this.serviceId = serviceId;
        brokerSet = brokerSet;
        subscribed = new ArrayList<>();
        highestOffer = null;
        this.owner = owner;
        time = time;
    }
    public float getPrice()
    {
        if (highestOffer != null)
        {
            return highestOffer.getPrice();
        }
        return -1;
    }


    public boolean receiveOffer(Offer of)
    {

        boolean result = false;
        if (highestOffer == null)
        {
            highestOffer = of;
            result = true;
        }
        else
        {
            if (highestOffer.getPrice() < of.getPrice())
            {
                highestOffer = of;
                result = true;
            }
        }
        return result;
    }




}



