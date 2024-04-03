package kademlia;
import com.google.protobuf.ByteString;
import java.util.ArrayList;

public class KrtBootStrap extends KademliaRoutingTable 
{
    public KrtBootStrap (byte[] nodeId, KademliaProtocol protocol, int k)
    {
        super(nodeId, protocol, k);
    }

    @Override
    public boolean insert(Node node)
    {
        KademliaNode kn = new KademliaNode(node.getIp(),node.getId().toByteArray(),node.getPort());
        lock.lock();
        TreeNode curr = root;
        // Função recursiva que ira percorrer a arvore
        boolean ret = insertRec(kn, curr,0, 7, 'd');
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
                            curr.kbucket = null;

                        }
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

    @Override
    public ArrayList<Node> findClosestNode(byte[] nodeId,int a)
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
        ArrayList<Node> ret = new ArrayList<Node>();
        for (KademliaNode kn : nodos )
        {
            Node.Builder nd = Node.newBuilder();
            nd.setIp(kn.ipAdress);
            nd.setPort(kn.port);
            nd.setId(ByteString.copyFrom(kn.nodeId));
            ret.add(nd.build());
        }
        return ret;
    }

    private ArrayList<KademliaNode> findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d, int a)
    {
        ArrayList<KademliaNode> nodes = null;
        if (i < 20)
        {
            boolean direction = (((myNodeId[i] >> j ) & 1) == 1) == (((nodeId[i] >> j) & 1) == 1);
            // Testa se tem um kbucket
            if (curr.kc >= 1)
            {
                nodes = searchMapClosest(curr.kbucket, nodeId,a);
                if (nodes.size() +1 < a)
                {
                    if (d=='d')
                    {
                        ArrayList<KademliaNode> nodos = searchMapClosest(parent.left.kbucket, nodeId,a-nodes.size());
                        nodes.addAll(nodos);
                    }
                    else
                    {
                        ArrayList<KademliaNode> nodos = searchMapClosest(parent.right.kbucket, nodeId,a-nodes.size());
                        nodes.addAll(nodos);
                    }
                }

            }
            else
            {
                nodes = testDirection(direction,curr,nodeId,i,j,d,a );
                if(nodes != null )
                {
                    if (nodes.size() +1 < a)
                    {
                        ArrayList<KademliaNode> nodos ;
                        nodos = testDirection(!direction,parent,nodeId,i,j,d,a );
                        if (nodos != null)
                        {
                            nodes.addAll(nodos);
                        }

                    }
                }
                else
                {
                        ArrayList<KademliaNode> nodos ;
                        nodos = testDirection(!direction,parent,nodeId,i,j,d,a );
                        if (nodos != null)
                        {
                            nodes.addAll(nodos);
                        }


                }
                // Caso contrario continua percorrendo a arvore e chamando a função recursiva

            }
        }
        return nodes;
    }

    private ArrayList<KademliaNode> testDirection (boolean direction, TreeNode curr, byte[] nodeId, int i, int j,char d, int a)
    {
        if (direction)
        {
            if (j == 0)
            {
                return findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d',a);

            }
            else
            {
                return findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d',a);


            }

        }
        else
        {
            if (j == 0)
            {
                return findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e',a);

            }
            else
            {
                return  findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e',a);


            }
        }
    }


}