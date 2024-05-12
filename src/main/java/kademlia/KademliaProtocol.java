package kademlia;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import kademlia.Offer;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;


public class KademliaProtocol
{
    public byte [] nodeId;
    public String ipAddress;
    public int port;
    public PublicKey publicKey;
    public PrivateKey privateKey;
    public byte[] cryptoPuzzleSol;

    public KademliaProtocol(byte[] nodeId, String ipAddress, int port, PublicKey publicKey, PrivateKey privateKey, byte[] cryptoPuzzleSol)
    {
        this.nodeId = nodeId;
        this.ipAddress = ipAddress;
        this.port = port;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
        this.cryptoPuzzleSol = cryptoPuzzleSol;
    }


    // Create signature for specific contect from RPC message
    public static byte[] sign(byte[] data, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(data);
        return signature.sign();
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


    public boolean pingOp(byte[] nodeId, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        // TODO O ip aqui , não está errado ?
        Node node = Node.newBuilder()
            .setId(ByteString.copyFrom(nodeId))
            .setIp(ipAddress)
            .setPort(port).build();

        // Sign node content
        byte[] signature = null;
        try {
            signature = sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        PingRequest request = PingRequest.newBuilder()
                .setNode(node)
                .setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature))
                .setCryptoPuzzle(ByteString.copyFrom(cryptoPuzzleSol)).build();

        // Receive response
        PingResponse response = stub.ping(request);

        // Check signature
        boolean signVal = false;
        try {
            signVal = verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            return response.getOnline();
        }
        return false;
    }

    /*

//TIREI NODEID DA DECLARACAO - QUERO ALTERAR KEY PARA BYTE[]
    public boolean storeOp(String key, String val, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                //.setId(ByteString.copyFrom(nodeId)) ALTEREI ISSO
                .setId(ByteString.copyFrom(this.nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .build();

        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + 2];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        infoToSign[nodeInfoToSign.length] = Byte.parseByte(key);
        infoToSign[nodeInfoToSign.length+1] = Byte.parseByte(val);

        byte[] signature = null;
        try {
            signature = sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        StoreRequest request = StoreRequest.newBuilder()
                .setNode(node)
                .setKey(key)
                .setValue(val)
                .setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature))
                .setCryptoPuzzle(ByteString.copyFrom(cryptoPuzzleSol)).build();

        StoreResponse response = stub.store(request);


        // Check signature
        boolean signVal = false;
        try {
            signVal = verify(response.getId().toByteArray(), response.getSignature().toByteArray(), response.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            return response.getStored();
        }
        return false;
    }



     */

    public KademliaFindOpResult findNodeOp(byte[] nodeId, byte[] key, String receiverIp, int receiverPort)
    {

        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();


        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port).build();

        byte[] signature = null;
        try {
            signature = sign(node.toByteArray(), privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Signature: " + Arrays.toString(signature));

        FindNodeRequest request = FindNodeRequest.newBuilder()
                .setNode(node)
                .setKey(ByteString.copyFrom(key))
                .setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature))
                .setCryptoPuzzle(ByteString.copyFrom(cryptoPuzzleSol)).build();

        FindNodeResponse response = stub.findNode(request);

        boolean idSignVal = false;
        boolean nodesSignVal = false;
        try {
            idSignVal = verify(response.getId().toByteArray(), response.getIdSignature().toByteArray(), response.getPublicKey());
            for (int i = 0; i < response.getNodesList().size(); i++) {
                if (verify(response.getNodesList().get(i).toByteArray(), response.getNsList().get(i).toByteArray(), response.getPublicKey())) {
                    nodesSignVal = true;
                }
                else {
                    break;
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (idSignVal && nodesSignVal) {
            return new KademliaFindOpResult(response.getId().toByteArray(), "", response.getNodesList());
        }
        return null;
    }

    /*
    TODO UNCOMMENT AND FIX
    public KademliaFindOpResult findValueOp(byte[] nodeId, String key, String receiverIp, int receiverPort) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(receiverIp, receiverPort).usePlaintext().build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .build();

        byte[] nodeInfoToSign = node.toByteArray();
        byte[] infoToSign = new byte[nodeInfoToSign.length + 1];
        System.arraycopy(nodeInfoToSign, 0, infoToSign, 0, nodeInfoToSign.length);
        infoToSign[nodeInfoToSign.length] = Byte.parseByte(key);

        byte[] signature = null;
        try {
            signature = sign(infoToSign, privateKey);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        FindValueRequest request = FindValueRequest.newBuilder()
                .setNode(node)
                .setKey(key)
                .setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()))
                .setSignature(ByteString.copyFrom(signature))
                .setCryptoPuzzle(ByteString.copyFrom(cryptoPuzzleSol)).build();

        FindValueResponse response = stub.findValue(request);

        boolean signVal = false;
        try {
            signVal = verify(response.getValue().getBytes(), response.getSignature().toByteArray(), response.getPublicKey());
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        if (signVal) {
            return new KademliaFindOpResult(response.getId().toByteArray(), response.getValue(), new ArrayList<>());
        }
        return null;
    }


*/

    public boolean subscribe(byte[] serviceId, Node n )
    {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort()).usePlaintext().build();

        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
                .build();

        KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);


        subscribeRequest request = subscribeRequest.newBuilder()
                .setNode(node)
                .setServiceId(ByteString.copyFrom(serviceId)).build();

        subscribeResponse response = stub.subscribe(request);

        return response.getResponse();

    }

    public void notifySubscribed(ArrayList<Node> subscribed, Offer highestOffer, byte[] serviceId) {
        for (Node n : subscribed) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(n.getIp(), n.getPort())
                    .usePlaintext()
                    .build();
            KademliaGrpc.KademliaBlockingStub stub = KademliaGrpc.newBlockingStub(channel);

            NotifyRequest request = NotifyRequest.newBuilder()
                    .setNode(n)
                    .setPrice(highestOffer.getPrice())
                    .setServiceId(ByteString.copyFrom(serviceId))
                    .build();

            stub.notify(request);


            // Send the request asynchronously

        }
    }





    public float getPrice(ArrayList<Node> selectedBrokers ,byte[] serviceId)
    {
        float price = -1;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
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
    /*
    public boolean initiateService(Node owner, byte[] serviceId, ArrayList<Node> brokerlist, int time)
    {

        boolean result = false;
        Node node = Node.newBuilder()
                .setId(ByteString.copyFrom(nodeId))
                .setIp(ipAddress)
                .setPort(port)
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