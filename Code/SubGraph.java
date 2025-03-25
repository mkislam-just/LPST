/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class SubGraph {

    private List<Integer> Q = new ArrayList<>();        //Stores the neighbour nodes
    private List<Double> P = new ArrayList<>();         //Stores the probability score of a node
    private List<Integer> Visit = new ArrayList<>();    //Stores the visiting status of a node
    private List<Integer> Pop = new ArrayList<>();      //Stores the popularity
    private List<Integer> L = new ArrayList<>();        //Stores the path length
    private List<Integer> D=new ArrayList<>();

    public SubGraph()
    {

    }
    public List<Integer> getNeighbours() { return this.Q; }
    public List<Double> getProbabilityScores() { return this.P; }
    public List<Integer> getPopularity() { return this.Pop; }
    public List<Integer> getPathLengths() { return this.L; }
    public List<Integer> getNodeDegree() { return this.D; }

    public void extractSubgraph(int s,int d)
    {
        //s is considered as the root
        Q.add(s);
        P.add(1.0);
        Visit.add(1);
        Pop.add(1);
        L.add(0);
        D.add(0);


        int index=0;
        while(index>=0)
        {
            int A=Q.get(index);                                 //Pick a node, A from Q, A=Q[index]
            Visit.set(index, 2);                                  //Set the status of A to visited, Visit[A]=Visited
            double probabilityA=P.get(index);                   //retrive probability
            int pathA=L.get(index);                             //retrive the pathlength of A
            if((probabilityA>0.0)&&(pathA<Config.ML))
            {
                //System.out.println("Path: "+L.get(index));
                List<Integer> ng=GetInfoDB.getNeoghbours(A);            //Get the neighbour list A
                D.set(index,ng.size());                             //Set the node degree
                for(int i=0;i<ng.size();++i)                        //For each neighbour do
                {
                    int nn=ng.get(i);                               //Get the neighbour node
                    double costnn=this.computeCost(A,nn);       //compute the cost(A->n)
                    int in_nn=Q.indexOf(nn);                        //Get the index of neighbour node in queue, index=-1 means not found 
                    if(in_nn<0){                                     //If the node is not in queue then add it to queue{
                        if(probabilityA>costnn) {
                            Q.add(nn);                                  //Add the neighbour node to queue
                            Visit.add(1);                               //The node is just explored
                            Pop.add(1);                                 //Initialize the popularity of the node
                            L.add(pathA + 1);                             //Initialize the path length of the node
                            P.add(probabilityA - costnn);                 //compute the probability of Probability(A->n)
                            D.add(0);
                        }
                    }
                    else{                                            //If the node is in queue then check is it already visited?{
                        Pop.set(in_nn, Pop.get(in_nn)+1);           // //The node is added to queue and only popularity is increased
                        if(Visit.get(in_nn)==1){                     //If the node is just explored but not visited
                            double ptmp=P.get(in_nn);                   //Retrive the existing probability of nn
                            if(ptmp<(probabilityA-costnn)){              //If the new proposed probability is greater than existing probability
                                P.set(in_nn, probabilityA-costnn);      //Update the probability score with new probaility
                                //if(L.get(in_nn)>(pathA+1)) L.set(in_nn,pathA+1); //New path length is set to the shortest path
                                L.set(in_nn, pathA+1);              //update the new path
                            }
                        }

                    }

                }
            }

            index=this.getNextIndex(Visit);
        }
    }
    private int getNextIndex(List<Integer> visitList)
    {
        int in=-1;
        for(int i=0;i<visitList.size();++i)
        {
            if(visitList.get(i)==1) in=i;
        }
        return in;
    }

    //Write cost function here: For attributed graph change here
    private double computeCost(int A, int nn)
    {
        double cost=0.0;

        double ssim=this.StructureSim(A, nn);
        double asim=0.0;
        if(Config.beta<1.0)
            asim=this.AttributeSim(A, nn);
        cost=1-((Config.beta*ssim)+(1.0-Config.beta)*asim);

        //System.out.println("\nCost: "+cost);
        return cost;
    }

    //Compute the attribute similarity between two nodes
    public double AttributeSim(int nid1, int nid2)
    {
        double sim=0.0;
        List<String> allAttr_node1=GetInfoDB.searchNode(nid1);
        List<String> allAttr_node2=GetInfoDB.searchNode(nid2);

        List<Double> numAttr_node1=new ArrayList<>();   //Extract the numerical attributes of node 1
        List<Double> numAttr_node2=new ArrayList<>();   //Extract the numerical attributes of node 2
        List<String> catAttr_node1=new ArrayList<>();   //Extract the categorical attributes of node 1
        List<String> catAttr_node2=new ArrayList<>();   //Extract the categorical attributes of node 2
        //Parse attribute set for node 1
        for(int i=0;i<allAttr_node1.size();++i)
        {
            try
            {
                double v=Double.parseDouble(allAttr_node1.get(i));
                numAttr_node1.add(v);
            }
            catch(Exception e)
            {
                catAttr_node1.add(allAttr_node1.get(i));
            }
        }

        //Parse attribute set for node 2
        for(int i=0;i<allAttr_node2.size();++i)
        {
            try
            {
                double v=Double.parseDouble(allAttr_node2.get(i));
                numAttr_node2.add(v);
            }
            catch(Exception e)
            {
                catAttr_node2.add(allAttr_node2.get(i));
            }
        }

        //System.out.println("\nThe number of numerical attribute: "+numAttr_node2.size()+"\tCategorical attribute: "+catAttr_node2.size());

        double numDist=0.0;
        if(numAttr_node1.size()>0)
            numDist=this.numAttrDist(numAttr_node1, numAttr_node2);

        double catSim=0.0;
        if(catAttr_node1.size()>0)
            catSim=this.catAttrDist(catAttr_node1, catAttr_node2);

        //Set the attribute contribute factor
        int N=numAttr_node1.size()+catAttr_node1.size();
        if(N<1) N=1;
        double gamma1=(double)numAttr_node1.size()/(double)N;
        double gamma2=(double)catAttr_node1.size()/(double)N;

        //Get the final distance [0,1]
        sim=(gamma1*numDist)+(gamma2*catSim);

        return sim;
    }
    //Implement the cosine sim
    public double numAttrDist(List<Double> attr_set1, List<Double> attr_set2)
    {
        double dot=0.0, mod_set1=0.0, mod_set2=0.0;

        for(int i=0;i<attr_set1.size();++i)
        {
            double xi=attr_set1.get(i);
            double yi=attr_set2.get(i);
            mod_set1=mod_set1+Math.pow(xi, 2);
            mod_set2=mod_set2+Math.pow(yi, 2);

            dot=dot+(xi*yi);
        }
        double sim=dot/(Math.sqrt(mod_set1)*Math.sqrt(mod_set2));

        //Scalling the distance from a range of [-1,1] or normalized to [0,1]
        sim=(sim+1)/2;

        return sim;
    }
    //Implement the overlap sim
    public double catAttrDist(List<String> attr_set1, List<String> attr_set2)
    {
        //Compute the distance between categorical attributes
        double dot=0.0;

        for(int i=0;i<attr_set1.size();++i)
        {
            if(attr_set1.get(i).equals(attr_set2.get(i))) dot=dot+1;
        }
        //double sim=dot/(Math.sqrt(mod_set1)+Math.sqrt(mod_set2)-dot); //Implement the Tanimoto distance [0, 1]
        double sim=dot/attr_set1.size();

        return sim;
    }
    //Implement the structural sim algorithm
    public double StructureSim1(int nid1, int nid2)
    {
        double sim=0.0;
        List<Integer> ng1=GetInfoDB.getNeoghbours(nid1);
        List<Integer> ng2=GetInfoDB.getNeoghbours(nid2);
        try
        {
            sim=this.JASim(ng1, ng2);
        }
        catch(Exception e)
        {
            System.out.println("\nError in algoJA due to"+e.getMessage()+"\n");
        }

        return sim;
    }
    //Implement the structural sim algorithm
    public double StructureSim(int nid1, int nid2)
    {
        double sim=GetInfoDB.JASim(nid1,nid2);
        return sim;
    }
    //Implement the JA algorithm
    public double JASim(List<Integer> ng1, List<Integer> ng2)
    {
        double sim=0.0;
        try
        {
            List<Integer> unn=new ArrayList<>();
            List<Integer> cmn=new ArrayList<>();
            //Do union operation
            unn.addAll(ng1);
            unn.addAll(ng2);
            unn=unn.stream().distinct().collect(Collectors.toList());
            //Do intersaction operation
            cmn.addAll(ng1);
            cmn.retainAll(ng2);
            sim=(double)cmn.size()/(double)unn.size();
        }
        catch(Exception e)
        {
            System.out.println("\nError in algoJA due to"+e.getMessage()+"\n");
        }

        return sim;
    }
}