
package blockchain;

import java.util.*;

public class Blockchain
{
    private List<Block> chain;
    private final int difficulty;

    // Constructor
    public Blockchain(int initialDifficulty) {
        this.chain = new ArrayList<>();
        this.difficulty = initialDifficulty;
        Block genesis = createGenesisBlock();
        this.chain.add(genesis);
    }

    // Create the genesis block
    private Block createGenesisBlock() {
        // Implement logic to create the first block in the blockchain
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        Block genesisBlock = new Block("", transactions, 0);
        // Ensure the genesis block has valid proof-of-work
        genesisBlock.mineBlock(difficulty);

        return genesisBlock;
    }


    // Getters
    public List<Block> getChain() {
        return chain;
    }

    public Block getLatestBlock() {
        return chain.get(chain.size() - 1);
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void newBid()
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }
    public void newAuction()
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }

    public void closeAuction()
    {
        // Adicionar no bloco
        // criar uma instancia de uma transaction
        // disseminar transaction
    }

    // Method to add a new block to the blockchain
    public void addBlock(Block newBlock) {
        // Set the previous hash to the hash of the latest block
        newBlock.mineBlock(difficulty); // Ensure the block has valid proof-of-work
        chain.add(newBlock);
    }

    // Method to validate the blockchain
    public boolean isChainValid() {
        for (int i = 1; i < chain.size(); i++) {
            Block currentBlock = chain.get(i);
            Block previousBlock = chain.get(i - 1);

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


    // Used by consensus group to verify validity of keyblocks and then pin one of them, then choose a new leader
    /*
    public void pinKeyblock(ArrayList<Block> keyblocks, int minReputation) {
        // TODO: verificar a validade da hash e da reputationScore
        for (Block keyblock : keyblocks) {
            if (!isHashValid(keyblock.getHash()) || !(keyblock.getReputationScore() >= minReputation)) {
                keyblocks.remove(keyblock);
                keyblock.setReputationScore(-100);
            }
        }

        // TODO: pin one of the keyblocks
        Block pinnedKeyblock = null;

        // miner que criou o keyblock que foi pinned recebe um reward
        pinnedKeyblock.setReputationScore(100);

        // escolher aleatoriamente novo lider do CG
        Random random = new Random(0);
        int randomIndex = random.nextInt(consensusGroup.size());
        String leader = consensusGroup.get(randomIndex);
    }
    */
}

