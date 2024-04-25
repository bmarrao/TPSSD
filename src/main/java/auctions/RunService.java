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
    RunService(BrokerService bs, KademliaProtocol kp)
    {
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


                if (bs.highestOffer.getPrice() == of.getPrice()) {
                    kp.endService(bs.owner, bs.serviceId);
                    break;
                } else {
                    of = clone(bs.highestOffer);
                }
                /*
                bs.endTimer.await();
                if (bs.sleep)
                {
                    Offer newOf= null; //getOfferFromBrokers();
                    communicateBiggest(newOf);
                    if (newOf.equals(of))
                    {
                        break;
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

                 */
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

    /*
    private Offer getOfferFromBrokers()
    {

        Offer of  = Offer.newBuilder().setNode(null).setPrice(-1).build();
        Offer newOffer ;
        for (Node n : bs.brokerSet)
        {
            newOffer = kp.timerOver(n,bs.serviceId);
            if (newOffer.getPrice() > of.getPrice())
            {
                of = newOffer;
            }
        }

        return null;
    }


     */
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
