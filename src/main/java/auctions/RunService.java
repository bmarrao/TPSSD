package auctions;

import auctions.BrokerService;
import kademlia.KademliaProtocol;
import kademlia.Node;
import kademlia.Offer;
import java.util.concurrent.locks.Condition;

import java.util.*;


public class RunService implements Runnable {

    BrokerService bs;
    KademliaProtocol kp;
    RunService(BrokerService bs, KademliaProtocol kp)
    {
        this.bs = bs;
        this.kp = kp;
    }

    @Override
    public void run()
    {

        Map<Node,Offer> offers  = new HashMap<>();
        Offer of = null;
        Sleeper sleeper = new Sleeper(bs);

        while (true)
        {
            try
            {
                sleeper.run();
                bs.endTimer.await();
                if (bs.sleep)
                {
                    Offer newOf= getOfferFromBrokers();
                    if (newOf.equals(of))
                    {
                        endService();
                    }
                    else
                    {
                        communicateBiggest(newOf);
                    }
                }
                else
                {
                    bs.waitBiggestOffer.wait();
                    if(of.equals(bs.highestOffer))
                    {
                        break;
                    }

                }
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

        }
    }

    private Offer getOfferFromBrokers()
    {
        /*
        Offer of  = new ;
        Offer newOffer ;
        for (Node n : bs.brokerSet)
        {
            newOffer = kp.timerOver(n,bs.serviceId);
            if (newOffer.getPrice() > of.getPrice())
            {
                of = newOffer;
            }
        }

         */
        return null;
    }

    private void endService()
    {

        for (Node n : bs.brokerSet)
        {
            //newOffer = kp.endService(bs.serviceId)
        }
    }

    private void communicateBiggest(Offer of)
    {
        for (Node n : bs.brokerSet)
        {
            //qnewOffer = kp.endService(bs.serviceId)
        }
    }
}

class Sleeper implements Runnable
{
    BrokerService bs;
    Sleeper(BrokerService bs)
    {
        this.bs = bs;

    }

    @Override
    public void run()
    {
        try
        {
            Thread.sleep(bs.time);
            bs.condition.signalAll();
            bs.sleep = true;
            // condition
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

    }

}
