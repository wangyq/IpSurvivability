package com.siwind.ipsurvivability;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Vector;

import com.siwind.tools.FileUtil;

public class LFA {

	/**
	 * 
	 * @return
	 */
	public static boolean testReadData(){
		boolean bOK = false;
		ArrayList<String> ff = FileUtil.getFileContents("data.txt");
		for( String str: ff){
				System.out.println(str);
		}
		return bOK;
	}
		
	/**
	 * 
	 */
	public static void testShortestPath(){
		UnDirectGraph graph = UnDirectGraph.loadFromFile("data.txt");
		graph.calcShortestPath();
		graph.printMatrix();
		graph.printShortestPath();
		
		Vector<FibTable> tbls = FibTable.makeAllFibTable(graph);
		
		FibTable.printTables(graph,tbls);
		
	}
	
	public static void testDirectGraph(){
		DirectGraph graph = DirectGraph.loadFromFile("direct_data.txt");
		graph.calcShortestPath();
		graph.printMatrix();
		graph.printShortestPath();
		
		Vector<FibTable> tbls = FibTable.makeAllFibTable(graph);
		
		FibTable.printTables(graph,tbls);
	}
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//testShortestPath();
		testDirectGraph();
		
	
		System.out.println(System.getProperty("user.dir"));
	}

}
