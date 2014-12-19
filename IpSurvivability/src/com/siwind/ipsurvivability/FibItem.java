package com.siwind.ipsurvivability;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

public class FibItem {
	protected int dest;
	protected Vector<NextHop> nextHops = new Vector<NextHop>();

	/**
	 * 
	 */
	public FibItem() {

	}

	public int getDest() {
		return dest;
	}

	public void setDest(int dest) {
		this.dest = dest;
	}

	public void addNextHop(NextHop h) {
		this.nextHops.add(h);
	}

	public NextHop getNextHop(int index) {
		return this.nextHops.get(index);
	}

	public Vector<NextHop> getNextHops() {
		return this.nextHops;
	}

	public void setNextHops(Vector<NextHop> nextHops) {
		this.nextHops = nextHops;
	}

	public void clearNextHops() {
		this.nextHops.clear();
	}

	/**
	 * 
	 * @param graph
	 * @param src
	 * @param dst
	 * @return
	 */
	public static FibItem makeFibItem(Graph graph, int src, int dst) {

		FibItem item = new FibItem();
		item.setDest(dst); // destination!

		Vector<Integer> path = graph.getShortestPathByIndex(src, dst);
		int hopmain = path.get(1);

		NextHop hop = new NextHop();
		hop.setHop(hopmain);
		hop.setType(NextHop.Type.MAIN);
		hop.setMetric(graph.getShortestPathMetric(src, dst));

		// add main next hop!
		item.addNextHop(hop);

		// we need to order the next hops one by one in their priority order!
		Vector<Integer> adj = graph.getAdjacencyNodes(src);
		Queue<NextHop> queue = new PriorityQueue<NextHop>(3, NextHop.createComparator());

		for (int i = 0; i < adj.size(); i++) {
			NextHop pp = null;
			int node = adj.get(i); // direct adjacency node!

			if (node == hopmain) {
				continue;
			} else if (node == dst) { // it is a direct link protected lfa
				pp = new NextHop();
				pp.setHop(node);
				pp.setType(NextHop.Type.DIRECT);
				pp.setMetric(graph.getShortestPathMetric(src,dst));     //here is the real metric from src to dst.
			} else {

				Vector<Integer> path_adj = graph.getShortestPathByIndex(node, dst); // path!!
				NextHop.Type type = NextHop.makeType(src, path, path_adj);
				if (type == NextHop.Type.UNKOWN)
					continue;

				pp = new NextHop();
				pp.setHop(node);
				pp.setType(type);
				pp.setMetric(graph.getShortestPathMetric(node, dst));
			}
			queue.add(pp);
		}
		while (!queue.isEmpty()) {
			item.addNextHop(queue.poll());
		}
		return item;
	}
}
