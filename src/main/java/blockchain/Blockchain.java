
package blockchain;

import blockchain.Block;

import java.util.ArrayList;
import java.util.List;

public class Blockchain {
    private List<Block> blockchain;
    private int difficulty;

    // Constructor
    public Blockchain(int initialDifficulty) {
        this.blockchain = new ArrayList<>();
        this.difficulty = initialDifficulty;

        // Create the genesis block (first block in the blockchain)
        // TODO faz sentido criarmos um primeiro bloco assim que a blockchain arranca?
        /*
        Block genesisBlock = new Block("", "0", "Genesis Block");
        genesisBlock.mineBlock(difficulty); // Ensure the genesis block has valid proof-of-work
        chain.add(genesisBlock);*/
    }

    // Getters
    public List<Block> getChain() {
        return blockchain;
    }

    public Block getLatestBlock() {
        return blockchain.get(blockchain.size() - 1);
    }

    public int getDifficulty() {
        return difficulty;
    }

    // Method to add a new block to the blockchain
    public void addBlock(Block newBlock) {
        // Set the previous hash to the hash of the latest block
        newBlock = new Block(getLatestBlock().getHash(), newBlock.getData());
        newBlock.mineBlock(difficulty); // Ensure the block has valid proof-of-work
        blockchain.add(newBlock);
    }

    // Method to validate the blockchain
    public boolean isChainValid() {
        for (int i = 1; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);
            Block previousBlock = blockchain.get(i - 1);

            // Validate the block's hash
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                System.out.println("Block " + i + " has been tampered with!");
                return false; // Hash doesn't match, block may have been tampered with
            }

            // Validate the previous hash
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                System.out.println("Block " + i + " has incorrect previous hash!");
                return false; // The block's previous hash doesn't point to the correct previous block
            }
        }
        return true; // All checks passed, the blockchain is valid
    }
}
