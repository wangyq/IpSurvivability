package com.siwind.ipsurvivability;

import java.util.Comparator;
import java.util.Vector;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NextHop {

	/**
	 * singleton design pattern!
	 */
	//protected static Comparator<NextHop> comparator = null;
	//protected static Lock theLock = new ReentrantLock(); // ReentrantLock
															// implements the
															// Lock interface

	public  enum Type{
		UNKOWN, MAIN, DIRECT, REGION, NODE,LINK;

		/**
		 * 标记类型的字符, 为类型的首字母
		 */
		private static String[] mark = null;
		
		static{
			Type[] values = Type.values();
			mark = new String[values.length];
			for(Type type : values){
				//mark[i] = values[i].name().substring(0,1);
				mark[type.ordinal()] = type.name().substring(0,1);
			}
		}

		/**
		 * 构造函数必须为私有
		 */
		private Type(){
		}

		public String getMark(){
			//return this.name().substring(0,1);   //会生成很多对象的
			return mark[this.ordinal()];
		}
	}

	protected int hop; // which one is the next hop!
	protected Type type; //


	protected int metric; // metric to destination

	public NextHop() {
		this.hop = 0;
		this.type = Type.UNKOWN;
		this.metric = 0;
	}

	public NextHop(int hop, Type type, int m) {
		this.hop = hop;
		this.type = type;
		this.metric = m;
	}

	public int getHop() {
		return hop;
	}

	public void setHop(int hop) {
		this.hop = hop;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
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
		sb.append(type.getMark());  //
		
		return sb.toString();
	}

	/**
	 * 
	 * @param src
	 * @param first
	 * @param second
	 * @return
	 */
	public static Type makeType(int src, Vector<Integer> first, Vector<Integer> second) {
		Type type = Type.UNKOWN;
		int i = first.size()-1, j=second.size()-1;
		do {
			if (first.get(i) != second.get(j))
				break;
			i--;
			j--;
		} while (i >=0 && j>=0); // until not equal or path finished!

		if (i > 1) { //region-lfa
			type = Type.REGION;
		} else if (i == 1) { //node-lfa
			type = Type.NODE;
		} else if ( i == 0) { // link-lfa
			type = Type.LINK;
		} else {   //loop-back, not available
			type = Type.UNKOWN;
		}

		return type;
	}

	public static Comparator<NextHop> createComparator(){
		return new Comparator<NextHop>() {

			@Override
			public int compare(NextHop o1, NextHop o2) {
				// TODO Auto-generated method stub
				int ret = 0; //equal!
				
				ret = o1.type.compareTo(o2.type);  // whether the same type!
				if( ret == 0 ){//if type is same, so we compare the metric
					ret = o1.metric - o2.metric;  //so if metric is the same , how ?
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
