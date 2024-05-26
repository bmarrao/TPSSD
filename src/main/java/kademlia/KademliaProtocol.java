package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;
import static kademlia.Kademlia.rt;

public class KademliaProtocol {
    public byte[] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public byte randomX;

    public KademliaProtocol(byte[] nodeId, String ipAddress, int port, PublicKey publicKey, PrivateKey privateKey, byte randomX) {
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
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        // Sign node content (node)
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(node.toByteArray(), privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        PingRequest request = PingRequest.newBuilder()
                .setNode(node)
                .setSignature(ByteString.copyFrom(signature)).build();

        // Receive response
        PingResponse response = stub.ping(request);

        // Check signature (id)
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
            rt.insert(node,0);
            return response.getOnline();
        }
        return false;
    }


    public List<Node> findNodeOp(byte[] nodeId, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();
        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        // Sign message content (node)
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(node.toByteArray(), privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setNodeID(ByteString.copyFrom(nodeId))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindNodeResponse response = stub.findNode(request);

        // Check id signature
        boolean idSignVal = false;
        try {
            idSignVal = SignatureClass.verify(response.getId().toByteArray(), response.getIdSignature().toByteArray(), response.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (idSignVal)
        {
            rt.insert(node,0);

            return response.getNodesList();
        }
        return new ArrayList<>();
    }


    public boolean storeTransactionOp(Transaction t, String receiverIp , int receiverPort) throws UnsupportedEncodingException {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();


        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(this.ipAddress)
                .setPort(this.port)
                .setRandomX(ByteString.copyFrom(new byte[]{this.randomX}))
                .setPublicKey(ByteString.copyFrom(this.publicKey.getEncoded()))
                .build();

        // Sign message content (node + transaction)
        byte[] nodeToSign  = node.toByteArray();
        byte[] transactionToSign = t.toByteArray();
        byte[] infoToSign = new byte[nodeToSign.length + transactionToSign.length];
        System.arraycopy(nodeToSign, 0, infoToSign, 0, nodeToSign.length);
        System.arraycopy(transactionToSign, 0, infoToSign, nodeToSign.length, transactionToSign.length);

        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        KademliaGrpc.KademliaStub stub = KademliaGrpc.newStub(channel);

        StoreTransactionRequest request = StoreTransactionRequest.newBuilder()
                .setNode(node)
                .setTransaction(t)
                .setSignature(ByteString.copyFrom(signature))
                .build();

        final boolean[] transactionSuccessful = {false};
        stub.storeTransaction(request, new StreamObserver<StoreTransactionResponse>() {
            @Override
            public void onNext(StoreTransactionResponse response) {
                // Process the response
                boolean signVal = false;
                try {
                    signVal = SignatureClass.verify(
                            response.getId().toByteArray(),
                            response.getSignature().toByteArray(),
                            response.getPublicKey().toByteArray()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (signVal)
                {
                    rt.insert(t.getSender().getNode(),0);

                    //System.out.println("Transaction stored successfully: " + response.getStored());
                    transactionSuccessful[0] = true;
                } else {
                    //System.out.println("Signature verification failed.");
                    transactionSuccessful[0] = false;
                }
            }

            @Override
            public void onError(Throwable t) {
                // Handle the error
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // Clean up resources
                channel.shutdown();
            }
        });
        return transactionSuccessful[0];
    }

    public void storeBlockOp(byte[] receiverNodeID, String receiverIp, int receiverPort, blockchain.Block block) {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        Node senderNode = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(this.ipAddress)
                .setPort(this.port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        Node receiverNode = Node.newBuilder()
                .setId(ByteString.copyFrom(receiverNodeID))
                .setIp(receiverIp)
                .setPort(receiverPort)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX})).build();

        // Sign GRPC Block
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] signedInfo = null;
        try {
            outputStream.write(block.getNode().toByteArray());
            outputStream.write(block.getPreviousHash());
            outputStream.write(block.getHash());
            outputStream.write(ByteBuffer.allocate(8).putLong(block.getTimestamp()).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(block.getNonce()).array());
            outputStream.write(block.getTransactionList().toString().getBytes(StandardCharsets.UTF_8));
            outputStream.close();
            signedInfo = SignatureClass.sign(outputStream.toByteArray(),this.privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // Build GRPC Block
        grpcBlock kBlock = grpcBlock.newBuilder()
                .setPrevHash(ByteString.copyFrom(block.getPreviousHash()))
                .setCurrentHash(ByteString.copyFrom(block.getHash()))
                .setTimestamp(block.getTimestamp())
                .setNonce(block.getNonce())
                .setSignature(ByteString.copyFrom(signedInfo))
                .build();


        // Sign node content (sender + receiver + block)
        byte[] senderNodeToSign = senderNode.toByteArray();
        byte[] receiverNodeToSign = receiverNode.toByteArray();
        byte[] kBlockToSign = kBlock.toByteArray();

        int senderAndReceiverLength = senderNodeToSign.length + receiverNodeToSign.length;
        int totalLength = senderAndReceiverLength + kBlockToSign.length;
        byte[] infoToSign = new byte[totalLength];

        System.arraycopy(senderNodeToSign, 0, infoToSign, 0, senderNodeToSign.length);
        System.arraycopy(receiverNodeToSign, 0, infoToSign, senderNodeToSign.length, receiverNodeToSign.length);
        System.arraycopy(kBlockToSign, 0, infoToSign, senderAndReceiverLength, kBlockToSign.length);

        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        KademliaGrpc.KademliaStub stub = KademliaGrpc.newStub(channel);

        StoreBlockRequest request = StoreBlockRequest.newBuilder()
                .setNode(senderNode)
                .setReceiver(receiverNode)
                .setBlock(kBlock)
                .setSignature(ByteString.copyFrom(signature)).build();

        stub.storeBlock(request, new StreamObserver<StoreBlockResponse>() {
            @Override
            public void onNext(StoreBlockResponse response) {
                // Process the response
                boolean signVal = false;
                try {
                    signVal = SignatureClass.verify(
                            response.getId().toByteArray(),
                            response.getSignature().toByteArray(),
                            response.getPublicKey().toByteArray()
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (signVal) {
                    rt.insert(senderNode,0);

                    System.out.println("Block stored successfully: " + response.getStored());
                } else {
                    System.out.println("Signature verification failed.");
                }
            }

            @Override
            public void onError(Throwable t) {
                // Handle the error
                t.printStackTrace();
            }

            @Override
            public void onCompleted() {
                // Clean up resources
                channel.shutdown();
            }
        });
    }


    public FindAuctionResponse findAuctionOp(byte[] nodeID, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setPort(port).build();


        // Sign message content (node, nodeId)
        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + nodeID.length];

        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(nodeID, 0, infoToSign, nodeInfoToSign.length, nodeID.length);

        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Send RPC request
        FindAuctionRequest request = FindAuctionRequest.newBuilder()
                .setNode(node)
                .setNodeID(ByteString.copyFrom(nodeID))
                .setSignature(ByteString.copyFrom(signature)).build();

        FindAuctionResponse response = stub.findAuction(request);

        // Check response's signature (id + transaction)
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getT().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];

        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify, 0, infoToVerify, idToVerify.length, valueToVerify.length);

        try
        {

            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal)
        {
            rt.insert(node,0);
            return response;
        }
        return null;
    }


    // TODO UNDO PROTO
    public FindBlockResponse findBlockOp(byte[] key, String receiverIp, int receiverPort)
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                .setPort(port).build();


        // Sign message content (node + key)
        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + key.length];

        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(key, 0, infoToSign, nodeInfoToSign.length, key.length);

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
                .setSignature(ByteString.copyFrom(signature)).build();

        FindBlockResponse response = stub.findBlock(request);

        // Check response's signature (id + block)
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getB().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];

        // TODO assinar novos nos ? <- acho que não é preciso
        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify,  0, infoToVerify, idToVerify.length, valueToVerify.length);

        try
        {

            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal)
        {
            rt.insert(node,0);
            return response;
        }
        return null;
    }

}