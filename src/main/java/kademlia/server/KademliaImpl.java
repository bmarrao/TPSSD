package kademlia.server;
import auctions.Auction;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import kademlia.*;

import java.security.*;
import java.util.List;

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
            signVal = SignatureClass.verify(request.getNode().toByteArray(), signature, request.getPublicKey().toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Atualizar o horario da última vez online do sender TODO: confirmar isto
            new KademliaNode(request.getNode().getIp(),
                    request.getNode().getId().toByteArray(),
                    request.getNode().getPort()).setTime();

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
            signVal = SignatureClass.verify(request.getNode().toByteArray(), signature, request.getPublicKey().toByteArray());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal && arePuzzlesValid(request.getNode().getId().toByteArray(), request.getNode().getRandomX().byteAt(0))) {
            rt.insert(request.getNode(), 1);

            // Get the closest node to the target ID from the routing table
            List<Node> closestNodes = rt.findClosestNode(request.getKey().toByteArray(), k_nodes);

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
                    .setId(request.getKey())
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


    /*
    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver)
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
            Node value = ks.findValue(key);

            if (value != null)
            {
                List<Node> closestNodes = rt.findClosestNode(key, k_nodes);

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

            FindValueResponse response = FindValueResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setValue(value)
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



    @Override
    public void getPrice(getPriceRequest request, StreamObserver<getPriceResponse> responseObserver)
    {
        System.out.println("Sending Biggest Price");
        float price = auc.getPrice(request.getServiceId().toByteArray());

        getPriceResponse response = getPriceResponse
                .newBuilder()
                .setPrice(price)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }

}
