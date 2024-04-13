package auctions;

import auctions.BrokerService;
import kademlia.Node;
import kademlia.Offer;

import java.util.ArrayList;
import java.util.List;



public class RunService implements Runnable {

    BrokerService bs;

    RunService(BrokerService bs)
    {
        this.bs = bs;
    }

    @Override
    public void run()
    {

        Offer of = null;
        while (true)
        {
            try
            {
                Thread.sleep(bs.time);
                bs.l.lock();
                if(of.equals(bs.highestOffer))
                {
                    of = bs.highestOffer;
                    for (Node n : bs.brokerSet)
                    {

                    }
                }

            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }

        }
    }
}
