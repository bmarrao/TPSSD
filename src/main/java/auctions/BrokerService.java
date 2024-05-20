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
    public ReentrantLock l = new ReentrantLock();
    private final Lock lock = new ReentrantLock();
    public Condition endTimer = lock.newCondition();
    public Condition waitBiggestOffer = lock.newCondition();
    public boolean sleep = true;
    byte[] serviceId ;
    Node owner;
    ArrayList<Node>brokerSet;
    Offer highestOffer;
    ArrayList <Node> subscribed;
    int time;
    BrokerService(byte[] serviceId, Node owner, int time, ArrayList<Node> brokerSet)
    {
        this.serviceId = serviceId;
        this.brokerSet = brokerSet;
        subscribed = new ArrayList<>();
        Offer.Builder bd = Offer.newBuilder();
        bd.setNode(owner);
        bd.setPrice(-1);

        highestOffer = null;
        this.owner = owner;
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



