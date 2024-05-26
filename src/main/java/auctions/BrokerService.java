package auctions;


import kademlia.Node;
import kademlia.Offer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// open auction
// bid
// transaction
public class BrokerService
{
    Lock l =new ReentrantLock();
    byte[] serviceId ;
    Offer highestOffer;
    int time;
    Node Owner;
    BrokerService(byte[] serviceId,int time,Node Owner)
    {
        this.serviceId = serviceId;
        Offer.Builder bd = Offer.newBuilder();
        bd.setPrice(-1);
        this.Owner = Owner;
        highestOffer = bd.build();
        this.time = time;
    }
    public Offer getOffer()
    {
        l.lock();
        Offer ret = highestOffer;
        l.unlock();
        return ret;
    }
    public boolean receiveOffer(Offer of)
    {
        l.lock();
        boolean result = false;

        if (highestOffer.getPrice() < of.getPrice())
        {
            highestOffer = of;
            result = true;
        }
        l.unlock();
        return result;
    }
}



