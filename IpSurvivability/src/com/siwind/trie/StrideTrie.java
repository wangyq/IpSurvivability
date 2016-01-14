package com.siwind.trie;

import java.util.Stack;

/**
 * Created by wang on 16-1-8.
 */
public class StrideTrie {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

    }
}


class FixNode{

    protected int branch = 0;
    protected int skip = 0;
    protected int skip_len = 0;

    protected FixNode[] next = null;
    protected int nhop[] = null;  //the size is 2^branch
    protected int lhop[] = null;  //the size is 2^brach - 2


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
    public FixNode(int branch,int skip,int skip_len){
        //
        this.branch = ((branch>=0)&&(branch<32))?branch:0;
        this.next = new FixNode[getBranchSize()];  //next pointer table!
        this.nhop = new int[getBranchSize()];      //next hop items, maybe using lazi-allocation

        //skip
        this.skip_len = ((skip_len>=0)&&(skip_len<32))?skip_len:0;
        this.skip = skip&((1<<this.skip_len)-1);  //make sure skip is right!
    }

    protected int getBranchSize(){
        return 1<<branch;
    }

    protected int getLessBranchSize(){
        return (1<<branch) - 2;
    }

    protected int getIPFragment(int ipaddr, int fragmentlen){
        if( fragmentlen<=0 ) return 0;
        if( fragmentlen>=32 ) return ipaddr;
        int striplen = 32 - fragmentlen;
        return (ipaddr >>> striplen) & ((1<<fragmentlen)-1);  //caution: must zero-right-shift of >>>
    }

    /**
     *
     * @param ipaddr
     * @param masklen
     * @param nhop
     * @return
     */
    protected boolean addEntryFragment(int ipaddr, int masklen,int nhop){
        Stack<Integer> stack = new Stack<Integer>();

        int upbound = getLessBranchSize();
        int start = getIPFragment(ipaddr,masklen);
        int index = start <<(branch-masklen); // nexthop index to be modify!

        stack.push(start);

        while( ! stack.empty() ){
            do {
                int left = 2 * stack.peek() + 2;
                if (left < upbound) stack.push(left);
                else break;
            }while( true);    //left child into stack

            int cur = stack.pop();
            if( lhop[cur] == 0 ){ //here need expand branch for next

            }
            stack.push(cur+1); //right child

            if( (cur & 0x1) == 0){//cur is left node
                cur ++;
                if( cur < upbound ) stack.push(cur);
            } else{ //cur is right

            }
            //
            int right = 2 * stack.peek() + 3;
        }

        return true;
    }
    public boolean addEntry(int ipaddr, int masklen, int nhop){
        if( masklen<=0 ) return false;

        if( masklen == branch ){ //just add exact entry.
            int ip = getIPFragment(ipaddr,branch);
            this.nhop[ip] = nhop;

        } else if(masklen < branch ){

        }else{//masklen > branch

        }

        return true;
    }

}