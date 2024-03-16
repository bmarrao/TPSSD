package kademlia;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.KademliaGrpc;
import kademlia.PingRequest;
import kademlia.PingResponse;
public class KademliaProtocol
{

    public KademliaProtocol()
    {

    }

    // TODO Fix errors in this class - Cristina
    public boolean ping(KademliaNode testPing, String nodeId)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(testPing.ipAdress, testPing.port)
                .usePlaintext()
                .build();

        // Auto generated stub class with the constructor wrapping the channel.
        KademliaGrpc.KademliaBlockingStub stub =KademliaGrpc.newBlockingStub(channel);

        // Start calling the `parkVehicle` method
        PingRequest pingRequest = PingRequest.newBuilder().setMyNodeId(nodeId)
                .build();
        PingResponse pingResponse = stub.ping(pingRequest);

        System.out.println("Response for the first call: " + pingResponse.getResponse());
        //return true;
        return pingResponse.getResponse();
    }

    public boolean pingOp(String nodeId)
    {
        PingRequest request = PingRequest.newBuilder().setMyNodeId(nodeId).build();

        PingResponse response = stub.ping(request);

        return response.getResponse();
    }

    public boolean storeOp(String nodeId, String key, String val, String ip, int port)
    {
        StoreRequest request = StoreRequest.newBuilder()
                .setId(nodeId)
                .setKey(key)
                .setVal(val)
                .setIp(ip)
                .setPort(port).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(String nodeId, String ip, int port, String key)
    {
        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setId(nodeId)
                .setIp(ip)
                .setPort(port)
                .setKey(key).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(String nodeId, String ip, int port, String key)
    {
        FindValueRequest request = FindValueRequest.newBuilder()
                .setId(nodeId)
                .setIp(ip)
                .setPort(port)
                .setKey(key).build();

        FindValueResponse response = stub.findValue(request);

        return new KademliaFindOpResult(response.getId(), response.getVal(), response.getNodesList());
    }
}
