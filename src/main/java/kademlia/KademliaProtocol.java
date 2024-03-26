package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;


public class KademliaProtocol
{
    public byte [] nodeId;

    public KademliaProtocol(byte[] nodeId)
    {
        this.nodeId = nodeId;
    }

    public byte[] pingOp(byte[] nodeId, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);
        PingRequest request = PingRequest.newBuilder().setMyNodeId(ByteString.copyFrom(nodeId)).build();

        PingResponse response = stub.ping(request);

        return response.getId().toByteArray();
    }

    public boolean storeOp(byte[] nodeId, byte[] key, String val, String ip, int port,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        StoreRequest request = StoreRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setKey(ByteString.copyFrom(key))
                .setVal(val)
                .setIp(ip)
                .setPort(port).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, String ip, int port, byte[] key,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(ByteString.copyFrom(key)).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String ip, int port, byte[] key,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        FindValueRequest request = FindValueRequest.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .setKey(ByteString.copyFrom(key)).build();

        FindValueResponse response = stub.findValue(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getVal(), response.getNodesList());
    }
}