package skademlia;

import io.grpc.stub.StreamObserver;
import proto.Kademlia;

import java.util.ArrayList;

public class KademliaService extends KademliaGrpc.KademliaImplBase {

    KademliaService() {}

    @Override
    public void pingService(Ping request, StreamObserver<Pong> responseObserver) {
        Pong response = Pong.newBuilder().setId(request.getIdSender()).build();

        responseObserver.onNext(response); // this is the next message to be sent to the client
        responseObserver.onCompleted();    // notify the client that all responses have been sent
    }

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
}
