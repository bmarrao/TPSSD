package kademlia;
import com.google.protobuf.ByteString;
import java.util.ArrayList;

public class KrtBootStrap extends KademliaRoutingTable 
{
    public KrtBootStrap (byte[] nodeId, KademliaProtocol protocol, int k, int s)
    {
        super(nodeId, protocol, k, s);
    }

    @Override
    public boolean insert(Node node, int valid)
    {
        KademliaNode kn = new KademliaNode(node.getIp(),node.getId().toByteArray(),node.getPort());
        lock.lock();
        TreeNode curr = root;
        boolean ret ;

        // actively valid
        if (valid == 1)
        {
            ret = insertRec(kn, curr,0, 7, 'd');

        }
        else // valid
        {
            if (n_diff32_prefix(myNodeId,kn.nodeId))
            {
                ret = insertRec(kn, curr,0, 7, 'd');
            }
            else
            {
                ret =false;
            }
        }
        // Função recursiva que ira percorrer a arvore
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
                else
                {
                    // Testa se o node curr esta na capacidade maxima
                    if (curr.kbucket.size() >= this.k)
                    {
                        // Testa se ele vem da direção que tem uma distancia mais perto do no
                        curr.kc = 0;
                        // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                        curr.left = new TreeNode();
                        curr.left.createKBucket();
                        curr.right = new TreeNode();
                        curr.right.createKBucket();
                        //caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                        boolean adicionou = testLeastRecentlySeen(curr.kbucket, node);
                        if (!adicionou)
                        {

                            if (j == 0) {
                                addToBuckets(curr.left, curr.right, curr.kbucket, node, i + 1, 7);

                            } else {
                                addToBuckets(curr.left, curr.right, curr.kbucket, node, i, j - 1);
                            }

                        }
                        curr.kbucket = null;
                        return true;
                    }
                    else
                    {
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