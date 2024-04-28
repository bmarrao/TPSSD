package auctions;

import com.google.protobuf.ByteString;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.KademliaGrpc;
import kademlia.KademliaProtocol;

import kademlia.Node;
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
                //kp.notifySubscribed(bs.subscribed,bs.highestOffer,bs.serviceId);
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


    public void initiateService(Node owner, byte[] serviceId, int time, ArrayList<Node> brokerSet)
    {
        l.lock();
        BrokerService bs = new BrokerService(serviceId,owner,time,brokerSet);
        services.add(bs);
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(-1);
        bs.highestOffer = nd.build();
        Thread rsThread = new Thread( new RunService(bs,this.kp));
        rsThread.start();
        l.unlock();
    }


    public boolean subscribe(Node node, byte[] serviceId)
    {
        l.lock();
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            bs.l.lock();
            l.unlock();
            bs.subscribed.add(node);
            bs.l.unlock();
            return true;
        }
        else
        {
            l.unlock();
        }
        return false;
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

    public Offer timerOver(byte[] serviceId, Node request)
    {

        l.lock();
        BrokerService bs = this.getService(serviceId);
        l.unlock();
        if(bs != null)
        {
            bs.endTimer.signal();
            //Tester essa parte
            if(bs.brokerSet.contains(request))
            {
                return bs.highestOffer;
            }
        }
        return null;
    }

    public boolean endService(byte[] serviceId, Node request)
    {

        l.lock();
        BrokerService bs = this.getService(serviceId);
        boolean ret ;
        if(bs!= null)
        {
            if(bs.brokerSet.contains(request))
            {
                System.out.println("Removi com sucesso");
                services.remove(serviceId);
                ret = true;
            }
            else
            {
                ret = false;
            }
        }
        else
        {
            ret = false;
        }
        l.unlock();
        return ret ;



    }

    private boolean compareId(byte[] id1, byte[] id2)
    {

        // Iterate through each byte and compare them
        for (int i = 0; i < id1.length; i++)
        {
            if (id1[i] != id2[i])
            {
                return false; // If any byte differs, return false
            }
        }


        // If all bytes are the same, return true
        return true;
    }
    public BrokerService getService(byte[] serviceId)
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

}
