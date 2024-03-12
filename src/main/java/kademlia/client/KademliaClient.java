package kademlia.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.KademliaGrpc;
import kademlia.PingRequest;
import kademlia.PingResponse;

public class KademliaClient
{

    //TODO: Initiate class , rt , create random operations requests

    public static void main(String[] args)
    {

        // Channel is used by the client to communicate with the server using the domain localhost and port 5003.
        // This is the port where our server is starting.
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 5003)
                .usePlaintext()
                .build();

        // Auto generated stub class with the constructor wrapping the channel.
        KademliaGrpc.KademliaBlockingStub stub =KademliaGrpc.newBlockingStub(channel);

        // Start calling the `parkVehicle` method
        PingRequest pingRequest = PingRequest.newBuilder().setMyNodeId("xx")
                .build();

        PingResponse pingResponse = stub.ping(pingRequest);
        System.out.println("Response for the first call: " + pingResponse.getResponse());

    }

}