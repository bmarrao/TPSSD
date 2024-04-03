package kademlia.client;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import kademlia.*;

import java.util.ArrayList;

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

    public byte[] pingOp(byte[] nodeId, String ip, int port)
    {

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .build();

        PingRequest request = PingRequest.newBuilder().setNode(node).build();

        PingResponse response = stub.ping(request);

        return response.getId().toByteArray();
    }

    public boolean storeOp(byte[] nodeId, byte[] key, String val, String ip, int port)
    {

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .build();

        StoreRequest request = StoreRequest.newBuilder()
                .setNode(node)
                .setKey(key.toString())
                .setValue(val).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, String ip, int port, byte[] key)
    {
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .build();

        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key)).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String ip, int port, byte[] key)
    {

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ip)
                .setPort(port)
                .build();

        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(key.toString()).build();

        FindValueResponse response = stub.findValue(request);

        //TODO É necessário estes nodes?
        ArrayList<Node> nodes = new ArrayList<>();

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getValue(), nodes);
    }
}