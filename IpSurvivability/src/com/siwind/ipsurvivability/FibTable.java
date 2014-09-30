package com.siwind.ipsurvivability;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Vector;

import javax.crypto.spec.IvParameterSpec;
import javax.swing.text.html.HTMLDocument.Iterator;

public class FibTable {

	protected int nodeID = 0;
	/**
	 * 
	 */
	protected int nodeNum = 0;

	/**
	 * 
	 */
	protected Vector<FibItem> tbl = null;

	/**
	 * 
	 * @param num
	 */
	public FibTable(int nodeid, int num) {
		if (num <= 0 || nodeid < 0)
			return;

		this.nodeID = nodeid;
		this.nodeNum = num;
		tbl = new Vector<FibItem>(this.nodeNum);
	}

	/**
	 * 
	 * @param item
	 */
	public void addFibItem(FibItem item) {
		tbl.add(item);
	}

	/**
	 * 
	 * @param g
	 * @param node
	 * @return
	 */
	public static FibTable makeFibTable(Graph g, int node) {
		FibTable fibTable = null;
		try {
			fibTable = new FibTable(node, g.nodeNum);
			for (int i = 0; i < g.nodeNum ; i++) {
				if( i==node ) continue;
				FibItem item = FibItem.makeFibItem(g, node, i);
				fibTable.addFibItem(item);

			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Node: " + node + " Failed!");
			e.printStackTrace();
		}

		return fibTable;
	}

	/**
	 * 生成全网拓扑
	 * 
	 * @param g
	 * @return
	 */
	public static Vector<FibTable> makeAllFibTable(Graph g) {
		Vector<FibTable> tables = new Vector<FibTable>(g.nodeNum);
		for (int i = 0; i < g.nodeNum; i++) {
			FibTable tbl = FibTable.makeFibTable(g, i);
			tables.add(tbl);
		}

		return tables;
	}

	// /////////////////////////////////////
	public void printTable(Graph g) {
		System.out.println("====== Fib of Node " + g.getNodeLabel(nodeID) + " :" + "======");
		System.out.println("Destination  \tNextHop \tNextHop \t...");
		for (int i = 0; i < this.tbl.size(); i++) {
			FibItem item = this.tbl.get(i);
			int dst = item.getDest();
			
			System.out.print(g.getNodeLabel(dst) + "         \t");
			Vector<NextHop> hops = item.getNextHops();
			for (int j = 0; j < hops.size(); j++) {
				NextHop p = hops.get(j);
				int hop = p.getHop();
				
				System.out.print(p.toHopString(g) + "{" + g.getShortestPathLabel(hop, dst) + "} \t");
			}
			System.out.println();
		}
	}

	/**
	 * 
	 * @param tbls
	 */
	public static void printTables(Graph g, Vector<FibTable> tbls) {
		for (int i = 0; i < tbls.size(); i++) {
			tbls.get(i).printTable(g);
		}
	}
}
