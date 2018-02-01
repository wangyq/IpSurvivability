package com.siwind.trie;

import com.siwind.tools.ArrayQueue;
import com.siwind.tools.IPv4Util;
import com.siwind.tools.Stack;
import sun.reflect.generics.tree.Tree;

import java.util.*;

import static com.siwind.tools.IPv4Util.IP2Array;

public class BinTrieGen<T>  {
    public final int MAXNHDB=3*1024*1024;
    protected final int MAXLEN=32;

    protected TreeNode<T> root = null;

    protected Stack<TreeNode<T> > stMemory = new Stack<>(MAXNHDB);
    protected ArrayQueue<TreeNode<T> > queueClear = new ArrayQueue<>(MAXNHDB);//for clear use!

    public BinTrieGen(){
        int i = 0;
        while (i<MAXNHDB ){
            i++;
            stMemory.Push(new TreeNode<T>());
        }
        this.root = new TreeNode<>(); //init with ZERO value
    }
    public int getCount(){
        return this.root.count;//number of valid prefix
    }

    public void clear(){
//        if( this.root!= null ) {
//            this.root.clear();
//            //this.root = null;
//        }

        if( this.root.left != null ) queueClear.push(this.root.left);
        if( this.root.right != null ) queueClear.push(this.root.right);

        while (!queueClear.isEmpty()){
            TreeNode<T> node = queueClear.pop();
            if( node.left!= null ) queueClear.push(node.left);
            if( node.right!=null ) queueClear.push(node.right);
            //node.left = node.right = null; no need

        }
    }

    protected TreeNode<T> Malloc(){
        TreeNode<T> node = stMemory.Pop();
        node.left = node.right = null;
        node.data = null;
        return node;
    }

    /**
     *
     * @param ip
     * @param masklen
     * @param value
     * @return value if insert failded, otherwise return old value.
     */
    public boolean insert(int ip, int masklen, T value){
        boolean bOK = false;

        if( masklen<0 || masklen > MAXLEN || value == null) return bOK;

        if( this.root == null ){
            this.root = new TreeNode<>(); //root node
        }
        if( masklen == 0 ){//default
            if( this.root.data == null ){
                this.root.count++;
            }
            this.root.data = value;
        } else {//recursive

            int[] ips = IPv4Util.IP2Array(ip);

            T old = insert(this.root, ips, masklen, value, 0); //array's index starts from zero!
            if( old == null ) this.root.count++;
        }
        return bOK;
    }

    /**
     *
     * @param node
     * @param ips
     * @param masklen
     * @param value
     * @param index
     * @return value if insert failed, otherwise return old value.
     */
    protected T insert(TreeNode<T> node, int[] ips, int masklen, T value, int index){
        T old = value;

        TreeNode<T> cur = null;

        //new node init!
        if(ips[index] ==0 ) {// left
            if (node.left == null) {
                //node.left = new TreeNode<>();
                node.left = Malloc();
            }
            cur = node.left;
        } else if( ips[index] == 1) {
            if (node.right == null) {
                //node.right = new TreeNode<>();
                node.right = Malloc();
            }
            cur = node.right;
        }

        if( index >= masklen-1 ){ //finish now, because index starts from ZERO!
            old = cur.data;
            cur.data = value;
        } else{ //recursive
            old = insert(cur,ips, masklen,value,index+1);
        }
        if( old == null ){
            cur.count ++; //number increase with one
        }
        return old;
    }

    public boolean delete(int ip, int masklen){
        boolean bOK = false;

        if( masklen<0 || masklen > MAXLEN || this.root == null ) return bOK;

        TreeNode<T> cur = this.root;

        if( masklen==0 ){
            if( cur.data != null ){
                cur.data = null;
                cur.count--;
            }
            bOK = true;
        } else{
            int[] ips = IPv4Util.IP2Array(ip);
            T old = delete(this.root,ips,masklen,1) ;
            if( old != null ) {
                this.root.count--;
                bOK = true;
            }

        }
        return bOK;
    }
    protected T delete(TreeNode<T> node, int[] ips, int masklen, int index){
        T old = null;
        TreeNode<T> cur = (ips[index] ==0)?node.left:node.right;

        if( index >= masklen ){//found
            if( cur != null ){
                if( cur.data!= null ){
                    old = cur.data; //
                    cur.data = null;
                    cur.count --;
                }
                if( cur.count <=0 ){
                    if( cur == node.left ) node.left = null;
                    else node.right = null;
                }
            }
        } else if( cur!= null ){
            old = delete(cur,ips,masklen,index+1);
            if( old != null ) node.count--;
            if( node.count <= 0 ){//
                node.left = node.right = null;
            }
        }

        return old;
    }

    /**
     *
     * @param ip
     * @param masklen
     * @return array of entries, each with the form of [ip,masklen,nhdb]
     */
    public ArrayList<VisitItem<T> > getEntries(int ip, int masklen){
        ArrayList<VisitItem<T> > entries = new ArrayList<>();

        if( this.root == null || masklen<0 || masklen>MAXLEN ) return entries;

        if( masklen == 0 ){// add first node
            if( this.root.data!= null ){//valid entry
                entries.add(new VisitItem<T>(0,0,this.root.data));
            }
            getEntry(this.root.left,0,1,entries);
            getEntry(this.root.right,(1<<31),1,entries);
        } else {
            //== next level nodes
            int[] ips = IPv4Util.IP2Array(ip);
            int index = 0; //index starts from ZERO
            TreeNode<T> cur = this.root;
            while (index <= masklen-1) {//move to the right place
                cur = (ips[index] == 0) ? cur.left : cur.right;
                if (cur == null) break;
                index++;
            }
            if( cur!= null ) getEntry(cur,ip,masklen,entries);
        }

        return entries;
    }

    protected void getEntry(TreeNode<T> node,int ip,int masklen,ArrayList<VisitItem<T> > entries){
        if( node== null ) return;
        ArrayQueue<TranverseItem<T> > queue = new ArrayQueue<TranverseItem<T> >();
        queue.push(new TranverseItem<T>(node,ip,masklen));

        while (!queue.isEmpty()){
            TranverseItem<T> item = queue.pop();
            if( item.node.data != null ){
                entries.add(new VisitItem<>(item.ip,item.masklen,item.node.data));
            }
            int ip1 = (item.ip) & (~(1<<(32-(item.masklen+1)))); //left
            int ip2 = (item.ip) | (1<<(32-(item.masklen+1)));

            if( item.node.left != null ) queue.push(new TranverseItem<T>(item.node.left,ip1,item.masklen+1));
            if( item.node.right != null ) queue.push(new TranverseItem<T>(item.node.right,ip2,item.masklen+1));
        }

    }

    public void printInfo(){
        System.out.println("Total entries = " + this.getCount() + ", Stack Memory Size = " + stMemory.Size()+", Stack Memory Capacity = " + stMemory.Capacity());
    }
    /**
     * main entry
     * @param args
     */
    public static void main(String[] args){
        //System.out.println("Hello world!");
        test();
    }

    public static void test(){
        String ipstr = "1.2.3.4";
        int[] ips = IPv4Util.IP2Array(ipstr);
        for(int ip:ips){
            System.out.print(ip);
        }
        System.out.println();

    }
}

class VisitItem<T> {
    public int ip =0;
    public int masklen = 0;
    public T data = null;

    public VisitItem(int ip,int masklen,T data){
        int mask = ((1<<masklen)-1)<<(32-masklen);
        this.ip = ip&mask;
        this.masklen = masklen;
        this.data = data;
    }
}

class TranverseItem<T>{
    TreeNode<T> node;
    int ip;
    int masklen;
    public TranverseItem(TreeNode<T> node, int ip,int masklen){
        this.node = node;
        this.ip = ip;
        this.masklen = masklen;
    }
}