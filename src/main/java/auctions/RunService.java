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
        while (true)
        {

                bs.l.lock();

                of = bs.highestOffer;
                while (offers.size() != bs.brokerSet.size())
                {
                    /*
                    Offer newOf = kp.timerOver();
                    if (newOf != null)
                    {

                    }

                     */
                }

                bs.l.unlock();





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
            // condition
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

    }

}
