package kademlia;

import java.util.ArrayList;

public class KrtBootStrap extends KademliaRoutingTable 
{
    public ArrayList<KademliaNode>allNodesList; 
    public KrtBootStrap (byte[] nodeId, KademliaProtocol protocol, int k)
    {
        super(nodeId, protocol, k);
        this.allNodesList = new ArrayList<KademliaNode>();
    }

    @Override
    public boolean insert(KademliaNode node)
    {
        lock.lock();
        this.allNodesList.add(node);
        TreeNode curr = root;
        System.out.println("My node = " + this.printId(this.myNodeId));
        System.out.println("Other Node = " + this.printId(node.nodeId));
        System.out.print("path = ");
        // Função recursiva que ira percorrer a arvore
        boolean ret = insertRec(node, curr,0, 7, 'd');
        lock.unlock();
        return ret;
    }

    // Função recursiva
    public boolean insertRec(KademliaNode node, TreeNode curr,int i,int j, char prevDir )
    {
        if (i < 20)
        {
            // Testa se tem um kbucket no node curr
            if (curr.kc >= 1)
            {
                if (hasObject(curr.kbucket,node))
                {
                    return false;
                }
                else {
                    // Testa se o node curr esta na capacidade maxima
                    if (curr.kbucket.size() >= this.k) {
                        // Testa se ele vem da direção que tem uma distancia mais perto do no
                        if (prevDir == 'd') {
                            //Como iremos expandir a arvore e criar dois novos buckets
                            //Marcamos o no atual como não tendo kbucket
                            curr.kc = 0;
                            // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                            curr.left = new TreeNode();
                            curr.left.createKBucket();
                            curr.right = new TreeNode();
                            curr.right.createKBucket();
                            // Agora vamos popular os novos buckets que criamos com os nos que estavam no anterios e adicionar o novo
                            if (j == 0) {
                                addToBuckets(curr.left, curr.right, curr.kbucket, node, i + 1, 7);

                            } else {
                                addToBuckets(curr.left, curr.right, curr.kbucket, node, i, j - 1);
                            }
                            // Depois disso marcamos o kbucket do no atual como null
                            curr.kbucket = null;
                            return true;
                        } else {
                            // Caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                            boolean adicionou = testLeastRecentlySeen(curr.kbucket, node);
                            if (!adicionou) {
                                if (j == 0) {
                                    addToBuckets(curr.left, curr.right, curr.kbucket, node, i + 1, 7);

                                } else {
                                    addToBuckets(curr.left, curr.right, curr.kbucket, node, i, j - 1);
                                }
                            }
                            return true;

                        }
                    } else {
                        //Adiciona o no a o kbucket
                        curr.kc++;
                        curr.kbucket.add(node);
                        return true;
                    }
                }
            }
            else
            {

                boolean direction = (((this.myNodeId[i]>> j ) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);

                // Testa se o no
                if (direction)
                {
                    System.out.print('d');
                    if (j == 0)
                    {
                        return insertRec(node, curr.right,i+1,7,'d');
                    }
                    else
                    {
                        return insertRec(node, curr.right,i,j-1,'d');

                    }
                }
                else
                {
                    System.out.print('e');
                    if (j == 0)
                    {
                        return insertRec(node, curr.left,i+1,7,'e');
                    }
                    else
                    {
                        return insertRec(node, curr.left,i,j-1,'e');

                    }
                }
            }
        }
        return false;
    }
    
}