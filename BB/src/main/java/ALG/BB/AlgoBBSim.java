/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ALG.BB;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author User
 */
public class AlgoBBSim {
    SubGraph grp;
    private GetInfoDB dbobj;
    List<Integer> Q1, Q2;       //Stores the neighbour nodes
    List<Double> P1, P2;        //Stores the probability score of a node
    List<Integer> Pop1, Pop2;   //Stores the popularity
    List<Integer> L1, L2;       //Stores the path length
    String output_directory="";
    String filename;
    public AlgoBBSim(String dir, String fl)
    {
        grp=new SubGraph();
        dbobj=new GetInfoDB();
        this.output_directory=dir;
        this.filename=fl;
    }

    //Implement the The Individual Attraction Index (IA)
    public double algoBB(int nid1, int nid2)
    {
        double score=0.0;
        System.out.println("\nExtracting subgraph for node 1");
        grp.extractSubgraph(nid1,nid2);      //Extract the first subgraph
        Q1=grp.getNeighbours();
        P1=grp.getProbabilityScores();
        Pop1=grp.getPopularity();
        L1=grp.getPathLengths();
        //Reset the structures first
        grp.resetStructures();
        
        System.out.println("\nExtracting subgraph for node 2");
        grp.extractSubgraph(nid2,nid1);      //Extract the second subgraph
        Q2=grp.getNeighbours();
        P2=grp.getProbabilityScores();
        Pop2=grp.getPopularity();
        L2=grp.getPathLengths();
        
        score=this.computeScore(nid1, nid2, Q1,Q2,P1,P2,Pop1,Pop2, L1, L2);
        return score;
    }
    
    public double computeScore(int nid1, int nid2, List<Integer> Q1,List<Integer> Q2, List<Double> P1,List<Double> P2, List<Integer> POP1,List<Integer> POP2,List<Integer> L1,List<Integer> L2)
    {
        System.out.println("\nComputing score");
        double score=0.0;
        
        List<Integer> Q=new ArrayList<>();
        List<Double> P=new ArrayList<>();
        List<Double> POP=new ArrayList<>();
        List<Double> L=new ArrayList<>();

        //Get the average propability for the common neighbours
        for(int i=0;i<Q1.size();++i)
        {
            int nd1=Q1.get(i);
            int in=Q2.indexOf(nd1);
            
            if(in>=0) 
            {
                Q.add(nd1);
                P.add((P1.get(i)+P2.get(in))/2.0);  //Average probability
                L.add((double)(L1.get(i)+L1.get(i))/2.0);  
                int deg=dbobj.getNeoghbours(nd1).size();
                double temp1=(double)Pop1.get(i)/(double)deg;                
                double temp2=(double)Pop2.get(in)/(double)deg;
                POP.add((temp1+temp2)/2.0);  //Average popularity
            }
        }
       
        double sim2=grp.AttributeSim(nid1, nid2);       //Attribute similarity
        
        
        double simQ=grp.JASim(Q1, Q2);
        int CN=Q.size(); //No of common neighbour
        double len=0.0;
        double simp=0.0, simpop=0.0;
        for(int i=0;i<CN;++i)
        {
            simp=simp+P.get(i);
            simpop=simpop+POP.get(i);
            len=len+L.get(i);
        }
        simp=simp/(double)CN;
        simpop=simpop/(double)CN;
        len=len/(double)CN;
        
        //score=Q.size()*Config.alpha1+avgp*Config.alpha2+avgl*Config.alpha3+avgpop*Config.alpha4;
        double sim1=simQ*Config.alpha1+simp*Config.alpha2+simpop*Config.alpha3; //Topological similarity
        
        score=Config.beta*sim1+(1-Config.beta)*sim2;
        
        try
        {
            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(output_directory+filename+"_"+"FET.csv",true));
            String row="\n"+Integer.toString(nid1)+","+Integer.toString(nid2)+","+Double.toString(simQ)+","+Double.toString(simp)+","+Double.toString(simpop)+","+Double.toString(len)+","+Double.toString(sim2);
            scoreFile.write(row);
            scoreFile.close();
        }
        catch(Exception e)
        {
            
        }
        
        return score;
    }
}