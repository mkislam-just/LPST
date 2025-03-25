import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
public class AlgoBBBackup {

    private GetInfoDB dbobj;
    List<Integer> Q1, Q2;       //Stores the neighbour nodes
    List<Double> P1, P2;        //Stores the probability score of a node
    List<Integer> Pop1, Pop2;   //Stores the popularity
    List<Integer> L1, L2;       //Stores the path length
    List<Integer> D1, D2;       //Stores the node degree
    String dataset;
    int fold=0;
    public AlgoBBBackup(String ds, int fold)
    {
        dbobj=new GetInfoDB();
        this.dataset=ds;
        this.fold=fold;
    }

    public double algoBB(int nid1, int nid2, String pn)
    {
        SubGraph grp=new SubGraph();
        SubGraph grp2=new SubGraph();

        double score=0.0;
        //System.out.println("\nExtracting subgraph for node 1");
        grp.extractSubgraph(nid1,nid2);      //Extract the first subgraph
        Q1=grp.getNeighbours();
        P1=grp.getProbabilityScores();
        Pop1=grp.getPopularity();
        L1=grp.getPathLengths();
        D1=grp.getNodeDegree();
        //Reset the structures first
        //grp.resetStructures();

        //System.out.println("\nExtracting subgraph for node 2");
        grp2.extractSubgraph(nid2,nid1);      //Extract the second subgraph
        Q2=grp2.getNeighbours();
        P2=grp2.getProbabilityScores();
        Pop2=grp2.getPopularity();
        L2=grp2.getPathLengths();
        D2=grp2.getNodeDegree();

        //System.out.println(Q1);
        //System.out.println(Q2);

        score=this.computeScore(nid1, nid2, Q1,Q2,P1,P2,Pop1,Pop2, L1, L2,D1,D2,pn);
        return score;
    }

    public double computeScore(int nid1, int nid2, List<Integer> Q1,List<Integer> Q2, List<Double> P1,List<Double> P2, List<Integer> POP1,List<Integer> POP2,List<Integer> L1,List<Integer> L2,List<Integer> D1,List<Integer> D2,String pn)
    {
        SubGraph grp=new SubGraph();

        System.out.println("\nComputing score");
        double score=0.0;

        List<Integer> Q=new ArrayList<>();
        List<Double> P=new ArrayList<>();
        List<Double> POP=new ArrayList<>();
        List<Double> L=new ArrayList<>();

        for(int i=0;i<Q1.size();++i)
        {
            //System.out.println("\n"+Q1.get(i)+"\t"+L1.get(i));
        }
        for(int i=0;i<Q2.size();++i)
        {
            //System.out.println("\n"+Q2.get(i)+"\t"+L2.get(i));
        }
        //Get the average propability for the common neighbours
        for(int i=0;i<Q1.size();++i)
        {
            int nd1=Q1.get(i);
            int deg=D1.get(i);
            int in=Q2.indexOf(nd1);

            if(in>=0)
            {
                Q.add(nd1);
                P.add((P1.get(i)+P2.get(in))/2.0);  //Average probability
                L.add((double)(L1.get(i)+L2.get(in))/2.0);
                //int deg=dbobj.getNeoghbours(nd1).size();
                double temp1=(double)Pop1.get(i)/(double)deg;
                double temp2=(double)Pop2.get(in)/(double)deg;
                POP.add((temp1+temp2)/2.0);  //Average popularity
            }
        }

        double asim=0.0;       //Attribute similarity
        //asim=grp.AttributeSim(nid1, nid2);       //Attribute similarity


        double simQ=grp.JASim(Q1, Q2);
        int CN=Q.size(); //No of common neighbour
        double simlen=0.0;
        double simp=0.0, simpop=0.0;
        //System.out.println("The no of common neighbours: "+CN);
        for(int i=0;i<CN;++i)
        {
            //System.out.println("\n"+Q.get(i)+"\t"+L.get(i));
            simp=simp+P.get(i);
            simpop=simpop+POP.get(i);
            simlen=simlen+L.get(i);
            //System.out.println(L.get(i));
        }
        if(CN>0) {
            simp = simp / (double) CN;
            simpop = simpop / (double) CN;
            simlen = simlen / (double) CN;
        }
        simpop=simpop/(1+simpop); //the more is good
        simlen=simlen/(double)Config.ML; //the more is bad

        //score=Q.size()*Config.alpha1+avgp*Config.alpha2+avgl*Config.alpha3+avgpop*Config.alpha4;
        double ssim=simQ*Config.alpha1+simp*Config.alpha2+simpop*Config.alpha3+simlen*Config.alpha4; //Topological similarity

        score=Config.beta*ssim+(1-Config.beta)*asim;

        boolean isdest=false;
        if(Q.contains(nid1)||Q.contains(nid2)) isdest=true;


        try
        {
            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(Config.outputdir+dataset+"_Test_"+pn+fold+"_FET.csv",true));
            String row="\n"+Integer.toString(nid1)+","+Integer.toString(nid2)+","+Double.toString(simQ)+","+Double.toString(simp)+","+Double.toString(simpop)+","+Double.toString(simlen)+","+Double.toString(asim)+","+Boolean.toString(isdest)+","+Integer.toString(Q1.size())+","+Integer.toString(Q2.size());
            scoreFile.write(row);
            scoreFile.close();
        }
        catch(Exception e)
        {

        }

        return score;
    }
}