package auctions;

import auctions.BrokerService;
import kademlia.KademliaProtocol;
import kademlia.Node;
import kademlia.Offer;

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
            try
            {
                Thread.sleep(bs.time);
                bs.l.lock();
                bs.running = false;

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

                bs.running = true;
                bs.l.unlock();


            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

        }
    }
}
