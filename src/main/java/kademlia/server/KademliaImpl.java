package kademlia.server;
import io.grpc.stub.StreamObserver;
import kademlia.KademliaGrpc;
import kademlia.PingRequest;
import kademlia.PingResponse;

public class KademliaImpl extends KademliaGrpc.KademliaImplBase {

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver)
    {

        String sender = request.getMyNodeId();

        // Atualizar o horario da ultima vez online do sender
        String resultMsg = "I'm online";

        PingResponse pingResponse = PingResponse
                .newBuilder()
                .setResult(resultMsg)
                .build();

        // Send the response to the client.
        responseObserver.onNext(pingResponse);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
        System.out.println(resultMsg);

    }

    /*
    @Override
    public void store(StoreRequest request, StreamObserver<Pong> responseObserver) {
        // TODO: define storeKeyValue() function
        boolean storeRes = storeKeyValue(request.getKey(), request.getValue());

        // if store successfull -> send true, else false
        StoreResponse response = StoreResponse.newBuilder().setStored(storeRes).build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }


    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {
        // TODO: define findClosestNodes function
        ArrayList<Node> closestNodes = findClosestNodes(request.getId());

        for (Node node : closestNodes) {
        }

        responseObserver.onNext();
        responseObserver.onCompleted();
    }

    public ArrayList<Node> findClosestNodes(String id) {
    }


    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver) {
        // TODO: define findClosestNodes function
        ArrayList<Node> closestNodes = findClosestNodes(request.getId());

        for (Node node : closestNodes) {
        }

        responseObserver.onNext();
        responseObserver.onCompleted();
    }



     */
}
