package kademlia.server;

import auctions.Auction;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import kademlia.KademliaGrpc;

import java.security.PrivateKey;
import java.security.PublicKey;

public class KademliaServer extends KademliaGrpc.KademliaImplBase implements Runnable
{
    public Server server;
    public int port;
    Auction auc;
    public int leadingZeros;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public KademliaServer(int port, Auction auc, int leadingZeros, PublicKey publicKey, PrivateKey privateKey)
    {
        this.port = port;
        this.auc = auc;
        this.leadingZeros = leadingZeros;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    @Override
    public void run() {
        // Server is kept alive for the client to communicate.
        server = ServerBuilder.forPort(port)
                .addService(new KademliaImpl(auc, leadingZeros, publicKey, privateKey))
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
