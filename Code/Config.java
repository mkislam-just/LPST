/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;

/**
 *
 * @author kislam
 */
public class Config {
    public static Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "#Parisha2020#" ) );
    public static String traindir="/home/kislam/DS/Train/";
    public static String nodedir="/home/kislam/DS/Node/";
    public static String testdir="/home/kislam/DS/Test/";
    public  static String outputdir="/home/kislam/BBOUTPUT/";
    public  static String perdir="/home/kislam/Per/";
    public static boolean isattributed=false;
    public static double beta=1.0; //=0.5 if attribute, =1 if no attribute : For attributed graph change here
    public static double alpha1=0.2, alpha2=0.2, alpha3=0.2, alpha4=0.2, alpha5=0.2; //1=cn, 2=prob, 3=pop, 4=leng, 5=degree
    public static int ML=3;
}