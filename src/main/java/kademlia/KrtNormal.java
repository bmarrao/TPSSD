package kademlia;

import java.util.ArrayList;

public class KrtNormal extends KademliaRoutingTable 
{
    public KrtNormal (byte[] nodeId, KademliaProtocol protocol, int k)
    {
        super(nodeId, protocol, k);

    }

    @Override
    public boolean insert(KademliaNode node)
    {
        lock.lock();
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
                            return (testLeastRecentlySeen(curr.kbucket, node));
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

    @Override
    public ArrayList<KademliaNode> findClosestNode(byte[] nodeId, int j, int a)
    {
        ArrayList<KademliaNode> nodos;
        lock.lock();
        // Testa se tem um kbucket
        if (this.root.kc >= 2) {
            // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
            nodos = searchMapClosest(this.root.kbucket, nodeId, a);
        }
        else if (this.root.kc == 1)
        {
            nodos = null;
        }
        else
        {
            boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

            // Caso contrario continua percorrendo a arvore e chamando a função recursiva
            if (direction)
            {
                nodos =findClosestNodeRec(this.root.right, this.root, nodeId, 0,6,'d',a);

            }
            else
            {
                nodos = findClosestNodeRec(this.root.left, this.root, nodeId, 0,6,'e',a);
            }
        }// Chama a função recursiva para resolver o problema
        lock.unlock();
        return nodos;
    }

    private ArrayList<KademliaNode> findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d, int a)
    {
        ArrayList<KademliaNode> nodes = null;
        if (i < 20)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 1)
            {
                System.out.println("Procurando num Map");
                nodes = searchMapClosest(curr.kbucket, nodeId,a);
                if (nodes.size() +1 < a)
                {
                    getNodesDown(parent.right,a,nodeId , nodes);
                }

            }
            else
            {
                boolean direction = (((myNodeId[i] >> j ) & 1) == 1) == (((nodeId[i] >> j) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        nodes = findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d',a);

                    }
                    else
                    {
                        nodes = findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d',a);


                    }

                }
                else
                {
                    if (j == 0)
                    {
                        nodes = findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e',a);

                    }
                    else
                    {
                        nodes = findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e',a);


                    }
                }
            }
        }
        if (nodes.size() +1 < a)
        {
            ArrayList<KademliaNode> nodos ;
            if (d == 'e')
            {
                nodos = searchMapClosest(parent.right.kbucket, nodeId,a-nodes.size());
            }
            else
            {
                nodos = searchMapClosest(parent.left.kbucket, nodeId,a-nodes.size());

            }
            nodes.addAll(nodos);

        }
        return nodes;
    }

    private void getNodesDown(TreeNode curr,int a,byte[] nodeId ,ArrayList<KademliaNode> nodes)
    {
        if (curr != null)
        {
            if (curr.kc >= 1)
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
            }
            else
            {
                ArrayList<KademliaNode> nodos = searchMapClosest(curr.left.kbucket, nodeId,a-nodes.size());
                nodes.addAll(nodos);
                if (nodes.size() +1 < a)
                {
                    getNodesDown(curr.right,a,nodeId, nodes);
                }
            }
        }
    }
}