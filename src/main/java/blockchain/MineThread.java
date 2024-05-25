package blockchain;

import java.util.ArrayList;
public class MineThread implements Runnable
{
    Block bloco ;
    int difficulty;
    Blockchain bc ;
    MineThread(Block bloco, int difficulty,Blockchain bc)
    {
        this.difficulty = difficulty;
        this.bc = bc;
        this.bloco = bloco;
    }

    @Override
    public void run()
    {
        this.bloco.mineBlock(difficulty);
        Block b = bc.addBlock(this.bloco);
    }

}