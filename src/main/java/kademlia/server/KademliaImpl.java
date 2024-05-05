package kademlia.server;
import auctions.Auction;

import auctions.BrokerService;
import io.grpc.stub.StreamObserver;
import kademlia.*;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static kademlia.Kademlia.rt;

// TODO Hugo Implementar metodos dessa classe
//  TODO parte do bootstrap - Cristina
public class KademliaImpl extends KademliaGrpc.KademliaImplBase
{
    private static final int k_nodes = 3;

    private final Auction auc;
    KademliaImpl(Auction auc)
    {
        this.auc = auc;
    }


    // Verify signatures from received RPC messages
    public static boolean verify(byte[] data, byte[] signature, String publicKeyStr) throws Exception {
        // Convert public key from String to PublicKey
        byte[] publicKeyBytes = Base64.getDecoder().decode(publicKeyStr);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKeyBytes));

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(data);
        return sig.verify(signature);
    }


    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {

        //TODO insert!
        //rt.insert(request.getNode());

        // String sender = request.getMyNodeId();

        // Atualizar o horario da última vez online do sender
        //boolean resultMsg = true;

        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
        try {
            signVal = verify(request.getNode().toByteArray(), signature, request.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            PingResponse pingResponse = PingResponse
                    .newBuilder()
                    .setOnline(true)
                    .build();

            // Send the response to the client.
            responseObserver.onNext(pingResponse);

            // Notifies the customer that the call is completed.
            responseObserver.onCompleted();
        }
        else {
            System.out.println("Signature is invalid, discarding ping request...");
        }
    }



    @Override
    public void store(StoreRequest request, StreamObserver<StoreResponse> responseObserver) {

        //TODO insert!
        //rt.insert(request.getNode());

        //Creates a new instance of storage. If already exists, use it.
        KademliaStore dataStore = KademliaStore.getInstance();

        // Retrieve the key, value and signature from the request
        String key = request.getKey();
        String value = request.getValue();
        byte[] signature = request.getSignature().toByteArray();

        // Verify signature
        boolean signVal = false;
        try {
            signVal = verify(request.toByteArray(), signature, request.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            dataStore.store(key,value);

            // if store successfull -> send true, else false
            //TODO [ When it's false? ]
            StoreResponse response = StoreResponse.newBuilder().setStored(true).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else {
            System.out.println("Signature is invalid, discarding store request...");
        }
        // blockchain bc
        // bid a b
    }


    @Override
    public void findNode(FindNodeRequest request, StreamObserver<FindNodeResponse> responseObserver) {
        //TODO insert!
        //rt.insert(request.getNode());

        // Retrieve the target ID from the request
        byte[] nodeID = request.getKey().toByteArray();

        byte[] signature = request.getSignature().toByteArray();
        byte[] signedInfo = request.getNode().toByteArray();

        boolean signVal = false;
        try {
            signVal = verify(signedInfo, signature, request.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Is signature valid? " + signVal);

        if (signVal) {
            // Get the closest node to the target ID from the routing table
            //TODO : Retirar o j??
            //TODO : replace KademliaNode to Node
            List<Node> closestNodes = rt.findClosestNode(nodeID, k_nodes);

            //TODO : AddAllNodes
            FindNodeResponse response = FindNodeResponse.newBuilder()
                    .setId(request.getKey()).addAllNodes(closestNodes).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else {
            System.out.println("Signature is invalid, discarding find node request...");
        }
    }

    @Override
    public void findValue(FindValueRequest request, StreamObserver<FindValueResponse> responseObserver) {

        //TODO insert!
        //rt.insert(request.getNode());

        //Creates a new instance of storage. If already exists, use it.
        KademliaStore dataStore = KademliaStore.getInstance();

        // Retrieve the key from the request
        String key = request.getKey();
        byte[] signature = request.getSignature().toByteArray();

        boolean signVal = false;
        try {
            signVal = verify(request.toByteArray(), signature, request.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            // Get the value associated with the key from the data store
            String value = dataStore.findValue(key);

            FindValueResponse response = FindValueResponse.newBuilder()
                    .setId(request.getNode().getId())
                    .setValue(value).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
        else {
            System.out.println("Signature is invalid, discarding find value request...");
        }
    }

    /*
    @Override
    public void notify(NotifyRequest request, StreamObserver<NotifyResponse> responseObserver)
    {
        System.out.println("Just received Notification from service " + request.getServiceId() + "With new price " + request.getPrice());

        //TODO insert!
        //rt.insert(request.getNode());

        byte[] signature = request.getSignature().toByteArray();

        boolean signVal = false;
        try {
            signVal = verify(request.toByteArray(), signature, request.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            // Get the value associated with the key from the data store
            //TODO insert!
            //rt.insert(request.getNode());

            NotifyResponse response = NotifyResponse
                    .newBuilder()
                    .setResponse(true)
                    .build();

            // Send the response to the client.
            responseObserver.onNext(response);

            // Notifies the customer that the call is completed.
            responseObserver.onCompleted();
        }
        else {
            System.out.println("Signature is invalid, discarding find value request...");
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

    @Override
    public void sendPrice(sendPriceRequest request, StreamObserver<sendPriceResponse> responseObserver)
    {
        System.out.println("Receiving Price");
        boolean res = auc.receiveOffer(request.getOffer(), request.getServiceId().toByteArray());

        sendPriceResponse response = sendPriceResponse
                .newBuilder()
                .setResult(res)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }


    @Override
    public void initiateService(initiateServiceRequest request, StreamObserver<initiateServiceResponse> responseObserver)
    {
        System.out.println("In initiate Service");
        auc.initiateService(request.getOwner(),request.getServiceId().toByteArray()
                                        , request.getTime()
                                        , new ArrayList<>(request.getNodesList()));

        initiateServiceResponse response = initiateServiceResponse
                .newBuilder()
                .setResponse(true)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }





    @Override
    public void subscribe(subscribeRequest request, StreamObserver<subscribeResponse> responseObserver)
    {

        System.out.println("Received Subscribed");
        boolean res = auc.subscribe(request.getNode(),request.getServiceId().toByteArray());

        subscribeResponse response = subscribeResponse
                .newBuilder()
                .setResponse(res)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }


    @Override
    public void endService(endServiceRequest request, StreamObserver<endServiceResponse> responseObserver)
    {

        System.out.println("End Service");
        boolean resp = auc.endService(request.getServiceId().toByteArray(),request.getNode());

        System.out.println(request.getOf().getPrice());
        endServiceResponse response = endServiceResponse
                .newBuilder()
                .setResponse(resp)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }

    /*
    @Override
    public void communicateBiggest(communicateBiggestRequest request, StreamObserver<communicateBiggestResponse> responseObserver)
    {
        boolean resp = auc.communicateBiggest(request.getServiceId().toByteArray(),request.getOf());

        communicateBiggestResponse response = communicateBiggestResponse
                .newBuilder()
                .setResponse(resp)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }

    @Override
    public void timerOver(timerOverRequest request, StreamObserver<timerOverResponse> responseObserver)
    {
        Offer of = auc.timerOver(request.getServiceId().toByteArray(),request.getNode());

        timerOverResponse response = timerOverResponse
                .newBuilder()
                .setResponse(true)
                .setOf(of)
                .build();

        // Send the response to the client.
        responseObserver.onNext(response);

        // Notifies the customer that the call is completed.
        responseObserver.onCompleted();
    }

     */
}
