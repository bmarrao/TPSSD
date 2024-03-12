package kademlia.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import kademlia.KademliaGrpc;
import kademlia.PingRequest;
import kademlia.PingResponse;

import java.io.IOException;

public class KademliaServer extends KademliaGrpc.KademliaImplBase
{

    public static void main(String[] args) throws IOException, InterruptedException
    {

        KademliaServer ks = new KademliaServer(5003);
    }
    KademliaServer(int port) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(port)
                .addService(new KademliaImpl())
                .build();

        // Server is kept alive for the client to communicate.
        try
        {
            server.start();
            server.awaitTermination();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


}
