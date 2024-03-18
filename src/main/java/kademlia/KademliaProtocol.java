package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class KademliaProtocol
{
    public byte [] nodeId;
    public KademliaGrpc.KademliaBlockingStub stub;

    public KademliaProtocol(byte[] nodeId, String serverIp, int serverPort)
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

    public boolean storeOp(byte[] nodeId, String key, String val, String ip, int port)
    {
        StoreRequest request = StoreRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setKey(key)
                .setVal(val)
                .setIp(ip)
                .setPort(port).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, String ip, int port, String key)
    {
        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(key).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String ip, int port, String key)
    {
        FindValueRequest request = FindValueRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(key).build();

        FindValueResponse response = stub.findValue(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getVal(), response.getNodesList());
    }
}