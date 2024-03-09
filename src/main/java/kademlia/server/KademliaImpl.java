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

}
