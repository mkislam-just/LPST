/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author User
 */
public class AlgoBBSim {
    String dataset;
    int fold=0;
    public AlgoBBSim(String ds, int fold)
    {
        this.dataset=ds;
        this.fold=fold;
    }

    public double algoBB(int nid1, int nid2, String pn)
    {
        double score=0.0;
        List<Integer> Q1=new ArrayList<>();
        List<Double> P1=new ArrayList<>();
        List<Integer> Pop1=new ArrayList<>();
        List<Integer> L1=new ArrayList<>();
        List<Integer> D1=new ArrayList<>();
        List<Integer> Q2=new ArrayList<>();
        List<Double> P2=new ArrayList<>();
        List<Integer> Pop2=new ArrayList<>();
        List<Integer> L2=new ArrayList<>();
        List<Integer> D2=new ArrayList<>();
        try (BufferedReader sgfile=new BufferedReader(new FileReader(Config.outputdir+dataset+"_Test_"+fold+"_NSG.txt"));)
        {
            //skip the header
            String row = sgfile.readLine();
            while ((row = sgfile.readLine()) != null) {
                String[] fet = row.split("\t");
                int node = Integer.parseInt(fet[0]);
                //System.out.println(row);
                if (fet.length > 1) {
                    String[] Qtmp = fet[1].split(";");
                    String[] Ptmp = fet[2].split(";");
                    String[] Poptmp = fet[3].split(";");
                    String[] Ltmp = fet[4].split(";");
                    String[] Dtmp = fet[5].split(";");
                    if (node == nid1) {
                        for (int i = 0; i < Qtmp.length; ++i) {
                            Q1.add(Integer.parseInt(Qtmp[i].trim()));
                        }
                        for (int i = 0; i < Ptmp.length; ++i) {
                            P1.add(Double.parseDouble(Ptmp[i].trim()));
                        }
                        for (int i = 0; i < Poptmp.length; ++i) {
                            Pop1.add(Integer.parseInt(Poptmp[i].trim()));
                        }
                        for (int i = 0; i < Ltmp.length; ++i) {
                            L1.add(Integer.parseInt(Ltmp[i].trim()));
                        }
                        for (int i = 0; i < Dtmp.length; ++i) {
                            D1.add(Integer.parseInt(Dtmp[i].trim()));
                        }
                    }
                    else if (node == nid2) {
                        for (int i = 0; i < Qtmp.length; ++i) {
                            Q2.add(Integer.parseInt(Qtmp[i].trim()));
                        }
                        for (int i = 0; i < Ptmp.length; ++i) {
                            P2.add(Double.parseDouble(Ptmp[i].trim()));
                        }
                        for (int i = 0; i < Poptmp.length; ++i) {
                            Pop2.add(Integer.parseInt(Poptmp[i].trim()));
                        }
                        for (int i = 0; i < Ltmp.length; ++i) {
                            L2.add(Integer.parseInt(Ltmp[i].trim()));
                        }
                        for (int i = 0; i < Dtmp.length; ++i) {
                            D2.add(Integer.parseInt(Dtmp[i].trim()));
                        }
                    }
                }
            }
        }
        catch(Exception e)
        {
            System.out.println("Error due to "+e.getMessage());
        }
        if(nid1==1433 && nid2==645)
        {
            //System.out.println("nothing");
        }
        score=this.computeScore(nid1, nid2, Q1,Q2,P1,P2,Pop1,Pop2, L1, L2,D1,D2,pn);
        return score;
    }

    public double computeScore(int nid1, int nid2, List<Integer> Q1,List<Integer> Q2, List<Double> P1,List<Double> P2, List<Integer> POP1,List<Integer> POP2,List<Integer> L1,List<Integer> L2,List<Integer> D1,List<Integer> D2,String pn)
    {
        SubGraph grp=new SubGraph();
        double score=0.0;
        int nx=D1.get(0);
        int ny=D2.get(0);

        List<Integer> Q=new ArrayList<>();
        List<Double> P=new ArrayList<>();
        List<Double> POP=new ArrayList<>();
        List<Double> L=new ArrayList<>();
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
                if(deg==0)
                    deg=GetInfoDB.getNeoghbours(nd1).size();
                double temp1=(double)POP1.get(i)/(double)deg;
                double temp2=(double)POP2.get(in)/(double)deg;
                POP.add((temp1+temp2)/2.0);  //Average popularity
            }
        }
        //: For attributed graph change here
        double asim=0.0;       //Attribute similarity
        if(Config.beta<1.0)
            asim=grp.AttributeSim(nid1, nid2);       //Attribute similarity


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


        int link=0;
        if(pn=="P")link=1;
        int N=GetInfoDB.getNodeCount();
        double simdeg=(double)(Q1.size()+Q2.size())/(2.0*(double)N);
        //score=Q.size()*Config.alpha1+avgp*Config.alpha2+avgl*Config.alpha3+avgpop*Config.alpha4;
        double ssim=simQ*Config.alpha1+simp*Config.alpha2+simpop*Config.alpha3+simlen*Config.alpha4+simdeg*Config.alpha5; //Topological similarity
        /*
        try
        {
            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(Config.outputdir+dataset+"_Test_"+pn+fold+"_FET.csv",true));
            String row="\n"+nid1+","+nid2+","+simQ+","+simp+","+simpop+","+simlen+","+asim+","+simdeg+","+link;
            scoreFile.write(row);
            scoreFile.close();
        }
        catch(Exception e)
        {
            System.out.println("Error due to "+e.getMessage());
        }*/
        score=Config.beta*ssim+(1-Config.beta)*asim;
        return score;
    }
    public double Union(List<Integer> set1, List<Integer> set2)
    {
        double sim=0.0;
        try
        {
            List<Integer> unn=new ArrayList<>();
            List<Integer> cmn=new ArrayList<>();
            //Do union operation
            unn.addAll(set1);
            unn.addAll(set2);
            unn=unn.stream().distinct().collect(Collectors.toList());

            return (double)unn.size();
        }
        catch(Exception e)
        {
            System.out.println("\nError in algoJA due to"+e.getMessage()+"\n");
        }

        return sim;
    }
}