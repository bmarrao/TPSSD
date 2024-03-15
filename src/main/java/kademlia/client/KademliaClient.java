package kademlia.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import kademlia.*;


public class KademliaClient
{
    public String nodeId;
    public KademliaRoutingTable rt;
    public KademliaGrpc.KademliaBlockingStub stub;

    public KademliaClient(String nodeId, String serverIp, int serverPort, KademliaRoutingTable rt)
    {
        this.rt = rt;
        this.nodeId = nodeId;

        // Create channel so client communicates with server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverIp, serverPort).usePlaintext().build();

        // Auto generated stub class with the constructor wrapping the channel
        stub = KademliaGrpc.newBlockingStub(channel);
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