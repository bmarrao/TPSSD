package kademlia;

import javax.swing.tree.TreeNode;

import kademlia.KademliaNode;
import kademlia.KademliaProtocol;
import kademlia.KademliaRoutingTable;

public class KrtNormal extends KademliaRoutingTable 
{
    public KrtNormal (byte[] nodeId, KademliaProtocol protocol, int k)
    {
        super(nodeId, protocol, k);

    }

    @Override
    public void insert(KademliaNode node)
    {
        lock.lock();
        TreeNode curr = root;
        System.out.println("My node = " + this.printId(this.myNodeId));
        System.out.println("Other Node = " + this.printId(node.nodeId));
        System.out.print("path = ");
        // Função recursiva que ira percorrer a arvore
        insertRec(node, curr,0, 7, 'd');
        lock.unlock();
    }

    // Função recursiva
    public void insertRec(KademliaNode node, TreeNode curr,int i,int j, char prevDir )
    {
        if (i < 20)
        {
            // Testa se tem um kbucket no node curr
            if (curr.kc >= 1)
            {
                System.out.println("Kbucket has size of " + curr.kc);
                // Testa se o node curr esta na capacidade maxima
                if (curr.kbucket.size() >= this.k)
                {
                    // Testa se ele vem da direção que tem uma distancia mais perto do no
                    if (prevDir =='d')
                    {
                        //Como iremos expandir a arvore e criar dois novos buckets
                        //Marcamos o no atual como não tendo kbucket
                        curr.kc = 0;
                        // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                        curr.left = new TreeNode();
                        curr.left.createKBucket();
                        curr.right = new TreeNode();
                        curr.right.createKBucket();
                        // Agora vamos popular os novos buckets que criamos com os nos que estavam no anterios e adicionar o novo
                        if (j == 0)
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i+1,7);

                        }
                        else
                        {
                            this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i,j-1);
                        }
                        // Depois disso marcamos o kbucket do no atual como null
                        curr.kbucket = null;
                        System.out.println("New Kbucket has size of " + curr.left.kc + "And " + curr.right.kc);
                    }
                    else
                    {
                        // Caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                        testLeastRecentlySeen(curr.kbucket, node);
                    }
                }
                else
                {
                    //Adiciona o no a o kbucket
                    curr.kc++;
                    curr.kbucket.add(node);
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
                        insertRec(node, curr.right,i+1,7,'d');
                    }
                    else
                    {
                        insertRec(node, curr.right,i,j-1,'d');

                    }
                }
                else
                {
                    System.out.print('e');
                    if (j == 0)
                    {
                        insertRec(node, curr.left,i+1,7,'e');
                    }
                    else
                    {
                        insertRec(node, curr.left,i,j-1,'e');

                    }
                }
            }
        }
    }
}