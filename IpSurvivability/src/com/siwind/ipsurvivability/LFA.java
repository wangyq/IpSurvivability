package com.siwind.ipsurvivability;

import java.nio.file.attribute.GroupPrincipal;
import java.util.ArrayList;

import com.siwind.tools.MyFileReader;

public class LFA {

	/**
	 * 
	 * @return
	 */
	public static boolean testReadData(){
		boolean bOK = false;
		MyFileReader ff = MyFileReader.readFile("data.txt");
		if( null != ff ){
			while( ff.hasNext() ){
				String lineString = ff.next();
				System.out.println(lineString);
			}
		}
		return bOK;
	}
	
	/**
	 * 
	 * @return
	 */
	public static UnDirectGraph readData(){
		ArrayList<String> nodes = new ArrayList<String>();
		int[][] m = null;
		UnDirectGraph graph = null;
		
		MyFileReader ff = MyFileReader.readFile("data.txt");
		if( null != ff ){
			String line = ff.next();
			System.out.println(line);
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
			while( ff.hasNext() ){ //read every line of weigh
				line = ff.next();
				n = line.split(" ");
				for(int i=0; i<n.length;i++){
					m[index][i+index] = Integer.parseInt(n[i]);

					m[i+index][index] =m[index][i+index];  //对称的三角矩阵才用

				}
				index ++;
				System.out.println(line);
			}
		}
		graph = new UnDirectGraph(nodes, m);		
		return graph;
	}
	
	/**
	 * 
	 */
	public static void testShortestPath(){
		UnDirectGraph graph = UnDirectGraph.loadFromFile("data.txt");
		graph.calcShortestPath();
		graph.printMatrix();
		graph.printShortestPath();
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		testShortestPath();
	}

}
