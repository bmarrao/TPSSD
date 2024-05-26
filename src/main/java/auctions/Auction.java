package auctions;

import blockchain.Blockchain;
import kademlia.*;

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


    public byte[] initiateService(String service, int time)
    {
        byte[] serviceId  = null;
        try {
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            serviceId = sha1.digest(service.getBytes(StandardCharsets.UTF_8));
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        l.lock();
        BrokerService bs = new BrokerService(serviceId, time,k.getOwnNode());
        services.add(bs);
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(-1);
        bs.highestOffer = nd.build();
        Thread rsThread = new Thread( new RunService(this,bs,k.protocol));
        rsThread.start();
        bc.newAuction(serviceId, k.getOwnNode(),bs);
        l.unlock();
        return serviceId;
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

