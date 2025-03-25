/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author User
 */
import java.util.HashMap;
import java.util.List;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.GraphDatabase;

public class GraphModi {
    public GraphModi()
    {
    }
    //Add a node to the graph
    public void addNode(List<String> nodes)
    {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            for(int i=0;i<nodes.size();++i)
            {
                String []attr=nodes.get(i).split(",");

                try (Transaction tx = session.beginTransaction())
                {
                    HashMap<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("id", Integer.parseInt(attr[0]));    //: For attributed graph change here
                    //parameters.put("fet1", attr[1]);
                    //parameters.put("fet2", attr[2]);
                    //parameters.put("fet3", Double.parseDouble(attr[3]));    //: For attributed graph change here
                    //parameters.put("fet4", Double.parseDouble(attr[4]));
                    //parameters.put("fet5", Double.parseDouble(attr[5]));
                    //parameters.put("fet3", attr[3]);

                    //parameters.put("days", Integer.parseInt(attr[1]));
                    //parameters.put("mature", attr[2]);
                    //parameters.put("views", Integer.parseInt(attr[3]));
                    //parameters.put("partner", attr[4]);

                    //String statement="CREATE (u:Node{NodeId:{id}, Days:{days}, Mature:{mature}, Views:{views}, Partner:{partner}})";
                    //String statement="CREATE (u:Node{NodeId:{id}, ATT1:{fet1}, ATT2:{fet2}, ATT3:{fet3}, ATT4:{fet4}, ATT5:{fet5}})";
                    //String statement="CREATE (u:Node{NodeId:{id}, ATT1:{fet1}, ATT2:{fet2}, ATT3:{fet3}})";
                    //String statement="CREATE (u:Node{NodeId:{id}, ATT1:{fet1}, ATT2:{fet2}})";
                    //String statement="CREATE (u:Node{NodeId:{id}, ATT1:{fet1}})";
                    String statement="CREATE (u:Node{NodeId:{id}})";
                    tx.run(statement, parameters);
                    tx.success();  // Mark this write as successful.
                }
            }
            session.close();
        }
        catch (Exception e)
        {
            System.out.println("Error due to +"+e.getMessage());
        }
    }
    //Add a node to the graph
    public void delNode(int id)
    {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            try (Transaction tx = session.beginTransaction())
            {
                HashMap<String, Object> parameters = new HashMap<String, Object>();
                parameters.put("x", id);
                String statement="MATCH (u:Node {NodeId: {x}}) DELETE u";
                tx.run(statement, parameters);
                tx.success();  // Mark this write as successful.
            }

            session.close();
        }
    }

    //Delete all nodes from the graph
    public void delAllNode()
    {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            try (Transaction tx = session.beginTransaction())
            {
                String statement="MATCH (u:Node) DELETE u";
                tx.run(statement);
                tx.success();  // Mark this write as successful.
            }

            session.close();
        }
    }

    //Delete all links from the graph
    public void delAllLinks()
    {

        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            try (Transaction tx = session.beginTransaction())
            {
                String statement="MATCH ()-[rel:REL]->() DELETE rel";
                tx.run(statement);
                tx.success();  // Mark this write as successful.
            }

            session.close();
        }
    }


    //Add a link/relation to the graph
    public void addLink(List<Integer> srcs, List<Integer> dsts)
    {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            for(int i=0;i<srcs.size();++i)
            {
                try (Transaction tx = session.beginTransaction())
                {
                    HashMap<String, Object> parameters = new HashMap<String, Object>();
                    parameters.put("x", srcs.get(i));
                    parameters.put("y", dsts.get(i));

                    String statement="MATCH (u1:Node),(u2:Node) WHERE u1.NodeId = {x} AND u2.NodeId = {y} CREATE (u1)-[:REL]->(u2) CREATE (u2)-[:REL]->(u1)";
                    tx.run(statement, parameters);
                    tx.success();  // Mark this write as successful.
                }
            }

            session.close();
        }
    }
    //Add a link/relation to the graph
    public void removeGraph()
    {
        // Sessions are lightweight and disposable connection wrappers.
        try (Session session = Config.driver.session())
        {
            try (Transaction tx = session.beginTransaction())
            {
                String statement="MATCH(n) detach delete n";
                tx.run(statement);
                tx.success();  // Mark this write as successful.
            }

            session.close();
        }
    }

    //session.run(query, parameters)
    //HashMap<String, Object> parameters = new HashMap<String, Object>();
    //parameters.put("par1", "Jaykant");
    //parameters.put("par2", "Neo4j");
    //String t = "MERGE (n1:"+Node1+"{"+PersonNameAttribute+":{par1}})"+"-[:"+relationBetweenNode1andNode2+"]->(n2:" + Node2 +" {"+ProgrammingLanguageAttribute+": {par2}})";
    //session.run(t, parameters);
}
