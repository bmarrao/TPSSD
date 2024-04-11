package auctions;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.*;
import kademlia.KademliaGrpc;
import java.util.concurrent.locks.ReentrantLock;
import java.util.*;

public class RunAuctions extends auctions.KademliaGrpc.KademliaImplBase
{

    private ReentrantLock l = new ReentrantLock();
    ArrayList<BrokerService> services;
    RunAuctions()
    {
        services = new ArrayList<>();

    }

    @Override
    public void getPrice(getPriceRequest request, StreamObserver<getPriceResponse> responseObserver)
    {
        l.lock();
        BrokerService bs = this.getService(request.getServiceId().toByteArray());

        if (bs != null)
        {
            bs.l.lock();
            l.unlock();

            getPriceResponse response = getPriceResponse
                    .newBuilder()
                    .setPrice(bs.getPrice())
                    .build();

            // Send the response to the client.
            responseObserver.onNext(response);

            // Notifies the customer that the call is completed.
            responseObserver.onCompleted();
            bs.l.unlock();
        }
        else
        {
            l.unlock();
        }
    }

    public boolean receiveOffer(Offer of,byte[] serviceId)
    {
        BrokerService bs = this.getService(serviceId);
        if (bs != null)
        {
            return bs.receiveOffer(of);
        }
        return false;


    }

    public void initiateService(Offer of,byte[] serviceId)
    {

    }

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



