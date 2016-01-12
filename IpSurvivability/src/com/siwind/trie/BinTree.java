package com.siwind.trie;

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

    public TreeNode<T> getRoot(){
        return root;
    }
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

    //===================
    public void printInOrder(){
        System.out.print("InOrder: ");
        if( root != null ) printInOrderInternal(root);
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
        System.out.println();
    }
    protected void printPostOrderInternal(TreeNode<T> node){
        if( node.getLeft() != null ) printPostOrderInternal(node.getLeft());
        if( node.getRight() != null ) printPostOrderInternal(node.getRight());
        System.out.print(node.getData() + " ");
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
