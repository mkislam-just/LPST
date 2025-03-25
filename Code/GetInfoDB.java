/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */

import org.neo4j.driver.v1.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;

import static org.neo4j.driver.v1.Values.parameters;

public class GetInfoDB{
    //Node(toInteger(NodeId) ,toInteger(Days) , toBoolean(Mature) ,toInteger(Views), toBoolean(Partner)
    //Search a node in the graph
    public static List<String> searchNode(int id)
    {
        List<String> attributes=new ArrayList<>();
        try ( Session session = Config.driver.session() )
        {
            Statement st = new Statement( "MATCH (g:Node{})WHERE g.NodeId={gid} RETURN g",parameters("gid",id));
            StatementResult result =session.run(st);
            while (result.hasNext())
            {
                Record record = result.next();
                attributes.add(record.get("g").get("NodeId").toString());   //: For attributed graph change here
                //attributes.add(record.get("g").get("ATT1").toString());
                //attributes.add(record.get("g").get("ATT2").toString());
                //attributes.add(record.get("g").get("ATT3").toString());
                //attributes.add(record.get("g").get("ATT4").toString());
                //attributes.add(record.get("g").get("ATT5").toString());
                //attributes.add(record.get("g").get("Partner").toString());
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in searchNode due to"+e.getMessage()+"\n");
        }
        return attributes;
    }

    //Search a relation between two nodes in the graph
    public static boolean searchRelation(int nid1, int nid2)
    {
        boolean hasRel=false;
        try ( Session session = Config.driver.session() )
        {
            //MATCH (:User { UserId: 1})-[r:FRIEND]->(:User { UserId: 131}) RETURN count(r)
            Statement st = new Statement("MATCH (:Node{NodeId:"+nid1+"})-[r:REL]->(:Node{NodeId:"+nid2+"}) RETURN count(r) as g");
            StatementResult result =session.run(st);
            while (result.hasNext())
            {
                Record record = result.next();
                if(record.get("g").asInt()>0) hasRel=true;
                break;
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in searchRelation due to"+e.getMessage()+"\n");
        }
        return hasRel;
    }
    //Count the number of relations
    public static long countRel(String rn)
    {
        long cnt=0;
        try ( Session session = Config.driver.session() )
        {
            //Statement st = new Statement( "MATCH (n:User{UserId:{x}})\n" +
            //"RETURN n.UserId as u", parameters("x",id));
            Statement st = new Statement( "MATCH ()-[r:"+rn+"]->() RETURN count(r) as C");
            StatementResult result =session.run(st);
            while (result.hasNext())
            {
                Record record = result.next();
                cnt=record.get("C").asLong();
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in countRel due to"+e.getMessage()+"\n");
        }
        return cnt;
    }
    //Add a node to the graph
    public static double JASim(int node1, int node2)
    {
        double val=0.0;
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session()) {
            try (Transaction tx = session.beginTransaction()) {
                HashMap<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("node1", node1);
                parameters.put("node2", node2);
                //parameters.put("days", Integer.parseInt(attr[1]));
                //parameters.put("mature", attr[2]);
                //parameters.put("views", Integer.parseInt(attr[3]));
                //parameters.put("partner", attr[4]);

                //String statement="CREATE (u:Node{NodeId:{id}, Days:{days}, Mature:{mature}, Views:{views}, Partner:{partner}})";
                String statement="MATCH (p1:Node {NodeId:{node1}})-[:REL]->(ng1)\n" +
                        "WITH collect(id(ng1)) AS ng1\n" +
                        "MATCH (p2:Node{NodeId:{node2}})-[:REL]->(ng2)\n" +
                        "WITH ng1, collect(id(ng2)) AS ng2\n" +
                        "RETURN algo.similarity.jaccard(ng1, ng2) AS similarity";
                tx.run(statement, parameters);
                StatementResult result =tx.run(statement, parameters);
                while (result.hasNext())
                {
                    Record record = result.next();
                    val=record.get("similarity").asDouble();
                }
                session.close();
            }
            session.close();
        }
        return  val;
    }
    //Get the list of nodes with id
    public static List<Integer> getAllNodes(String nodetype)
    {

        List<Integer> nodes=new ArrayList<>();
        try ( Session session = Config.driver.session() )
        {
            String st="MATCH (u:"+nodetype+") RETURN u";
            Statement stNeighbour=new Statement(st);
            StatementResult result =session.run(stNeighbour);
            while(result.hasNext())
            {
                Record record = result.next();
                nodes.add(record.get("u").get("UserId").asInt());
            }
            session.close();
        }
        catch(Exception e)
        {
            nodes=new ArrayList<>();
            System.out.println("\nError in getAllNodes due to"+e.getMessage()+"\n");
        }
        return nodes;
    }
    //Get the list of neighbours
    public static List<Integer> getNeoghbours(int nid)
    {
        List<Integer> neigbours=new ArrayList<>();
        try ( Session session = Config.driver.session() )
        {
            String st="MATCH (u:Node {NodeId: "+nid+"})-[:REL]->(n)\n"+
                    "RETURN n";
            Statement stNeighbour=new Statement(st);
            StatementResult result =session.run(stNeighbour);
            while(result.hasNext())
            {
                Record record = result.next();
                neigbours.add(record.get("n").get("NodeId").asInt());
            }

            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in getNeighbours due to"+e.getMessage()+"\n");
        }
        return neigbours;
    }
    //Get the list of common neoghbours
    public static List<Integer> getCommonNeoghbourstmp(int nid1, int nid2)
    {
        List<Integer> neigbours=new ArrayList<>();
        try ( Session session = Config.driver.session() )
        {
            String st="MATCH (u1:Node {NodeId: "+nid1+"})-[:REL]->(nbr)\n"+
                    "MATCH (u2:Node {NodeId: "+nid2+"})-[:REL]->(nbr)\n"+
                    "WHERE u1<>u2 \n"+
                    "WITH nbr WHERE nbr.NodeId IS NOT NULL \n"+
                    "RETURN nbr.NodeId as CN";
            Statement stNeighbour=new Statement(st);
            StatementResult result =session.run(stNeighbour);
            while (result.hasNext())
            {
                Record record = result.next();
//                System.out.println("\n The value is"+v);
                neigbours.add(record.get("CN").asInt());
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in getCommonNeighbours due to"+e.getMessage()+"\n");
        }
        return neigbours;
    }
    //Get the list of common neoghbours
    public static List<Integer> getCommonNeoghbours(int nid1, int nid2)
    {
        List<Integer> neighbours=new ArrayList<>();
        try
        {
            List<Integer> ng1=getNeoghbours(nid1);
            List<Integer> ng2=getNeoghbours(nid2);
            neighbours.addAll(ng1);

            neighbours.retainAll(ng2);
        }
        catch(Exception e)
        {
            System.out.println("\nError in getCommonNeighbours due to"+e.getMessage()+"\n");
        }

        LinkedHashSet<Integer> hashSet = new LinkedHashSet<>(neighbours);

        List<Integer> fneighbours = new ArrayList<>(hashSet);


        return fneighbours;
    }
    public static int getNodeCount()
    {
        int N=0;
        try ( Session session = Config.driver.session() )
        {
            String st="match (n:Node) return count(n) as total";
            Statement stNeighbour=new Statement(st);
            StatementResult result =session.run(stNeighbour);
            while (result.hasNext())
            {
                Record record = result.next();
                N=record.get("total").asInt();
            }
            session.close();
        }
        catch(Exception e)
        {
            System.out.println("\nError in getCommonNeighbours due to"+e.getMessage()+"\n");
        }
        return N;
    }
}