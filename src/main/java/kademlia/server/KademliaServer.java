package kademlia.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class KademliaServer 
{

    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(5003)
                .addService(new KademliaImpl())
                .build();

        server.start();

        // Server is kept alive for the client to communicate.
        server.awaitTermination();

    }
}
