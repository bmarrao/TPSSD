package blockchain;

import java.util.Date;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import java.nio.charset.StandardCharsets;

public class Block
{

    public String hash;
    public String previousHash;
    private long timestamp;
    private int nonce;

    //private ArrayList<Transaction> transactionList;

    //TODO : definir que dados serão estes, lista de transações?
    private String data;

    public Block(String previousHash, String data) {

        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.data = data;
        this.hash = calculateHash();
    }

    public String getHash() {
        return hash;
    }

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
        return applySha256(
                previousHash +
                        Long.toString(timestamp) +
                        Integer.toString(nonce) +
                        data
        );
    }

    // Mine the block using Proof-of-Work
    //TODO definir dificuldade
    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); // Create a string with difficulty * "0"

        while (!hash.startsWith(target)) { // Check if the hash has the required leading zero bits
            nonce++;
            hash = calculateHash(); // Recalculate the hash with the incremented nonce
        }

        System.out.println("Block mined: " + hash); // Block successfully mined
    }

}
