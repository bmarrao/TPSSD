package kademlia.server;
import io.grpc.stub.StreamObserver;
import kademlia.*;

import java.util.ArrayList;
import java.util.List;
// TODO Hugo Implementar metodos dessa classe
//  TODO parte do bootstrap - Cristina
public class KademliaImpl extends KademliaGrpc.KademliaImplBase
{

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver)
    {
        String sender = request.getMyNodeId();

        // Atualizar o horario da última vez online do sender
        boolean resultMsg = true;

        PingResponse pingResponse = PingResponse
                .newBuilder()
                .setResponse(resultMsg)
                .build();

        // Send the response to the client.
        responseObserver.onNext(pingResponse);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
        System.out.println(resultMsg);
    }


    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver)
    {
        // TODO: define storeKeyValue() function
        boolean storeRes = storeKeyValue(request.getKey(), request.getVal());

        // if store successfull -> send true, else false
        StoreResponse response = StoreResponse.newBuilder().setStored(storeRes).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public boolean storeKeyValue(String key, String val) {
        return true;
    }

    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {
        // TODO: define findClosestNodes function
        List<Node> closestNodes = findClosestNodes(request.getId());

        for (Node node : closestNodes) {
        }

        FindNodeResponse response = FindNodeResponse.newBuilder()
                .setId(request.getId())
                .addAllNodes(closestNodes).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    public ArrayList<Node> findClosestNodes(String id) {
        ArrayList<Node> closestNodes = new ArrayList<>();
        // ...
        return closestNodes;
    }

    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver)
    {
        // TODO: define findClosestNodes function
        List<Node> closestNodes = findClosestNodes(request.getId());
        String value = "";

        for (Node node : closestNodes) {
        }

        FindValueResponse response = FindValueResponse.newBuilder()
                .setId(request.getId())
                .setVal(value)
                .addAllNodes(closestNodes).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
