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
    private final Auction auc;
    private final int leadingZeros;
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

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
        // Verify signature from request RPC
        byte[] signature = request.getSignature().toByteArray();
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(request.getNode().toByteArray(), signature, request.getNode().getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Atualizar o horario da última vez online do sender TODO: confirmar isto
            new KademliaNode(request.getNode().getIp(),
                    request.getNode().getId().toByteArray(),
                    request.getNode().getPort(), publicKey.getEncoded()).setTime();

            // Sign RPC response
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

        // Verify received signature
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
        // Retrieve the key from the request
        byte[] key = request.getNodeID().toByteArray();
        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
        byte[] nodeToVerify = request.getNode().toByteArray();
        byte[] infoToVerify = new byte[nodeToVerify.length + key.length];
        System.arraycopy(nodeToVerify, 0, infoToVerify, 0, nodeToVerify.length);;
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
            // TODO GET A COPY OF BLOCKCHAIN
            Transaction value = null;//bc.findTransaction(key);
            boolean hasTransaction = true;
            List<Node> closestNodes = new ArrayList<>();
            if (value == null)
            {
                closestNodes = rt.findClosestNode(key, k_nodes);
                hasTransaction = false;
            }
            // Sign RPC response
            try {
                byte[] idToSign = request.getNode().getId().toByteArray();
                byte[] valueToSign = value.toByteArray();
                byte[] infoToSign = new byte[idToSign.length + valueToSign.length];

                System.arraycopy(idToSign, 0, infoToSign, 0, idToSign.length);
                System.arraycopy(valueToSign, 0, infoToSign, idToSign.length, valueToSign.length);

                signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

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

    /*


    @Override
    public void findBlock(FindAuctionRequest request, StreamObserver<FindAuctionResponse> responseObserver)
    {
        // Retrieve the key from the request
        byte[] key = request.getKey().toByteArray();
        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
        byte[] nodeToVerify = request.getNode().toByteArray();
        byte[] infoToVerify = new byte[nodeToVerify.length + key.length];
        System.arraycopy(nodeToVerify, 0, infoToVerify, 0, nodeToVerify.length);;
        System.arraycopy(key, 0, infoToVerify, nodeToVerify.length, key.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            //Creates a new instance of storage. If already exists, use it.

            rt.insert(request.getNode(), 1);

            // Get the value associated with the key from the data store
            // TODO GET A COPY OF BLOCKCHAIN
            Block value = null;//bc.findBlock(key);
            boolean hasBlockk = true;
            List<Node> closestNodes = new ArrayList<>();
            if (value == null)
            {
                closestNodes = rt.findClosestNode(key, k_nodes);
                hasBlock = false;
            }
            // Sign RPC response
            try {
                byte[] idToSign = request.getNode().getId().toByteArray();
                byte[] valueToSign = value.toByteArray();
                byte[] infoToSign = new byte[idToSign.length + valueToSign.length];

                System.arraycopy(idToSign, 0, infoToSign, 0, idToSign.length);
                System.arraycopy(valueToSign, 0, infoToSign, idToSign.length, valueToSign.length);

                signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            FindAuctionResponse response = FindAuctionResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setBlock(value)
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
    */
    /*

    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver)
    {
        // Retrieve the key, value and signature from the request
        byte[] key = request.getKey().toByteArray();
        Node value = request.getValue();
        byte[] signature = request.getSignature().toByteArray();

        // Build data byte array for signature verification
        byte[] nodeBytes = request.getNode().toByteArray();
        byte[] valueBytes = request.getValue().toByteArray();
        byte[] infoToVerify = new byte[key.length + valueBytes.length + nodeBytes.length];
        System.arraycopy(nodeBytes, 0, infoToVerify, 0, nodeBytes.length);
        System.arraycopy(key, 0, infoToVerify, nodeBytes.length, key.length);
        System.arraycopy(valueBytes, 0, infoToVerify, nodeBytes.length + key.length, valueBytes.length);


        // Verify signature from request RPC
        boolean signVal = false;
        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Creates a new instance of storage. If already exists, use it.
            // TODO PUT IN BLOCKCHAIN
            // Hash <HashTransaction,Transaction> ?
            if (request.getTransaction() == null)
            {
                // Recebi um bloco
            }
            else
            {
                // recebi uma transação
            }
            boolean isInBlockChain = bc.addTransaction(request.getTransaction);
            if (isInBlockChain)
            {
                // Não precisa propagar
            }
            else
            {
                // RANDOM - PROPAGA OU NÃO?
                // VARIAVEL PROBABILIDADE CONFIGURAVEL
            }
            // Sign RPC response
            try {
                signature = SignatureClass.sign(request.getNode().getId().toByteArray(), privateKey);
            }
            catch(Exception e) {
                e.printStackTrace();
            }

            // if store successfull -> send true, else false
            //TODO [ When it's false? ]
            StoreResponse response = StoreResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setStored(true)
                    .setPublicKey(ByteString.copyFrom(publicKey.getEncoded()))
                    .setSignature(ByteString.copyFrom(signature)).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else if (!signVal) {
            System.out.println("Signature is invalid, discarding store request...");
        }
        else {
            System.out.println("Node didn't solve crypto puzzles, discarding store request...");
        }

    }
     */

    @Override
    public void storeTransaction(StoreTransactionRequest request, StreamObserver<StoreTransactionResponse> responseObserver) {

        // Retrieve the nodeID from the request
        byte[] nodeID = request.getNodeID().toByteArray();
        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
        byte[] senderNodeToVerify = request.getNode().toByteArray();
        byte[] transactionToVerify = request.getTransaction().toByteArray();

        int senderAndTransactionLength = senderNodeToVerify.length + transactionToVerify.length;
        int totalLength = senderAndTransactionLength + nodeID.length;


        byte[] infoToVerify = new byte[totalLength];
        System.arraycopy(senderNodeToVerify, 0, infoToVerify, 0, senderNodeToVerify.length);;
        System.arraycopy(transactionToVerify, 0, infoToVerify, senderNodeToVerify.length, transactionToVerify.length );;
        System.arraycopy(nodeID, 0, infoToVerify, senderAndTransactionLength, nodeID.length);

        try {
            signVal = SignatureClass.verify(infoToVerify, signature, request.getNode().getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {

            //Creates a new instance of storage. If already exists, use it.
            rt.insert(request.getNode(), 1);

            //TODO Lógica


            // Sign RPC response
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
    public void storeBlock(StoreBlockRequest request, StreamObserver<StoreBlockResponse> responseObserver) {

        // Retrieve the nodeID from the request
        //TODO do I need nodeID?
        byte[] nodeID = request.getNode().getId().toByteArray();
        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
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

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {

            //Creates a new instance of storage. If already exists, use it.
            rt.insert(request.getNode(), 1);

            ArrayList<blockchain.Transaction> transactions = new ArrayList<>();

            for(Transaction transaction : request.getBlock().getTransList()){

                blockchain.Transaction convertedTransaction = convertGRPCTransaction(transaction);
                transactions.add(convertedTransaction);

            }

            String previousHash = request.getBlock().getPrevHash().toString();

            Block newBlock = new Block(previousHash, transactions);

            //TODO Verificar Lógica
            bc.addBlock(newBlock);

            //Check if chain is valid!
            if(!bc.isChainValid()){

                System.out.println("Blockchain is invalid, discarding store block request...");

            }else {

                // Sign RPC response
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

    private blockchain.Transaction convertGRPCTransaction(Transaction grpcTransaction)
    {

        Node ownerNode = grpcTransaction.getOwner();

        KademliaNode convertedOwner = convertGRPCNode(ownerNode);

        float price = grpcTransaction.getSender().getPrice();

        byte[] serviceID = grpcTransaction.getId().toByteArray();

        blockchain.Transaction.TransactionType transactionType = convertTransactionType(grpcTransaction.getType());

        blockchain.Transaction convertedTransaction = new blockchain.Transaction(convertedOwner, price, serviceID, transactionType);

        return convertedTransaction;

    }

    private KademliaNode convertGRPCNode(Node grpcNode)
    {
        KademliaNode convertedNode = new KademliaNode(grpcNode.getIp(), grpcNode.getId().toByteArray() , grpcNode.getPort(), grpcNode.getPublicKey().toByteArray());

        return convertedNode;
    }

    private blockchain.Transaction.TransactionType convertTransactionType(int type)
    {

        //There are 3 Transactions Type:
        // 0 -> Bid
        // 1 -> NewAuction
        // 2 -> CloseAuction
        blockchain.Transaction.TransactionType transactionType = blockchain.Transaction.TransactionType.BID;

        switch (type) {
            case 1:
                transactionType = blockchain.Transaction.TransactionType.OPENING;
                break;
            case 2:
                transactionType = blockchain.Transaction.TransactionType.CLOSURE;
                break;
            default:
                break;
        }

        return transactionType;
    }

}
