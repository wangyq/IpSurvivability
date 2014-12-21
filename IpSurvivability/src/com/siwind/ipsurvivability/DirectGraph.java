package com.siwind.ipsurvivability;

import java.util.ArrayList;

import com.siwind.tools.FileUtil;

public class DirectGraph extends Graph {

	/**
	 * 
	 * @param nodes
	 * @param m
	 */
	public DirectGraph(ArrayList<String> nodes, int[][] m) {
		super(nodes, m);
		// TODO Auto-generated constructor stub
	}
	

	public static DirectGraph loadFromFile(String filename){
		ArrayList<String> nodes = new ArrayList<String>();
		int[][] m = null;
		DirectGraph graph = null;
		
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
			for( int j=1; j<ff.size(); j++ ){ //read every line of weigh
				line = ff.get(j);
				n = line.split(" ");
				for(int i=0; i<n.length;i++){
					m[index][i] = Integer.parseInt(n[i]);

					//m[i+index][index] =m[index][i+index];  //对称的三角矩阵才用

				}
				
				index ++;
				//System.out.println(line);
			}
		}
		graph = new DirectGraph(nodes, m);		
		return graph;
	}

}
