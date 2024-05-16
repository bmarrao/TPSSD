package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;


public class KademliaProtocol
{
    public byte [] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public byte randomX;

    StreamObserver<NotifyResponse> response = new StreamObserver<NotifyResponse>() {
        @Override
        public void onNext(NotifyResponse response) {
        }

        @Override
        public void onError(Throwable t) {
        }

        @Override
        public void onCompleted() {

        }
    };
    public KademliaProtocol(byte[] nodeId, String ipAddress, int port, PublicKey publicKey, PrivateKey privateKey, byte randomX)
    {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.randomX = randomX;
    }


    public boolean pingOp(byte[] nodeId, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(nodeId))
            .setIp(ipAddress)
            .setPort(port)
            .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        // Sign node content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        PingRequest request = PingRequest.newBuilder()
                .setNode(node)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        // Receive response
        PingResponse response = stub.ping(request);

        // Check signature
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
            return response.getOnline();
        }
        return false;
    }

    public boolean storeOp(byte[] key, Node val, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        byte[] nodeInfoToSign = node.toByteArray();
        byte[] valToStore = val.toByteArray();

        int totalLength = nodeInfoToSign.length + key.length + valToStore.length;

        // Add message content to byte[] for signature
        byte[] infoToSign = new byte[totalLength];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(key, 0, infoToSign, nodeInfoToSign.length, key.length);
        System.arraycopy(valToStore, 0, infoToSign, nodeInfoToSign.length + key.length, valToStore.length);

        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        StoreRequest request = StoreRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setValue(val)
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        StoreResponse response = stub.store(request);

        // Check response's signature
        boolean signVal = false;
        try
        {
            signVal = SignatureClass.verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
            return response.getStored();
        }
        return false;
    }




    public List<Node> findNodeOp(byte[] nodeId, byte[] key, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();
        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        // Send RPC request
        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindNodeResponse response = stub.findNode(request);

        // Check id signature
        boolean idSignVal = false;
        try {
            idSignVal = SignatureClass.verify(response.getId().toByteArray(), response.getIdSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (idSignVal) {
            return response.getNodesList();
        }
        return new ArrayList<>();
    }

    public FindValueResponse findValueOp(byte[] nodeId, byte[] key, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPort(port).build();


        // TODO FIX THIS
        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + key.length];

        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(key, 0, infoToSign, nodeInfoToSign.length, key.length);



        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindValueResponse response = stub.findValue(request);

        // Check response's signature
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getValue().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];

        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify,  0, infoToVerify, idToVerify.length, valueToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
            return response;
        }
        return null;
    }


    public boolean subscribe(byte[] serviceId, Node n )
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        byte[] signature = null;
        try {
            signature = SignatureClass.sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        subscribeRequest request = subscribeRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        subscribeResponse response = stub.subscribe(request);

        channel.shutdown();

        return response.getResponse();

    }

    public void notifySubscribed(ArrayList<Node> subscribed, Offer highestOffer, byte[] serviceId, int type) {
        ManagedChannel channel = null;
        for (Node n : subscribed) {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort())
                    .usePlaintext()
                    .build();
            KademliaGrpc.KademliaStub stub = KademliaGrpc.newStub(channel);

            NotifyRequest request = NotifyRequest.newBuilder()
                    .setNode(n)
                    .setPrice(highestOffer.getPrice())
                    .setType(type)
                    .setServiceId(ByteString.copyFrom(serviceId))
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .build();

            stub.notify(request, response);
        }
        channel.shutdown();
    }

    public float getPrice(ArrayList<Node> selectedBrokers ,byte[] serviceId)
    {
        float price = -1;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
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

    /*
    public Offer timerOver(Node n , byte[] serviceId)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        timerOverRequest request = timerOverRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        timerOverResponse response = stub.timerOver(request);

        return response.getOf();
    }
    */
    public boolean sendPrice(ArrayList<Node> selectedBrokers , float price, byte[] serviceId)
    {
        boolean result = false;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        ManagedChannel channel;
        ByteString bs= ByteString.copyFrom(serviceId);
        for (Node n: selectedBrokers)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

            Offer of = Offer.newBuilder().setNode(node).setPrice(price).build();

            sendPriceRequest request = sendPriceRequest.newBuilder()
                    .setOffer(of)
                    .setServiceId(bs)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

            sendPriceResponse  sr= stub.sendPrice(request);
            if(sr.getResult())
            {
                result = true;
            }

        }

        return result ;
    }

    public void newAuctionOp(byte[] serviceId, Node owner) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(owner.getIp(), owner.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        byte[] nodeInfoToSign = node.toByteArray();
        ByteString bs= ByteString.copyFrom(serviceId);

        int totalLength = nodeInfoToSign.length + bs.size();

        // Add message content to byte[] for signature
        byte[] infoToSign = new byte[totalLength];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(bs, 0, infoToSign, nodeInfoToSign.length, bs.size());

        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        NewAuctionRequest request = NewAuctionRequest.newBuilder()
                .setSender(node)
                .setOwner(owner)
                .setServiceID(bs)
                .setSignature(ByteString.copyFrom(signature)).build();

        stub.newAuction(request);
    }

    public void closeAuctionOp(byte[] serviceId, Node owner, Node winner, float winningPrice) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(owner.getIp(), owner.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        Offer of = Offer.newBuilder().setNode(winner).setPrice(winningPrice).build();

        byte[] nodeInfoToSign = node.toByteArray();
        ByteString bs= ByteString.copyFrom(serviceId);
        byte[] offerInfoToSign = of.toByteArray();

        int totalLength = nodeInfoToSign.length + bs.size() + offerInfoToSign.length;

        // Add message content to byte[] for signature
        byte[] infoToSign = new byte[totalLength];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(bs, 0, infoToSign, nodeInfoToSign.length, bs.size());
        System.arraycopy(offerInfoToSign, 0, infoToSign, 0, offerInfoToSign.length);

        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        CloseAuctionRequest request = CloseAuctionRequest.newBuilder()
                .setServiceID(bs)
                .setSender(node)
                .setOwner(owner)
                .setWinner(of)
                .setSignature(ByteString.copyFrom(signature)).build();

        stub.closeAuction(request);
    }

    public void placeBidOp(byte[] serviceId, Node owner, Node newBidder, float newBidPrice) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(owner.getIp(), owner.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        Offer of = Offer.newBuilder().setNode(newBidder).setPrice(newBidPrice).build();

        byte[] nodeInfoToSign = node.toByteArray();
        ByteString bs= ByteString.copyFrom(serviceId);
        byte[] offerInfoToSign = of.toByteArray();

        int totalLength = nodeInfoToSign.length + bs.size() + offerInfoToSign.length;

        // Add message content to byte[] for signature
        byte[] infoToSign = new byte[totalLength];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(bs, 0, infoToSign, nodeInfoToSign.length, bs.size());
        System.arraycopy(offerInfoToSign, 0, infoToSign, 0, offerInfoToSign.length);

        // Sign message content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        PlaceBidRequest request = PlaceBidRequest.newBuilder()
                .setServiceID(bs)
                .setSender(node)
                .setOwner(owner)
                .setNewBid(of)
                .setSignature(ByteString.copyFrom(signature)).build();

        stub.placeBid(request);
    }

    /*
    public boolean initiateService(Node owner, byte[] serviceId, ArrayList<Node> brokerlist, int time)
    {

        boolean result = false;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();
        ManagedChannel channel;
        ByteString bs= ByteString.copyFrom(serviceId);
        List<Node> allNodes = new ArrayList<>(brokerlist);

        for (Node n: brokerlist)
        {
            channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


            initiateServiceRequest request = initiateServiceRequest.newBuilder()
                    .setOwner(owner).setServiceId(bs).setTime(time)
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


    public boolean endService (Node n, byte[] serviceId,Offer of)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .build();

        endServiceRequest request = endServiceRequest.newBuilder()
                .setNode(node)
                .setOf(of)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        endServiceResponse response = stub.endService(request);

        return response.getResponse();
    }

     */
}