package kademlia;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;


// TODO Colocar LOCKS, Alterar nodeID para bytes - Breno
// Classe do no da arvore

class SortedArrayList<T> extends ArrayList<T> {
    private Comparator<T> comparator;

    public SortedArrayList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T element) {
        boolean result = super.add(element);
        Collections.sort(this, comparator);
        return result;
    }
}
class TreeNode
{
    //Variavel que guarda o kbucket caso exista , caso contrario tera o valor de null
    public SortedArrayList<KademliaNode> kbucket;
    //Variavel que diz se o no tem um kbucket, caso o valor seja zero não tem kbucket , caso contrario tem
    public int kc;
    //Continuação da arvore 
    public TreeNode left, right;

    // Inicialização da classe
    public TreeNode()
    {
        left = right = null;
        this.kc = 0;
    }
    //Criação de KBucket
    public void createKBucket()
    {
        this.kbucket = new SortedArrayList<>(Comparator.comparing(KademliaNode::getDateTime).reversed());
        this.kc = 1;
    }

}

public class KademliaRoutingTable
{
    public KademliaProtocol protocol;
    // Raiz da arvore
    TreeNode root;
    // Id do node ao qual pertence a arvore
    byte[] myNodeId;
    // Tamanho dos buckets
    int k;
    //Inicialização da classe
    public KademliaRoutingTable(byte[] nodeId, int k )
    {
        this.root = new TreeNode();
        this.root.createKBucket();
        // Colocar o proprio node this.insert(OWN NODE)
        this.myNodeId = nodeId;
        this.k = k;
        this.protocol = new KademliaProtocol();
    }

    //  Função que insere um no na arvore
    public void insert(KademliaNode node)
    {
        System.out.println("My node = " + printId(this.myNodeId));
        System.out.println("Other Node = " +printId(node.nodeId));
        TreeNode curr = root;
        Tuple resposta  = findClosestKbucket(curr, curr,node, 7,'d','r');
        if (resposta.found.kbucket.size() >= this.k)
        {
            // Testa se ele vem da direção que tem uma distancia mais perto do no
            if (resposta.direction =='d')
            {
                //Como iremos expandir a arvore e criar dois novos buckets
                //Marcamos o no atual como não tendo kbucket
                resposta.found.kc = 0;
                // Criamos um no novo a esquerda e a direita cada um deles com um kbucket
                resposta.found.left = new TreeNode();
                resposta.found.left.createKBucket();
                resposta.found.right = new TreeNode();
                resposta.found.right.createKBucket();
                // Agora vamos popular os novos buckets que criamos com os nos que estavam no anterios e adicionar o novo
                if (resposta.j == 0)
                {
                    this.addToBuckets(resposta.found.left, resposta.found.right, resposta.found.kbucket, node,resposta.i+1,7);

                }
                else
                {
                    this.addToBuckets(resposta.found.left, resposta.found.right, resposta.found.kbucket, node,resposta.i,resposta.j-1);
                }
                // Depois disso marcamos o kbucket do no atual como null
                resposta.found.kbucket = null;
                System.out.println("New Kbucket has size of " + resposta.found.left.kc + "And " + resposta.found.right.kc);
            }
            else
            {
                // Caso ele não venha da direção que está mais perto do proprio id e o bucket esta cheio ele tenta inserir no kbucket q ja existe
                testLeastRecentlySeen(resposta.found.kbucket, node);
            }
        }
        else
        {
            //Adiciona o no a o kbucket
            resposta.found.kc++;
            resposta.found.kbucket.add(node);
        }
        // Função recursiva que ira percorrer a arvore
    }

    // Todo testar
    // Função recursiva

    // TOdo testar
    // Adiciona os nodes que estão na variavel kbucket para os novos buckets criados de acordo com a distancia
    // em relação ao id do Node ao qual pertence a routing table
    private void addToBuckets(TreeNode left, TreeNode right, ArrayList<KademliaNode> kbucket,KademliaNode node, int i ,int j)
    {
        // Conta quantos nos estão indo na direção a direita
        int count_right = 0;
        // Percorre a lista dos nos no bucket
        for (KademliaNode BNode: kbucket)
        {

            boolean bit1 = ((myNodeId[i] >> j) & 1) == 1;
            boolean bit2 = ((BNode.nodeId[i] >> j) & 1) == 1;

            // Testa se o no
            if (bit1 == bit2)
            {
                right.kc++;
                right.kbucket.add(BNode);
                count_right++;
            }
            else
            {
                left.kc++;
                left.kbucket.add(BNode);
            }
        }
        // Testa se todos os nos estão indo para uma direção ou outra pois neste caso teremos que adicionar o novo no por um outro metodo
        boolean direction = (((myNodeId[i] >> j) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);
        if (count_right == this.k)
        {
            if (direction)
            {
                testLeastRecentlySeen(right.kbucket,node);
            }
            else
            {
                left.kc++;
                left.kbucket.add(node);
            }
        }
        else if (count_right == 0)
        {
            if (direction)
            {
                right.kc++;
                right.kbucket.add(node);
            }
            else
            {
                testLeastRecentlySeen(left.kbucket,node);
            }
        }
        // Se nenhum dos buckets esta cheio podemos so adicionar na direção correta
        else
        {
            if(direction)
            {
                left.kc++;
                left.kbucket.add( node);
            }
            else
            {
                right.kc++;
                right.kbucket.add( node);
            }
        }
    }

    // TOdo testar
    // Função que testa se o no visto pela ultima vez online ainda esta online e neste caso descarta a variavel 'node' caso contrario
    //Remove o no que foi visto pela ultima vez online e adiciona a variavel 'node' a o kbucket
    private void testLeastRecentlySeen(SortedArrayList<KademliaNode> kbucket, KademliaNode node)
    {
        int tamanho = kbucket.size();
        // Função que ira retornar o node ultimo visto no kbucket
        KademliaNode testPing = kbucket.get(tamanho-1);
        //boolean notActive =  this.protocol.ping(testPing,this.myNodeId)
        //TODO Ajeitar isso quando o protocol.ping tiver funcionando
        boolean notActive = true;
        // Ping least recently active node
        if (!notActive)
        {
            kbucket.remove(tamanho-1);
            kbucket.add(node);
        }
        else
        {
            testPing.setTime();
        }
    }

    private class Tuple
    {
        TreeNode found;
        char direction;
        TreeNode parent ;
        int i ;
        int j ;
        private Tuple(TreeNode found,TreeNode parent,int i , int j,char d)
        {
            this.found = found;
            this.parent = parent ;
            this.direction = d;
            this.i = i;
            this.j = j;
        }
    }
    private Tuple findClosestKbucket (TreeNode curr, TreeNode parent,KademliaNode node, int i, int j,char d)
    {
        System.out.print(d);

        if (i < 20)
        {
            // Testa se tem um kbucke
            if (curr.kc >= 1)
            {
                //System.out.println("Procurando num Map");
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return new Tuple(curr,parent,i,j,d);
            }
            else
            {
                boolean direction = (((myNodeId[i] >> j) & 1) == 1) == (((node.nodeId[i] >> j) & 1) == 1);
                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        return findClosestKbucket(curr.right,curr, node, i+1,7,'d');

                    }
                    else
                    {
                        return findClosestKbucket(curr.right,curr,node, i,j-1,'d');
                    }

                }
                else
                {
                    if (j == 0)
                    {
                        return findClosestKbucket(curr.left,curr, node, i+1,7,'e');
                    }
                    else
                    {
                        return findClosestKbucket(curr.left,curr,node, i,j-1,'e');
                    }
                }
            }
        }
        return null;
    }

    // TODO testar
    // Função para achar o node mais perto da variavel 'nodeId'
    private KademliaNode findClosestNode(byte[] nodeId)
    {
            // Testa se tem um kbucket
            if (this.root.kc >= 2) {
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return searchMapClosest(this.root.kbucket, nodeId);
            }
            else if (this.root.kc == 1)
            {
                return null;
            }
            //TODO CASO O curr.kc == 1 ou seja não tem elementos
            else
            {
                boolean direction = (((myNodeId[0] >> 7) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    return findClosestNodeRec(this.root.right, this.root, nodeId, 0,6,'d');

                }
                else
                {
                    return findClosestNodeRec(this.root.left, this.root, nodeId, 0,6,'e');
                }
            }// Chama a função recursiva para resolver o problema
    }

    //TODO Testar
    private KademliaNode findClosestNodeRec(TreeNode curr, TreeNode parent, byte[] nodeId, int i, int j,char d)
    {
        if (i < 20)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 2)
            {
                System.out.println("Procurando num Map");
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return searchMapClosest(curr.kbucket, nodeId);
            }
            else if (curr.kc == 1)
            {
                if(d == 'd')
                {
                    return findClosestNodeRec(parent.left,parent, nodeId, i,j,'e');
                }
                else
                {
                    return findClosestNodeRec(parent.right,parent, nodeId, i,j,'e');
                }
            }
            else
            {
                boolean direction = (((myNodeId[i] >> 7 ) & 1) == 1) == (((nodeId[0] >> 7) & 1) == 1);

                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (direction)
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.right,curr, nodeId, i+1,7,'d');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.right,curr,nodeId, i,j-1,'d');


                    }

                }
                else
                {
                    if (j == 0)
                    {
                        return findClosestNodeRec(curr.left,curr, nodeId, i+1,7,'e');

                    }
                    else
                    {
                        return findClosestNodeRec(curr.left,curr,nodeId, i,j-1,'e');


                    }
                }
            }
        }
        return null;
    }


    // TODO testar
    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    private KademliaNode searchMapClosest(ArrayList<KademliaNode> kbucket,byte[] nodeId)
    {
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
        System.out.println(kbucket);
        KademliaNode node = kbucket.get(0);
        BigInteger distance = calculateDistance(nodeId, node.nodeId);
        for (int i = 1 ; i <kbucket.size();i++)
        {
            KademliaNode bnode = kbucket.get(i);
            // Calcula a distancia relativamente ao no 'bnode'
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            BigInteger newDistance = calculateDistance(nodeId, bnode.nodeId);
            System.out.println(newDistance.compareTo(distance));
            // Caso seja menor guardamos como o node mais perto e a menor distancia
            if (newDistance.compareTo(distance) >= 0) {
                distance = newDistance;
                node = bnode;
            }
        }
        return node;
    }

    // TODO testar se isso funciona direito
    // Função que calcula a distancia de um no
    private BigInteger calculateDistance (byte[] node1, byte[] node2)
    {
        BigInteger distance = new BigInteger("0");
        for (int i = 0; i < 20; i++)
        {
            for (int j = 7 ; j >= 0 ; j--)
            {
                boolean bit1 = ((node1[i] >> j) & 1) == 1;
                boolean bit2 = ((node2[i] >> j) & 1) == 1;

                // Testa se o no
                if (bit1 != bit2)
                {
                    distance = distance.add(BigInteger.valueOf((long) Math.pow(2, 160 - i)));

                }
            }
        }
        return distance;
    }
    // Função que calcula o node visto pela ultima vez online em um kbucket


    public void printTree()
    {
        printTreeRec("Root",this.root,0);
    }

    private static void printTreeRec(String dir,TreeNode node, int depth)
    {

        if (node == null) {
            return;
        }

        if (node.kc >= 1)
        {
            /*
            System.out.println("Node with kbucket: {");
            for (Map.Entry<String, KademliaNode> entry : node.kbucket.entrySet()) {
                System.out.print("Key = " + new BigInteger(entry.getKey(), 2) +
                    ", Value = " + entry.getValue()+ ", ");
            */
                System.out.println("Direction " + dir+ "And depth " + depth+ " Kbucket with size "+ (node.kc-1));

            //System.out.println("}");
            //System.out.println("");
            // Recursively print left and right subtrees
            printTreeRec(dir+" r",node.right, depth + 1);
        }
        else
        {
            // Recursively print left and right subtrees
            printTreeRec(dir+" l",node.left, depth + 1);
            printTreeRec(dir+" r",node.right, depth + 1);
        }



    }


    public String printId(byte [] id)
    {
        String nodeId="";
        for (int j = 0 ; j < 20; j++)
        {
            byte b = id[j];
            for (int i = 7; i >= 0; i--) { // Start from the most significant bit (bit 7)
                // Extract the i-th bit using bitwise AND operation
                boolean bit = ((b >> i) & 1) == 1;
                // Print the bit value
                if (bit)
                {
                    nodeId = nodeId+"1";
                }
                else
                {
                    nodeId = nodeId +"0";
                }
            }
        }
        return nodeId;
    }
    public static void main(String[] args)
    {

        // Convert array of bits to bytes
        Kademlia kd = new Kademlia();
        KademliaRoutingTable  krt = new KademliaRoutingTable(kd.generateNodeId(),20 );
        //System.out.println(krt.findClosestKbucket(krt.root,krt.root, kd.generateNodeId(),0,7,'r'));
        byte teste = (byte)0b11110000;

        System.out.println(teste);
        String nodeId ="";
        for (int j = 7 ; j >= 0; j--)
        {
            boolean bit = ((teste>> j) & 1) == 1;
            // Print the bit value
            if (bit)
            {
                nodeId = nodeId+"1";
            }
            else
            {
                nodeId = nodeId +"0";
            }
        }
        System.out.println(nodeId);
        /*
        for (int i  = 0 ; i < 50; i++)
        {
            krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));
        }
        */
        //krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));

    }
}


