package com.siwind.ipsurvivability;

import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NextHop {

	/**
	 * singleton design pattern!
	 */
	protected static Comparator<NextHop> comparator = null;
	protected static Lock theLock = new ReentrantLock(); // ReentrantLock
															// implements the
															// Lock interface

	public static final int NEXT_HOP_UNKOWN = -1;

	public static final int HOP_UNKOWN = -1;

	public static final int HOP_MAIN = 0;

	public static final int HOP_LFA_REGION = 1;
	public static final int HOP_LFA_NODE = 2;
	public static final int HOP_LFA_DOWNSTREAM = 3;

	public static final int HOP_LFA_LINK = 4;

	protected int hop; // which one is the next hop!
	protected int type; // -1: unkown, 0: main-nexthop, 1: region-lfa, 2:
						// node-protected,4:link-protected lfa,
						// 3:downstream-protected lfa
	protected int metric; // metric to destination

	public NextHop() {
		this.hop = NEXT_HOP_UNKOWN;
		this.type = HOP_UNKOWN;
		this.metric = 0;
	}

	public NextHop(int hop, int type, int metri) {
		this.hop = hop;
		this.type = type;
		this.metric = metri;
	}

	public int getHop() {
		return hop;
	}

	public void setHop(int hop) {
		this.hop = hop;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getMetric() {
		return metric;
	}

	public void setMetric(int metric) {
		this.metric = metric;
	}
	
	public String toHopString(Graph g){
		StringBuilder sb = new StringBuilder();
		
		sb.append(g.getNodeLabel(hop) + "(" + metric + ")");
		if( type == HOP_MAIN){
			sb.append("M");
		}else if( type == HOP_LFA_REGION){
			sb.append("R");
		} else if( type == HOP_LFA_NODE){
			sb.append("N");
		} else if( type == HOP_LFA_LINK){
			sb.append("L");
		} else { //unavailable
			sb.append("X");
		}
		
		return sb.toString();
	}

	/**
	 * 
	 * @param src
	 * @param first
	 * @param second
	 * @return
	 */
	public static int makeType(int src, Vector<Integer> first, Vector<Integer> second) {
		int type = HOP_UNKOWN;
		int i = first.size()-1, j=second.size()-1;
		do {
			if (first.get(i) != second.get(j))
				break;
			i--;
			j--;
		} while (i >=0 && j>=0); // until not equal or
															// path finished!

		if (i > 1) { //region-lfa
			type = HOP_LFA_REGION;
		} else if (i == 1) { //node-lfa
			type = HOP_LFA_NODE;
		} else if ( i == 0) { // link-lfa
			type = HOP_LFA_LINK;
		} else {   //loop-back, not available
			type = HOP_UNKOWN;
		}

		return type;
	}

	public static Comparator<NextHop> createComparator(){
		return new Comparator<NextHop>() {

			@Override
			public int compare(NextHop o1, NextHop o2) {
				// TODO Auto-generated method stub
				int ret = 0; //equal!
				if (o2.type > o1.type) { // higher type number get lower
					ret = -1;  // priority
				} else if( o2.type < o1.type ){
					ret = 1;
				}else if (o2.metric > o1.metric) { // larger metric get
													// lower priority!
					ret = -1;
				} else if( o2.metric < o1.metric ){
					ret = 1;
				} 

				return ret;
			}
		};
	}
	/**
	 * 
	 * @return
	 */
/*	public static Comparator<NextHop> getComparator() {
		if (comparator != null) {// generate the lock!
			theLock.lock();
			try {
				// critical section
				comparator = new Comparator<NextHop>() {

					@Override
					public int compare(NextHop o1, NextHop o2) {
						// TODO Auto-generated method stub
						if (o2.type > o1.type) { // higher type number get lower
													// priority
							return 1;
						} else if (o2.metric > o1.metric) { // larger metric get
															// lower priority!
							return 1;
						}

						return 0;
					}
				};
			} finally {
				theLock.unlock(); // make sure the lock is unlocked even if an
									// exception is thrown
			}

		}

		return comparator;
	}*/

}
