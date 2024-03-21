package kademlia.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import kademlia.*;

public class KademliaClient
{
    public byte[] nodeId;
    public KademliaGrpc.KademliaBlockingStub stub;

    public KademliaClient(byte[] nodeId, String serverIp, int serverPort)
    {
        this.nodeId = nodeId;

        // Create channel so client communicates with server
        ManagedChannel channel = ManagedChannelBuilder.forAddress(serverIp, serverPort).usePlaintext().build();

        // Auto generated stub class with the constructor wrapping the channel
        stub = KademliaGrpc.newBlockingStub(channel);
    }

    public byte[] pingOp(byte[] nodeId)
    {
        PingRequest request = PingRequest.newBuilder().setMyNodeId(ByteString.copyFrom(nodeId)).build();

        PingResponse response = stub.ping(request);

        return response.getId().toByteArray();
    }

    public boolean storeOp(byte[] nodeId, byte[] key, String val, String ip, int port)
    {
        StoreRequest request = StoreRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setKey(ByteString.copyFrom(key))
                .setVal(val)
                .setIp(ip)
                .setPort(port).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, String ip, int port, byte[] key)
    {
        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(ByteString.copyFrom(key)).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String ip, int port, byte[] key)
    {
        FindValueRequest request = FindValueRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(ByteString.copyFrom(key)).build();

        FindValueResponse response = stub.findValue(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getVal(), response.getNodesList());
    }
}