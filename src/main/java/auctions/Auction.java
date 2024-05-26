package auctions;

import blockchain.Blockchain;
import com.google.protobuf.ByteString;
import kademlia.*;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
public class Auction
{
    Kademlia k;
    private final ReentrantLock l;
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
        byte[] owner = bs.Owner.toByteArray();
        byte[] offer = bs.highestOffer.toByteArray();
        byte[] data = new byte[bs.serviceId.length + owner.length + offer.length];

        System.arraycopy(bs.serviceId, 0, data, 0, bs.serviceId.length);
        System.arraycopy(owner, 0, data, bs.serviceId.length, owner.length);
        System.arraycopy(offer, 0, data, bs.serviceId.length+owner.length, offer.length);

        byte[] signature = this.k.signData(data);

        Transaction t= Transaction.newBuilder()
                .setId(ByteString.copyFrom(bs.serviceId)).setOwner(bs.Owner).setType(2)
                .setSignature(ByteString.copyFrom(signature)).build();
        try
        {
            this.bc.addFromMyAuction(t);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }    }

}

