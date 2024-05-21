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


    public boolean storeTransactionOp(byte[] nodeId, String receiverIp, int receiverPort,
                                      byte[] ownerNodeID, String ownerIP, int ownerPort,
                                      byte[] brokerNodeID, String brokerIP, int brokerPort,
                                      byte[] auctionID, int transactionType, float price) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        Node ownerNode = Node.newBuilder()
                .setId(ByteString.copyFrom(ownerNodeID))
                .setIp(ownerIP)
                .setPort(ownerPort)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Node brokerNode = Node.newBuilder()
                .setId(ByteString.copyFrom(brokerNodeID))
                .setIp(brokerIP)
                .setPort(brokerPort)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Node senderNode = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Offer senderOffer = Offer.newBuilder().setNode(senderNode).setPrice(price).build();

        Transaction transaction = Transaction.newBuilder()
                                .setId(ByteString.copyFrom(auctionID))
                                .setType(transactionType)
                                .setOwner(ownerNode)
                                .setBroker(brokerNode)
                                .setSender(senderOffer)
                                .build();


        // Sign node content
        byte[] signature = null;
        try {
            //TODO signature = SignatureClass.sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        StoreTransactionRequest request = StoreTransactionRequest.newBuilder()
                .setNode(senderNode)
                .setTransaction(transaction)
                .setSignature(ByteString.copyFrom(signature))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        // Receive response
        StoreTransactionResponse response = stub.storeTransaction(request);

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
            return response.getStored();
        }
        return false;
    }


    /*

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
    */

    public FindAuctionResponse findAuctionOp(byte[] key, String receiverIp, int receiverPort)
    {
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
        FindAuctionRequest request = FindAuctionRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindAuctionResponse response = stub.findAuction(request);

        // Check response's signature
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getT().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];
        // TODO assinar novos nos ?
        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify,  0, infoToVerify, idToVerify.length, valueToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal)
        {
            // TODO NEED TO INSERT IN THESE CASES
            return response;
        }
        return null;
    }
    /*
    public FindBlockResponse findAuctionOp(byte[] key, String receiverIp, int receiverPort)
    {
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
        FindBlockRequest request = FindBlockRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindAuctionResponse response = stub.findBlock(request);

        // Check response's signature
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getT().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];
        // TODO assinar novos nos ?
        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify,  0, infoToVerify, idToVerify.length, valueToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal)
        {
            // TODO NEED TO INSERT IN THESE CASES
            return response;
        }
        return null;
    }


     */
    /*
    public void newAuctionOp(byte[] serviceId, Node owner)
    {

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



     */
}