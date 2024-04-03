package kademlia.server;
import io.grpc.stub.StreamObserver;
import kademlia.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
// TODO Hugo Implementar metodos dessa classe
//  TODO parte do bootstrap - Cristina
public class KademliaImpl extends KademliaGrpc.KademliaImplBase
{

    public static KademliaRoutingTable rt ;

    private static int k_nodes = 3;

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver)
    {

        //TODO insert!
        //rt.insert(request.getNode());

        // String sender = request.getMyNodeId();

        // Atualizar o horario da última vez online do sender
        //boolean resultMsg = true;

        PingResponse pingResponse = PingResponse
                .newBuilder()
                .setId(request.getNode().getId())
                .build();

        // Send the response to the client.
        responseObserver.onNext(pingResponse);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
        System.out.println(request.getNode().getId());
    }


    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver)
    {

        //TODO insert!
        //rt.insert(request.getNode());

        //Creates a new instance of storage. If already exists, use it.
        KademliaStore dataStore = KademliaStore.getInstance();

        // Retrieve the key and value from the request
        String key = request.getKey();
        String value = request.getValue();

        dataStore.store(key,value);

        // if store successfull -> send true, else false
        //TODO [ When it's false? ]
        StoreResponse response = StoreResponse.newBuilder().setStored(true).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {

        //TODO insert!
        //rt.insert(request.getNode());

        // Retrieve the target ID from the request
        byte[] nodeID = request.getKey().toByteArray();

        // Get the closest node to the target ID from the routing table
        //TODO : Retirar o j??
        //TODO : replace KademliaNode to Node
        ArrayList<KademliaNode> closestNodes = rt.findClosestNode(nodeID, 0, k_nodes );

        //TODO : AddAllNodes
        FindNodeResponse response = FindNodeResponse.newBuilder()
                .setId(request.getKey()).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver)
    {

        //TODO insert!
        //rt.insert(request.getNode());

        //Creates a new instance of storage. If already exists, use it.
        KademliaStore dataStore = KademliaStore.getInstance();

        // Retrieve the key from the request
        String key = request.getKey();

        // Get the value associated with the key from the data store
        String value = dataStore.findValue(key);

        FindValueResponse response = FindValueResponse.newBuilder()
                .setId(request.getNode().getId())
                .setValue(value).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
