package kademlia.server;

import auctions.Auction;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import kademlia.KademliaGrpc;
import kademlia.KademliaStore;

import java.io.IOException;
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
    public KademliaStore ks;
    public KademliaServer(int port, Auction auc, int leadingZeros, PublicKey publicKey, PrivateKey privateKey, KademliaStore ks)
    {
        this.port = port;
        this.auc = auc;
        this.leadingZeros = leadingZeros;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.ks = ks;
    }

    @Override
    public void run() {
        // Server is kept alive for the client to communicate.
        server = ServerBuilder.forPort(port)
                .addService(new KademliaImpl(auc, leadingZeros, publicKey, privateKey, ks))
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
