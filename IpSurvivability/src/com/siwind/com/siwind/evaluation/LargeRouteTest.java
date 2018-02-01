package com.siwind.com.siwind.evaluation;

import com.siwind.tools.IPv4Generator;
import com.siwind.tools.IPv4Util;
import com.siwind.tools.Util;
import com.siwind.trie.BinTrie;
import com.siwind.trie.BinTrieGen;
import com.siwind.trie.SchemeVSTS;
import com.siwind.trie.StrideTrie;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.rmi.server.ExportException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class LargeRouteTest {

    public static void main(String[] args) {
        //test_FixNode();
        //System.out.println('9' - '4');
        //BuildMRibTreeTest();
        //testIPPublic();

        getScheme();
    }

    public static void MainTestBTrie(String strCmd, BinTrieGen<Integer> btrie) {

        //testCDFBuildBTrieTime(strCmd, btrie);

        testQueryBTrieTime(strCmd, btrie);

    }

    public static void MainTest(String strCmd, StrideTrie trie) {

        //BuildMRibTreeTest(strCmd, trie);

        //testCDFBuildMRibTime(strCmd, trie);
        testQueryMRibTime(strCmd, trie);

    }

    public static String StrOfScheme(int[] scheme) {
        String str = "";
        int i = 0;
        for (i = 0; i < scheme.length - 1; i++) {
            str += scheme[i] + "-";
        }
        str += scheme[i];
        return str;
    }


    public static ArrayList<int[]> getScheme() {

        ArrayList<int[]> scheme = new ArrayList<>();

        String strScheme = "";
//        strScheme =
//                "14-6-4-8 16-4-4-8 18-4-4-6 20-4-4-4  " +
//                "14-6-4-4-4 16-4-4-4-4 18-2-2-2-8 20-2-2-4-4 " +
//                "14-6-2-2-4-4 16-4-2-2-4-4 18-2-2-2-4-4 20-2-2-2-2-4";

        strScheme = "16-16 8-16-8 8-8-8-8 8-8-4-4-8 8-4-4-4-4-8 8-4-4-4-2-2-8  8-4-4-2-2-2-2-8 8-4-2-2-2-2-2-2-8 8-2-2-2-2-2-2-2-2-8";

        String[] sstr = strScheme.split("\\s+");
        for (String str : sstr) {
            //System.out.println("[" + s+"] ");
            String[] ss = str.split("-");
            int[] s = new int[ss.length];
            for (int i = 0; i < ss.length; i++) {
                //System.out.print("[" + ss[i]+"] ");
                s[i] = Integer.parseInt(ss[i]);
            }
            //System.out.println(" ");
            scheme.add(s);
        }

        return scheme;
    }

    public static void testQueryBTrieTime(String strCmd, BinTrieGen<Integer> btrie) {
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


                items = StrideTrie.loadEntryFromFile(strFile, 0, 127);
                //Collections.reverse(items); //reverse order! no need reverse

                int testcasenum = 10;
                ArrayList<long[]> alltimes = new ArrayList<>(testcasenum + 10);

                //init-btrie
                btrie.clear();
                long[] tt = new long[testcasenum];
                int index = 0;

                for (int k = 0; k < testcasenum; k++) {
                    //init-btrie
                    //btrie.clear();

                    //int count = 10, waypoint = items.size() / count;
//                    long[] tt = new long[count + 2];
//                    int index = 0;

                    long t1 = System.nanoTime();

                    for (int i = 0; i < items.size(); i++) {
                        int[] ips = items.get(i);
                        btrie.insert(ips[0], ips[1], ips[2]);

//                        if ((i + 1) % waypoint == 0) {
//                            long t3 = System.nanoTime();
//                            long us = t3 - t1;
//                            tt[index++] = us;
//                            //System.out.println(i/gapcount);
//                        }
                    } //end of insert

                    long t2 = System.nanoTime();
                    tt[index++] = t2 - t1;

                    //alltimes.add(tt);

                }//end of test case!
                alltimes.add(tt);

                //== out test time
                for (int i = 0; i < alltimes.size(); i++) {
                    long[] time = alltimes.get(i);
                    //==
                    System.out.println("Test Case Num: " + (i + 1));
                    for (int j = 0; j < time.length; j++) {
                        System.out.println(time[j] / 1000000);
                    }
                }
            }

            //get ip address and netmasklen

        } catch (Exception ex) {

            System.out.println(strCmd + " failed!");
            ex.printStackTrace();

        }
    }

    public static void testCDFBuildBTrieTime(String strCmd, BinTrieGen<Integer> btrie) {
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


                items = StrideTrie.loadEntryFromFile(strFile, 0, btrie.MAXNHDB);
                Collections.reverse(items); //reverse order!

                int testcasenum = 10;
                ArrayList<long[]> alltimes = new ArrayList<>(testcasenum + 10);
                for (int k = 0; k < testcasenum; k++) {
                    //init-btrie
                    btrie.clear();

                    int count = 10, waypoint = items.size() / count;
                    long[] tt = new long[count + 2];
                    int index = 0;

                    long t1 = System.nanoTime();

                    for (int i = 0; i < items.size(); i++) {
                        int[] ips = items.get(i);
                        btrie.insert(ips[0], ips[1], ips[2]);

                        if ((i + 1) % waypoint == 0) {
                            long t3 = System.nanoTime();
                            long us = t3 - t1;
                            tt[index++] = us;
                            //System.out.println(i/gapcount);
                        }
                    } //end of insert

                    long t2 = System.nanoTime();
                    tt[index] = t2 - t1;

                    alltimes.add(tt);

                }//end of test case!
                //== out test time
                for (int i = 0; i < alltimes.size(); i++) {
                    long[] time = alltimes.get(i);
                    //==
                    System.out.println("Test Case Num: " + (i + 1));
                    for (int j = 0; j < time.length; j++) {
                        System.out.println(time[j]);
                    }
                }
            }

            //get ip address and netmasklen

        } catch (Exception ex) {

            System.out.println(strCmd + " failed!");
            ex.printStackTrace();

        }
    }

    public static void testQueryMRibTime(String strCmd, StrideTrie trie) {
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

                //Collections.reverse(items); //reverse order!

                testQueryMRibTimeEach(trie, items);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {

            System.out.println(strCmd + " failed!");
            ex.printStackTrace();

        }
    }

    public static void testCDFBuildMRibTime(String strCmd, StrideTrie trie) {
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

                Collections.reverse(items); //reverse order!

                testCDFBuildMRibTimeEach(trie, items);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {

            System.out.println(strCmd + " failed!");
            ex.printStackTrace();

        }

    }

    public static void testQueryMRibTimeEach(StrideTrie trie, ArrayList<int[]> items) throws Exception {
        ArrayList<SchemeVSTS> schemes = SchemeVSTS.makeAllSchemes();


        ArrayList<ArrayList<long[]>> allTimes = new ArrayList<>();

        int count = 10, waypoint = items.size() / count;
        int caseNumber = 7; //for each scheme, test number for this scheme

        for (int k = 0; k < schemes.size(); k++) { //every time
            ArrayList<long[]> time = new ArrayList<>();
            SchemeVSTS scheme = schemes.get(k);
            //System.out.println("Scheme Name="+scheme.getName() + ", alpha="+scheme.getAlpha()+ ", beta="+scheme.getBeta());

            trie.init(scheme.getScheme());//clear all old entry
            long[] tt = new long[caseNumber];

            int index = 0;

            for (int j = 0; j < caseNumber; j++) { //for each scheme, test caseNumber times!
                if (j == 0) {//construction tree
                    long t1 = System.nanoTime();
                    for (int i = 0; i < items.size(); i++) {
                        //trie.addEntry(items.get(i)[0], items.get(i)[1], items.get(i)[2], items.get(i)[3]);
                        trie.addRandomFixItem(items.get(i)[0], items.get(i)[1]);
                    }
                    long t2 = System.nanoTime();
                    tt[index++] = t2 - t1;

                } else {//query !
                    long t1 = System.nanoTime();
                    for (int i = 0; i < items.size(); i++) {
                        trie.Query(items.get(i)[0],items.get(i)[1]);
                    }
                    long t2 = System.nanoTime();
                    tt[index++] = t2 - t1;
                }


            }//end of casenumber test!

            time.add(tt);
            allTimes.add(time);//all times
        }//end of scheme!

        saveSchemeTimes(schemes, allTimes);
    }

    public static void testCDFBuildMRibTimeEach(StrideTrie trie, ArrayList<int[]> items) throws Exception {
        //ArrayList<int[]> schemes = getScheme(); //

        ArrayList<SchemeVSTS> schemes = SchemeVSTS.makeAllSchemes();


        ArrayList<ArrayList<long[]>> allTimes = new ArrayList<>();

        int count = 10, waypoint = items.size() / count;
        int caseNumber = 6; //for each scheme, test number for this scheme

        for (int k = 0; k < schemes.size(); k++) { //every time
            ArrayList<long[]> time = new ArrayList<>();
            SchemeVSTS scheme = schemes.get(k);
            //System.out.println("Scheme Name="+scheme.getName() + ", alpha="+scheme.getAlpha()+ ", beta="+scheme.getBeta());

            for (int j = 0; j < caseNumber; j++) { //for each scheme, test caseNumber times!
                long[] tt = new long[count + 2];
                int index = 0;
                trie.init(scheme.getScheme());//clear all old entry

                long t1 = System.nanoTime();
                for (int i = 0; i < items.size(); i++) {

                    //trie.addEntry(items.get(i)[0], items.get(i)[1], items.get(i)[2], items.get(i)[3]);
                    trie.addRandomFixItem(items.get(i)[0], items.get(i)[1]);

                    if ((i + 1) % waypoint == 0) {
                        long t3 = System.nanoTime();
                        long us = t3 - t1;
                        tt[index++] = us;
                        //System.out.println(i/gapcount);
                    }
                }
                long t2 = System.nanoTime();
                tt[index] = t2 - t1;
                time.add(tt); //
            }//end of schemem test!

            allTimes.add(time);//all times
        }

        saveSchemeTimes(schemes, allTimes);
        //printSchemeTimes(schemes, allTimes); //print all times!
        //
        //printAlltime(schemes,times);

    }

    public static void saveSchemeTimes(ArrayList<SchemeVSTS> schemes, ArrayList<ArrayList<long[]>> allTimes) {
        //String strResFile = "AllSchemeTimes-" + Util.getCurTimeString()+ ".txt";
        String strResFile = "AllSchemeTimes.txt";
        String strFile = Util.getCurFilePath(strResFile);

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(strFile));

            for (int i = 0; i < schemes.size(); i++) {
                SchemeVSTS scheme = schemes.get(i);
                //System.out.println("Scheme Name="+scheme.getName() + ", alpha="+scheme.getAlpha()+ ", beta="+scheme.getBeta());

                ArrayList<long[]> time = allTimes.get(i);
                for (int j = 0; j < time.size(); j++) {
                    out.write("Scheme Name=" + scheme.getName() + ", alpha=" + scheme.getAlpha() + ", beta=" + scheme.getBeta());
                    out.newLine();
                    out.write("Test Case Number: " + (j + 1));
                    out.newLine();
                    long[] caseTime = time.get(j);
                    for (long t : caseTime) {
                        out.write(String.valueOf(t / 1000000)); //msecond
                        out.newLine();
                    }
                }
            }
            System.out.println("Save to file \"" + strFile + "\" finished!");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            //e.printStackTrace();
            System.out.println(e.getMessage());
        } finally {
            try {
                if (out != null) out.close();
            } catch (Exception ex) {

            }
        }
    }

    public static void printSchemeTimes(ArrayList<SchemeVSTS> schemes, ArrayList<ArrayList<long[]>> allTimes) {
        for (int i = 0; i < schemes.size(); i++) {
            SchemeVSTS scheme = schemes.get(i);
            //System.out.println("Scheme Name="+scheme.getName() + ", alpha="+scheme.getAlpha()+ ", beta="+scheme.getBeta());

            ArrayList<long[]> time = allTimes.get(i);
            for (int j = 0; j < time.size(); j++) {
                System.out.println("Scheme Name=" + scheme.getName() + ", alpha=" + scheme.getAlpha() + ", beta=" + scheme.getBeta());
                System.out.println("Test Case Number: " + (j + 1));
                long[] caseTime = time.get(j);
                for (long t : caseTime) {
                    System.out.println(t);
                }
            }
        }
    }

    public static void printAlltime(ArrayList<int[]> schemes, ArrayList<long[]> times) {
        for (int i = 0; i < schemes.size(); i++) {

            System.out.println(StrOfScheme(schemes.get(i)));
            long[] time = times.get(i);
            for (long t : time) {
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

