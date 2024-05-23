package auctions;

import kademlia.KademliaProtocol;
import kademlia.Offer;
import kademlia.Transaction;


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
        byte[] = bs.serviceId
        Node = bs.owner
        Offer = bs.highestOffer;
        byte[] data = ;
        byte[] signature = this.a.k.signData(data);
        Transaction t= Transaction.newBuilder()
        .setId(bs.serviceId).setOwner(bs.Owner).setSender(bs.highestOffer)
        .setSignature(signature).build();
        kp.storeTransactionOp(t);
    }

    Offer clone (Offer o)
    {
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(o.getPrice());
        nd.setNode(o.getNode());
        return nd.build();
    }

}

