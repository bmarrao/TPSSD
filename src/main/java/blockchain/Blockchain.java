
package blockchain;

import blockchain.Block;

import java.util.*;

public class Blockchain
{
    public static List<Block> blockchain;
    private final int difficulty;

    // Constructor
    public Blockchain(int initialDifficulty) {
        Blockchain.blockchain = new ArrayList<>();
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
        newBlock = new Block(getLatestBlock().getHash(), newBlock.getData(), true, newBlock.getReputationScore());
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

