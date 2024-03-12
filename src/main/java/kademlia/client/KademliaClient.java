package kademlia.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.*;

public class KademliaClient
{

    //TODO: Initiate class , rt , create random operations requests
    public static void main(String[] args)
    {
        KademliaProtocol kp = new KademliaProtocol();
        kp.ping(new KademliaNode("localhost","",5003),"");
    }

}