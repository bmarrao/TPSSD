package skademlia;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;


public class KademliaClient {
    public final ManagedChannel channel;
    private final KademliaGrpc.KademliaStub clientStub;

    KademliaClient(String serverIp, int serverPort) {
        this.channel = ManagedChannelBuilder.forAddress(serverIp, serverPort).usePlaintext().build();

        // generate stubs from .proto files
        this.clientStub = KademliaGrpc.newStub(channel);
    }

    public void callPingService(String id, String ip, int port) {
        Ping request = Ping.newBuilder().setId(id).setIp(ip).setPort(port).build();
        try {
            Pong response = clientStub.pingService(request);
        }
        catch(StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
    }
}
