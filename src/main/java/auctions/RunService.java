package auctions;

import auctions.BrokerService;
import com.google.protobuf.ByteString;
import kademlia.KademliaProtocol;
import kademlia.Node;
import kademlia.Offer;
import java.util.concurrent.locks.Condition;

import java.util.*;


public class RunService implements Runnable {

    BrokerService bs;
    KademliaProtocol kp;
    Auction a ;
    RunService(Auction a, BrokerService bs, KademliaProtocol kp)
    {
        this.a = a;
        this.bs = bs;
        this.kp = kp;
    }

    @Override
    public void run()
    {

        Map<Node,Offer> offers  = new HashMap<>();
        Offer of = clone(bs.highestOffer);
        Sleeper sleeper = new Sleeper(bs);

        while (true)
        {

                //sleeper.run();

            try
            {
                System.out.println(bs.time);
                Thread.sleep(bs.time);


                if (bs.highestOffer.getPrice() == of.getPrice())
                {
                    a.endService(bs.serviceId);
                    break;
                }
                else
                {
                    of = clone(bs.highestOffer);
                }
            }
            catch (InterruptedException e)
            {
                throw new RuntimeException(e);
            }
        }
        //kp.endService()
    }

    Offer clone (Offer o)
    {
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(o.getPrice());
        nd.setNode(o.getNode());
        return nd.build();
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
            //bs.endTimer.signalAll();
            bs.sleep = true;
            // condition
        }
        catch (InterruptedException e)
        {
            throw new RuntimeException(e);
        }

    }

}
