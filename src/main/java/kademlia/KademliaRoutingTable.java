package kademlia;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

// TODO Colocar LOCKS, Alterar nodeID para bytes - Breno
// Classe do no da arvore
class TreeNode
{
    //Variavel que guarda o kbucket caso exista , caso contrario tera o valor de null
    public Map<String,KademliaNode> kbucket;
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
        this.kbucket = new HashMap<String, KademliaNode>();
        this.kc = 1;
    }

}

public class KademliaRoutingTable
{
    public KademliaProtocol protocol;
    // Raiz da arvore
    TreeNode root;
    // Id do node ao qual pertence a arvore
    String myNodeId;
    // Tamanho dos buckets
    int k;
    //Inicialização da classe
    public KademliaRoutingTable(String nodeId, int k )
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
        TreeNode curr = root;
        // Função recursiva que ira percorrer a arvore
        insertRec(node, curr,159, 'd');
    }
    
    // Função recursiva
    public void insertRec(KademliaNode node, TreeNode curr,int i, char prevDir )
    {
        System.out.println(i);
        if (i >= 0)
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
                        this.addToBuckets(curr.left, curr.right, curr.kbucket, node,i++);
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
                    curr.kbucket.put(node.nodeId,node);
                }
            }
            else
            {
                // Testa se o no
                if (myNodeId.charAt(i) == node.nodeId.charAt(0))
                {
                    insertRec(node, curr.right,i-1,'d');
                }
                else
                {
                    insertRec(node, curr.left,i-1,'e');
                }
            }
        }
    }

    // Adiciona os nodes que estão na variavel kbucket para os novos buckets criados de acordo com a distancia
    // em relação ao id do Node ao qual pertence a routing table
    private void addToBuckets(TreeNode left, TreeNode right, Map<String,KademliaNode> kbucket,KademliaNode node, int i )
    {
        // Conta quantos nos estão indo na direção a direita
        int count_right = 0;
        // Percorre a lista dos nos no bucket
        for (KademliaNode BNode: kbucket.values())
        {
            // Testa se está indo na mesma direçao do proprio no
            if(BNode.nodeId.charAt(i) == this.myNodeId.charAt(i))
            {
                right.kc++;
                right.kbucket.put(BNode.nodeId, BNode);
                count_right++;
            }
            else
            {
                left.kc++;
                left.kbucket.put(BNode.nodeId, BNode);
            }
        }
        // Testa se todos os nos estão indo para uma direção ou outra pois neste caso teremos que adicionar o novo no por um outro metodo
        boolean direction = node.nodeId.charAt(i) == this.myNodeId.charAt(i);
        if (count_right == this.k)
        {
            if (direction)
            {
                testLeastRecentlySeen(right.kbucket,node);
            }
            else
            {
                left.kc++;
                left.kbucket.put(node.nodeId, node);
            }
        }
        else if (count_right == 0)
        {
            if (direction)
            {
                right.kc++;
                right.kbucket.put(node.nodeId, node);
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
                left.kbucket.put(node.nodeId, node);
            }
            else
            {
                right.kc++;
                right.kbucket.put(node.nodeId, node);
            }
        }
    }

    // Função que testa se o no visto pela ultima vez online ainda esta online e neste caso descarta a variavel 'node' caso contrario
    //Remove o no que foi visto pela ultima vez online e adiciona a variavel 'node' a o kbucket
    private void testLeastRecentlySeen(Map<String,KademliaNode> kbucket, KademliaNode node)
    {
        // Função que ira retornar o node ultimo visto no kbucket
        KademliaNode testPing = leastRecentlySeen(kbucket);
        //boolean notActive =  this.protocol.ping(testPing,this.myNodeId)
        boolean notActive = true;
        // Ping least recently active node
        if (!notActive)
        {
            kbucket.remove(testPing.nodeId);
            kbucket.put(node.nodeId, node);
        }
        else
        {
            testPing.setTime();
        }
    }



    // Função para achar o node mais perto da variavel 'nodeId'
    private KademliaNode findClosestNode(String nodeId)
    {
        // Chama a função recursiva para resolver o problema
        return findClosestNodeRec(this.root,nodeId, 159);
    }

    //
    private KademliaNode findClosestNodeRec(TreeNode curr, String nodeId, int i)
    {
        if (i < 160)
        {
            // Testa se tem um kbucket
            if (curr.kc >= 2)
            {
                System.out.println("Procurando num Map");
                // Neste caso pesquisa pela função 'searchMapClosest' o node mais perto
                return searchMapClosest(curr.kbucket, nodeId);
            }
            //TODO CASO O curr.kc == 1 ou seja não tem elementos
            else
            {
                // Caso contrario continua percorrendo a arvore e chamando a função recursiva
                if (nodeId.charAt(i) == this.myNodeId.charAt(i))
                {
                    return findClosestNodeRec(curr.right,nodeId, i++);

                }
                else
                {
                    return findClosestNodeRec(curr.left,nodeId, i++);
                }
            }
        }
        return null;
    }


    // Função que pesquisa o map pelo node mais perto da variavel 'nodeId'
    private KademliaNode searchMapClosest(Map<String,KademliaNode> kbucket,String nodeId)
    {
        // Iniciamos a distancia por 1
        // Iniciamos uma variavel para guardar o no mais perto para retorrmos
        // Percorremos a lista procurando a distancia mais perto
        System.out.println(kbucket);
        Collection<KademliaNode> values = kbucket.values();
        int size = values.size();
        KademliaNode[] array = values.toArray(new KademliaNode[values.size()]);
        KademliaNode node = array[0];
        BigInteger distance = calculateDistance(nodeId, node.nodeId);
        for (int i = 1 ; i <size;i++)
        {
            KademliaNode bnode = array[i];
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

    // Função que calcula a distancia de um no
    private BigInteger calculateDistance (String node1, String node2)
    {
        BigInteger distance = new BigInteger("0");
        for (int i = 0; i <= 159; i++)
        {
            if (node1.charAt(i) != node2.charAt(i))
            {
                distance = distance.add(BigInteger.valueOf((long) Math.pow(2, 160 - i)));
            }
        }
        return distance;
    }
    // Função que calcula o node visto pela ultima vez online em um kbucket
    private KademliaNode leastRecentlySeen(Map<String,KademliaNode> kbucket)
    {
        // Inicializamos uma variavel para retornar o valor do menor node
        KademliaNode menor = null;
        //Inicializamos uma data que ira ser maior q qualquer outra comparavel
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 5, 12, 30);

        // Percorremos os nos
        for (KademliaNode node : kbucket.values())
        {
            // Comparamos as datas
            if (dateTime.isAfter(node.time))
            {
                menor = node;
                dateTime = node.time;
            }
        }
        // Retornamos os nos
        return menor;
    }

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
    public static void main(String[] args)
    {
        Kademlia kd = new Kademlia();
        KademliaRoutingTable  krt = new KademliaRoutingTable(kd.generateNodeId(),20 );
        System.out.println(kd.generateNodeId());
        for (int i  = 0 ; i < 2000; i++)
        {
            krt.insert(new KademliaNode("localhost",kd.generateNodeId(),5000));
        }
        krt.printTree();
    }
}


