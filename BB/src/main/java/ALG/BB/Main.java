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
import java.io.*;

public class Main
{
    public Main()
    {
        System.out.println("\nReading graph database............");
    }

    
    //Evaluate the score using BB algorithm
    public void evScore_BB(String input_directory, String output_directory, String filename)
    {
        try
        {
            BufferedWriter fetFile=new BufferedWriter(new FileWriter(output_directory+filename+"_"+"FET.csv",true));
            String row="Source, Target, JA, PROB, POP, LEN, ATTSIM";
            fetFile.write(row);
            fetFile.close();
        }
        catch(Exception e)
        {
            return;
        }
            
        //Get the list of missing edge
        try (BufferedReader csvReader = new BufferedReader(new FileReader(input_directory+filename+".csv"))) 
        {
            BufferedWriter scoreFile=new BufferedWriter(new FileWriter(output_directory+filename+"_"+"BB.csv"));
            String row="";
            scoreFile.write("Node1,Node2,BB");
            AlgoBBSim algo=new AlgoBBSim(output_directory,filename);
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
                //The score using different algorithms
                String score=Double.toString(algo.algoBB(node1, node2));
                scoreFile.write("\n"+row+","+score);

                ++r;
                if(r==5)
                    break; //Execute for the 10 missing link (Test case)
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
    

    public static void main( String... args ) throws Exception
    {
        Main mobj = new Main(); 
        String input_directory="G:\\PhD\\Databases\\DS\\";
        String output_directory="G:\\PhD\\Code\\BB\\";
        String filename="DS11_EP1";
        mobj.evScore_BB(input_directory,output_directory,filename);
        System.out.println("\nFile Write Complete..................");
    }
}