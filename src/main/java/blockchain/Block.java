
package blockchain;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import kademlia.Node;
import kademlia.Transaction;
import kademlia.grpcBlock;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import java.nio.charset.StandardCharsets;

import static kademlia.KademliaNode.reputation;

public class Block
{
    //TODO Needed?
    //private int index;
    private static final int TRANSACTIONS_LIMIT = 5;
    Node node;
    public byte[] hash;
    public Block previousBlock;
    public byte[] previousHash;
    private long timestamp;
    private int nonce;
    private ArrayList<Transaction> transactionList;
    private int reputationScore;
    private final byte[] signature;

    public Block(byte[] previousHash, ArrayList<Transaction> transactionList,Block previousBlock)
    {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.transactionList = transactionList;
        this.hash = calculateHash();
        this.signature = null;
        this.previousBlock = previousBlock;
    }
    public Block(grpcBlock grpcBlock)
    {
        this.previousHash = grpcBlock.getPrevHash().toByteArray();
        this.hash = grpcBlock.getCurrentHash().toByteArray();
        this.timestamp = grpcBlock.getTimestamp();
        this.nonce = grpcBlock.getNonce();
        this.transactionList = new ArrayList<>();
        for (Transaction t : transactionList)
        {
            this.transactionList.add(t);
        }
        this.nonce = grpcBlock.getNonce();
        this.signature = grpcBlock.getSignature().toByteArray();

    }

    public Node getNode()
    {
        return this.node;
    }

    public void setNode()
    {

    }
    public byte []getHash() {
        return hash;
    }
    public Block getPreviousBlock()
    {
        return this.previousBlock;
    }

    public void setPreviousBlock(Block prevBlock)
    {
        this.previousBlock = prevBlock;
    }
    public float getReputation() { return reputation; }
    public void setReputation(int reputationScore) { reputation = reputationScore; }

    public byte[] getPreviousHash() {
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


    public Transaction lookFor (byte[] serviceId, Node Owner)
    {
        for(Transaction t : transactionList)
        {
            if (compareId(serviceId,t.getId().toByteArray()) && Owner.equals(t.getOwner()))
            {
                return t;
            }
        }
        return null;
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

            outputStream.write(previousHash);
            outputStream.write(transactionList.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.write(hash);
            outputStream.write(ByteBuffer.allocate(8).putLong(timestamp).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(nonce).array());
            outputStream.write(ByteBuffer.allocate(4).putInt(reputationScore).array());

            outputStream.close();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }


    // Method to apply SHA-256 hash
    private byte[] applySha256(String input) {
        SHA256.Digest sha256 = new SHA256.Digest(); // Create a new SHA-256 digest with BouncyCastle

        byte[] hashBytes = sha256.digest(input.getBytes(StandardCharsets.UTF_8)); // Compute the hash

        /*
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xFF & b); // Convert each byte to hexadecimal
            if (hex.length() == 1) hexString.append('0'); // Ensure 2-digit hexadecimal
            hexString.append(hex);
        }
        */
        return hashBytes; // Return the full hash
    }


    // Calculate the block's hash || almost a checksum
    public byte[] calculateHash()
    {
        return applySha256(
               Arrays.toString(previousHash) +
                    timestamp +
                    nonce +
                    reputationScore +
                    transactionList.toString()
        );
    }

    // Add a transaction to the block
    public boolean addTransaction(Transaction transaction)
    {

        transactionList.add(transaction);
        return transactionList.size() == TRANSACTIONS_LIMIT;
    }

    //TODO : Faz sentido? Validate transactions in the block
    /*public boolean validateTransactions() {
        // Implement validation logic here (e.g., check if transactions are valid)
        return true; // Placeholder, implement actual validation
    }*/

    // Mine the block using Proof-of-Work
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with difficulty * "0"

        // Convert the byte array to a hex string
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        while (!hexString.toString().startsWith(target)) { // Check if the hash has the required leading zero bits
            nonce++;
            hash = calculateHash(); // Recalculate the hash with the incremented nonce
        }

        System.out.println("Block mined: " + hexString.toString()); // Block successfully mined
    }
}
