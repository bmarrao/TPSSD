package kademlia.server;
import auctions.Auction;

import blockchain.Block;
import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import kademlia.*;

import java.security.*;
import java.util.ArrayList;
import java.util.List;

import static kademlia.Kademlia.bc;
import static kademlia.Kademlia.rt;


public class KademliaImpl extends KademliaGrpc.KademliaImplBase
{
    private static final int k_nodes = 3;
    private final int leadingZeros;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;
    public Auction auc;

    KademliaImpl(Auction auc, int leadingZeros, PublicKey publicKey, PrivateKey privateKey)
    {
        this.auc = auc;
        this.leadingZeros = leadingZeros;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }


    // Check crypto puzzle zero count
    public static boolean checkZeroCount(byte[] puzzle, int numOfLeadingZeros) {
        int leadingZeros = 0;

        for (byte b : puzzle) {
            // If byte is zero -> add 8 since byte contains 8 bits
            if (b == 0)
            {
                leadingZeros += 8;
            }
            else
            {
                // Count leading zeros in the byte using bit manipulation
                int byteLeadingZeros = Integer.numberOfLeadingZeros(b & 0xFF) - 24;
                leadingZeros += byteLeadingZeros;
                break;
            }
        }
        System.out.println("Leading zeros count: " + leadingZeros);

        return leadingZeros >= numOfLeadingZeros;
    }


    // Checks if node from incoming RPC request solved the crypto puzzles
    public boolean arePuzzlesValid(byte[] nodeId, byte randomX) {
        // verify static puzzle
        boolean isStaticPuzzleValid = false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            isStaticPuzzleValid = checkZeroCount(md.digest(nodeId), leadingZeros);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        // verify dynamic puzzle
        boolean isDynamicPuzzleValid = false;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] xorOp = new byte[nodeId.length];

            for (int i = 0; i < nodeId.length; i++) {
                xorOp[i] = (byte) (nodeId[i] ^ randomX);
            }

            isDynamicPuzzleValid = checkZeroCount(md.digest(xorOp), leadingZeros);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isStaticPuzzleValid && isDynamicPuzzleValid;
    }


    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        // Verify signature from request RPC (node)
        byte[] signature = request.getSignature().toByteArray();
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(request.getNode().toByteArray(), signature, request.getNode().getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Update timestamp of last time sender was online
            new KademliaNode(request.getNode().getIp(),
                    request.getNode().getId().toByteArray(),
                    request.getNode().getPort(), publicKey.getEncoded()).setTime();

            // Sign RPC response (id)
            try {
                signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            } catch (Exception e) {
                e.printStackTrace();
            }

            PingResponse pingResponse = PingResponse
                    .newBuilder()
                    .setId(request.getNode().getId())
                    .setOnline(true)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .setSignature(ByteString.copyFrom(signature))
                    .build();

            // Send the response to the client.
            responseObserver.onNext(pingResponse);

            // Notifies the customer that the call is completed.
            responseObserver.onCompleted();
        } else if (!signVal) {
            System.out.println("Signature is invalid, discarding ping request...");
        }
        else {
            System.out.println("Node didn't solve crypto puzzles, discarding find ping request...");
        }
    }


    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {

        // Verify received signature (node)
        byte[] signature = request.getSignature().toByteArray();
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(request.getNode().toByteArray(), signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Get the closest node to the target ID from the routing table
            List<Node> closestNodes = rt.findClosestNode(request.getNodeID().toByteArray(), k_nodes);

            // Sign id
            byte[] idSignature = null;
            try {
                idSignature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // send RPC response
            FindNodeResponse response = FindNodeResponse.newBuilder()
                    .setId(request.getNodeID())
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .addAllNodes(closestNodes)
                    .setIdSignature(ByteString.copyFrom(idSignature)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else if (!signVal) {
            System.out.println("Signature is invalid, discarding find node request...");
        }
        else {
            System.out.println("Node didn't solve crypto puzzles, discarding find node request...");
        }
    }


    @Override
    public void findAuction(FindAuctionRequest request, StreamObserver<FindAuctionResponse> responseObserver)
    {

        // Verify signature (node + nodeId)
        boolean signVal = false;
        byte[] nodeToVerify = request.getNode().toByteArray();
        byte[] key = request.getNodeID().toByteArray();
        byte[] signature = request.getSignature().toByteArray();

        byte[] infoToVerify = new byte[nodeToVerify.length + key.length];
        System.arraycopy(nodeToVerify, 0, infoToVerify, 0, nodeToVerify.length);
        System.arraycopy(key, 0, infoToVerify, nodeToVerify.length, key.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            //Creates a new instance of storage. If already exists, use it.
            rt.insert(request.getNode(), 1);

            // Get the value associated with the key from the data store
            Transaction value = null;
            boolean hasTransaction = true;
            List<Node> closestNodes = new ArrayList<>();
            if (value == null)
            {
                closestNodes = rt.findClosestNode(key, k_nodes);
                hasTransaction = false;
            }

            // Sign RPC response (id + transaction)
            try {
                byte[] idToSign = request.getNode().getId().toByteArray();
                byte[] valueToSign = value.toByteArray();
                byte[] infoToSign = new byte[idToSign.length + valueToSign.length];

                System.arraycopy(idToSign, 0, infoToSign, 0, idToSign.length);
                System.arraycopy(valueToSign, 0, infoToSign, idToSign.length, valueToSign.length);

                signature = SignatureClass.sign(infoToSign, privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // Send find auction response
            FindAuctionResponse response = FindAuctionResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setT(value)
                    .addAllNodes(closestNodes)
                    .setHasTransaction(hasTransaction)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .setSignature(ByteString.copyFrom(signature)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else if (!signVal) {
            System.out.println("Signature is invalid, discarding find value request...");
        }
        else {
            System.out.println("Node didn't solve crypto puzzles, discarding find value request...");
        }
    }


    @Override
    public void findBlock(FindBlockRequest request, StreamObserver<FindBlockResponse> responseObserver)
    {
        // Verify signature (node + key)
        boolean signVal = false;
        byte[] signature = request.getSignature().toByteArray();
        byte[] nodeToVerify = request.getNode().toByteArray();
        byte[] key = request.getKey().toByteArray();

        byte[] infoToVerify = new byte[nodeToVerify.length + key.length];
        System.arraycopy(nodeToVerify, 0, infoToVerify, 0, nodeToVerify.length);
        System.arraycopy(key, 0, infoToVerify, nodeToVerify.length, key.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            grpcBlock value = null;//bc.findBlock(key);
            boolean hasBlock = true;
            List<Node> closestNodes = new ArrayList<>();
            if (value == null)
            {
                closestNodes = rt.findClosestNode(key, k_nodes);
                hasBlock = false;
            }

            // Sign RPC response (id + block)
            try {
                byte[] idToSign = request.getNode().getId().toByteArray();
                byte[] valueToSign = value.toByteArray();
                byte[] infoToSign = new byte[idToSign.length + valueToSign.length];

                System.arraycopy(idToSign, 0, infoToSign, 0, idToSign.length);
                System.arraycopy(valueToSign, 0, infoToSign, idToSign.length, valueToSign.length);

                signature = SignatureClass.sign(infoToSign, privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // Send find block response
            FindBlockResponse response = FindBlockResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setB(value)
                    .addAllNodes(closestNodes)
                    .setHasBlock(hasBlock)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .setSignature(ByteString.copyFrom(signature)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else if (!signVal) {
            System.out.println("Signature is invalid, discarding find value request...");
        }
        else {
            System.out.println("Node didn't solve crypto puzzles, discarding find value request...");
        }
    }


    @Override
    public void storeTransaction(StoreTransactionRequest request, StreamObserver<StoreTransactionResponse> responseObserver)
    {
        // Verify signature (senderNode + id + transaction)
        boolean signVal = false;
        byte[] signature = request.getSignature().toByteArray();
        byte[] nodeToVerify = request.getNode().toByteArray();
        byte[] transactionToVerify = request.getTransaction().toByteArray();

        byte[] infoToVerify = new byte[nodeToVerify.length + transactionToVerify.length];
        System.arraycopy(nodeToVerify, 0, infoToVerify, 0, nodeToVerify.length);
        System.arraycopy(transactionToVerify, 0, infoToVerify, nodeToVerify.length, transactionToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {

            //Creates a new instance of storage. If already exists, use it.
            rt.insert(request.getNode(), 1);

            boolean isValid = bc.isTransactionValid(request.getTransaction(),null);

            if (isValid)
            {
                bc.addTransaction(request.getTransaction());

            }

            // Sign RPC response (id)
            try {
                signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            StoreTransactionResponse response = StoreTransactionResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setStored(true)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .setSignature(ByteString.copyFrom(signature)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        }
        else if (!signVal)
        {
            System.out.println("Signature is invalid, discarding find node request...");
        }
        else
        {
            System.out.println("Node didn't solve crypto puzzles, discarding find node request...");
        }
    }


    @Override
    public void storeBlock(StoreBlockRequest request, StreamObserver<StoreBlockResponse> responseObserver)
    {
        // Verify signature (senderNode + receiverNode + grpcBlock)
        boolean signVal = false;
        byte[] signature = request.getSignature().toByteArray();
        byte[] senderNodeToVerify = request.getNode().toByteArray();
        byte[] receiverNodeToVerify = request.getReceiver().toByteArray();
        byte[] blockToVerify = request.getBlock().toByteArray();

        int senderAndReceiverLength = senderNodeToVerify.length + receiverNodeToVerify.length;
        int totalLength = senderAndReceiverLength + blockToVerify.length;

        byte[] infoToVerify = new byte[totalLength];
        System.arraycopy(senderNodeToVerify, 0, infoToVerify, 0, senderNodeToVerify.length);
        System.arraycopy(receiverNodeToVerify, 0, infoToVerify, senderNodeToVerify.length, receiverNodeToVerify.length );
        System.arraycopy(blockToVerify, 0, infoToVerify, senderAndReceiverLength, blockToVerify.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0)))
        {
            rt.insert(request.getNode(), 1);

            ArrayList<Transaction> transactions = new ArrayList<>(request.getBlock().getTransList());

            String previousHash = request.getBlock().getPrevHash().toString();

            Block newBlock = new Block(previousHash.getBytes(), transactions, null);
            bc.addBlock(newBlock);

            // Check if chain is valid!
            if(!bc.isChainValid()) {
                System.out.println("Blockchain is invalid, discarding store block request...");

            } else {
                // Sign RPC response (id)
                try {
                    signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                StoreBlockResponse response = StoreBlockResponse.newBuilder()
                        .setId(request.getNode().getId())
                        .setStored(true)
                        .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                        .setSignature(ByteString.copyFrom(signature)).build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        }
        else if (!signVal)
        {
            System.out.println("Signature is invalid, discarding store block request...");
        }
        else
        {
            System.out.println("Node didn't solve crypto puzzles, discarding store block request...");
        }
    }

}
