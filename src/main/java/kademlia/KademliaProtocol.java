package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.ArrayList;


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

        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(nodeId))
            .setIp("127.0.0.1")
            .setPort(8080)
            .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);
        PingRequest request = PingRequest.newBuilder().setNode(node).build();

        PingResponse response = stub.ping(request);

        return response.getId().toByteArray();
    }

    public boolean storeOp(byte[] nodeId, String key, String val, String ip, int port,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp("127.0.0.1")
                .setPort(8080)
                .build();

        StoreRequest request = StoreRequest.newBuilder()
                .setNode(node)
                .setKey(key)
                .setValue(val).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, String ip, int port, byte[] key,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp("127.0.0.1")
                .setPort(8080)
                .build();

        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key)).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String ip, int port, String key,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp("127.0.0.1")
                .setPort(8080)
                .build();

        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(key).build();

        FindValueResponse response = stub.findValue(request);

        //TODO É necessário estes nodes?
        ArrayList<Node> nodes = new ArrayList<>();

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getValue(), nodes);
    }
}