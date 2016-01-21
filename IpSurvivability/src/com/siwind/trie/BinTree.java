package com.siwind.trie;

import com.siwind.tools.Stack;

/**
 * Created by wang on 16-1-12.
 */
public class BinTree<T> {
    protected TreeNode<T> root = null;

    /**
     *
     */
    public BinTree() {

    }

    public static void printFullArrayBinTree(int depth){
        if( depth<=0 ) return;
        int span = 4;
        int gap = 1;

        int index = 0;
        int i,j;
        for( i=1;i<=depth;i++){
            //out span space!
            for(j=0;j<(depth-i)*span;j++) System.out.print(" ");
                for (j = 0; j < (1 << i ); j++, index++) {
                    System.out.print(index + " ");
                }
                System.out.println();
        }
    }
    /**
     *
     */
    public static void test_tree(){
        BinTree<Integer> tree = new BinTree<Integer>();

        Integer[] val = {1,2,3,0,0,4};

        tree.createPreOrder(val,0);

        tree.printInOrder();
        tree.printPreOrder();
        tree.printPostOrder();
    }

    /**
     *
     * @param args
     */
    public static void main(String[] args) {

        test_tree();

        printFullArrayBinTree(8);
    }

    public TreeNode<T> getRoot(){
        return root;
    }

    //===================

    //===================
    protected TreeNode<T> createPreOrderInternal(T[] val, int[] index,T nulVal) {
        TreeNode<T> node = null;

        int cur = index[0];

        if (cur < val.length){

            index[0] ++;   //now move to next value!

            if( val[cur]!= nulVal ) {
                node = new TreeNode<T>(val[cur]);

                node.setLeft(createPreOrderInternal(val, index, nulVal));
                node.setRight(createPreOrderInternal(val, index, nulVal));
            }
        }
        return node;
    }

    /**
     * @param val
     */
    public void createPreOrder(T[] val, T nulVal) {
        int index[] = {0};  // must be an integer reference. Integer can not work for it!
        root = createPreOrderInternal(val, index, nulVal);
    }

    protected void travelPreOrder(){
        Stack<TreeNode<T> > st = new Stack<TreeNode<T> > ();
        TreeNode<T> node = root;

        do{
            if( node!= null ){
                System.out.print(node.getData() + " ");
                st.Push(node.getRight());
                node = node.getLeft();

            }else if( !st.isEmpty() ) {
                    node = st.Pop();
            } else{
                break;
            }
        }while(true);
    }
    protected void travelInOrder(){
        Stack<TreeNode<T> > st = new Stack<TreeNode<T> > ();
        TreeNode<T> node = root;

        do{
            if( node!= null ){
                st.Push(node);
                node = node.getLeft();
            }else if( !st.isEmpty() ) {
                node = st.Pop();
                System.out.print(node.getData() + " ");
                node = node.getRight();

            } else{
                break;
            }
        }while(true);
    }
    protected void travelPostOrder(){
        Stack<TreeNode<T> > st = new Stack<TreeNode<T> > ();
        TreeNode<T> node = root;

        do{
            if( node!= null ){
                st.Push(node);
                node = node.getLeft();
            }else{
                while (!st.isEmpty()) {
                    if (node == st.Top().getRight()) {
                        node = st.Pop();
                        System.out.print(node.getData() + " ");
                    } else {
                        node = st.Top().getRight();
                        break;
                    }
                }//end of while
                if( st.isEmpty() ) break;  //finish travel!
            }
        }while(true);
    }

    public void printInOrder(){
        System.out.print("InOrder: ");
        if( root != null ) printInOrderInternal(root);
        travelInOrder();
        System.out.println();
    }

    protected void printInOrderInternal(TreeNode<T> node){
        if( node.getLeft() != null ) printInOrderInternal(node.getLeft());
        System.out.print(node.getData() + " ");
        if( node.getRight() != null ) printInOrderInternal(node.getRight());
    }

    public void printPreOrder(){
        System.out.print("PreOrder: ");
        if( root != null ) printPreOrderInternal(root);
        travelPreOrder();
        System.out.println();
    }

    protected void printPreOrderInternal(TreeNode<T> node){
        System.out.print(node.getData() + " ");
        if( node.getLeft() != null ) printPreOrderInternal(node.getLeft());
        if( node.getRight() != null ) printPreOrderInternal(node.getRight());
    }

    public void printPostOrder(){
        System.out.print("PostOrder: ");
        if( root != null ) printPostOrderInternal(root);
        travelPostOrder();
        System.out.println();
    }

    protected void printPostOrderInternal(TreeNode<T> node){
        if( node.getLeft() != null ) printPostOrderInternal(node.getLeft());
        if( node.getRight() != null ) printPostOrderInternal(node.getRight());
        System.out.print(node.getData() + " ");
    }
}

class TreeNode<T> {
    protected T data;
    protected TreeNode<T> left, right;  //left and right child

    public TreeNode(T data) {
        this.data = data;
        this.left = this.right = null; //
    }

    public T getData() {
        return data;
    }
    public void setData(T data) {
        this.data = data;
    }

    public TreeNode<T> getLeft() {
        return left;
    }

    public void setLeft(TreeNode<T> left) {
        this.left = left;
    }

    public TreeNode<T> getRight() {
        return right;
    }

    public void setRight(TreeNode<T> right) {
        this.right = right;
    }
}
