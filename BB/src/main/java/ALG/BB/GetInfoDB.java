/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ALG.BB;

/**
 *
 * @author User
 */
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Statement;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.GraphDatabase;
import java.util.*;
import java.util.List;
import static org.neo4j.driver.v1.Values.parameters;

public class GetInfoDB implements AutoCloseable{
    private final Driver driver;
    public GetInfoDB()
    {
        driver = GraphDatabase.driver( Config.uri, AuthTokens.basic( Config.user, Config.password ) );
    }
    @Override
    public void close() throws Exception
    {
        driver.close();
    }
    
   //Node(toInteger(NodeId) ,toInteger(Days) , toBoolean(Mature) ,toInteger(Views), toBoolean(Partner)
    //Search a node in the graph
    public List<String> searchNode(int id)
    {
        List<String> attributes=new ArrayList<>();
        try ( Session session = driver.session() )
        {
            Statement st = new Statement( "MATCH (g:Node{})WHERE g.NodeId={gid} RETURN g",parameters("gid",id));
            StatementResult result =session.run(st);
            while (result.hasNext())
            {
                Record record = result.next();
                //attributes.add(record.get("g").get("NodeId").toString());
                attributes.add(record.get("g").get("Days").toString());
                attributes.add(record.get("g").get("Mature").toString());
                attributes.add(record.get("g").get("Views").toString());
                attributes.add(record.get("g").get("Partner").toString());
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
    public boolean searchRelation(int nid1, int nid2)
    {
        boolean hasRel=false;
        try ( Session session = driver.session() )
        {
            //MATCH (:User { UserId: 1})-[r:FRIEND]->(:User { UserId: 131}) RETURN count(r)
            Statement st = new Statement("MATCH (:Node{NodeId:"+nid1+"})-[r:LINK]->(:Node{NodeId:"+nid2+"}) RETURN count(r) as g");
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
    public long countRel(String rn)
    {
        long cnt=0;
        try ( Session session = driver.session() )
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
    //Get the list of nodes with id
    public List<Integer> getAllNodes(String nodetype)
    {
        
        List<Integer> nodes=new ArrayList<>();
        try ( Session session = driver.session() )
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
    public List<Integer> getNeoghbours(int nid)
    {
        List<Integer> neigbours=new ArrayList<>();
        try ( Session session = driver.session() )
        {
            String st="MATCH (u:Node {NodeId: "+nid+"})-[:LINK]->(n)\n"+
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
    public List<Integer> getCommonNeoghbourstmp(int nid1, int nid2)
    {
        List<Integer> neigbours=new ArrayList<>();
        try ( Session session = driver.session() )
        {
            String st="MATCH (u1:Node {NodeId: "+nid1+"})-[:LINK]->(nbr)\n"+
                    "MATCH (u2:Node {NodeId: "+nid2+"})-[:LINK]->(nbr)\n"+
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
    public List<Integer> getCommonNeoghbours(int nid1, int nid2)
    {
        List<Integer> neighbours=new ArrayList<>();
        try
        {
            List<Integer> ng1=this.getNeoghbours(nid1);
            List<Integer> ng2=this.getNeoghbours(nid2);
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
}