package auctions;

import com.google.protobuf.ByteString;
import com.google.protobuf.Empty;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.locks.ReentrantLock;

public class BrokerService
{
    byte[] serviceId ;
    Node[] brokerSet;
    Offer highestOffer;
    Node[] subscribed;
    public ReentrantLock l = new ReentrantLock();

    public float getPrice()
    {
        if (highestOffer != null)
        {
            return highestOffer.price;
        }
        return -1;
    }


    public boolean receiveOffer(Offer of)
    {
        boolean result = false;
        if (highestOffer == null)
        {
            highestOffer = of;
            result = true;
        }
        else
        {
            if (highestOffer.price < of.price)
            {
                highestOffer = of;
                result = true;
            }
        }
        if (result)
        {
            this.notifySubscribed();
        }
        return result;
    }

    public void notifySubscribed()
    {
        ManagedChannel channel;
        for (Node n: subscribed)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            auctions.KademliaGrpc.KademliaStub stub = auctions.KademliaGrpc.newStub(channel);


            NotifyRequest request = NotifyRequest.newBuilder()
                    .setNode(n).setPrice(highestOffer.price).
                    setServiceId(ByteString.copyFrom(serviceId)).build();

            stub.notify(request, new EmptyStreamObserver());

        }
    }

}

class EmptyStreamObserver implements StreamObserver<Empty> {
    @Override
    public void onNext(Empty value) {
        // Handle successful response if needed
    }

    @Override
    public void onError(Throwable t) {
        // Handle error
        t.printStackTrace();
    }

    @Override
    public void onCompleted() {
        // Handle completion
        // Here, you may want to perform any cleanup or further actions
    }
}

