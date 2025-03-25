import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
public class Evaluation {
    private static List<Integer> generateRandomInteger(long max,int N)
    {
        List<Integer> listtmp = new ArrayList<>();
        for (Integer i = 1; i <= max; i++)
        {
            listtmp.add(i);
        }
        //Shuffle the value
        Collections.shuffle(listtmp);
        List<Integer> list = new ArrayList<>();
        for (Integer i = 1; i <= N; i++)
        {
            list.add(listtmp.get(i));
        }
        return list;
    }
    private static double AUP(List<Double> scores,List<Integer> links, int L,String dataset){
        double aup=0.0,l=0.0;
        double [][]prs=new double[10][2];
        for(int i=0;i<=9;++i){
            l=l+0.1;
            Integer topL= Math.toIntExact(Math.round(L * l));
            List<Double> scorestmp=new ArrayList<>();
            scorestmp.addAll(scores);
            List<Integer> linkstmp=new ArrayList<>();
            linkstmp.addAll(links);
            prs[i][0]=l;
            double prtmp=Precision(scorestmp,linkstmp,topL);
            prs[i][1]=prtmp;
        }

        try(BufferedWriter prFile=new BufferedWriter(new FileWriter(Config.perdir+dataset+"Pre.csv"))){
            prFile.write("L,Precision");
            for (int i=0;i<=9;++i){
                String line="\n"+prs[i][0]+","+prs[i][1];
                prFile.write(line);
            }
            prFile.close();
        }catch (Exception e){
            System.out.println("Error due to "+e.getMessage());
        }

        double area=0;
        for(int i=0;i<=8;++i){
            area=area+(prs[i+1][0]-prs[i][0])*(prs[i+1][1]+prs[i][1]);
        }
        aup=area/2.0;
        return aup;
    }
    private static double Precision(List<Double> scores,List<Integer> links, Integer L){
        double pr=0.0;
        int Lr=0;
        for(int i=0;i<L;++i){
            //pick the current maximum value from scores
            int maxindex=0;
            double maxvalue=scores.get(0);
            for(int j=1;j<scores.size();++j){
                if(maxvalue<scores.get(j)) maxindex=j;
            }
            Lr=Lr+links.get(maxindex);
            scores.remove(maxindex);
            links.remove(maxindex);
        }
        pr=(double) Lr/(double) L;
        return pr;
    }
    private static double AUC(List<Double> pscores,List<Double> nscores, int n){
        double auc=0.0;
        int L=nscores.size();
        double n1=0.0,n2=0.0;
        List<Integer> rnum=generateRandomInteger(L-1,n);
        for(int i=0;i<n;++i){
            int index=rnum.get(i);
            double ps=pscores.get(index);
            double ns=nscores.get(index);
            if(ps>ns) n1=n1+1.0;
            else if(ps==ns) n2=n2+1.0;
        }
        auc=(n1+(0.5*n2))/(double)n;
        return auc;
    }
    public static double[][] Performance(String dataset, int fold)
    {
        double [][]metric=new double[1][3];
        double precision=0.0,auc=0.0,aup=0.0;
        int L=0;
        int n=0;

        List<Double> scores=new ArrayList<>();
        List<Double> pscores=new ArrayList<>();
        List<Double> nscores=new ArrayList<>();
        List<Integer> links=new ArrayList<>();
        try(BufferedReader nscoreFile=new BufferedReader(new FileReader(Config.outputdir+dataset+"_Test_N"+fold+"_BB.csv")))
        {
            String row = nscoreFile.readLine(); //skip header
            while ((row = nscoreFile.readLine()) != null) {
                String []data=row.split(",");
                double sc=Double.parseDouble(data[2]);
                scores.add(sc);
                nscores.add(sc);
                links.add(0);
            }
            L=nscores.size();
            BufferedReader pscoreFile=new BufferedReader(new FileReader(Config.outputdir+dataset+"_Test_P"+fold+"_BB.csv"));
            row = pscoreFile.readLine(); //skip header
            while ((row = pscoreFile.readLine()) != null) {
                String []data=row.split(",");
                double sc=Double.parseDouble(data[2]);
                scores.add(sc);
                pscores.add(sc);
                links.add(1);
            }
            n=Math.round(L/2);
        }catch (IOException e){
            System.out.println("Error due to "+e.getMessage());
        }
        //System.out.println("Lr: "+ Lr+" L: "+L+" AUC: n1="+n1+" n2="+n2+" n3="+n3);
        auc=AUC(pscores,nscores,n);
        List<Double> scorestmp=new ArrayList<>();
        scorestmp.addAll(scores);
        List<Integer> linkstmp=new ArrayList<>();
        linkstmp.addAll(links);
        int Ltmp=L;
        precision=Precision(scorestmp,linkstmp,Ltmp);
        aup=AUP(scores,links,Ltmp,dataset);

        metric[0][0]=precision;
        metric[0][1]=auc;
        metric[0][2]=aup;
        System.out.println("Dataset: "+dataset+" Fold: "+fold+" The precision: "+precision+" AUC:"+auc+" & AUP:"+aup);
        return metric;
    }
}
