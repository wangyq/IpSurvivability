package com.siwind.trie;


import com.siwind.com.siwind.evaluation.LargeRouteTest;
import com.siwind.tools.*;

import java.io.*;
import java.lang.reflect.Array;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Random;

/**
 * Created by wang on 16-1-8.
 */
public class StrideTrie {

    static final int DEFAULT_ROUTERS = 3;
    static final int DEFAULT_MAXNUM = 2*1024 * 1024; //2M items

    protected int[] m_steps;
    protected FixNode m_root;
    protected int m_total = 0;       //total rib entry!

    private int m_numOfRouters = DEFAULT_ROUTERS;

    protected MultiNextHopDb m_mnhdb = null;

    public StrideTrie(int... args) {
        init(args);

    }

    public int getTotalEntry(){
        return m_total;
    }

    /**
     * add [ipaddr/masklen] via [nexthop]
     *
     * @param strCmd
     * @param trie
     */
    public static boolean addCmd(String strCmd, StrideTrie trie) {
        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("add") != 0) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) return false;
            if (strs[2].compareTo("via") != 0) return false;

            int nhop = 0;
            nhop = Integer.parseInt(strs[3]);

            //trie.addEntry(ips[0],ips[1],nhop);
            trie.addRandomItem(ips[0], ips[1]);

            System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * del [ipaddr/masklen]
     *
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean delCmd(String strCmd, StrideTrie trie) {

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("del") != 0) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) return false;

            trie.delEntry(ips[0], ips[1]);
            System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * clear [step step step step ...]
     *
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean clearCmd(String strCmd, StrideTrie trie) {

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("clear") != 0) return false;

            int[] steps = null;
            if (strs.length > 1) {
                steps = new int[strs.length - 1];
                for (int i = 0; i < steps.length; i++) steps[i] = Integer.parseInt(strs[i + 1]);
            }

            FixNode.deReferenceAll(trie.m_root);
            trie.init(steps);

            System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    public int[] loadFromFile(String strFile, int maxline, int numOfRouters) {
        if (numOfRouters > 0) {
            setNumOfRouters(numOfRouters);
        }
        return loadFromFile(strFile, maxline);
    }

    public int[] loadFromFile(String strFile, int maxline) {
        int[] nhdb = new int[2];

        int first = this.m_mnhdb.getNumberOfNextHop();
        this.m_mnhdb.loadFromFile(strFile, maxline, getNumOfRouters());
        int last = this.m_mnhdb.getNumberOfNextHop();

        nhdb[0] = first;
        nhdb[1] = last;

        return nhdb;
    }

    public boolean addRandomItem(int ip, int masklen) {
        int nhdb = this.m_mnhdb.addRandomNextItem(ip, masklen, getNumOfRouters());
        this.addEntry(ip, masklen, nhdb);

        return true;
    }

    /**
     * Add fix item for nhdb
     * @param ip
     * @param masklen
     * @return
     */
    public boolean addRandomFixItem(int ip, int masklen) {
        int nhdb = this.m_mnhdb.addRandomFixItem(ip, masklen, getNumOfRouters());
        this.addEntry(ip, masklen, nhdb);

        return true;
    }
    /**
     * white space char or / as separator
     *
     * @param trie
     * @param strFile
     * @param maxNum: number record to read in. 0 means read total file.
     * @return
     */
    public static boolean loadFromFile(StrideTrie trie, String strFile, int maxNum) {

        boolean bOK = false;
        int num = 0; //
        BufferedReader in = null;
        String line = "";
        try {
            in = new BufferedReader(new FileReader(strFile));

            while ((line = in.readLine()) != null) {
                //System.out.println(line);
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] strs = line.trim().split("\\s+|/");
                if (strs.length < 2) continue; //wrong format!

                if (!IPv4Util.isIPv4Valid(strs[0])) continue;
                int ip = IPv4Util.IPfromStr(strs[0]);
                int masklen = Integer.parseInt(strs[1]);

                //int nhop = (strs.length > 2) ? Integer.parseInt(strs[2]) : 0;

                //trie.addEntry(ip, masklen, nhop);
                trie.addRandomFixItem(ip,masklen); //add random fix item.

                if (maxNum > 0) {
                    num++;
                    if (num >= maxNum) {  //read finished!
                        break;
                    }
                }
            }

            bOK = true;
        } catch (Exception ex) {
            if (ex instanceof FileNotFoundException) {
                System.out.println("File not found! " + strFile);
            } else {
                System.out.println("Load file error : " + line);
            }
            // e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ex) {

            }
        }

        return bOK;
    }

    public static boolean saveToFile(StrideTrie trie, String strFile) {

        boolean bOK = false;
        File file = null;
        PrintStream ps = null;
        try {
            file = new File(strFile);

            ps = new PrintStream(new FileOutputStream(file));

            FixNode.saveToFile(trie.m_root, ps);

            bOK = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Save file error : " + strFile);

        } finally {
            try {
                if (ps != null) ps.close();
            } finally {

            }

        }

        return bOK;
    }

    public static boolean loadCmd(String strCmd, StrideTrie trie) {
        String strFile = "";

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("load") != 0) return false;

            if (strs.length < 2) {
                System.out.println(" Load filename is needed!");
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
                int numOfRouters = 0;
                if (strs.length >= 4) {
                    numOfRouters = Integer.parseInt(strs[3]);
                }

                trie.init();//clear all old entry

                long t1 = System.nanoTime();
                long t11 = System.currentTimeMillis();

                loadFromFile(trie, strFile, 0);

                long t2 = System.nanoTime();
                long t22 = System.currentTimeMillis();

                long between = t2 - t1;
                long microsecond = between / 1000;
                long milis = (t22 - t11);
                System.out.println("Total time is : " + microsecond + " us (microsecond). " + milis + " milisecond. average insert time = " + microsecond / trie.m_total + " us (microsecond).");
                System.out.println("Total entries is : " + trie.m_total);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * load [file]
     * and file line is:
     * x.x.x.x/y
     *
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean loadCmd_old(String strCmd, StrideTrie trie) {

        String strFile = "";

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("load") != 0) return false;

            if (strs.length < 2) {
                System.out.println(" Load filename is needed!");
            } else {
                strFile = strs[1];
                if (strFile.charAt(0) != '/') {
                    strFile = Util.getCurDir() + strFile;
                }
                int maxNum = 0;
                if (strs.length >= 3) {
                    maxNum = Integer.parseInt(strs[2]);
                }
                //System.out.println("Current Directory: " + Util.getCurDir());

                long t1 = System.nanoTime();
                long t11 = System.currentTimeMillis();
                loadFromFile(trie, strFile, maxNum);
                long t2 = System.nanoTime();
                long t22 = System.currentTimeMillis();

                long between = t2 - t1;
                long ms = between / 1000;
                long milis = (t22 - t11);
                System.out.println("Total time is : " + ms + " milisecond. " + milis + " milisecond. insert time =" + milis / trie.m_total + " milisecond.");
                System.out.println("Total entries is : " + trie.m_total);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * add route entry from file.
     * file format :
     * IpAddr/masklen [nexthop]
     * or:
     * IpAddr masklen [nexthop]
     *
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean addfileCmd(String strCmd, StrideTrie trie) {
        String strFile = "";

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("addfile") != 0) return false;

            if (strs.length < 2) {
                System.out.println(" addfile filename is needed!");
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
                ArrayList<int[]> items = loadEntryFromFile(strFile, maxNum, numOfRouters);

                long t1 = System.nanoTime();
                long t11 = System.currentTimeMillis();
                for (int i = 0; i < items.size(); i++) {
                    trie.addEntry(items.get(i)[0], items.get(i)[1], items.get(i)[2], items.get(i)[3]);
                }
                long t2 = System.nanoTime();
                long t22 = System.currentTimeMillis();

                long between = t2 - t1;
                long microsecond = between / 1000;
                long milis = (t22 - t11);
                System.out.println("Total time is : " + microsecond + " us (microsecond). " + milis + " milisecond. insert time = " + microsecond / trie.m_total + " us (microsecond).");
                System.out.println("Total entries is : " + trie.m_total);
            }

            //get ip address and netmasklen

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean saveCmd(String strCmd, StrideTrie trie) {

        try {

            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("save") != 0) return false;

            if (strs.length < 2) {
                System.out.println(" Save filename is neede!");
            } else {
                String strFile = strs[1];
                if (strFile.charAt(0) != '/') {
                    strFile = Util.getCurDir() + strFile;
                }

                saveToFile(trie, strFile);

                System.out.println("Save to File completed! Total entries is " + trie.m_total);
            }
            //get ip address and netmasklen

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    public static boolean showCmd(String strCmd, StrideTrie trie) {

        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("show") != 0) return false;

            int[] ips = {0, 0};
            if (strs.length >= 2) {
                //get ip address and netmasklen
                if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) {
                    throw new InvalidParameterException("Command parameter wrong!");
                    //return false;
                }
            }

            ArrayList<Integer> nhdb = FixNode.showEntry(trie.m_root, ips[0], ips[1]); //show

            trie.m_mnhdb.printNextHop(nhdb);

            System.out.println("Number nodes to show : " + nhdb.size());

            System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    public static boolean showCmd_old(String strCmd, StrideTrie trie) {

        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("show") != 0) return false;

            int[] ips = {0, 0};
            if (strs.length >= 2) {
                //get ip address and netmasklen
                if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) {
                    throw new InvalidParameterException("Command parameter wrong!");
                    //return false;
                }
            }

            int num = FixNode.show(trie.m_root, ips[0], ips[1]); //show
            System.out.println("Number nodes to show : " + num);

            System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    public static boolean infoCmd(String strCmd, StrideTrie trie) {

        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("info") != 0) return false;

            TrieInfo data = FixNode.info(trie.m_root);

            int total = data.next + data.nhop + data.bitSet + data.lhop;
            float ratio = (float) (((data.entry) * 100.0) / total);
            float ratio1 = (float) (((float) total) / (data.entry));

            String strFormat = "%8s %14s %14s %10s %12s %14s %16.4f %16.2f";
            String strFormat1 = "%8s %14s %14s %10s %12s %14s %16s %16s";
            System.out.println(String.format(strFormat1, "Entry", "nexthop", "next", "bitSet", "lhop", "TOTAL", "Entry/Total(%)", "Total/Entry"));

            System.out.println(String.format(strFormat, data.entry, data.nhop, data.next, data.bitSet, data.lhop, total, ratio, ratio1));

            System.out.print("Scheme = ( ");
            for (int i = 0; i < trie.m_steps.length; i++) System.out.print(trie.m_steps[i] + " ");
            System.out.println(" )");

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static boolean randCmd(String strCmd, StrideTrie trie) {
        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("rand") != 0) return false;
            int maxEntries = 1010680; //1.01K
            if( strs.length>1 ){
                maxEntries = Integer.parseInt(strs[1]);
            }

            randIPv4Prefix(trie,maxEntries);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static boolean bAddCmd(String strCmd, BinTrie<Integer> btrie) {
        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("badd") != 0) throw new Exception("Command format error!");
            int[] ips = new int[2];
            //get ip address and netmasklen
            if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) return false;
            if (strs[2].compareTo("via") != 0) return false;

            int nhop = 0;
            nhop = Integer.parseInt(strs[3]);

            btrie.insert(ips[0],ips[1],nhop);

            System.out.println("Total entries is : " + btrie.getCount());

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean bDelCmd(String strCmd, BinTrie<Integer> btrie) {

        return true;
    }
    public static boolean bLoadCmd(String strCmd, BinTrie<Integer> btrie) {

        return true;
    }
    public static boolean bShowCmd(String strCmd, BinTrie<Integer> btrie) {
        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("show") != 0) return false;

            int[] ips = {0, 0};
            if (strs.length >= 2) {
                //get ip address and netmasklen
                if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) {
                    throw new InvalidParameterException("Command parameter wrong!");
                    //return false;
                }
            }



            //System.out.println("Number nodes to show : " + nhdb.size());

            //System.out.println("Total entries is : " + trie.m_total);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }
    public static boolean testCmd(String strCmd, StrideTrie trie) {
        try {
            if (strCmd.isEmpty()) return false;
            String[] strs = strCmd.split("\\s+");
            if (strs[0].compareTo("test") != 0) return false;

            LargeRouteTest.MainTest(strCmd,trie);

        } catch (Exception ex) {
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    public static String readLine() {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String strLine = null;
        //System.out.print("Please input your command:");
        try {
            strLine = br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("ÊäÈëÊý¾Ý£º"+read);
        return strLine;
    }

    public static void showMenu(StrideTrie trie,BinTrie<Integer> btrie) {

        String strMenu = "Available command are: \n" +
                "add [ipaddr/masklen] via [nexthop] \n" +
                "del [ipaddr/masklen] \n" +
                "clear [step step step step ...]  \n" +
                "load [file] [maxLine=0] [numberRouters]\n" +
                "addfile [file] [maxLine=0] [numberRouters]\n" +
                "save [file] \n" +
                "show [ipaddr/masklen] \n" +
                "info \n" +
                "rand [num] \n" +
                "test \n" +
                "============\n" +
                "badd [ipaddr/masklen] via [nexthop] \n" +
                "bdel [ipaddr/masklen] \n" +
                "bload [file] \n" +
                "show [ipaddr/masklen] \n" +
                "============ \n" +
                "help | ? \n" +
                "exit | quit \n" +
                "";

        String strLine;
        String strCmd;

        int iTotal = 0;
        do {
            //System.out.println(strCmd);
            System.out.print("In[" + (iTotal++) + "]: ");

            strLine = readLine();
            strLine = strLine.trim();
            strLine.toLowerCase();

            strCmd = strLine.split("\\s+")[0];

            if (strCmd.isEmpty()) continue;

            System.out.println("Out[" + (iTotal++) + "]: ");

            if ((strCmd.compareTo("quit") == 0) || (strCmd.compareTo("exit") == 0)) break;
            else if (strCmd.compareTo("add") == 0) {
                addCmd(strLine, trie);

            } else if (strCmd.compareTo("del") == 0) {
                delCmd(strLine, trie);

            } else if (strCmd.compareTo("clear") == 0) {
                clearCmd(strLine, trie);

            } else if (strCmd.compareTo("load") == 0) {
                loadCmd(strLine, trie);

            } else if (strCmd.compareTo("addfile") == 0) {
                addfileCmd(strLine, trie);

            } else if (strCmd.compareTo("save") == 0) {
                saveCmd(strLine, trie);

            } else if (strCmd.compareTo("show") == 0) {
                showCmd(strLine, trie);

            } else if (strCmd.compareTo("info") == 0) {
                infoCmd(strLine, trie);

            } else if (strCmd.compareTo("rand") == 0) {
                randCmd(strLine, trie);

            }else if (strCmd.compareTo("badd") == 0) {
                bAddCmd(strLine, btrie);

            }else if (strCmd.compareTo("bdel") == 0) {
                bDelCmd(strLine, btrie);

            }else if (strCmd.compareTo("bload") == 0) {
                bLoadCmd(strLine, btrie);

            }else if (strCmd.compareTo("bshow") == 0) {
                bShowCmd(strLine, btrie);

            }
            else if (strCmd.compareTo("test") == 0) {
                testCmd(strLine, trie);

            } else if ((strCmd.compareTo("help") == 0) || (strCmd.compareTo("?") == 0)) {
                System.out.println(strMenu);
            } else {
                System.out.println("Unkown command '" + strLine + "' !");
            }
        } while (true);

    }
    public static void randIPv4Prefix(StrideTrie trie, int maxEntries){
        //prefix length distribution!
        int countPrefix[] = {18,13,36,96,262,505,1016,1772,12971,7569,12869,26093,37203,39749,61801,54728,306943,102,97,184,62,66,142,68,690};
        IPv4Generator IP = new IPv4Generator(countPrefix);

        while (trie.getTotalEntry() < maxEntries ){
            int[] ipandlen = IP.nextIPandPrefixLen();
            trie.addEntry(ipandlen[0],ipandlen[1],0,0);
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        //test_FixNode();
        runTrie();
        //System.out.println('9' - '4');
    }

    //======================
    public static void test_FixNode() {
        FixNode.travelPreOrder(8, 0);

    }

    public static void runTrie() {
        int[] steps = {16,4,2,2,4,4};//{8, 4, 4, 2, 2, 2, 2, 4, 4};//{8,8,8,8};//{16,8,4,4};
        StrideTrie trie = new StrideTrie(steps);
        BinTrie<Integer> btrie = new BinTrie<>();

        //StrideTrie trie = new StrideTrie(16,8,4,4);

        showMenu(trie,btrie);
    }

    public void init() {
        init(null);
    }

    protected void init(int[] steps) {
        m_total = 0;
        if (steps != null) {
            m_steps = new int[steps.length];
            System.arraycopy(steps, 0, m_steps, 0, steps.length);
        }

        FixNode.deReferenceAll(m_root); //free memory!
        m_root = new FixNode(new StrideTrieScheme(m_steps));

        if (m_mnhdb != null) {
            m_mnhdb.clear();  //GC now!
        } else {
            m_mnhdb = new MultiNextHopDb(DEFAULT_MAXNUM);
        }

    }

    /**
     * @param ipaddr
     * @param masklen
     * @param srcid
     * @param destid
     * @return
     */
    public boolean addEntry(int ipaddr, int masklen, int srcid, int destid) {
        int nhdb = this.m_mnhdb.addNextItem(ipaddr, masklen, srcid, destid, m_numOfRouters);
        return addEntry(ipaddr, masklen, nhdb);
    }

    protected boolean addEntry(int ipaddr, int masklen, int nhop) {
        if (masklen <= 0 || masklen > 32) return false;
        StrideTrieScheme scheme = new StrideTrieScheme(m_steps);
        scheme.nextStride();

        int oldHop = m_root.addEntry(ipaddr, masklen, nhop, scheme); //not delete
        if (oldHop == 0) {
            m_total++; //add success!
        }
        return true;
    }

    public boolean delEntry(int ipaddr, int masklen) {
        if (masklen <= 0 || masklen > 32) return false;

        int oldHop = m_root.delEntry(ipaddr, masklen, 0); //not delete
        if (oldHop != 0) {
            m_total--; //del success!
        }
        return true;
    }

    public int getNumOfRouters() {
        return m_numOfRouters;
    }

    public void setNumOfRouters(int m_numOfRouters) {
        this.m_numOfRouters = m_numOfRouters;
    }

    /**
     * File format :
     * IpAddr/masklen [src_rid  dest_rid]
     * or:
     * IpAddr masklen [src_rid  dest_rid]
     *
     * @param strFile
     * @param maxline
     * @param numOfRouters
     * @return int[]
     * where : int[0]=IpAddr, int[1]=masklen,int[2]=SrcRid,int[3]=DestRid,
     */
    public static ArrayList<int[]> loadEntryFromFile(String strFile, int maxline, int numOfRouters) {
        BufferedReader in = null;
        String line = "";

        int total = 0;
        ArrayList<int[]> items = new ArrayList<>();
        Random rand = new Random(); //

        try {
            in = new BufferedReader(new FileReader(strFile));

            while ((line = in.readLine()) != null) {
                //System.out.println(line);
                line = line.trim();
                if (line.isEmpty()) continue;

                //split with whitespace or "/"
                String[] strs = line.trim().split("\\s+|/|\\\\"); //separator of /,\ or white
                if (strs.length < 2) continue; //wrong format!

                if (!IPv4Util.isIPv4Valid(strs[0])) continue;
                int ip = IPv4Util.IPfromStr(strs[0]);
                int masklen = Integer.parseInt(strs[1]);
                int[] item = new int[4];
                item[0] = ip;
                item[1] = masklen;
                if (strs.length >= 4) {
                    item[2] = Integer.parseInt(strs[2]);
                    item[3] = Integer.parseInt(strs[3]);
                } else {
                    item[2] = rand.nextInt(numOfRouters);
                    item[3] = rand.nextInt(numOfRouters);
                    //make sure not the same!
                    if (item[3] == item[2]) {
                        item[3] = numOfRouters - 1 - item[3];
                    }
                }

                items.add(item);//add item

                total++;
                if (maxline > 0) {
                    if (total >= maxline) break;  //already read enough item.
                }
            }

        } catch (Exception ex) {
            if (ex instanceof FileNotFoundException) {
                System.out.println("File not found! " + strFile);
            } else {
                System.out.println("Load file error : " + line);
            }
            // e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException ex) {

            }
        }
        return items;
    }
}


class FixNode {

    public static final int NULL_NHDB = 0;

    protected int branch = 0;
    protected int skip = 0;
    protected int skip_len = 0;

    protected FixNode[] next = null;
    protected BitSet isRib = null;
    protected int nhop[] = null;  //the size is 2^branch
    protected int lhop[] = null;  //the size is 2^brach - 2

    protected StrideTrieScheme m_scheme;

    /**
     * @param scheme
     */
    public FixNode(StrideTrieScheme scheme) {
        this(scheme.nextStride(), 0, 0);
    }

    /**
     * @param branch
     */
    public FixNode(int branch) {
        this(branch, 0, 0);
    }

    /**
     * @param branch
     * @param skip
     * @param skip_len
     */
    public FixNode(int branch, int skip, int skip_len) {
        //
        this.branch = ((branch >= 0) && (branch < 32)) ? branch : 0;
        this.next = new FixNode[getBranchSize()];  //next pointer table!
        this.nhop = new int[getBranchSize()];      //next hop items, maybe using lazi-allocation
        this.isRib = new BitSet(getBranchSize());  //if Rib is here!

        //skip
        this.skip_len = ((skip_len >= 0) && (skip_len < 32)) ? skip_len : 0;
        this.skip = skip & ((1 << this.skip_len) - 1);  //make sure skip is right!
    }

    /**
     * giving parent node of k, it's left and right child node are (2K+2) and (2K+3)
     *
     * @param nodeID
     * @return
     */
    public static int getNodeLevel(int nodeID) {
        int level = 1;
        while (nodeID > 1) {
            level++;
            nodeID = (nodeID - 2) / 2;
        }
        return level;
    }

    public static void travelPreOrder(int depth, int nodeID) {
        if (depth <= 0 || depth > 32) return;

        long bound = (1 << (depth + 1)) - 2;
        if (nodeID >= bound) return;

        int level = getNodeLevel(nodeID);

        Stack<Long> st = new Stack<Long>(depth - level + 4);

        long index = nodeID;
        do {
            if (index < bound) {
                System.out.print(index + " ");
                index = 2 * index + 2;  //goto left child.
                st.Push(index + 1);   //right child into stack
            } else if (!st.isEmpty()) { // index >=bound!
                index = st.Pop();
            } else {// stop now!
                break;
            }
        } while (true);
    }

    public static int getIPFragment(int ipaddr, int fragmentlen) {
        if (fragmentlen <= 0) return 0;
        if (fragmentlen >= 32) return ipaddr;
        int striplen = 32 - fragmentlen;
        return ipaddr & (((1 << fragmentlen) - 1) << (32 - fragmentlen));  //caution: must zero-right-shift of >>>
    }

    public static FixNode queryNode(FixNode root, int ipaddr, int masklen) {
        if ((null == root) || (masklen <= 0)) return root;

        FixNode node = root;

        while ((node != null) && (node.branch < masklen)) {
            masklen -= node.branch;
            int index = node.getIPFragmentOffset(ipaddr, node.branch);
            ipaddr = (ipaddr << node.branch); //new ip address

            node = node.next[index];

        }

        return node;
    }

    /**
     * @param root
     */
    public static void deReferenceAll(FixNode root) {
        if (root == null) return;
        ArrayQueue<FixNode> queue = new ArrayQueue<FixNode>(5 * (1 << 8), (1 << 8));

        queue.push(root);
        do {
            FixNode node = queue.pop();
            for (int i = 0; i < node.getBranchSize(); i++) {
                if (node.next[i] != null) {
                    queue.push(node.next[i]);
                    node.next[i] = null;     //dereference!
                }
            }
        } while (!queue.isEmpty());
    }

    public static int saveToFile(FixNode root, PrintStream ps) {
        if (root == null) return 0;
        ArrayQueue<TrieVisit> queue = new ArrayQueue<TrieVisit>(5 * (1 << 8), (1 << 8));

        int num = 0;

        int baseIP = 0, baseMasklen = 0;

        queue.push(new TrieVisit(root, 0, 0));
        TrieVisit nodeVisit;

        do {
            num++;
            nodeVisit = queue.pop();

            nodeVisit.node.printRoute(nodeVisit.baseIP, nodeVisit.baseMasklen, null, ps);

            for (int i = 0; i < nodeVisit.node.getBranchSize(); i++) {
                if (nodeVisit.node.next[i] != null) {
                    baseIP = (nodeVisit.baseIP << nodeVisit.node.branch) + i;
                    baseMasklen = nodeVisit.baseMasklen + nodeVisit.node.branch;

                    queue.push(new TrieVisit(nodeVisit.node.next[i], baseIP, baseMasklen));
                }
            }

        } while (!queue.isEmpty());

        return num;
    }

    /**
     * @param root
     */
    public static TrieInfo info(FixNode root) {
        TrieInfo data = new TrieInfo();

        if (root == null) return data;

        ArrayQueue<FixNode> queue = new ArrayQueue<FixNode>(5 * (1 << 8), (1 << 8));

        queue.push(root);

        do {
            FixNode node = queue.pop();  //

            data.add(node.getInfo());

            for (int i = 0; i < node.getBranchSize(); i++) {
                if (node.next[i] != null) queue.push(node.next[i]);
            }

        } while (!queue.isEmpty());

        return data;
    }

    /**
     * @param nhdb
     * @param node
     * @return
     */
    public static boolean showEntry(ArrayList<Integer> nhdb, FixNode node) {

        if (node.lhop != null) {//could find route here!
            for (int i = 0; i < node.lhop.length; i++) {
                if (node.lhop[i] > NULL_NHDB) {
                    nhdb.add(node.lhop[i]);
                }
            }
        }

        for (int i = 0; i < node.isRib.length(); i++) {//
            if (node.isRib.get(i)) {
                nhdb.add(node.nhop[i]);
            }

        }
        for (int i = 0; i < node.next.length; i++) {//find in the next level
            if (node.next[i] != null) {
                showEntry(nhdb, node.next[i]);
            }

        }
        return true;
    }

    /**
     * @param root
     * @param ipaddr
     * @param masklen
     * @return
     */
    public static ArrayList<Integer> showEntry(FixNode root, int ipaddr, int masklen) {
        ArrayList<Integer> nhdb = new ArrayList<Integer>();

        if ((ipaddr == 0) || (masklen == 0)) {//from root show all node.
            showEntry(nhdb, root);
        } else {//not default route

            FixNode node = root;

            while ((node != null) && (node.branch < masklen)) {
                masklen -= node.branch;
                int index = node.getIPFragmentOffset(ipaddr, node.branch);
                ipaddr = (ipaddr << node.branch); //new ip address

                node = node.next[index];

            }
            if (node != null) { //find route, masklen <= node.branch
                int d = node.branch - masklen;
                int span = 1;  //
                int ip_offset = node.getIPFragmentOffset(ipaddr, masklen);
                int nodeID = node.getNodIDfromOffset(ip_offset, masklen);

                if (node.lhop != null) {//
                    for (int i = 0; i < d; i++) { //from level of masklen to branch-1
                        for (int j = 0; j < span; j++) {
                            int index = nodeID + j;
                            if (node.lhop[nodeID + j] != 0) {//find rib item!
                                //int hop = this.lhop[index];
                                nhdb.add(node.lhop[index]);
                            }
                        }//end of j
                        nodeID = nodeID * 2 + 2;
                        span *= 2;
                    }
                }
                //==========================================================
                int index = node.getIPFragmentOffset(ipaddr, node.branch);
                int count = 1 << (node.branch - masklen);  // d = 0 to branch-1

                for (int i = 0; i < count; i++) {
                    if (node.next[index + i] != null) {
                        showEntry(nhdb, node.next[index + i]);  //next level!
                    }
                }
            }
        }
        return nhdb;
    }

    public static int show(FixNode root) {
        return show(root, 0, 0);
    }

    /**
     * @param root
     * @return
     */
    public static int show(FixNode root, int ipaddr, int masklen) {
        if (root == null) return 0;

        int num = 0;

        int baseIP = 0, baseMasklen = 0;

        ArrayQueue<TrieVisit> queue = null;

        String strFormat = "%20s %20s %15d";

        FixNode node = root;

        System.out.println(String.format("%20s %20s %15s", "IPAddress", "Netmask", "Nexthop"));

        if ((ipaddr == 0) && (masklen == 0)) { //default show all!
            queue = new ArrayQueue<TrieVisit>(node.getBranchSize(), (1 << 8));
            queue.push(new TrieVisit(root, baseIP, baseMasklen));
        } else if (masklen > 0) {
            //node = queryNode(root, ipaddr, masklen); //and new masklen
            while ((node != null) && (masklen > node.branch)) {
                masklen -= node.branch;
                int index = node.getIPFragmentOffset(ipaddr, node.branch);
                ipaddr = (ipaddr << node.branch); //new ip address

                //== update baseIP and baseMasklen
                baseMasklen += node.branch; //new baseMasklen
                baseIP = (baseIP << node.branch) + index; //new baseIP

                node = node.next[index];
            }
            if ((node != null)) {
                num += node.printEPRoute(ipaddr, masklen, baseIP, baseMasklen, strFormat, System.out);

                int index = node.getIPFragmentOffset(ipaddr, node.branch);
                int count = 1 << (node.branch - masklen);  // d = 0 to branch-1
                for (int i = 0; i < count; i++) {
                    if (node.next[index + i] != null) {
                        if (queue == null) { //lazy init!
                            queue = new ArrayQueue<TrieVisit>(5 * (1 << 8), (1 << 8));
                        }
                        // == push into queue ==
                        int newBaseMasklen = baseMasklen + node.branch;
                        int newBaseIP = (baseIP << node.branch) + index + i;

                        queue.push(new TrieVisit(node.next[index + i], newBaseIP, newBaseMasklen));
                    }
                }
            } //end if node != null

        }

        if (queue == null) return 0;

        //ArrayQueue<TrieVisit> queue = new ArrayQueue<TrieVisit>(5*(1<<8),(1<<8));
        //queue.push(new TrieVisit(node,baseIP,baseMasklen));
        //TrieVisit nodeVisit;

        do {
            num++;
            TrieVisit nodeVisit = queue.pop();

            nodeVisit.node.printEPRoute(nodeVisit.baseIP, nodeVisit.baseMasklen, strFormat, System.out);

            for (int i = 0; i < nodeVisit.node.getBranchSize(); i++) {
                if (nodeVisit.node.next[i] != null) {
                    baseIP = (nodeVisit.baseIP << nodeVisit.node.branch) + i;
                    baseMasklen = nodeVisit.baseMasklen + nodeVisit.node.branch;

                    queue.push(new TrieVisit(nodeVisit.node.next[i], baseIP, baseMasklen));
                }
            }

        } while (!queue.isEmpty());

        return num;
    }

    protected int getBranchSize() {
        return 1 << branch;
    }

    protected int getLessBranchSize() {
        return (1 << branch) - 2;
    }

    protected int getIPFragmentOffset(int ipaddr, int fragmentlen) {//the lowest branch-bit
        int ip = getIPFragment(ipaddr, fragmentlen);
        return ip >>> (32 - branch);
    }

    protected int getNodIDfromOffset(int ip, int level) {//from nhop offset and level , get nodeID
        int nodeID = ip >> (branch - level);
        return nodeID + (1 << level) - 2;
    }

    /**
     * giving node of m, its nexthop's start index is (m+2)*2^d - 2^branch. where d=branch - level(m)
     *
     * @param nodeID
     * @return
     */
    protected int getNodeStartIndex(int nodeID) {
        int l = getNodeLevel(nodeID);
        int d = branch - l;

        return ((nodeID + 2) << d) - (1 << branch);
    }

    protected int getNodeEndIndex(int nodeID) {
        int l = getNodeLevel(nodeID);
        int d = branch - l;

        return getNodeStartIndex(nodeID) + (1 << d) - 1;
    }

    /**
     * @param nodeID  : nodeID from 0,1,..., (2^branch -3)
     * @param masklen : from 1 to branch
     * @param nhop
     */
    protected void travelPreOrderUpdateLHop(int nodeID, int masklen, int nhop) {

        //if( masklen>= branch ) return;//?

        //int nodeID = prefix + ((1<<masklen)-2);

        int level = masklen;
        int bound = (1 << (branch - 1)) - 2;  //getLessBranchSize();
        int i = 0;

        if (nodeID >= bound) { //here do NOT need go through child node
            if (this.lhop[nodeID] != 0) {
                i = (nodeID << 1) + 4 - (1 << branch);//getNodeStartIndex(nodeID); //
                if (!this.isRib.get(i)) this.nhop[i] = nhop;
                if (!this.isRib.get(i + 1)) this.nhop[i] = nhop;
            }
        } else {//travel from left to right sub-tree

            int index = (nodeID << 1) + 2; //left child!

            Stack<Integer> st = new Stack<Integer>(branch - level + 4);  //initail size must less than branch-level.

            st.Push(index + 1); //right child push into stack

            do {
                if ((index < bound) && (this.lhop[index] == 0)) {// not reach leaf!
                    index = (index << 1) + 2;//2 * index + 2;  //goto left node!
                    st.Push(index + 1);    //right node push stack.

                } else {// if((index<bound) && (this.lhop[index]!=0)){
                    if ((index >= bound) && (this.lhop[index] == 0)) {//need update!
                        i = (index << 1) + 4 - (1 << branch);
                        if (!this.isRib.get(i)) this.nhop[i] = nhop;
                        if (!this.isRib.get(i + 1)) this.nhop[i] = nhop;
                    }

                    if (!st.isEmpty()) {
                        index = st.Pop();
                    } else {
                        break;  // end travel now!
                    }
                } //end if!

            } while (true);
        }
    }

    /**
     * Addd entry to routing table!
     *
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return the old Next hop
     */
    protected int updateEntry(int ipaddr, int masklen, int nhop, boolean bDel, StrideTrieScheme scheme) {
        //if( masklen<=0 || masklen>32 ) return false;
        int oldHop = 0;


        return oldHop;
    }

    /**
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return
     */
    public int delEntry(int ipaddr, int masklen, int nhop) {
        int oldHop = 0;
        int ip = getIPFragmentOffset(ipaddr, branch);

        if (masklen > branch) {
            if (this.next[ip] != null) {
                int newIP = ipaddr << branch;
                int newMasklen = masklen - branch;

                oldHop = this.next[ip].delEntry(newIP, newMasklen, nhop);
            }

        } else if (masklen == branch) { //
            if (this.nhop[ip] != 0) {
                oldHop = this.nhop[ip];
                this.nhop[ip] = 0;
                this.isRib.clear(ip); //clear the rib info!
            }

        } else { //( masklen < branch){
            if (this.lhop != null) {
                int nodeID = getNodIDfromOffset(ip, masklen);

                oldHop = this.lhop[nodeID]; //

                if (oldHop != 0) {
                    travelPreOrderUpdateLHop(nodeID, masklen, 0);
                    this.lhop[nodeID] = 0;  //mark being deleted
                }
            }
        }
        return oldHop;
    }

    /**
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @param scheme
     * @return
     */
    public int addEntry(int ipaddr, int masklen, int nhop, StrideTrieScheme scheme) {
        int oldHop = 0;
        int ip = getIPFragmentOffset(ipaddr, branch);

        if (masklen > branch) {// go through next level trie-node!
            int newIP = ipaddr << branch;
            int newMasklen = masklen - branch;

            int step = scheme.nextStride();  //move to next stride trie.

            if ((this.next[ip] == null)) {
                this.next[ip] = new FixNode(step);  //next fix stride trie!
            }
            oldHop = this.next[ip].addEntry(newIP, newMasklen, nhop, scheme);

        } else if (masklen == branch) {

            if (this.isRib.get(ip)) {// here old next hop is valid!
                oldHop = this.nhop[ip]; //old next hop
            } else {
                this.isRib.set(ip);  //set rib mark!
            }

            if (this.nhop[ip] != nhop) {
                this.nhop[ip] = nhop;
            }
        } else { //( masklen < branch){
            if (this.lhop == null) { //lazy init!
                this.lhop = new int[this.getLessBranchSize()];
            }
            int nodeID = getNodIDfromOffset(ip, masklen);

            oldHop = this.lhop[nodeID]; //zero or last rib entry!

            if (this.lhop[nodeID] != nhop) { //add new next hop!
                //oldHop = this.lhop[nodeID]; //
                travelPreOrderUpdateLHop(nodeID, masklen, nhop);
                this.lhop[nodeID] = nhop;  //mark being added
            }
        }
        return oldHop;
    }

    protected TrieInfo getInfo() {
        TrieInfo val = new TrieInfo();

        val.next = this.getBranchSize();
        val.nhop = this.getBranchSize();
        val.lhop = (this.lhop != null) ? this.getLessBranchSize() : 0;
        val.bitSet = this.getBranchSize() >>> 5; //2^6 = 64; a long, which consists of 64 bits, and a int which consists of 32 bits

        val.entry = 0;
        if (this.lhop != null) {
            for (int i = 0; i < this.getLessBranchSize(); i++) {
                if (this.lhop[i] != 0) val.entry++;
            }
        }
        for (int i = 0; i < this.getBranchSize(); i++) {
            if (this.isRib.get(i)) val.entry++;
        }
        return val;
    }

    /**
     * print route with Expansion route
     *
     * @param ipaddr
     * @param masklen
     * @param baseIP
     * @param baseMasklen
     * @param strFormat
     * @param out
     * @return
     */
    protected int printEPRoute(int ipaddr, int masklen, int baseIP, int baseMasklen, String strFormat, PrintStream out) {
        int num = 0;

        if (masklen <= 0) {
            ; //nothing to do!
        } else if (masklen < this.branch) {
            int d = this.branch - masklen;
            int span = 1;  //
            int ip_offset = getIPFragmentOffset(ipaddr, masklen);
            int nodeID = getNodIDfromOffset(ip_offset, masklen);

            if (this.lhop != null) {//
                for (int i = 0; i < d; i++) { //from level of masklen to branch-1
                    for (int j = 0; j < span; j++) {
                        int index = nodeID + j;
                        if (this.lhop[nodeID + j] != 0) {//find rib item!
                            int hop = this.lhop[index];
                            int ipmasklen = baseMasklen + masklen + i;
                            int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                            int ip = (baseIP << (masklen + i)) + (ip_offset >>> (this.branch - masklen - i)) + j;
                            ip = ip << (32 - ipmasklen);

                            if (strFormat != null) {
                                out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen, hop));
                            } else {
                                out.println(IPv4Util.IP2Str(ip) + " " + ipmasklen + " " + hop);
                            }
                            // == num ==
                            num++;
                        }
                    }//end of j
                    nodeID = nodeID * 2 + 2;
                    span *= 2;
                }
            }

            //== next node==
            for (int i = 0; i < (1 << (this.branch - masklen)); i++) {
                int index = ip_offset + i;
                if (this.nhop[index] != 0) {
                    int hop = this.nhop[index];
                    int ipmasklen = baseMasklen + this.branch;
                    int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                    int ip = (baseIP << (this.branch)) + ip_offset + i;
                    ip = ip << (32 - ipmasklen);

                    if (strFormat != null) {
                        out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen, hop));
                    } else {
                        out.println(IPv4Util.IP2Str(ip) + " " + ipmasklen + " " + hop);
                    }
                    // = num ==
                    num++;
                }
            }

        } else if (masklen == this.branch) {
            int ip_offset = getIPFragmentOffset(ipaddr, this.branch);
            if (this.nhop[ip_offset] != 0) {
                int hop = this.nhop[ip_offset];
                int ipmasklen = baseMasklen + this.branch;
                int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                int ip = (baseIP << this.branch) + ip_offset;
                ip = ip << (32 - ipmasklen);

                if (strFormat != null) {
                    out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen, hop));
                } else {
                    out.println(IPv4Util.IP2Str(ip) + " " + ipmasklen + " " + hop);
                }

                num++;
            }

        } else { //masklen > this.branch
            ;//
        }

        return num;
    }

    protected int printEPRoute(int baseIP, int baseMasklen, String strFormat, PrintStream out) {
        int num = 0;

        num += printEPRoute(0, 1, baseIP, baseMasklen, strFormat, out);
        num += printEPRoute(1 << 31, 1, baseIP, baseMasklen, strFormat, out);

        return num;
    }

    /**
     * print only route, without Expansion route
     *
     * @param ipaddr
     * @param masklen
     * @param baseIP
     * @param baseMasklen
     * @param strFormat
     * @param out
     * @return
     */
    protected int printRoute(int ipaddr, int masklen, int baseIP, int baseMasklen, String strFormat, PrintStream out) {
        int num = 0;

        if (masklen <= 0) {
            ; //nothing to do!
        } else if (masklen < this.branch) {
            int d = this.branch - masklen;
            int span = 1;  //
            int ip_offset = getIPFragmentOffset(ipaddr, masklen);
            int nodeID = getNodIDfromOffset(ip_offset, masklen);

            if (this.lhop != null) {//
                for (int i = 0; i < d; i++) { //from level of masklen to branch-1
                    for (int j = 0; j < span; j++) {
                        int index = nodeID + j;
                        if (this.lhop[nodeID + j] != 0) {//find rib item!
                            int hop = this.lhop[index];
                            int ipmasklen = baseMasklen + masklen + i;
                            int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                            int ip = (baseIP << (masklen + i)) + (ip_offset >>> (this.branch - masklen - i)) + j;
                            ip = ip << (32 - ipmasklen);

                            if (strFormat != null) {
                                out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen));
                            } else {
                                out.println(IPv4Util.IP2Str(ip) + "/" + ipmasklen + " ");
                            }
                            // == num ==
                            num++;
                        }
                    }//end of j
                    nodeID = nodeID * 2 + 2;
                    span *= 2;
                }
            }

            //== next node==
            for (int i = 0; i < (1 << (this.branch - masklen)); i++) {
                int index = ip_offset + i;
                if (this.nhop[index] != 0 && this.isRib.get(index)) {
                    int hop = this.nhop[index];
                    int ipmasklen = baseMasklen + this.branch;
                    int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                    int ip = (baseIP << (this.branch)) + ip_offset + i;
                    ip = ip << (32 - ipmasklen);

                    if (strFormat != null) {
                        out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen));
                    } else {
                        out.println(IPv4Util.IP2Str(ip) + "/" + ipmasklen + " ");
                    }
                    // = num ==
                    num++;
                }
            }
        } else if (masklen == this.branch) {
            int ip_offset = getIPFragmentOffset(ipaddr, this.branch);
            if (this.nhop[ip_offset] != 0) {
                int hop = this.nhop[ip_offset];
                int ipmasklen = baseMasklen + this.branch;
                int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                int ip = (baseIP << this.branch) + ip_offset;
                ip = ip << (32 - ipmasklen);

                if (strFormat != null) {
                    out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask) + "/" + ipmasklen));
                } else {
                    out.println(IPv4Util.IP2Str(ip) + "/" + ipmasklen + " ");
                }

                num++;
            }

        } else { //masklen > this.branch
            ;//
        }

        return num;
    }

    protected int printRoute(int baseIP, int baseMasklen, String strFormat, PrintStream out) {
        int num = 0;

        num += printRoute(0, 1, baseIP, baseMasklen, strFormat, out);
        num += printRoute(1 << 31, 1, baseIP, baseMasklen, strFormat, out);

        return num;
    }
    //==============================

}

class StrideTrieScheme {
    protected int[] m_steps;
    protected int index = 0;

    /**
     * @param steps
     */
    public StrideTrieScheme(int[] steps) {
        if ((steps == null) || (steps.length <= 0)) {
            throw new InvalidParameterException("scheme such as '8-8-8-8',... ");
        }
        m_steps = new int[steps.length];
        int sum = 0;

        for (int i = 0; i < steps.length; i++) {
            m_steps[i] = (steps[i]);
            sum += m_steps[i];  //
        }

        if (sum != 32) {
            throw new InvalidParameterException("scheme such as '8-8-8-8',... and the sum must be 32.");
        }
    }

    /**
     * @return
     */
    public int nextStride() {
        if (index >= m_steps.length) return 0;
        return m_steps[index++];
    }
}

class TrieInfo {
    public int next;
    public int nhop = 0;
    public int lhop = 0;
    public int bitSet = 0;
    public int entry = 0;

    public TrieInfo() {

    }

    public TrieInfo add(TrieInfo val) {
        this.next += val.next;
        this.nhop += val.nhop;
        this.lhop += val.lhop;
        this.bitSet += val.bitSet;
        this.entry += val.entry;

        return this;
    }
}

/**
 * visit helper!
 */
class TrieVisit {
    public int baseIP;
    public int baseMasklen;
    public FixNode node;

    public TrieVisit(FixNode node, int baseIP, int baseMasklen) {
        this.node = node;
        this.baseIP = baseIP;
        this.baseMasklen = baseMasklen;
    }
}