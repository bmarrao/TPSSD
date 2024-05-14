
package blockchain;

import java.security.*;
import java.util.Base64;
import java.util.Date;

import kademlia.SignatureClass;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import java.nio.charset.StandardCharsets;

import static blockchain.Blockchain.blockchain;

public class Block
{

    public String hash;
    public String previousHash;
    private long timestamp;
    private int nonce;
    private int reputationScore;

    //private ArrayList<Transaction> transactionList;

    //TODO : definir que dados serão estes, lista de transações?
    private String data;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private byte[] signature;

    public Block(String previousHash, String data, int reputationScore) {
        this.previousHash = previousHash;
        this.timestamp = new Date().getTime();
        this.nonce = 0;
        this.data = data;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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


    private void signBlockContent() {
        byte[] infoToSign = {
                Byte.parseByte(previousHash),
                (byte) timestamp,
                (byte) nonce,
                Byte.parseByte(data),
                Byte.parseByte(hash),
                (byte) reputationScore
        };

        try {
            this.signature = SignatureClass.sign(infoToSign, this.privateKey);
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
                    Base64.getEncoder().encodeToString(publicKey.getEncoded()) +
                    reputationScore +
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


    // TODO: why would a miner want to have higher reputation in our case
    public boolean isBlockValid(Block block, int difficulty, float repIncreasePercentage) {
        int currRepScore = block.getReputationScore();

        // Verify previous block reference and that POW was done
        String target = new String(new char[difficulty]).replace('\0', '0');
        String bcLatestBlockHash = blockchain.get(blockchain.size() - 1).getHash();
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
                Byte.parseByte(block.getData()),
                Byte.parseByte(block.getHash()),
                (byte) block.getReputationScore()
        };

        try {
            if (!SignatureClass.verify(infoToVerify, block.getSignature(), Base64.getEncoder().encodeToString(publicKey.getEncoded()))) {
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
        String transaction = block.getData();


        // increases reputation based on defined percentage
        if (block.getReputationScore() != 0) {
            block.setReputationScore((int) ((currRepScore * repIncreasePercentage) + currRepScore));
        }
        else {
            block.setReputationScore((int) (0.01 + currRepScore));
        }
        return true;
    }
}
