package auctions;

import kademlia.Node;

import java.util.*;

public class Auction
{

    ArrayList<BrokerService> services;

    public float getPrice(byte[] serviceId)
    {
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            return bs.getPrice();
        }
        return -1;
    }

    public boolean sendOffer(Offer of,byte[] serviceId)
    {
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            return bs.sendOffer(of);
        }
        return false;


    }

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

class BrokerService
{
    byte[] serviceId ;
    kademlia.Node[] brokerSet;
    Offer highestOffer;

    public float getPrice()
    {
        if (highestOffer != null)
        {
            return highestOffer.price;
        }
        return -1;
    }

    public boolean sendOffer(Offer of)
    {
        boolean result = false;
        if (highestOffer == null)
        {
            highestOffer = of;
            result = true;
        }
        else
        {
            if (highestOffer.price < of.price)
            {
                highestOffer = of;
                result = true;
            }
        }
        if (result)
        {
            this.notifySubscribed();
        }
        return result;
    }
    public void notifySubscribed()
    {

    }

}

class Offer
{
    Node nd ;
    float price;
}
