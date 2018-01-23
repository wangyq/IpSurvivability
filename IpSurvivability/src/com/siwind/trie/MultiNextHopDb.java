package com.siwind.trie;

import com.siwind.tools.IPv4Util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by wang on 16-4-26.
 */
public class MultiNextHopDb {


    static final int DEFAULT_MAXNUM = 2*1024*1024; //2M items
    static final int NULL_NHDB_INDEX = 0;

    ArrayList< NextHopItem > nhdb = null;

    public MultiNextHopDb(){
        this(DEFAULT_MAXNUM);
    }

    public MultiNextHopDb( int maxRoutingNum){
        if(  (maxRoutingNum<=0) ){
            throw new InvalidParameterException("Number of Routers must greater than zero, and maxRoutingNum >0.");
        }

        this.nhdb = new ArrayList<NextHopItem>(maxRoutingNum); //allocate memory!
        this.nhdb.add( new NextHopItem(0,0,1));

    }

    public void clear(){
        this.nhdb.clear();
        this.nhdb.add( new NextHopItem(0,0,1)); //default route!
    }
    public NextHopItem getRouteItem(int index){
        return nhdb.get(index);
    }

    public void setNextHopItem(int index, NextHopItem item){
        nhdb.set(index,item);
    }

    public int addNextItem(int ip, int masklen, int srcid, int destid, int numRouters){
        NextHopItem item = new NextHopItem(ip,masklen,numRouters);
        item.setNhdbWithRouterID(srcid,destid);

        this.nhdb.add(item);
        return this.nhdb.size()-1;
    }

    /**
     *
     * @param ip
     * @param masklen
     * @param numRouters
     * @return
     */
    public int addRandomNextItem(int ip, int masklen, int numRouters){

        this.nhdb.add(NextHopItem.generateRandomNextHop(ip,masklen, numRouters));

        return this.nhdb.size()-1;
    }

    /**
     * add a random fix item index from nhdb table.
     * @param ip
     * @param masklen
     * @param numRouters
     * @return
     */
    public int addRandomFixItem(int ip, int masklen, int numRouters){

        int nhdb_index = NextHopItem.generateRandomFixHop(this.DEFAULT_MAXNUM);
        return nhdb_index;
    }

    public int getNumberOfNextHop(){
        return this.nhdb.size();
    }

    public int loadFromFile(String strFile, int numOfRouters){
        return loadFromFile(strFile,0,numOfRouters);
    }
    /**
     * File format :
     *      IPADDR masklen  nexthop
     *
     *
     * @param strFile
     *          File name
     * @param maxline
     *           0 - default, load all lines
     *
     * @param numOfRouters
     *           how routers in one item of NHDB
     *
     * @return
     */
    public int loadFromFile(String strFile, int maxline, int numOfRouters){
        BufferedReader in = null;
        String line="";

        int total = 0;

        try
        {
            in=new BufferedReader(new FileReader(strFile));

            while ((line=in.readLine() )!=null) {
                //System.out.println(line);
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] strs = line.trim().split("\\s+|/|\\\\"); // separator of /\ or white char
                if (strs.length < 2) continue; //wrong format!

                if (!IPv4Util.isIPv4Valid(strs[0])) continue;
                int ip = IPv4Util.IPfromStr(strs[0]);
                int masklen = Integer.parseInt(strs[1]);

                addRandomNextItem(ip,masklen, numOfRouters); //add one ip address

                total ++;
                if( maxline>0 ){
                    if( total>= maxline ) break;  //already read enough item.
                }
            }

        } catch (Exception ex)        {
            if( ex instanceof FileNotFoundException){
                System.out.println("File not found! " + strFile);
            } else {
                System.out.println("Load file error : " + line);
            }
            // e.printStackTrace();
        }finally {
            try {
                if( in!=null ) in.close();
            }catch (IOException ex){

            }
        }
        return total;
    }

    public void printNextHop(int index){
       this.nhdb.get(index).print();

    }

    public void printNextHop(ArrayList<Integer> index){
        int[] nh = new int[index.size()];
        for(int i=0;i<index.size();i++){
            nh[i] = index.get(i);
        }
        printNextHop(nh);
    }
    public void printNextHop(int[] index){

        NextHopItem.printNexhHopTitle(); //Title!

        for(int i=0;i<index.length;i++){
            NextHopItem.printNexhHopItem(this.nhdb.get(index[i]),index[i]);
        }

    }

    public void printAll(){
        NextHopItem.printNexhHopTitle();
        for(int i=0;i<this.nhdb.size();i++){
            this.nhdb.get(i).print();
        }
    }



    public static void testPrint(){
        NextHopItem.printNexhHopTitle();
        NextHopItem.generateRandomNextHop(1234,24,5).print();
        NextHopItem.generateRandomNextHop(1234,8,2,5).print();
        NextHopItem.generateRandomNextHop(1234,2,5).print();
        NextHopItem.generateRandomNextHop(1234,28,3,5).print();

        NextHopItem.generateRandomNextHop(1234,0,5).print();
        NextHopItem.generateRandomNextHop(1234,1,5).print();
        NextHopItem.generateRandomNextHop(1234,30,5).print();
        NextHopItem.generateRandomNextHop(1234,31,5).print();
        NextHopItem.generateRandomNextHop(1234,32,5).print();
    }

    public static void main(String[] args){
        testPrint();
    }
}

/**
 *
 */
class NextHopItem{

    /**
     * Invalid NextHop value.
     */
    public static final int NULL_NHDB = -1;

    static Random random = null;

    int ipAddr = 0;
    byte maskLen = 0;

    int nhdb[] = null; // array of number routers!

    static {
        random = new Random(); //init
    }

    public NextHopItem(int ipAddr, int maskLen, int numOfRouter){
        if( numOfRouter<=0 ){
            throw new InvalidParameterException("Number of Routers must greater than zero.");
        }
        if( (maskLen >32) || (maskLen<0) ){
            throw new InvalidParameterException("maskLen must between 0-32. ");
        }

        this.ipAddr = ipAddr;
        this.maskLen = (byte) (maskLen&0xff);

        nhdb = new int[numOfRouter];
        for(int i=0;i<numOfRouter;i++){
            nhdb[i] = NULL_NHDB;
        }
    }

    public int[] getNhdb() {
        return nhdb;
    }

    public void setNhdb(int[] nhdb) {
        this.nhdb = nhdb;
    }

    public void setNhdbWithRouterID(int rid, int nhdb){
        this.nhdb[rid] = nhdb;
    }

    public int getNhdbWithRouterID(int rid){
        return this.nhdb[rid];
    }

    public static boolean checkMatchIPwithMaskLen(int ip, int masklen){

        int maskip = getIPfromMaskLen(masklen);
        return ip == (ip&maskip);
    }
    public static int getIPfromMaskLen(int maskLen){
        int ip = (maskLen==32)?0xFFFFFFFF:(((1<<maskLen)-1)<<(32-maskLen));
        return ip;
    }

    public static int generateRandomFixHop(int maxNum){

        return random.nextInt(maxNum-1)+1; //not zero
    }

    public static NextHopItem generateRandomNextHop(int ipAddr, int maskLen, int numOfRouter){
        return generateRandomNextHop(ipAddr,maskLen, numOfRouter, numOfRouter);
    }

    public static NextHopItem generateRandomNextHop(int ipAddr, int maskLen, int numOfRouter, int maxRouterID){
        NextHopItem item = new NextHopItem(ipAddr,maskLen, numOfRouter);
        if( random== null ){
            random = new Random();
        }

        for(int i=0;i<numOfRouter;i++){
            item.nhdb[i] = random.nextInt(maxRouterID);  //[0, numOfRouter)
        }
        return item;
    }

    public static String getIPString(int ip){
        return String.format("%d.%d.%d.%d", (ip>>24)&0xFF,(ip>>16)&0xFF,(ip>>8)&0xFF,(ip>>0)&0xFF);
    }

    public static String getMaskString(int maskLen){
        int ip = getIPfromMaskLen(maskLen);

        return String.format("%d.%d.%d.%d/%d", (ip>>24)&0xFF,(ip>>16)&0xFF,(ip>>8)&0xFF,(ip>>0)&0xFF, maskLen);
    }

    public static String getNextHopString(int nh[]){
        StringBuilder str = new StringBuilder();

        for(int i=0;i<nh.length;i++){
            if(nh[i] != NextHopItem.NULL_NHDB) {
                str.append("<").append(i).append(",").append(nh[i]).append("> ");
            }
        }
        return str.toString();


    }

    public static void printNexhHopTitle(){
        String strFormat = "%20s %20s  %10s    %-20s";
        System.out.println(String.format(strFormat, "IP-Address","Netmask/Len","NHIndex", "Nexthop"));
    }
    public static void printNexhHopItem(NextHopItem item){
        printNexhHopItem(item,0);
    }
    public static void printNexhHopItem(NextHopItem item, int index){
        String strFormat = "%20s %20s  %10d    %-20s";
        System.out.println(String.format(strFormat, getIPString(item.ipAddr), item.getMaskString(item.maskLen),index,item.getNextHopString(item.nhdb)));
    }
    public void print(){
        printNexhHopItem(this);
    }
}