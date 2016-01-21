package com.siwind.trie;


import com.siwind.tools.ArrayQueue;
import com.siwind.tools.IPv4Util;
import com.siwind.tools.Stack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidParameterException;

/**
 * Created by wang on 16-1-8.
 */
public class StrideTrie {

    protected int[] m_steps;
    protected FixNode m_root;

    public StrideTrie(int... args){
        m_steps = new int[args.length];
        System.arraycopy(args,0,m_steps,0,args.length);
        m_root = new FixNode(new StrideTrieScheme(m_steps));
    }

    public boolean addEntry(int ipaddr, int masklen,int nhop){
        return m_root.updateEntry(ipaddr,masklen,nhop,false,new StrideTrieScheme(m_steps)); //not delete
    }
    public boolean delEntry(int ipaddr, int masklen){
        return m_root.updateEntry(ipaddr,masklen,0,true,null); //not delete
    }

    public void show(){

    }
    /**
     * add [ipaddr/masklen] via [nexthop]
     * @param strCmd
     * @param trie
     */
    public static boolean addCmd(String strCmd,StrideTrie trie){
        try{
            if( strCmd.isEmpty() ) return false;
            String[] strs = strCmd.split(" ");
            if( strs[0].compareTo("add")!=0 ) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if( !IPv4Util.IPandMasklenfromStr(strs[1],ips) ) return false;
            if( strs[2].compareTo("via")!=0 ) return false;

            int nhop = 0;
            nhop = Integer.parseInt(strs[3]);

            trie.addEntry(ips[0],ips[1],nhop);

        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            ex.printStackTrace();
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
            String[] strs = strCmd.split(" ");
            if( strs[0].compareTo("del")!=0 ) return false;
            int[] ips = new int[2];
            //get ip address and netmasklen
            if( !IPv4Util.IPandMasklenfromStr(strs[1],ips) ) return false;

            trie.delEntry(ips[0],ips[1]);
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
            if( strCmd.compareTo("show")!=0 ) return false;

            //trie.delEntry(ips[0],ips[1]);
            FixNode.show(trie.m_root); //show
        }catch (Exception ex){
            System.out.println(strCmd + " failed!");
            ex.printStackTrace();
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
    public static void showMenu(){
        StrideTrie trie = new StrideTrie(8,8,8,8);

        String strMenu = "Available command are: \n" +
                "add [ipaddr/masklen] via [nexthop] \n" +
                "del [ipaddr/masklen] \n" +
                "show \n" +
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

            strCmd = strLine.split(" ")[0];

            if( (strCmd.compareTo("quit") == 0) || (strCmd.compareTo("exit")==0) ) break;
            else if(strCmd.compareTo("add") == 0 ){
                addCmd(strLine,trie);

            }else if(strCmd.compareTo("del") == 0 ){
                delCmd(strLine,trie);

            }else if(strCmd.compareTo("show") == 0 ){
                showCmd(strLine,trie);

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
        showMenu();
        System.out.println('9' - '4');
    }

    //======================
    public static void test_FixNode(){
        FixNode.travelPreOrder(8,0);

        StrideTrie trie = new StrideTrie(8,8,8,8);

    }
}


class FixNode{

    protected int branch = 0;
    protected int skip = 0;
    protected int skip_len = 0;

    protected FixNode[] next = null;
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

        if( masklen>= branch ) return;//?

        //int nodeID = prefix + ((1<<masklen)-2);

        int level = masklen;

        Stack<Integer> st = new Stack<Integer>(branch -level +4);  //initail size must less than branch-level.

        int index = nodeID;
        int bound = (1<<(branch-1)) -2;  //getLessBranchSize();

        int start = getNodeStartIndex(nodeID);
        int end = start;
        int i = 0;
        do {
            if( (index<bound) && (this.lhop[index]==0) ){// not reach leaf!
                index = 2*index +2;  //goto left node!
                st.Push(index+1);    //right node push stack.

            } else{// if((index<bound) && (this.lhop[index]!=0)){
                if( (index>=bound) && (this.lhop[index] == 0) ) {//need update!
                    end = getNodeEndIndex(index)+1;
                    for (i = start; i < end; i++) {
                        this.nhop[i] = nhop;  //update next hop!
                    }
                }

                start = getNodeEndIndex(index) +1; //update start index.

                if (!st.isEmpty() ){
                    index = st.Pop();
                } else{
                    break;  // end travel now!
                }
            } //end if!

        }while (true);
    }


    /**
     * Addd entry to routing table!
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return
     */
    public boolean updateEntry(int ipaddr, int masklen, int nhop,boolean bDel, StrideTrieScheme scheme){
        if( masklen<=0 ) return false;

        int ip = getIPFragmentOffset(ipaddr,branch);

        if(masklen < branch ){
            if( lhop==null ){ //lazy init!
                this.lhop = new int[this.getLessBranchSize()];
            }

            int nodeID = getNodIDfromOffset(ip,masklen);

            if( (bDel && this.lhop[nodeID]!=0) || !bDel ){
                travelPreOrderUpdateLHop((nodeID<<1)+2,masklen+1,nhop);
                travelPreOrderUpdateLHop((nodeID<<1)+3,masklen+1,nhop);

                this.lhop[nodeID] = nhop;  //mark being added
            }

        }else {//masklen > branch

            //this.nhop[ip] = nhop;
            if( masklen == branch ){
                this.nhop[ip] = nhop;
            }
            else {//( masklen>branch )

                int newIP = ipaddr<<branch;
                int newMasklen = masklen - branch;

                if( !bDel ) {
                    //only when is add entry!
                    int step = scheme.nextStride();  //move to next stride trie.

                    if ((this.next[ip] == null)) {
                        this.next[ip] = new FixNode(step);  //next fix stride trie!
                    }
                    this.next[ip].updateEntry(newIP, newMasklen, nhop, bDel,scheme);
                } else if( this.next[ip]!=null){ //bDel is true!
                    this.next[ip].updateEntry(newIP, newMasklen, nhop, bDel,scheme);
                }
            }
        }

        return true;
    }

    protected void printRoute(int baseIP,int baseMasklen,String strFormat){
        int i,j,count,index;
        int ip,ipmask,ipmasklen,hop;

        count = 1;
        index = 0; //nodID
        int[] nhop = this.nhop; //

        if( lhop!= null) {

            nhop = new int[this.nhop.length];
            System.arraycopy(this.nhop,0,nhop,0,this.nhop.length);

            for (i = 1; i < branch; i++) {  //every level from 1
                count = count << 1;   //double element!
                for (j = 0; j < count; j++, index++) {
                    if (lhop[index] != 0) {//find routing!
                        hop = lhop[index];
                        ipmasklen = baseMasklen + i; //
                        ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);

                        ip = (baseIP<<branch) + ((index + 2 - count)<<(branch-i));
                        ip = ip << (32-(branch+baseMasklen));   //ip address!

                        System.out.println(String.format(strFormat, IPv4Util.IP2Str(ip), IPv4Util.IP2Str(ipmask)+"/"+ipmasklen, hop));

                        //== clear short routing info
                        int p ,q,r;
                        p = getNodeStartIndex(index);
                        q = getNodeEndIndex(index);
                        for (r=p;r<=q;r++){
                            nhop[r] = 0; //
                        }
                    }
                }
            }
        }
        ipmasklen = branch + baseMasklen;
        ipmask = IPv4Util.NetmaskFromMasklen(ipmasklen);;
        for(i=0;i<getBranchSize();i++){
            if( nhop[i] !=0 ){
                ip = (baseIP<<branch) + i;
                ip = ip<<(32-(branch+baseMasklen));

                hop = nhop[i];
                System.out.println(String.format(strFormat,IPv4Util.IP2Str(ip),IPv4Util.IP2Str(ipmask)+"/"+ipmasklen,hop ));
            }
        }
    }
    public static void show(FixNode root){
        if( root==null ) return;
        ArrayQueue<TrieVisit> queue = new ArrayQueue<TrieVisit>(5*(1<<8));

        int baseIP=0,baseMasklen=0;

        String strFormat = "%20s %20s %15d";

        queue.push(new TrieVisit(root,0,0));
        TrieVisit nodeVisit;

        System.out.println(String.format("%20s %20s %15s","IPAddress","Netmask","Nexthop"));

        do {
            nodeVisit = queue.pop();

            nodeVisit.node.printRoute(nodeVisit.baseIP,nodeVisit.baseMasklen,strFormat);

            for(int i=0;i<nodeVisit.node.getBranchSize();i++){
                if( nodeVisit.node.next[i] != null ){
                    baseIP = (nodeVisit.baseIP<<nodeVisit.node.branch) + i;
                    baseMasklen = nodeVisit.baseMasklen + nodeVisit.node.branch;

                    queue.push(new TrieVisit(nodeVisit.node.next[i],baseIP,baseMasklen));
                }
            }

        }while(!queue.isEmpty());

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
        if( steps.length<=0 ){
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
        return m_steps[index++];
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