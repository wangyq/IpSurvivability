package com.siwind.com.siwind.evaluation;

import com.siwind.tools.IPv4Generator;
import com.siwind.tools.IPv4Util;
import com.siwind.tools.Util;
import com.siwind.trie.StrideTrie;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Random;

public class LargeRouteTest {

    public static void main(String[] args) {
        //test_FixNode();
        //System.out.println('9' - '4');
        //BuildMRibTreeTest();
        //testIPPublic();

        getScheme();
    }

    public static void MainTest(String strCmd, StrideTrie trie) {

        //BuildMRibTreeTest(strCmd, trie);
        testCDFBuildMRibTime(strCmd,trie);

    }

    public static String StrOfScheme(int[] scheme){
        String str = "";
        int i = 0;
        for(i=0;i<scheme.length-1;i++){
            str += scheme[i] + "-";
        }
        str += scheme[i] ;
        return str;
    }
    public static ArrayList<int[]> getScheme(){
        ArrayList<int[]> scheme = new ArrayList<>();

        String strScheme =
                "14-6-4-8 16-4-4-8 18-4-4-6 20-4-4-4  " +
                "14-6-4-4-4 16-4-4-4-4 18-2-2-2-8 20-2-2-4-4 " +
                "14-6-2-2-4-4 16-4-2-2-4-4 18-2-2-2-4-4 20-2-2-2-2-4";

        String[] sstr = strScheme.split("\\s+");
        for(String str:sstr ){
            //System.out.println("[" + s+"] ");
            String[] ss = str.split("-");
            int[] s = new int[ss.length];
            for( int i=0;i<ss.length;i++ ){
                //System.out.print("[" + ss[i]+"] ");
                s[i] = Integer.parseInt(ss[i]);
            }
            //System.out.println(" ");
            scheme.add(s);
        }

        return scheme;
    }
    public static void testCDFBuildMRibTime(String strCmd, StrideTrie trie){
        String strFile = "";
        ArrayList<int[]> items = new ArrayList<>();

        try {

            if (strCmd.isEmpty()) return;
            String[] strs = strCmd.split("\\s+");
            //if (strs[0].compareTo("addfile") != 0) return ;

            if (strs.length < 2) {
                System.out.println(" filename is needed!");
            } else {
                strFile = strs[1];
                if (strFile.charAt(0) != '/') {
                    strFile = Util.getCurDir() + strFile;
                }

                int numOfRouters = trie.getNumOfRouters(); //default numOf Routers
                items = StrideTrie.loadEntryFromFile(strFile, 0, numOfRouters);

                testCDFBuildMRibTimeEach(trie,items);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {

            System.out.println(strCmd + " failed!");
            ex.printStackTrace();

        }

    }
    public static void testCDFBuildMRibTimeEach(StrideTrie trie, ArrayList<int[]> items){
        ArrayList<int[]> schemes = getScheme(); //
        ArrayList<long[]> times = new ArrayList<>();

        int count = 10, waypoint = items.size()/count;

        for(int k=0;k < schemes.size();k++){ //every time
            long[] tt = new long[count+2];
            int index = 0;

            trie.init();//clear all old entry

            long t1 = System.nanoTime();
            long t11 = System.currentTimeMillis();
            for (int i = 0; i < items.size(); i++) {

                //trie.addEntry(items.get(i)[0], items.get(i)[1], items.get(i)[2], items.get(i)[3]);
                trie.addRandomFixItem(items.get(i)[0], items.get(i)[1]);

                if (i % waypoint == 0) {
                    long t3 = System.nanoTime();
                    long us = t3 - t1;
                    tt[index++] = us;
                    //System.out.println(i/gapcount);
                }
            }
            long t2 = System.nanoTime();
            tt[index] = t2 - t1;
            times.add(tt); //
        }

        //
        printAlltime(schemes,times);

    }

    public static void printAlltime(ArrayList<int[]> schemes, ArrayList<long[]> times ){
        for(int i=0;i<schemes.size();i++){

            System.out.println(StrOfScheme(schemes.get(i)));
            long[] time = times.get(i);
            for(long t:time){
                System.out.println(t);
            }

        }
    }
    public static void testIPPublic() {
        String ips[] = {"1.2.3.4", "170.12.3.6", "156.58.69.24", "169.254.3.2", "127.0.0.1", "10.0.0.0", "172.16.2.3", "172.16.31.0.2", "192.168.1.2", "100.64.2.3", "224.3.6.5"};
        for (String ip : ips) {
            System.out.println(ip + ": " + IPv4Util.isIPv4Public(ip));
        }
    }

    public static void BuildMRibTreeTest(String strCmd, StrideTrie trie) {

        String strFile = "";

        try {

            if (strCmd.isEmpty()) return;
            String[] strs = strCmd.split("\\s+");
            //if (strs[0].compareTo("addfile") != 0) return ;

            if (strs.length < 2) {
                System.out.println(" filename is needed!");
            } else {
                strFile = strs[1];
                if (strFile.charAt(0) != '/') {
                    strFile = Util.getCurDir() + strFile;
                }
                //System.out.println("Current Directory: " + Util.getCurDir());
                int maxNum = 0;
                if (strs.length >= 3) {
                    maxNum = Integer.parseInt(strs[2]);
                }
                int numOfRouters = trie.getNumOfRouters(); //default numOf Routers
                if (strs.length >= 4) {
                    numOfRouters = Integer.parseInt(strs[3]);
                    if (numOfRouters > trie.getNumOfRouters()) {
                        trie.setNumOfRouters(numOfRouters); //reset numberOfRouters
                    }
                }

                trie.init();//clear all old entry

                ArrayList<int[]> items = StrideTrie.loadEntryFromFile(strFile, maxNum, numOfRouters);

                ArrayList<Long> timeGap = new ArrayList<Long>(20);

                int gapcount = 200000; //2k
                long t1 = System.nanoTime();
                long t11 = System.currentTimeMillis();
                for (int i = 0; i < items.size(); i++) {
                    trie.addEntry(items.get(i)[0], items.get(i)[1], items.get(i)[2], items.get(i)[3]);
                    if (i % gapcount == 0) {
                        long t3 = System.nanoTime();
                        long us = t3 - t1;
                        timeGap.add(us);
                        //System.out.println(i/gapcount);
                    }
                }
                long t2 = System.nanoTime();
                long t22 = System.currentTimeMillis();

                long between = t2 - t1;
                long microsecond = between / 1000;
                long milis = (t22 - t11);
                timeGap.add(between);

                System.out.println("Total time is : " + microsecond + " us (microsecond). " + milis + " milisecond. insert time = " + microsecond / trie.getTotalEntry() + " us (microsecond).");
                System.out.println("Total entries is : " + trie.getTotalEntry());

                for (Long t : timeGap) {
                    System.out.println(t);
                }
            }

            //get ip address and netmasklen

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();

        }

    }
}

