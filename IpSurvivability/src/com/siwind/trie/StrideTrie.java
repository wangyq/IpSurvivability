package com.siwind.trie;


import com.siwind.tools.ArrayQueue;
import com.siwind.tools.IPv4Util;
import com.siwind.tools.Stack;
import com.siwind.tools.Util;

import java.io.*;
import java.security.InvalidParameterException;
import java.util.BitSet;

/**
 * Created by wang on 16-1-8.
 */
public class StrideTrie {

    protected int[] m_steps;
    protected FixNode m_root;
    protected int m_total=0;       //total rib entry!

    protected void init(){
        init(null);
    }
    protected void init(int[] steps){
        m_total = 0;
        if( steps!=null ){
            m_steps = new int[steps.length];
            System.arraycopy(steps,0,m_steps,0,steps.length);
        }
        m_root = new FixNode(new StrideTrieScheme(m_steps));
    }
    public StrideTrie(int... args){
        init(args);
    }

    public boolean addEntry(int ipaddr, int masklen,int nhop){
        if( masklen<=0 || masklen>32 ) return false;
        StrideTrieScheme scheme = new StrideTrieScheme(m_steps);
        scheme.nextStride();

        int oldHop =  m_root.addEntry(ipaddr,masklen,nhop,scheme); //not delete
        if( oldHop ==0 ){
            m_total ++ ; //add success!
        }
        return true;
    }
    public boolean delEntry(int ipaddr, int masklen){
        if( masklen<=0 || masklen>32 ) return false;

        int oldHop = m_root.delEntry(ipaddr,masklen,0); //not delete
        if( oldHop!=0 ){
            m_total --; //del success!
        }
        return true;
    }

    /**
     * add [ipaddr/masklen] via [nexthop]
     * @param strCmd
     * @param trie
     */
    public static boolean addCmd(String strCmd,StrideTrie trie){
        try{
            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("add")!=0 ) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if( !IPv4Util.IPandMasklenfromStr(strs[1],ips) ) return false;
            if( strs[2].compareTo("via")!=0 ) return false;

            int nhop = 0;
            nhop = Integer.parseInt(strs[3]);

            trie.addEntry(ips[0],ips[1],nhop);

            System.out.println("Total entries is : " + trie.m_total);

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * del [ipaddr/masklen]
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean delCmd(String strCmd, StrideTrie trie){

        try{

            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("del")!=0 ) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if( !IPv4Util.IPandMasklenfromStr(strs[1],ips) ) return false;

            trie.delEntry(ips[0],ips[1]);
            System.out.println("Total entries is : " + trie.m_total);

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }

    /**
     * clear [step step step step ...]
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean clearCmd(String strCmd, StrideTrie trie){

        try{

            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("clear")!=0 ) return false;

            int[] steps = null;
            if( strs.length>1 ){
                steps = new int[strs.length-1];
                for(int i=0;i<steps.length;i++) steps[i] = Integer.parseInt(strs[i+1]);
            }

            FixNode.deReferenceAll(trie.m_root);
            trie.init(steps);

            System.out.println("Total entries is : " + trie.m_total);

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }
    /**
     *
     * @param trie
     * @param strFile
     * @param maxNum: number record to read in. 0 means read total file.
     * @return
     */
    public static boolean loadFromFile(StrideTrie trie,String strFile, int maxNum){

        boolean bOK = false;
        int num = 0; //
        BufferedReader in = null;
        String line="";
        try
        {
            in=new BufferedReader(new FileReader(strFile));

            while ((line=in.readLine() )!=null)
            {
                //System.out.println(line);
                line=line.trim();
                if( line.isEmpty() ) continue;

                String[] strs = line.trim().split("\\s+");
                if( strs.length<3) continue; //wrong format!

                if( !IPv4Util.isIPv4Valid(strs[0])) continue;
                int ip = IPv4Util.IPfromStr(strs[0]);
                int masklen = Integer.parseInt(strs[1]);
                int nhop = Integer.parseInt(strs[2]);

                trie.addEntry(ip,masklen,nhop);

                if( maxNum>0 ){
                    num++;
                    if( num>=maxNum ){  //read finished!
                        break;
                    }
                }
            }

            bOK = true;
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Load file error : " + line);

        }finally {
            try {
                if( in!=null ) in.close();
            }catch (IOException ex){

            }
        }

        return bOK;
    }

    public static boolean saveToFile(StrideTrie trie,String strFile){

        boolean bOK = false;
        File file = null;
        PrintStream ps = null;
        try
        {
            file = new File(strFile);

            ps = new PrintStream(new FileOutputStream(file));

            FixNode.saveToFile(trie.m_root,ps);

            bOK = true;
        } catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Save file error : " + strFile);

        }finally {
            try {
                if( ps!=null ) ps.close();
            }finally {

            }

        }

        return bOK;
    }
    /**
     * load [file]
     * and file line is:
     *     x.x.x.x/y
     *
     * @param strCmd
     * @param trie
     * @return
     */
    public static boolean loadCmd(String strCmd, StrideTrie trie){

        try{

            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("load")!=0 ) return false;

            String strFile = strs[1];
            if( strFile.charAt(0) != '/'){
                strFile = Util.getCurDir() + strFile;
            }
            int maxNum = 0;
            if( strs.length>=3 ){
                maxNum = Integer.parseInt(strs[2]);
            }
            //System.out.println("Current Directory: " + Util.getCurDir());

            loadFromFile(trie,strFile,maxNum);

            System.out.println("Total entries is : " + trie.m_total);

            //get ip address and netmasklen

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static boolean saveCmd(String strCmd, StrideTrie trie){

        try{

            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("save")!=0 ) return false;

            String strFile = strs[1];
            if( strFile.charAt(0) != '/'){
                strFile = Util.getCurDir() + strFile;
            }

            saveToFile(trie,strFile);

            System.out.println("Save to File completed! Total entries is " + trie.m_total);

            //get ip address and netmasklen

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static boolean showCmd(String strCmd, StrideTrie trie){

        try{
            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("show")!=0 ) return false;

            int[] ips = {0,0};
            if( strs.length>=2 ) {
                //get ip address and netmasklen
                if (!IPv4Util.IPandMasklenfromStr(strs[1], ips)) {
                    throw new InvalidParameterException("Command parameter wrong!");
                    //return false;
                }
            }

            int num = FixNode.show(trie.m_root,ips[0],ips[1]); //show
            System.out.println("Number nodes to show : " + num);

            System.out.println("Total entries is : " + trie.m_total);

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static boolean infoCmd(String strCmd, StrideTrie trie){

        try{
            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split("\\s+");
            if( strs[0].compareTo("info")!=0 ) return false;

            TrieInfo data = FixNode.info(trie.m_root);

            int total = data.next + data.nhop + data.bitSet + data.lhop;
            float ratio = (float) (((data.entry)*100.0)/total);
            float ratio1 = (float) (((float)total)/(data.entry));

            String strFormat  = "%8s %14s %14s %10s %12s %14s %16.4f %16.2f";
            String strFormat1 = "%8s %14s %14s %10s %12s %14s %16s %16s";
            System.out.println(String.format(strFormat1,"Entry","nexthop","next","bitSet","lhop","TOTAL","Entry/Total(%)","Total/Entry"));

            System.out.println(String.format(strFormat,data.entry,data.nhop,data.next,data.bitSet,data.lhop,total,ratio,ratio1));

            System.out.print("Scheme = ( ");
            for(int i=0;i<trie.m_steps.length;i++ ) System.out.print(trie.m_steps[i] + " ");
            System.out.println(" )");

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            //ex.printStackTrace();
            return false;
        }
        return true;

    }
    public static String readLine(){
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in ));

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
    public static void showMenu(StrideTrie trie){

        String strMenu = "Available command are: \n" +
                "add [ipaddr/masklen] via [nexthop] \n" +
                "del [ipaddr/masklen] \n" +
                "clear [step step step step ...]  \n" +
                "load [file] [maxLine]\n" +
                "save [file] \n" +
                "show [ipaddr/masklen] \n" +
                "info \n" +
                "help | ? \n" +
                "exit | quit \n" +
                "";

        String strLine ;
        String strCmd;

        int iTotal = 0;
        do {
            //System.out.println(strCmd);
            System.out.print("In[" + (iTotal++) + "]: ");

            strLine = readLine();
            strLine = strLine.trim();
            strLine.toLowerCase();

            strCmd = strLine.split("\\s+")[0];

            if( strCmd.isEmpty() ) continue;

            System.out.println("Out[" + (iTotal++) + "]: ");

            if( (strCmd.compareTo("quit") == 0) || (strCmd.compareTo("exit")==0) ) break;
            else if(strCmd.compareTo("add") == 0 ){
                addCmd(strLine,trie);

            }else if(strCmd.compareTo("del") == 0 ){
                delCmd(strLine,trie);

            }else if(strCmd.compareTo("clear") == 0 ){
                clearCmd(strLine,trie);

            }else if(strCmd.compareTo("load") == 0 ){
                loadCmd(strLine,trie);

            }else if(strCmd.compareTo("save") == 0 ){
                saveCmd(strLine,trie);

            }else if(strCmd.compareTo("show") == 0 ){
                showCmd(strLine,trie);

            }else if(strCmd.compareTo("info") == 0 ){
                infoCmd(strLine,trie);

            }else if( (strCmd.compareTo("help") == 0) || (strCmd.compareTo("?") == 0) ){
                System.out.println(strMenu);
            } else{
                System.out.println("Unkown command '" + strLine + "' !");
            }
        }while(true);

    }
    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //test_FixNode();
        runTrie();
        //System.out.println('9' - '4');
    }

    //======================
    public static void test_FixNode(){
        FixNode.travelPreOrder(8,0);

    }
    public static void runTrie(){
        int[] steps={8,8,8,8};//{16,8,4,4};
        StrideTrie trie = new StrideTrie(steps);

        //StrideTrie trie = new StrideTrie(16,8,4,4);

        showMenu(trie);
    }
}


class FixNode{

    protected int branch = 0;
    protected int skip = 0;
    protected int skip_len = 0;

    protected FixNode[] next = null;
    protected BitSet isRib = null;
    protected int nhop[] = null;  //the size is 2^branch
    protected int lhop[] = null;  //the size is 2^brach - 2

    protected StrideTrieScheme m_scheme;

    /**
     *
     * @param scheme
     */
    public FixNode(StrideTrieScheme scheme){
        this(scheme.nextStride(),0,0);
    }
    /**
     *
     * @param branch
     */
    public FixNode(int branch){
        this(branch,0,0);
    }

    /**
     *
     * @param branch
     * @param skip
     * @param skip_len
     */
    public FixNode(int branch,int skip,int skip_len ){
        //
        this.branch = ((branch>=0)&&(branch<32))?branch:0;
        this.next = new FixNode[getBranchSize()];  //next pointer table!
        this.nhop = new int[getBranchSize()];      //next hop items, maybe using lazi-allocation
        this.isRib = new BitSet(getBranchSize());  //if Rib is here!

        //skip
        this.skip_len = ((skip_len>=0)&&(skip_len<32))?skip_len:0;
        this.skip = skip&((1<<this.skip_len)-1);  //make sure skip is right!
    }

    /**
     * giving parent node of k, it's left and right child node are (2K+2) and (2K+3)
     * @param nodeID
     * @return
     */
    public static int getNodeLevel(int nodeID){
        int level = 1;
        while( nodeID>1 ){
            level ++;
            nodeID = (nodeID-2)/2;
        }
        return level;
    }

    public static  void travelPreOrder(int depth, int nodeID){
        if( depth<=0 || depth >32 ) return;

        long bound = (1<<(depth+1)) - 2;
        if( nodeID>= bound ) return;

        int level = getNodeLevel(nodeID);

        Stack<Long> st = new Stack<Long>(depth -level +4);

        long index = nodeID;
        do {
            if( index<bound ){
                System.out.print(index + " ");
                index = 2*index+2;  //goto left child.
                st.Push(index+1);   //right child into stack
            } else if( !st.isEmpty()){ // index >=bound!
                index = st.Pop();
            } else{// stop now!
                break;
            }
        }while(true);
    }

    protected int getBranchSize(){
        return 1<<branch;
    }

    protected int getLessBranchSize(){
        return (1<<branch) - 2;
    }

    protected int getIPFragmentOffset(int ipaddr,int fragmentlen){//the lowest branch-bit
        int ip = getIPFragment(ipaddr,fragmentlen);
        return ip>>>(32-branch);
    }

    public static int getIPFragment(int ipaddr, int fragmentlen){
        if( fragmentlen<=0 ) return 0;
        if( fragmentlen>=32 ) return ipaddr;
        int striplen = 32 - fragmentlen;
        return ipaddr & (((1<<fragmentlen)-1)<<(32-fragmentlen));  //caution: must zero-right-shift of >>>
    }

    protected int getNodIDfromOffset(int ip,int level){//from nhop offset and level , get nodeID
        int nodeID = ip>>(branch-level);
        return nodeID + (1<<level) -2   ;
    }

    /**
     * giving node of m, its nexthop's start index is (m+2)*2^d - 2^branch. where d=branch - level(m)
     * @param nodeID
     * @return
     */
    protected int getNodeStartIndex(int nodeID){
        int l = getNodeLevel(nodeID);
        int d = branch -l;

        return ((nodeID+2)<<d) - (1<<branch);
    }

    protected int getNodeEndIndex(int nodeID){
        int l = getNodeLevel(nodeID);
        int d = branch -l;

        return getNodeStartIndex(nodeID) + (1<<d) -1;
    }

    /**
     *
     * @param nodeID : nodeID from 0,1,..., (2^branch -3)
     * @param masklen : from 1 to branch
     * @param nhop
     */
    protected void travelPreOrderUpdateLHop(int nodeID, int masklen, int nhop){

        //if( masklen>= branch ) return;//?

        //int nodeID = prefix + ((1<<masklen)-2);

        int level = masklen;
        int bound = (1<<(branch-1)) -2;  //getLessBranchSize();
        int i = 0;

        if( nodeID>=bound ){ //here do NOT need go through child node
            if( this.lhop[nodeID] !=0 ){
                i = (nodeID<<1) + 4 - (1<<branch);//getNodeStartIndex(nodeID); //
                if( !this.isRib.get(i)) this.nhop[i] = nhop;
                if( !this.isRib.get(i+1)) this.nhop[i] = nhop;
            }
        }else {//travel from left to right sub-tree

            int index = (nodeID<<1) + 2; //left child!

            Stack<Integer> st = new Stack<Integer>(branch -level +4);  //initail size must less than branch-level.

            st.Push(index+1); //right child push into stack

            do {
                if ((index < bound) && (this.lhop[index] == 0)) {// not reach leaf!
                    index = (index<<1) + 2 ;//2 * index + 2;  //goto left node!
                    st.Push(index + 1);    //right node push stack.

                } else {// if((index<bound) && (this.lhop[index]!=0)){
                    if ((index >= bound) && (this.lhop[index] == 0)) {//need update!
                        i = (index<<1) + 4 - (1<<branch);
                        if( !this.isRib.get(i)) this.nhop[i] = nhop;
                        if( !this.isRib.get(i+1)) this.nhop[i] = nhop;
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
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return the old Next hop
     */
    protected int updateEntry(int ipaddr, int masklen, int nhop,boolean bDel, StrideTrieScheme scheme){
        //if( masklen<=0 || masklen>32 ) return false;
        int oldHop = 0;



        return oldHop;
    }

    /**
     *
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return
     */
    public int delEntry(int ipaddr, int masklen, int nhop){
        int oldHop = 0;
        int ip = getIPFragmentOffset(ipaddr,branch);

        if( masklen>branch ){
            if(this.next[ip]!=null) {
                int newIP = ipaddr << branch;
                int newMasklen = masklen - branch;

                oldHop = this.next[ip].delEntry(newIP, newMasklen, nhop);
            }

        }else if( masklen==branch ){ //
            if(this.nhop[ip]!=0) {
                oldHop = this.nhop[ip];
                this.nhop[ip] = 0;
                this.isRib.clear(ip); //clear the rib info!
            }

        }else { //( masklen < branch){
            if(this.lhop!=null) {
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
     *
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @param scheme
     * @return
     */
    public int addEntry(int ipaddr, int masklen, int nhop,StrideTrieScheme scheme){
        int oldHop = 0;
        int ip = getIPFragmentOffset(ipaddr,branch);

        if( masklen>branch ){// go through next level trie-node!
            int newIP = ipaddr<<branch;
            int newMasklen = masklen - branch;

            int step = scheme.nextStride();  //move to next stride trie.

            if ((this.next[ip] == null)) {
                this.next[ip] = new FixNode(step);  //next fix stride trie!
            }
            oldHop = this.next[ip].addEntry(newIP,newMasklen,nhop,scheme);

        }else if( masklen==branch ){

            if( this.isRib.get(ip) ){// here old next hop is valid!
                oldHop = this.nhop[ip]; //old next hop
            } else{
                this.isRib.set(ip);  //set rib mark!
            }

            if( this.nhop[ip] != nhop ){
                this.nhop[ip] = nhop;
            }
        }else { //( masklen < branch){
            if( this.lhop==null ){ //lazy init!
                this.lhop = new int[this.getLessBranchSize()];
            }
            int nodeID = getNodIDfromOffset(ip,masklen);

            oldHop = this.lhop[nodeID]; //zero or last rib entry!

            if( this.lhop[nodeID] != nhop ) { //add new next hop!
                //oldHop = this.lhop[nodeID]; //
                travelPreOrderUpdateLHop(nodeID, masklen, nhop);
                this.lhop[nodeID] = nhop;  //mark being added
            }
        }
        return oldHop;
    }

    public static FixNode queryNode(FixNode root, int ipaddr, int masklen){
        if( (null == root) || (masklen<=0) ) return root;

        FixNode node = root;

        while ( ( node!=null ) && (node.branch < masklen) ){
            masklen -= node.branch;
            int index = node.getIPFragmentOffset(ipaddr,node.branch);
            ipaddr = (ipaddr<<node.branch); //new ip address

            node = node.next[index];

        }

        return node;
    }

    protected TrieInfo getInfo(){
        TrieInfo val = new TrieInfo();

        val.next = this.getBranchSize();
        val.nhop = this.getBranchSize();
        val.lhop = (this.lhop!=null)?this.getLessBranchSize():0;
        val.bitSet = this.getBranchSize() >>> 5; //2^6 = 64; a long, which consists of 64 bits, and a int which consists of 32 bits

        val.entry = 0;
        if( this.lhop!=null) {
            for (int i = 0; i < this.getLessBranchSize(); i++) {
                if( this.lhop[i]!=0 ) val.entry++;
            }
        }
        for(int i=0;i<this.getBranchSize();i++){
            if( this.isRib.get(i) ) val.entry++;
        }
        return val;
    }

    protected int printRoute(int baseIP,int baseMasklen,String strFormat, PrintStream out){
        int i,j,count,index;
        int ip,ipmask,ipmasklen,hop;
        int num = 0;

        count = 1;
        index = 0; //nodID

        baseIP = (baseIP<<branch);

        if( lhop!= null) {
            for (i = 1; i < branch; i++) {  //every level from 1
                count = count << 1;   //double element!
                for (j = 0; j < count; j++, index++) {
                    if (lhop[index] != 0) {//find routing!
                        hop = lhop[index];
                        ipmasklen = baseMasklen + i; //
                        ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                        ip = (baseIP) + ((index + 2 - count)<<(branch-i));
                        ip = ip << (32-(branch+baseMasklen));   //ip address!

                        if( strFormat!=null ){
                            out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask)+"/"+ipmasklen, hop));
                        } else{
                            out.println(IPv4Util.IP2Str(ip) + " " + ipmasklen + " " + hop);
                        }

                        num++;
                    }
                }
            }
        }
        ipmasklen = branch + baseMasklen;
        ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);;
        for(i=0;i<getBranchSize();i++){
            if( this.isRib.get(i) ){
                ip = (baseIP) + i;
                ip = ip<<(32-(branch+baseMasklen));

                hop = this.nhop[i];
                if( strFormat!=null ){
                    out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask)+"/"+ipmasklen, hop));
                } else{
                    out.println(IPv4Util.IP2Str(ip) + " " + ipmasklen + " " + hop);
                }
                num++;
            }
        }

        return num;
    }

    /**
     *
     * @param root
     */
    public static void deReferenceAll(FixNode root){
        if( root==null ) return ;
        ArrayQueue<FixNode> queue = new ArrayQueue<FixNode>(5*(1<<8),(1<<8));

        queue.push(root);
        do{
            FixNode node = queue.pop();
            for(int i=0;i<node.getBranchSize();i++){
                if( node.next[i] !=null ){
                    queue.push(node.next[i]);
                    node.next[i] = null;     //dereference!
                }
            }
        }while(!queue.isEmpty());
    }

    public static int saveToFile(FixNode root,PrintStream ps){
        if( root==null ) return 0;
        ArrayQueue<TrieVisit> queue = new ArrayQueue<TrieVisit>(5*(1<<8),(1<<8));

        int num = 0;

        int baseIP=0,baseMasklen=0;

        queue.push(new TrieVisit(root,0,0));
        TrieVisit nodeVisit;

        do {
            num ++;
            nodeVisit = queue.pop();

            nodeVisit.node.printRoute(nodeVisit.baseIP,nodeVisit.baseMasklen,null,ps);

            for(int i=0;i<nodeVisit.node.getBranchSize();i++){
                if( nodeVisit.node.next[i] != null ){
                    baseIP = (nodeVisit.baseIP<<nodeVisit.node.branch) + i;
                    baseMasklen = nodeVisit.baseMasklen + nodeVisit.node.branch;

                    queue.push(new TrieVisit(nodeVisit.node.next[i],baseIP,baseMasklen));
                }
            }

        }while(!queue.isEmpty());

        return num;
    }

    /**
     *
     * @param root
     */
    public static TrieInfo info(FixNode root){
        TrieInfo data = new TrieInfo();

        if( root==null ) return data;

        ArrayQueue<FixNode> queue = new ArrayQueue<FixNode>(5*(1<<8),(1<<8));

        queue.push(root);

        do {
            FixNode node = queue.pop();  //

            data.add(node.getInfo());

            for(int i=0;i<node.getBranchSize();i++){
                if( node.next[i]!=null ) queue.push(node.next[i]);
            }

        }while(!queue.isEmpty());

        return data;
    }

    public static int show(FixNode root){
        return show(root,0,0);
    }
    /**
     *
     * @param root
     * @return
     */
    public static int show(FixNode root, int ipaddr, int masklen){
        if( root==null ) return 0;

        int num = 0;

        int baseIP=0,baseMasklen=0;

        String strFormat = "%20s %20s %15d";

        FixNode node = root;

        System.out.println(String.format("%20s %20s %15s","IPAddress","Netmask","Nexthop"));

        if( masklen>0 ) {
            //node = queryNode(root, ipaddr, masklen); //and new masklen
            while ( ( node!=null ) && (masklen > node.branch) ){
                masklen -= node.branch;
                int index = node.getIPFragmentOffset(ipaddr,node.branch);
                ipaddr = (ipaddr<<node.branch); //new ip address

                //== update baseIP and baseMasklen
                baseMasklen += node.branch; //new baseMasklen
                baseIP = (baseIP << node.branch) + index; //new baseIP

                node = node.next[index];
            }
            if( (node!=null ) && (masklen== node.branch) ){
                int index = node.getIPFragmentOffset(ipaddr,node.branch);

                if( node.nhop[index] !=0 ){//found!
                    int hop = node.nhop[index];
                    int ipmasklen = baseMasklen + node.branch;
                    int ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                    int ip = (baseIP<<node.branch) + index;
                    ip = ip<<(32-ipmasklen);

                    System.out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask)+"/"+ipmasklen, hop));
                }
                //== update baseIP and baseMasklen
                baseMasklen += node.branch; //new baseMasklen
                baseIP = (baseIP << node.branch) + index; //new baseIP

                ipaddr = (ipaddr << node.branch); //new ip address

                masklen -= node.branch;
                node = node.next[index];
            }

        }

        if( node==null ) return 0;

        ArrayQueue<TrieVisit> queue = new ArrayQueue<TrieVisit>(5*(1<<8),(1<<8));
        queue.push(new TrieVisit(node,baseIP,baseMasklen));
        TrieVisit nodeVisit;

        do {
            num ++;
            nodeVisit = queue.pop();

            nodeVisit.node.printRoute(nodeVisit.baseIP,nodeVisit.baseMasklen,strFormat,System.out);

            for(int i=0;i<nodeVisit.node.getBranchSize();i++){
                if( nodeVisit.node.next[i] != null ){
                    baseIP = (nodeVisit.baseIP<<nodeVisit.node.branch) + i;
                    baseMasklen = nodeVisit.baseMasklen + nodeVisit.node.branch;

                    queue.push(new TrieVisit(nodeVisit.node.next[i],baseIP,baseMasklen));
                }
            }

        }while(!queue.isEmpty());

        return num;
    }

    //==============================

}

class StrideTrieScheme {
    protected int[] m_steps;
    protected int index = 0;

    /**
     *
     * @param steps
     */
    public StrideTrieScheme(int[] steps){
        if( (steps==null) || (steps.length<=0) ){
            throw new InvalidParameterException("scheme such as '8-8-8-8',... ");
        }
        m_steps = new int[steps.length];
        int sum = 0;

        for(int i=0;i<steps.length;i++){
            m_steps[i] = (steps[i]);
            sum += m_steps[i];  //
        }

        if( sum!= 32 ){
            throw new InvalidParameterException("scheme such as '8-8-8-8',... and the sum must be 32.");
        }
    }

    /**
     *
     * @return
     */
    public int nextStride(){
        if( index>= m_steps.length ) return 0;
        return m_steps[index++];
    }
}

class TrieInfo{
    public int next;
    public int nhop = 0;
    public int lhop = 0;
    public int bitSet = 0;
    public int entry = 0;

    public TrieInfo(){

    }

    public TrieInfo add(TrieInfo val){
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
class TrieVisit{
    public int baseIP;
    public int baseMasklen;
    public FixNode node;

    public TrieVisit(FixNode node,int baseIP,int baseMasklen){
        this.node = node;
        this.baseIP = baseIP;
        this.baseMasklen = baseMasklen;
    }
}