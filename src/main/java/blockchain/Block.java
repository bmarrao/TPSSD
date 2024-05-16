
package blockchain;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;

import kademlia.SignatureClass;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import java.nio.charset.StandardCharsets;

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

    public Block(String previousHash, ArrayList<Transaction> transactionList, int reputationScore) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.transactionList = transactionList;
        this.hash = calculateHash();
        this.reputationScore = reputationScore;

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

    public int getReputationScore() {
        return reputationScore;
    }

    public void setReputationScore(int score) {
        reputationScore = score;
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


    private void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("RSA");
        keyPairGen.initialize(2048);
        KeyPair pair = keyPairGen.generateKeyPair();
        this.publicKey = pair.getPublic();
        this.privateKey = pair.getPrivate();
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


    // TODO: why would a miner want to have higher reputation in our case
    /*public boolean isBlockValid(Block block, int difficulty, float repIncreasePercentage) {
        int currRepScore = block.getReputationScore();

        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        String bcLatestBlockHash = chain.get(chain.size() - 1).getHash();
        String previousBlockHash = block.getPreviousHash();
        if (!bcLatestBlockHash.equals(previousBlockHash) || !block.getHash().startsWith(target)) {
            block.setReputationScore(0);
            return false;
        }

        // Verify block signature
        // No RepuCoin:
        //   - keyblock sig_m = H(prev_KB_hash, nonce, PK, reputation=
        //   - sig microblock = Sign(H(KB_hash, prev_MB_hash, TXs))
        // Neste caso inclui assinatura do bloco completo
        byte[] infoToVerify = {
                Byte.parseByte(block.getPreviousHash()),
                (byte) block.getTimestamp(),
                (byte) block.getNonce(),
                //TODO como fica agora?
                //Byte.parseByte(block.getTransactionList()),
                Byte.parseByte(block.getHash()),
                (byte) block.getReputationScore()
        };

        try {
            if (!SignatureClass.verify(infoToVerify, block.getSignature(), publicKey.getEncoded())) {
                block.setReputationScore(0);
                return false;
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }


        // TODO:
        //  - decrease reputation and return false if:
        //     - transaction is null or transaction amount <= 0
        //     - sender/receiver/hash is null or empty
        //     - transaction amount > sender's funds
        ArrayList<Transaction> transactions = block.getTransactionList();


        // increases reputation based on defined percentage
        if (block.getReputationScore() != 0) {
            block.setReputationScore((int) ((currRepScore * repIncreasePercentage) + currRepScore));
        }
        else {
            block.setReputationScore((int) (0.01 + currRepScore));
        }
        return true;
    }*/
}
