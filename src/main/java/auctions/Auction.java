package auctions;

import blockchain.Blockchain;
import kademlia.KademliaProtocol;

import kademlia.Kademlia;
import kademlia.Node;
import kademlia.Offer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
public class Auction
{
    Kademlia k;
    private ReentrantLock l ;
    ArrayList<BrokerService> services;
    Blockchain bc ;
    public Auction(Kademlia k, Blockchain bc)
    {
        l = new ReentrantLock();
        services = new ArrayList<>();
        this.k = k;
        this.bc = bc;
    }



    public ArrayList<Node> createService (String service, int a, int time)
    {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1"); // Create a new SHA-1 digest

            byte[] serviceId = sha1.digest(service.getBytes(StandardCharsets.UTF_8)); // Compute the hash
            //Get closest nodes to serviceID
            ArrayList<Node> nodes = k.skadLookup(serviceId,a);
            for (Node n : nodes)
            {
                //k.protocol.storeOp(serviceId, /*TODO COLOCAR MEU PROPRIONODE*/,n.getIp(),n.getPort());

            }

            this.initiateService(serviceId, time );
            //TODO UNCOMMENT
            // bc.newAuction(serviceId);
            return nodes;
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
            return null;
        }

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
                NotifyThread ns = new NotifyThread(bs.subscribed,bs.serviceId,bs.highestOffer,0,k.protocol);
                Thread notifyThread = new Thread(ns);
                notifyThread.start();
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

    public void initiateService(byte[] serviceId, int time)
    {
        l.lock();
        BrokerService bs = new BrokerService(serviceId,null, time,null);
        services.add(bs);
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(-1);
        bs.highestOffer = nd.build();
        Thread rsThread = new Thread( new RunService(this,bs,k.protocol));
        rsThread.start();
        //TODO UNCOMMENT
        //bc.newAuction(serviceId);
        l.unlock();
    }


    public boolean subscribe(Node node, byte[] serviceId)
    {
        l.lock();
        BrokerService bs = this.getService(serviceId);
        l.unlock();
        if (bs != null)
        {
            bs.l.lock();
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
        l.unlock();
        float price = -1;
        if (bs != null)
        {
            bs.l.lock();
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


    public void endService(byte[] serviceId)
    {

        l.lock();
        BrokerService bs = this.getService(serviceId);
        System.out.println();
        if (bs != null)
        {
            services.remove(bs);
        }
        l.unlock();
        NotifyThread ns = new NotifyThread(bs.subscribed,bs.serviceId,bs.highestOffer,1,k.protocol);
        Thread notifyThread = new Thread(ns);
        //TODO UNCOMMENT
        // bc.closeAuction(bs.serviceId,bs.highestOffer );

        notifyThread.start();

    }
    /*
        public boolean endService(byte[] serviceId, Node request)
    {

        l.lock();
        BrokerService bs = this.getService(serviceId);
        boolean ret = false;
        System.out.println();
        if(bs!= null)
        {
            for(Node n : bs.brokerSet)
            {
                if (compareId(n.getId().toByteArray(),request.getId().toByteArray()))
                {
                    System.out.println(services.size());
                    services.remove(bs);
                    System.out.println(services.size());
                    ret = true ;
                    break;
                }
            }
        }
        else
        {
            ret = false;
        }
        l.unlock();
        return ret ;



    }

        public void initiateService(Node owner, byte[] serviceId, int time, ArrayList<Node> brokerSet)
    {
        l.lock();
        BrokerService bs = new BrokerService(serviceId,owner,time,brokerSet);
        services.add(bs);
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(-1);
        bs.highestOffer = nd.build();
        Thread rsThread = new Thread( new RunService(bs,k.protocol));
        rsThread.start();
        l.unlock();
    }


     */

}

class NotifyThread implements Runnable
{
    ArrayList<Node> subscribed;
    byte[] serviceId;
    Offer highestOffer;
    KademliaProtocol protocol;
    int type;
    NotifyThread(ArrayList<Node> subscribed, byte[] serviceId, Offer highestOffer,int type,KademliaProtocol protocol)
    {
        this.subscribed = subscribed;
        this.serviceId = serviceId;
        this.highestOffer = highestOffer;
        this.protocol = protocol;
        this.type = type;
    }

    @Override
    public void run()
    {
        protocol.notifySubscribed(subscribed,highestOffer,serviceId, type);
    }

}
