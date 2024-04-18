package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.Offer;

import java.security.PublicKey;
import java.util.ArrayList;


public class KademliaProtocol
{
    public byte [] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;

    public KademliaProtocol(byte[] nodeId, String ipAddress, int port, PublicKey publicKey)
    {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
    }

    public boolean pingOp(byte[] nodeId, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        // TODO O ip aqui , não está errado ?
        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(nodeId))
            .setIp(ipAddress)
            .setPort(port)
            .setPublickey(String.valueOf(publicKey))
            .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);
        PingRequest request = PingRequest.newBuilder().setNode(node).build();

        PingResponse response = stub.ping(request);

        return response.getOnline();
    }


    public boolean storeOp(byte[] nodeId, String key, String val, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        StoreRequest request = StoreRequest.newBuilder()
                .setNode(node)
                .setKey(key)
                .setValue(val).build();

        StoreResponse response = stub.store(request);

        return response.getStored();
    }

    public KademliaFindOpResult findNodeOp(byte[] nodeId, byte[] key, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key)).build();

        FindNodeResponse response = stub.findNode(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
    }

    public KademliaFindOpResult findValueOp(byte[] nodeId, String key, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(key).build();

        FindValueResponse response = stub.findValue(request);

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getValue(), new ArrayList<>());
    }

    public void notifySubscribed(ArrayList<Node> subscribed, Offer highestOffer, byte[] serviceId)
    {
        ManagedChannel channel;
        for (Node n: subscribed)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaStub stub = KademliaGrpc.newStub(channel);


            NotifyRequest request = NotifyRequest.newBuilder()
                    .setNode(n).setPrice(highestOffer.getPrice()).
                    setServiceId(ByteString.copyFrom(serviceId)).build();

            // TODO FINISH THIS

        }
    }

    public Offer timerOver(Node n, byte[] serviceId)
    {
        ManagedChannel channel;

        channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        timerOverRequest request = timerOverRequest.newBuilder().
                setServiceId(ByteString.copyFrom(serviceId)).build();

        timerOverResponse  to= stub.timerOver(request);

        return to.getOf();

    }

    public boolean endService (Node n, byte[] serviceId)
    {
        ManagedChannel channel;

        channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        return true;
    }
}