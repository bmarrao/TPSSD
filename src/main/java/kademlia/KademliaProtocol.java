package kademlia;

import blockchain.Block;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;


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

        // Sign node content
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

        // Check signature
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
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

        // Sign message content
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

        if (idSignVal) {
            return response.getNodesList();
        }
        return new ArrayList<>();
    }

    public void storeTransactionOp(Transaction t, String receiverIp , int receiverPort) throws UnsupportedEncodingException {

        byte[] receiverNodeID = t.getOwner().getId().toByteArray();

        //int transactionType = getTransactionType(t.getType());

        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        Node ownerNode = t.getOwner();

        Node senderNode = Node.newBuilder()
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(this.ipAddress)
                .setPort(this.port)
                .setRandomX(ByteString.copyFrom(new byte[]{randomX}))
                .setPublicKey(ByteString.copyFrom(publicKey.getEncoded())).build();

        Offer senderOffer = t.getSender();

        Transaction transaction = Transaction.newBuilder()
                .setId(t.getId())
                .setType(t.getType())
                .setOwner(ownerNode)
                .setSender(senderOffer)
                .build();

        byte[] ownerNodeToSign   = ownerNode.toByteArray();
        byte[] senderNodeToSign  = senderNode.toByteArray();

        byte[] infoToSign = new byte[this.nodeId.length + ownerNodeToSign.length + senderNodeToSign.length];

        System.arraycopy(this.nodeId, 0, infoToSign, 0, this.nodeId.length);
        System.arraycopy(ownerNodeToSign, 0, infoToSign, this.nodeId.length, ownerNodeToSign.length);
        System.arraycopy(senderNodeToSign, 0, infoToSign, this.nodeId.length+ownerNodeToSign.length, senderNodeToSign.length);

        // Sign node content
        byte[] signature = null;
        try {
            signature = SignatureClass.sign(infoToSign, privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }

        KademliaGrpc.KademliaStub stub = KademliaGrpc.newStub(channel);

        StoreTransactionRequest request = StoreTransactionRequest.newBuilder()
                .setNode(senderNode)
                .setNodeID(ByteString.copyFrom(nodeId))
                .setTransaction(transaction)
                .setSignature(ByteString.copyFrom(signature))
                .build();

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

                if (signVal) {
                    System.out.println("Transaction stored successfully: " + response.getStored());
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

        byte[] infoToSign =
        {
                Byte.parseByte(block.getPreviousHash()),
                (byte) block.getTimestamp(),
                (byte) block.getNonce(),
                Byte.parseByte(String.valueOf(block.getTransactionList())),
                Byte.parseByte(block.getHash()),
        };
        ByteString signedInfo;
        try {
            signedInfo = ByteString.copyFrom(SignatureClass.sign(infoToSign,this.privateKey));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        grpcBlock kBlock = grpcBlock.newBuilder()
                .setPrevHash(ByteString.copyFrom(block.getPreviousHash(), StandardCharsets.UTF_8))
                .setCurrentHash(ByteString.copyFrom(block.getHash(), StandardCharsets.UTF_8))
                .setTimestamp(block.getTimestamp())
                .setNonce(block.getNonce())
                .setSignature(signedInfo)
                .build();

        byte[] senderNodeToSign = senderNode.toByteArray();
        byte[] receiverNodeToSign = receiverNode.toByteArray();
        byte[] kBlockToSign = kBlock.toByteArray();

        int senderAndReceiverLength = senderNodeToSign.length + receiverNodeToSign.length;
        int totalLength = senderAndReceiverLength + kBlockToSign.length;

        infoToSign = new byte[totalLength];

        System.arraycopy(senderNodeToSign, 0, infoToSign, 0, senderNodeToSign.length);
        System.arraycopy(receiverNodeToSign, 0, infoToSign, senderNodeToSign.length, receiverNodeToSign.length);
        System.arraycopy(kBlockToSign, 0, infoToSign, senderAndReceiverLength, kBlockToSign.length);

        // Sign node content
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
                    System.out.println("Transaction stored successfully: " + response.getStored());
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


        // TODO FIX THIS
        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + nodeID.length];

        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        System.arraycopy(nodeID, 0, infoToSign, nodeInfoToSign.length, nodeID.length);


        // Sign message content
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

        // Check response's signature
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getT().toByteArray();
        byte[] infoToVerify = new byte[idToVerify.length + valueToVerify.length];
        // TODO assinar novos nos ?
        System.arraycopy(idToVerify, 0, infoToVerify, 0, idToVerify.length);
        System.arraycopy(valueToVerify, 0, infoToVerify, idToVerify.length, valueToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, response.getSignature().toByteArray(), response.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        channel.shutdown();

        if (signVal) {
            // TODO NEED TO INSERT IN THESE CASES
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
                .setSignature(ByteString.copyFrom(signature)).build();

        FindBlockResponse response = stub.findBlock(request);

        // Check response's signature
        boolean signVal = false;
        byte[] idToVerify = response.getId().toByteArray();
        byte[] valueToVerify = response.getB().toByteArray();
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