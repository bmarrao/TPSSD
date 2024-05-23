package auctions;


import kademlia.Node;
import kademlia.Offer;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

// open auction
// bid
// transaction
public class BrokerService
{
    byte[] serviceId ;
    Offer highestOffer;
    int time;
    Node Owner;
    BrokerService(byte[] serviceId,int time,Node Owner)
    {
        this.serviceId = serviceId;
        Offer.Builder bd = Offer.newBuilder();
        bd.setPrice(-1);
        this.owner = Owner;
        highestOffer = bd.build();
        this.time = time;
    }
    public float getPrice()
    {
        return highestOffer.getPrice();
    }

    public byte[] getServiceId() { return this.serviceId; }

    public boolean receiveOffer(Offer of)
    {

        boolean result = false;

        if (highestOffer.getPrice() < of.getPrice())
        {
            highestOffer = of;
            result = true;
        }
        return result;
    }




}



