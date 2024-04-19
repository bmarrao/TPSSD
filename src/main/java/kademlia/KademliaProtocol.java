package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.Offer;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;


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


    public boolean subscribe(byte[] serviceId, Node n )
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        subscribeRequest request = subscribeRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        subscribeResponse response = stub.subscribe(request);

        return response.getResponse();

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

    public float getPrice(ArrayList<Node> selectedBrokers ,byte[] serviceId)
    {
        float price = -1;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        ManagedChannel channel;
        ByteString bs= ByteString.copyFrom(serviceId);
        for (Node n: selectedBrokers)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

            Offer of = Offer.newBuilder().setNode(node).setPrice(price).build();

            getPriceRequest request = getPriceRequest.newBuilder().setServiceId(bs).build();

            getPriceResponse  sr= stub.getPrice(request);
            if(sr.getPrice() > price)
            {
                price = sr.getPrice();
            }

        }

        return price ;
    }

    public Offer timerOver(Node n , byte[] serviceId)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        timerOverRequest request = timerOverRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        timerOverResponse response = stub.timerOver(request);

        return response.getOf();
    }

    public boolean sendPrice(ArrayList<Node> selectedBrokers , float price, byte[] serviceId)
    {
        boolean result = false;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();
        ManagedChannel channel;
        ByteString bs= ByteString.copyFrom(serviceId);
        for (Node n: selectedBrokers)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

            Offer of = Offer.newBuilder().setNode(node).setPrice(price).build();

            sendPriceRequest request = sendPriceRequest.newBuilder().setOffer(of).setServiceId(bs).build();

            sendPriceResponse  sr= stub.sendPrice(request);
            if(sr.getResult())
            {
                result = true;
            }

        }

        return result ;
    }

    public boolean initiateService(byte[] owner, byte[] serviceId, ArrayList<Node> brokerlist, int time)
    {

        boolean result = false;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();
        ManagedChannel channel;
        ByteString bs= ByteString.copyFrom(serviceId);
        List<Node> allNodes = new ArrayList<>(brokerlist);

        for (Node n: brokerlist)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


            initiateServiceRequest request = initiateServiceRequest.newBuilder()
                    .setOwner(ByteString.copyFrom(owner)).setServiceId(bs).setTime(time)
                    .addAllNodes(allNodes).build();

            initiateServiceResponse  sr= stub.initiateService(request);
            // TODO fix this
            if(sr.getResponse())
            {
                result = true;
            }

        }
        return result;
    }




    public boolean endService (Node n, byte[] serviceId)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setPublickey(String.valueOf(publicKey))
                .build();

        endServiceRequest request = endServiceRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        endServiceResponse response = stub.endService(request);

        return response.getResponse();
    }
}