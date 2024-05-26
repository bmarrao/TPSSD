package auctions;

import com.google.protobuf.ByteString;
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


        byte[] owner = bs.Owner.toByteArray();
        byte[] offer = bs.highestOffer.toByteArray();
        byte[] data = new byte[bs.serviceId.length + owner.length + offer.length];

        System.arraycopy(bs.serviceId, 0, data, 0, bs.serviceId.length);
        System.arraycopy(owner, 0, data, bs.serviceId.length, owner.length);
        System.arraycopy(offer, 0, data, bs.serviceId.length+owner.length, offer.length);

        byte[] signature = this.a.k.signData(data);

        Transaction t= Transaction.newBuilder()
        .setId(ByteString.copyFrom(bs.serviceId)).setOwner(bs.Owner).setType(2)
        .setSignature(ByteString.copyFrom(signature)).build();
        try
        {
            this.a.bc.addFromMyAuction(t);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    Offer clone (Offer o)
    {
        Offer.Builder nd = Offer.newBuilder();
        nd.setPrice(o.getPrice());
        nd.setNode(o.getNode());
        return nd.build();
    }

}

