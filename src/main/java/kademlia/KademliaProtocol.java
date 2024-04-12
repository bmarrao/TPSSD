package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.Offer;
import java.util.ArrayList;


public class KademliaProtocol
{
    public byte [] nodeId;
    public String ipAdress;
    public int port;
    public KademliaProtocol(byte[] nodeId, String ipAdress, int port )
    {
        this.nodeId = nodeId;
        this.ipAdress = ipAdress;
        this.port = port;
    }

    public boolean pingOp(byte[] nodeId, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        // TODO O ip aqui , não está errado ?
        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(nodeId))
            .setIp(ipAdress)
            .setPort(port)
            .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);
        PingRequest request = PingRequest.newBuilder().setNode(node).build();

        PingResponse response = stub.ping(request);

        return response.getOnline();
    }


    public boolean storeOp(byte[] nodeId, String key, String val, String ip, int port,String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAdress)
                .setPort(port)
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
                .setIp(ipAdress)
                .setPort(port)
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
                .setIp(ipAdress)
                .setPort(port)
                .build();

        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(key).build();

        FindValueResponse response = stub.findValue(request);

        //TODO É necessário estes nodes?
        ArrayList<Node> nodes = new ArrayList<>();

        return new KademliaFindOpResult(response.getId().toByteArray(), response.getValue(), nodes);
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
}