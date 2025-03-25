import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class NodeSubGen {
    String dataset;
    int fold=0;
    public NodeSubGen(String ds, int fold)
    {
        this.dataset=ds;
        this.fold=fold;
    }

    //Implement the The Individual Attraction Index (IA)
    public void nodeSubgraph(int nid)
    {
        SubGraph grp=new SubGraph();
        //System.out.println("\nExtracting subgraph for node 1");
        grp.extractSubgraph(nid,-1);      //Extract the first subgraph
        List<Integer> Q=grp.getNeighbours();
        List<Double> P=grp.getProbabilityScores();
        List<Integer> Pop=grp.getPopularity();
        List<Integer> L=grp.getPathLengths();
        List<Integer> D=grp.getNodeDegree();
        try
        {
            BufferedWriter sgfile=new BufferedWriter(new FileWriter(Config.outputdir+dataset+"_Test_"+fold+"_NSG.txt",true));
            String line="\n"+Integer.toString(nid);
            for(int i=0;i<Q.size();++i) {
                if(i==0) line=line+"\t"+Q.get(i);
                else line=line+";"+Q.get(i);
            }

            for(int i=0;i<P.size();++i) {
                if(i==0) line=line+"\t"+Double.toString(P.get(i));
                else line=line+";"+Double.toString(P.get(i));
            }

            for(int i=0;i<Pop.size();++i) {
                if(i==0) line=line+"\t"+Pop.get(i);
                else line=line+";"+Pop.get(i);
            }

            for(int i=0;i<L.size();++i) {
                if(i==0) line=line+"\t"+L.get(i);
                else line=line+";"+L.get(i);
            }

            for(int i=0;i<D.size();++i) {
                if(i==0) line=line+"\t"+D.get(i);
                else line=line+";"+D.get(i);
            }

            sgfile.write(line);
            sgfile.close();
        }
        catch(Exception e)
        {
            System.out.println("Exception: "+e.getMessage());

        }
    }
}