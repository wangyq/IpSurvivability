package com.siwind.ipsurvivability;

import java.util.ArrayList;
import java.util.Vector;

import com.siwind.tools.FileUtil;

/**
 * 
 * @author wang
 * 
 */
public class UnDirectGraph extends Graph{


	/**
	 * 
	 * @param nodes
	 * @param matrix
	 */
	public UnDirectGraph(ArrayList<String> nodes, int[][] m) {
		super(nodes, m);
	}


	/**
	 * line NO.1 节点数目及节点名称
	 * line NO.2 上三角表示的邻接矩阵, 数值0代表无直接相连链路
	 * 分号(;) 是行注释
	 * Example:
	 *   4 A B C D
	 *     0 2 3 1
	 *       0 1 2
	 *         0 3
	 *           0
	 * @return
	 */
	/**
	 * @param filename
	 * @return
	 */
	public static UnDirectGraph loadFromFile(String filename){
		ArrayList<String> nodes = new ArrayList<String>();
		int[][] m = null;
		UnDirectGraph graph = null;
		
		ArrayList<String> ff = FileUtil.getFileContents(filename);
		if( ff.size() >=1 ){
			String line = ff.get(0);
			//System.out.println(line);
			//line NO.1 is nodenum nodename nodename ...
			
			String[] n = line.split(" ");
			int num = Integer.parseInt(n[0]);
			if( num != (n.length-1) ) {
				throw new IllegalArgumentException("Data file format error!");
			}
			for(int i=1;i<n.length;i++){  //add node, but not the nodenumber
				nodes.add(n[i]);
			}
			m = new int[num][num];
			
			int index = 0;
			for( int j=1; j<ff.size(); j++){ //read every line of weigh
				line = ff.get(j);
				n = line.split(" ");
				for(int i=0; i<n.length;i++){
					m[index][i+index] = Integer.parseInt(n[i]);

					m[i+index][index] =m[index][i+index];  //对称的三角矩阵才用

				}
				
				index ++;
				//System.out.println(line);
			}
		}
		graph = new UnDirectGraph(nodes, m);		
		return graph;
	}


}
