package com.siwind.ipsurvivability;

import java.util.ArrayList;
import java.util.Vector;

public class Graph {
	/**
	 * 链路不可达的最大数值, 方便计算时使用!!
	 */
	protected static final int MAX_UNREACHEABLE_METRIC = (1 << 26); // 2^26

	/**
	 * number of node
	 */
	protected int nodeNum = 0;

	/**
	 * 节点名字
	 */
	protected ArrayList<String> nodeLabel = new ArrayList<String>();

	/**
	 * 邻接矩阵
	 */
	protected int[][] matrix = null;

	/**
	 * 存放最短路径信息, Vector数组实现, 数组中每个元都是路由器的索引 例如:　shortestPath[1] = {2,3,4,1},
	 * 表示下标为1的这条最短路径中, 路径为 2-3-4-1。
	 * 其中的2,3,4,1都是路由器下标,可以通过getRouterID()来转换为路由器ID.
	 */
	// private Vector<Integer>[] shortestPath = null; Vector数组实现
	protected Object[] shortestPath = null;

	/**
	 * 存放最短路径数值
	 */
	protected int[] shortestNum = null;
	
	public Graph(ArrayList<String> nodes, int[][] m){
		nodeLabel.addAll(nodes);
		nodeNum = nodeLabel.size();
		int i = 0;

		if (nodeNum > 0) { // copy weigh value
			matrix = new int[nodeNum][nodeNum];
			for (i = 0; i < m.length; i++) {
				System.arraycopy(m[i], 0, matrix[i], 0, nodeNum); //
				matrix[i][i] = 0; // make sure is zero!
			}
		}

		// 初始化最短路径信息
		int spn = getShortestPathCount();
		// shortestPath = new Vector<Integer> [spn];
		shortestPath = new Object[spn];
		for (i = 0; i < shortestPath.length; i++) {
			shortestPath[i] = new Vector<Integer>();
		}

		// 最短路径的数值
		shortestNum = new int[spn];
	}
	/**
	 * 根据节点的名字
	 * 
	 * @param index
	 * @return
	 */
	public String getNodeLabel(int index) {
		return nodeLabel.get(index);
	}

	/**
	 * 取得某条最短路径, 如果该条最短路径不存在, 返回null
	 * 
	 * @param i
	 *            源节点的索引
	 * @param j
	 *            目的节点的索引
	 * @return 返回该条最短路径
	 */
	public Vector<Integer> getShortestPathByIndex(int i, int j) {
		Vector<Integer> sp = (Vector<Integer>) shortestPath[getPathIndex(i, j)];
		return sp;
	}

	public String getShortestPathLabel(int i, int j) {
		Vector<Integer> sp = (Vector<Integer>) shortestPath[getPathIndex(i, j)];
		StringBuilder sb = new StringBuilder("");
		if( sp!= null){
		for(int k=0;k<sp.size();k++){
			sb.append(getNodeLabel(sp.get(k)));
		}
		}
		return sb.toString();
	}
	
	/**
	 * 返回最短路径的数组索引, 由 i-> ..->j的最短路径的索引= i*路由器数目+j
	 * 
	 * @param i
	 *            路由器的索引
	 * @param j
	 *            路由器的索引
	 * @return
	 */
	public int getPathIndex(int i, int j) {
		int index = nodeNum * i + j;
		return index;
	}
	/**
	 * the shortest path length from src node to dst node
	 * 
	 * @return
	 */
	public int getShortestPathMetric(int src, int dst) {
		return shortestNum[getPathIndex(src, dst)];
	}
	
	/**
	 * 最短路径的数目 n*n
	 * 
	 * @return
	 */
	public int getShortestPathCount() {
		return nodeNum * nodeNum;
	}
	
	/**
	 * return adjacency node, but not the src node itself!
	 * @param src
	 * @return
	 */
	public Vector<Integer> getAdjacencyNodes(int src){
		Vector<Integer> adj = new Vector<Integer>();
		
		for(int i=0;i<nodeNum;i++){
			if( i == src ) continue;
			if( matrix[src][i] != 0 ){
				adj.add(i);
			}
		}
				
		return adj;
	}
	
	/**
	 * 计算出最短路径及路径数据
	 */
	public void calcShortestPath() {

		// 错误判断
		if (nodeNum <= 0)
			return;

		int i = 0, j = 0, k = 0;
		// 构造临时的矩阵
		int[][] oldmatrix = new int[nodeNum][nodeNum];
		for (i = 0; i < matrix.length; i++) {
			for (j = 0; j < matrix[i].length; j++) {
				if (matrix[i][j] == 0 /*&& i != j*/)  //
					oldmatrix[i][j] = MAX_UNREACHEABLE_METRIC;
				else
					oldmatrix[i][j] = matrix[i][j];
			}
		}

		// 初始化最短路径信息 i->j
		for (i = 0; i < nodeNum; i++) {
			for (j = 0; j < nodeNum; j++) {
				getShortestPathByIndex(i, j).add(i);
				// addNextHopByIndexWithIndex(i, j, i);
				// addNextHopByIndexWithIndex(i, j, j);
			}
		}
		// 计算最短路径及路径信息i->j
		for (k = 0; k < nodeNum; k++) {
			for (i = 0; i < nodeNum; i++) {
				for (j = 0; j < nodeNum; j++) {
					int tmp = oldmatrix[i][k] + oldmatrix[k][j];   //
					if (oldmatrix[i][j] > tmp) {
						oldmatrix[i][j] = tmp;

						// 更新最短路径信息
						Vector<Integer> p = getShortestPathByIndex(i, j);
						Vector<Integer> p1 = getShortestPathByIndex(i, k);
						;
						Vector<Integer> p2 = getShortestPathByIndex(k, j);
						;
						p.removeAllElements();
						p.addAll(p1);
						// p.removeElementAt(p.size()-1);
						p.addAll(p2);

					}
				}
			}
		}
		// end of calc

		for (i = 0; i < oldmatrix.length; i++) {
			for (j = 0; j < oldmatrix[i].length; j++) {
				int spindex = getPathIndex(i, j);

				Vector<Integer> sp = (Vector<Integer>) shortestPath[spindex]; // 取出该条最短路径

				sp.add(j); // 完成最短路径信息的最终节点

				if (i == j || oldmatrix[i][j] >= MAX_UNREACHEABLE_METRIC) { // 链路不可达!!
					shortestNum[getPathIndex(i, j)] = 0;
					shortestPath[spindex] = null; // 该条最短路径不可用
				} else {
					shortestNum[getPathIndex(i, j)] = oldmatrix[i][j];
				}
			}
		}
	}
	/**
	 * 
	 */
	public void printShortestPath() {
		int i, j;

		for (i = 0; i < getShortestPathCount(); i++) {
			Vector<Integer> sp = (Vector<Integer>) shortestPath[i]; // 取出该条最短路径
			if (sp == null)
				continue; // 不存在

			String s = getNodeLabel(sp.get(0));
			String t = getNodeLabel(sp.get(sp.size() - 1));

			System.out.println("第" + i + "条最短路径 [" + s + "->" + t + "] = " + shortestNum[i]);

			for (int k = 0; k < sp.size(); k++) {
				String node = getNodeLabel(sp.get(k));
				System.out.print(node + " ");
			}
			System.out.println("");

		}

	}
	
	/**
	 * 
	 */
	public void printMatrix(){
		int i, j;
		for(i=0;i<nodeNum;i++){
			for(j=0;j<nodeNum;j++){
				System.out.print(matrix[i][j] + " ");
			}
			System.out.println();
		}
		
		
	}
}
