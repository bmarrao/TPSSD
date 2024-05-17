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



    public void createService (String service, int a, int time)
    {
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");

            byte[] serviceId = sha1.digest(service.getBytes(StandardCharsets.UTF_8));
            this.initiateService(serviceId, time );
            bc.newAuction(serviceId, k.getOwnNode());
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }

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
        bc.newAuction(serviceId, k.getOwnNode());
        l.unlock();
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
        bc.closeAuction(bs.serviceId,bs.highestOffer , k.getOwnNode());
    }


}

