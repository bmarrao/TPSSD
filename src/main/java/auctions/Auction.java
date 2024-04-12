package auctions;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.KademliaGrpc;
import kademlia.KademliaProtocol;

import kademlia.Offer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Auction
{
    KademliaProtocol kp;
    private ReentrantLock l ;
    ArrayList<BrokerService> services;
    public Auction(KademliaProtocol kp)
    {
        l = new ReentrantLock();
        services = new ArrayList<>();
        this.kp = kp;
    }



    public boolean receiveOffer(Offer of,byte[] serviceId)
    {
        l.lock();
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            bs.l.lock();
            l.unlock();
            if (bs.receiveOffer(of))
            {
                kp.notifySubscribed(bs.subscribed,bs.highestOffer,bs.serviceId);
                return true;
            }
            bs.l.unlock();
        }
        else
        {
            l.unlock();
        }
        return false;
    }

    /*
    public void initiateService(Offer of,byte[] serviceId)
    {

    }


     */
    private BrokerService getService(byte[] serviceId)
    {
        for(BrokerService bs : this.services)
        {
            if (compareId(bs.serviceId,serviceId))
            {
                return bs;
            }
        }
        return null;
    }

    public float getPrice(byte[] serviceId)
    {
        l.lock();
        BrokerService bs = this.getService(serviceId);
        float price = -1;
        if (bs != null)
        {
            bs.l.lock();
            l.unlock();

            price = bs.getPrice();
            bs.l.unlock();
        }
        else
        {
            l.unlock();
        }
        return price;

    }

    private boolean compareId(byte[] id1, byte[] id2)
    {

        // Iterate through each byte and compare them
        for (int i = 0; i < id1.length; i++) {
            if (id1[i] != id2[i]) {
                return false; // If any byte differs, return false
            }
        }

        // If all bytes are the same, return true
        return true;
    }
}
