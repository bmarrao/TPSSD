package kademlia.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import kademlia.KademliaGrpc;


import java.io.IOException;

public class KademliaServer extends KademliaGrpc.KademliaImplBase implements Runnable
{
    public Server server;
    public int port;

    public static void main(String[] args) throws IOException, InterruptedException
    {
        KademliaServer ks = new KademliaServer(5003);
    }

    public KademliaServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        // Server is kept alive for the client to communicate.
        server = ServerBuilder.forPort(port)
                .addService(new KademliaImpl())
                .build();
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
