/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author User
 */
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
//This is original main class
public class Main
{
    public Main()
    {
        System.out.println("\nReading graph database............");
    }
    //Evaluate the score using BB algorithm
    public void evScore_BB(String dataset, int fold, String pn)
    {
        /*
        try
        {
            File file = new File(Config.outputdir+dataset+"_Test_"+pn+fold+"_FET.csv");
            if(file.exists())   file.delete();

            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(Config.outputdir+dataset+"_Test_"+pn+fold+"_FET.csv"));
            String row="From,To,JA,PRO,POP,LEN,ATT,DEG,Status";
            scoreFile.write(row);
            scoreFile.close();
        }
        catch(Exception e)
        {
            System.out.println("Error due to +"+e.getMessage());
        }
        */

        DecimalFormat df = new DecimalFormat("#.####");
        int link=0;
        if(pn=="P") link=1;
        //Get the list of missing edge
        try (BufferedReader csvReader = new BufferedReader(new FileReader(Config.testdir+dataset+"_Test_"+pn+fold+".csv")))
        {
            File file = new File(Config.outputdir+dataset+"_Test_"+pn+fold+"_BB.csv");
            if(file.exists())   file.delete();

            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(Config.outputdir+dataset+"_Test_"+pn+fold+"_BB.csv"));
            String row="";
            scoreFile.write("Node1,Node2,BB,Link");
            AlgoBBSim algo=new AlgoBBSim(dataset,fold);
            int r=0;
            //skip the header
            row = csvReader.readLine();
            //scoreFile.write(row);
            while ((row = csvReader.readLine()) != null)
            {
                String[] data = row.split(",");
                int node1= Integer.parseInt(data[0]);
                int node2= Integer.parseInt(data[1]);
                System.out.println("\nCalculating score using BB for link: "+r+" ("+node1+"->"+node2+")");
                if(node1!=node2) {
                    String score = df.format(algo.algoBB(node1, node2, pn));
                    scoreFile.write("\n" + row + "," + score + "," + link);
                }
                ++r;
            }
            System.out.println("\nWritting to file............");
            scoreFile.close();
            csvReader.close();
            //algo.closeDriver(); //Release the driver instance
        }
        catch(IOException e)
        {
            System.out.println("File not found due to "+e.getMessage());
        }
    }
    public void subGraphGen(String dataset, int fold)
    {
        try (BufferedReader csvReader = new BufferedReader(new FileReader(Config.nodedir+dataset+"_Nodes.csv")))
        {
            NodeSubGen obj=new NodeSubGen(dataset,fold);
            //skip the header
            String row = csvReader.readLine();
            //scoreFile.write(row);
            while ((row = csvReader.readLine()) != null)
            {
                int node=Integer.parseInt(row.split(",")[0]);
                System.out.println("Node: "+node);
                obj.nodeSubgraph(node);
            }
        }
        catch(IOException e)
        {
            System.out.println("File not found due to "+e.getMessage());
        }
    }
    //Prepare the neo4j graph database
    public boolean prepareDatabase(String dataset,int fold)
    {
        GraphModi obj=new GraphModi();
        //Clear the existing rdatabase
        obj.removeGraph();

        List<String> nodes=new ArrayList<>();
        //Creates the new nodes in database
        try (BufferedReader csvReader = new BufferedReader(new FileReader(Config.nodedir+dataset+"_Nodes.csv")))
        {
            //skip the header
            String row = csvReader.readLine();
            //scoreFile.write(row);
            while ((row = csvReader.readLine()) != null)
            {
                nodes.add(row);
            }
            csvReader.close();
        }
        catch(IOException e)
        {
            System.out.println("File not found due to "+e.getMessage());
        }

        List<Integer> srcs=new ArrayList<>();
        List<Integer> dsts=new ArrayList<>();
        //Creates new links in database
        try (BufferedReader csvReader1 = new BufferedReader(new FileReader(Config.traindir+dataset+"_Train_P"+fold+".csv")))
        {
            //skip the header
            String row = csvReader1.readLine();
            //scoreFile.write(row);
            while ((row = csvReader1.readLine()) != null)
            {
                String[] data = row.split(",");
                int node1= Integer.parseInt(data[0]);
                int node2= Integer.parseInt(data[1]);
                if(node1!=node2){
                    srcs.add(node1);
                    dsts.add(node2);
                }

            }
            csvReader1.close();
        }
        catch(IOException e)
        {
            System.out.println("File not found due to "+e.getMessage());
        }

        obj.addNode(nodes);
        obj.addLink(srcs, dsts);

        //Creates the new relations in database
        return true;
    }
    public void runBB(String dataset){
        for(int fold=1;fold<=10;++fold) {
            System.out.println("Dataset: "+dataset+" Fold: "+fold);
            this.prepareDatabase(dataset, fold);
            this.subGraphGen(dataset, fold);
            this.evScore_BB(dataset, fold, "P");
            this.evScore_BB(dataset,fold,"N");
        }
    }
    public void writePerformance(String dataset,double [][]metric){
        try(BufferedWriter perFile=new BufferedWriter(new FileWriter(Config.perdir+dataset+".csv"))){
            perFile.write("Fold,Precision,AUC,AUP");
            for (int i=0;i<10;++i){
                String line="\n"+metric[i][0]+","+metric[i][1]+","+metric[i][2]+","+metric[i][3];
                perFile.write(line);
            }
            perFile.close();
        }catch (Exception e){
            System.out.println("Error due to "+e.getMessage());
        }
    }
    public static void main( String... args ) throws Exception
    {
        String []ds={"PrimarySchool–Day1","AdjNoun","AnnaKarenina","Book-Crossing","CIAO","DavidCopperfield","Epinions","Football","MovielensLatestNew","Movie-Tweetings","PoliticalBooks","UK-Faculty","Workplace"};
        String []ds1={"Ecoli","PB","Router","USAir97","Power","Yeast","NS","Hamster"};
        String []synds1={"SynDS110","SynDS19","SynDS18","SynDS17","SynDS16","SynDS15","SynDS14","SynDS13","SynDS12","SynDS11","Celegans"};
        String []synds2={"SynDS210","SynDS29","SynDS28","SynDS27","SynDS26","SynDS25","SynDS24","SynDS23","SynDS22","SynDS21"};
        String []dsA={"SynDSA10","SynDSA9","SynDSA8","SynDSA7","SynDSA6","SynDSA5","SynDSA4","SynDSA3","SynDSA2","SynDSA1"};
        String []dsNA={"SynDSNA10","SynDSNA9","SynDSNA8","SynDSNA7","SynDSNA6","SynDSNA5","SynDSNA4","SynDSNA3","SynDSNA2","SynDSNA1"};
        //USAir, Router
        Main mobj = new Main();

        String dataset=ds[1];
        //mobj.runBB(dataset);

        double [][]metric=new double[10][4];
        for(int fold=1;fold<=1;++fold) {
            double [][]met=Evaluation.Performance(dataset, fold);
            metric[fold-1][0]=fold;
            metric[fold-1][1]=met[0][0];
            metric[fold-1][2]=met[0][1];
            metric[fold-1][3]=met[0][2];
        }
        //mobj.writePerformance(dataset,metric);
        System.out.println("\nFile Write Complete..................");
        Config.driver.close();
    }
}