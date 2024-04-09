package Auction;

import java.util.*;

import kademlia.Node;
public class Broker {
    ArrayList<BrokerService> services;
    int m;

    Broker() {
        services = new ArrayList<BrokerService>();
    }

    //getPrice returns the current bid price (ask price) for a ser-vice.
    // If no price offers are available, zero (infinite) is returned.
    public float getPrice(byte[] serviceId, boolean type) {
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            // bid price
            if (type)
            {
                if (bs.lowestSell.size() > 0)
                {
                    return bs.lowestSell.get(0).price;
                }
                else
                {
                    return Float.MAX_VALUE;
                }
            }
            // ask price
            else
            {
                if(bs.highestBuy.size() > 0)
                {
                    return bs.highestBuy.get(0).price;
                }
                else
                {
                    return 0;
                }
            }
        }
        return -1;
    }

    //sendOffer accepts a price offer for a service. It returns true,
    //if the offer could successfully be entered into the table. It
    //returns false, if the price is lower (higher) than the m/2-highest
    //buy price (m/2-lowest sell price) and therefore had to be
    //dropped.
    public boolean sendOffer(Offer of,byte[] serviceId,boolean type)
    {
        BrokerService bs = this.getService(serviceId);
        int tamanho = bs.highestBuy.size() - 1;

        if (bs != null)
        {
            //buy offer
            if (type)
            {
                if (of.price > bs.highestBuy.get(tamanho).price)
                {
                    bs.highestBuy.add(of);
                    Collections.sort(bs.highestBuy, new HighestBuyComparator());
                    return true;
                }
            }
            //sell offer
            else
            {
                if (of.price > bs.lowestSell.get(tamanho).price)
                {
                    bs.lowestSell.add(of);
                    Collections.sort(bs.lowestSell, new LowestSellComparator());
                    return true;

                }
            }
        }
        return false;

    }


    //public void notifyLeafSet()

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
    Node[] brokerSet;
    ArrayList<Offer> highestBuy;
    ArrayList<Offer> lowestSell;
}

class Offer
{
    Node nd ;
    float price;
}


class HighestBuyComparator implements Comparator<Offer> {
    @Override
    public int compare(Offer o1, Offer o2) {
        // Sort in descending order of price for highest buy
        return Float.compare(o2.price, o1.price);
    }
}

class LowestSellComparator implements Comparator<Offer> {
    @Override
    public int compare(Offer o1, Offer o2) {
        // Sort in ascending order of price for lowest sell
        return Float.compare(o1.price, o2.price);
    }
}
