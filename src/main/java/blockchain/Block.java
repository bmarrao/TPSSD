
package blockchain;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;

import kademlia.Node;
import kademlia.SignatureClass;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import java.nio.charset.StandardCharsets;

import static kademlia.KademliaNode.reputation;

public class Block
{
    //TODO Needed?
    //private int index;

    public String hash;
    public String previousHash;
    private long timestamp;
    private int nonce;
    private int reputationScore;
    private ArrayList<Transaction> transactionList;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private byte[] signature;

    public Block(String previousHash, ArrayList<Transaction> transactionList)
    {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.transactionList = transactionList;
        this.hash = calculateHash();

        try {
            generateKeyPair();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        signBlockContent();
    }

    public String getHash() {
        return hash;
    }

    public int getReputation() { return reputation; }
    public void setReputation(int reputationScore) { reputation = reputationScore; }

    public String getPreviousHash() {
        return previousHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public ArrayList<Transaction> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(ArrayList<Transaction> transactionList) {
        this.transactionList = transactionList;
    }

    public byte[] getSignature() { return this.signature; }

    public PublicKey getPublicKey() { return this.publicKey; }

    public Transaction lookFor (byte[] serviceId, Node Owner)
    {
        for(Transaction t : transactionList)
        {
            if (compareId(serviceId,t.getServiceID().getBytes()) && Owner.equals(t.getReceiver()))
            {
                return t;
            }
        }
        return null;
    }
    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
    }
    private boolean compareId(byte[] id1, byte[] id2)
    {

        // Iterate through each byte and compare them
        for (int i = 0; i < id1.length; i++)
        {
            if (id1[i] != id2[i])
            {
                return false; // If any byte differs, return false
            }
        }


        // If all bytes are the same, return true
        return true;
    }


    public static byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }


    private void signBlockContent() {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(previousHash.getBytes(StandardCharsets.UTF_8));
            outputStream.write(transactionList.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.write(hexStringToByteArray(hash));
            outputStream.write(ByteBuffer.allocate(8).putLong(timestamp).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(nonce).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(reputationScore).array());

            this.signature = SignatureClass.sign(outputStream.toByteArray(), this.privateKey);
            outputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Method to apply SHA-256 hash
    private String applySha256(String input) {
        SHA256.Digest sha256 = new SHA256.Digest(); // Create a new SHA-256 digest with BouncyCastle

        byte[] hashBytes = sha256.digest(input.getBytes(StandardCharsets.UTF_8)); // Compute the hash

        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xFF & b); // Convert each byte to hexadecimal
            if (hex.length() == 1) hexString.append('0'); // Ensure 2-digit hexadecimal
            hexString.append(hex);
        }
        return hexString.toString(); // Return the full hash as a string
    }


    // Calculate the block's hash || almost a checksum
    public String calculateHash() {
        //return applySha256(previousHash + nonce + publicKeyStr + reputationScore);
        return applySha256(
            previousHash +
                    timestamp +
                    nonce +
                    //Base64.getUrlEncoder().withoutPadding().encodeToString(publicKey.getEncoded()) +
                    reputationScore +
                    transactionList.toString()
        );
    }

    // Add a transaction to the block
    public void addTransaction(Transaction transaction) {
        transactionList.add(transaction);
    }

    //TODO : Faz sentido? Validate transactions in the block
    /*public boolean validateTransactions() {
        // Implement validation logic here (e.g., check if transactions are valid)
        return true; // Placeholder, implement actual validation
    }*/

    // Mine the block using Proof-of-Work
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with difficulty * "0"

        while (!hash.startsWith(target)) { // Check if the hash has the required leading zero bits
            nonce++;
            hash = calculateHash(); // Recalculate the hash with the incremented nonce
        }

        System.out.println("Block mined: " + hash); // Block successfully mined
    }
}
