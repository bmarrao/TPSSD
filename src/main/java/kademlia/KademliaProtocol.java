package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

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


    public boolean pingOp(String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(this.nodeId))
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


    public List<Node> findNodeOp(byte[] nodeId, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();
        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
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
                .setNodeID(ByteString.copyFrom(nodeId))
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

    //TODO mudar para objeto Transaction, implementar KademliaIMPL para lidar com a resposta
/*    public boolean storeTransactionOp(byte[] nodeId, String receiverIp, int receiverPort,
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
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(this.ipAddress)
                .setPort(this.port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Offer senderOffer = Offer.newBuilder().setNode(senderNode).setPrice(price).build();

        Transaction transaction = Transaction.newBuilder()
                                .setId(ByteString.copyFrom(auctionID))
                                .setType(transactionType)
                                .setOwner(ownerNode)
                                .setBroker(brokerNode)
                                .setSender(senderOffer)
                                .build();

        byte[] ownerNodeToSign = ownerNode.toByteArray();
        byte[] brokerNodeToSign = brokerNode.toByteArray();
        byte[] senderNodeToSign = senderNode.toByteArray();
        byte[] offerInfoToSign = senderOffer.toByteArray();

        int ownerAndBrokerLength = ownerNodeToSign.length + brokerNodeToSign.length;
        int withSenderLength = ownerAndBrokerLength + senderNodeToSign.length;
        int withOfferLength = withSenderLength + offerInfoToSign.length;
        int totalLength = withOfferLength + nodeId.length;

        byte[] infoToSign = new byte[totalLength];

        System.arraycopy(ownerNodeToSign, 0, infoToSign, 0, ownerNodeToSign.length);
        System.arraycopy(brokerNodeToSign, 0, infoToSign, ownerNodeToSign.length, brokerNodeToSign.length);
        System.arraycopy(senderNodeToSign, 0, infoToSign, ownerAndBrokerLength, senderNodeToSign.length);
        System.arraycopy(offerInfoToSign, 0, infoToSign, withSenderLength, offerInfoToSign.length);
        System.arraycopy(nodeId, 0, infoToSign, withOfferLength, nodeId.length);

        // Sign node content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        StoreTransactionRequest request = StoreTransactionRequest.newBuilder()
                .setNode(senderNode)
                .setNodeID(ByteString.copyFrom(nodeId))
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
    }*/

    public boolean storeTransactionOp(blockchain.Transaction t) {

        byte[] receiverNodeID = t.getReceiver().getNodeId();

        String receiverIP = t.getReceiver().getIpAdress();

        int receiverPort = t.getReceiver().getPort();

        int transactionType = getTransactionType(t.getType());

        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIP, receiverPort).usePlaintext().build();

        Node ownerNode = Node.newBuilder()
                .setId(ByteString.copyFrom(receiverNodeID))
                .setIp(receiverIP)
                .setPort(receiverPort)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Node senderNode = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(this.ipAddress)
                .setPort(this.port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        Offer senderOffer = Offer.newBuilder().setNode(senderNode).setPrice(t.getPrice()).build();

        Transaction transaction = Transaction.newBuilder()
                .setId(t.getServiceID())
                .setType(transactionType)
                .setOwner(ownerNode)
                .setSender(senderOffer)
                .build();

        byte[] ownerNodeToSign = ownerNode.toByteArray();
        byte[] senderNodeToSign = senderNode.toByteArray();
        byte[] offerInfoToSign = senderOffer.toByteArray();

        int ownerAndSenderLength = ownerNodeToSign.length + senderNodeToSign.length;
        int withOfferLength = ownerAndSenderLength + offerInfoToSign.length;
        int totalLength = withOfferLength + nodeId.length;

        byte[] infoToSign = new byte[totalLength];

        System.arraycopy(ownerNodeToSign, 0, infoToSign, 0, ownerNodeToSign.length);
        System.arraycopy(senderNodeToSign, 0, infoToSign, ownerNodeToSign.length, senderNodeToSign.length);
        System.arraycopy(offerInfoToSign, 0, infoToSign, ownerAndSenderLength, offerInfoToSign.length);
        System.arraycopy(nodeId, 0, infoToSign, withOfferLength, nodeId.length);

        // Sign node content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        StoreTransactionRequest request = StoreTransactionRequest.newBuilder()
                .setNode(senderNode)
                .setNodeID(ByteString.copyFrom(nodeId))
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

    private int getTransactionType(blockchain.Transaction.TransactionType type)
    {

        //There are 3 Transactions Type:
        // 0 -> Bid
        // 1 -> NewAuction
        // 2 -> CloseAuction
        int transactionType = 0;

        switch(type) {
            case OPENING:
                transactionType = 1;
                break;
            case CLOSURE:
                transactionType = 2;
                break;
            default:
                break;
        }

        return transactionType;

    }

    /*public boolean storeBlockOp(byte[] nodeId, String receiverIp, int receiverPort,
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

        byte[] ownerNodeToSign = ownerNode.toByteArray();
        byte[] brokerNodeToSign = brokerNode.toByteArray();
        byte[] senderNodeToSign = senderNode.toByteArray();
        byte[] offerInfoToSign = senderOffer.toByteArray();

        int ownerAndBrokerLength = ownerNodeToSign.length + brokerNodeToSign.length;
        int withSenderLength = ownerAndBrokerLength + senderNodeToSign.length;
        int withOfferLength = withSenderLength + offerInfoToSign.length;
        int totalLength = withOfferLength + nodeId.length;

        byte[] infoToSign = new byte[totalLength];

        System.arraycopy(ownerNodeToSign, 0, infoToSign, 0, ownerNodeToSign.length);
        System.arraycopy(brokerNodeToSign, 0, infoToSign, ownerNodeToSign.length, brokerNodeToSign.length);
        System.arraycopy(senderNodeToSign, 0, infoToSign, ownerAndBrokerLength, senderNodeToSign.length);
        System.arraycopy(offerInfoToSign, 0, infoToSign, withSenderLength, offerInfoToSign.length);
        System.arraycopy(nodeId, 0, infoToSign, withOfferLength, nodeId.length);

        // Sign node content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
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
    }*/


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

    public FindAuctionResponse findAuctionOp(byte[] nodeID, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPort(port).build();


        // TODO FIX THIS
        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + nodeID.length];

        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(nodeID, 0, infoToSign, nodeInfoToSign.length, nodeID.length);



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
                .setNodeID(ByteString.copyFrom(nodeID))
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


}