package com.siwind.trie;

public class BinTrie {
}


//=================
/**
 *
 */
class BTrieNode {
    protected int data = 0;
    protected int count = 0;
    protected BTrieNode left = null;
    protected BTrieNode right = null;
    protected final Integer nullValue = 0;

    public BTrieNode(){

    }
    public boolean isEmpty(){
        return this.data == nullValue;
    }
}