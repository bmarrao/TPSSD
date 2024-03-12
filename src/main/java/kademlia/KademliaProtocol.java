package kademlia;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.KademliaGrpc;
import kademlia.PingRequest;
import kademlia.PingResponse;
public class KademliaProtocol
{
    private ManagedChannel channel;

    public KademliaProtocol()
    {

    }

    public boolean ping(KademliaNode testPing, String nodeId)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(testPing.ipAdress, testPing.port)
                .usePlaintext()
                .build();

        // Auto generated stub class with the constructor wrapping the channel.
        KademliaGrpc.KademliaBlockingStub stub =KademliaGrpc.newBlockingStub(channel);

        // Start calling the `parkVehicle` method
        PingRequest pingRequest = PingRequest.newBuilder().setMyNodeId(nodeId)
                .build();
        PingResponse pingResponse = stub.ping(pingRequest);

        System.out.println("Response for the first call: " + pingResponse.getResponse());
        //return true;
        return pingResponse.getResponse();
    }
}
