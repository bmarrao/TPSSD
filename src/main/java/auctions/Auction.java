package auctions;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import kademlia.KademliaGrpc;


import java.io.IOException;

public class Auction extends auctions.KademliaGrpc.KademliaImplBase implements Runnable
{
    public Server server;
    public int port;


    public Auction(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        // Server is kept alive for the client to communicate.
        server = ServerBuilder.forPort(port)
                .addService(new RunAuctions())
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
