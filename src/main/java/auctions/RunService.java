package auctions;

import kademlia.KademliaProtocol;
import kademlia.Offer;


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

        Offer of = clone(bs.highestOffer);

        while (true)
        {


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
        // TODO FIX THIS
        a.k.
        kademlia.Transaction t= kademlia.Transaction.newBuilder().setId(bs.serviceId).setOwner(bs.Owner).setSender(bs.highestOffer).setSignature().build()
        kp.storeTransactionOp(new Transaction())
    }

    Offer clone (Offer o)
    {
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(o.getPrice());
        nd.setNode(o.getNode());
        return nd.build();
    }

}

