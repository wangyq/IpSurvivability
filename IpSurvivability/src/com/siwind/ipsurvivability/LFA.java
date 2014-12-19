package com.siwind.ipsurvivability;

import java.io.IOException;
import java.net.NetworkInterface;
import java.util.Vector;

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
	
	public static void testHopMemory(){
		int maxlen = 1000000;
		String str = "";
		NextHop hops[] = new NextHop[maxlen];
		for(int i=0;i<maxlen;i++){
			hops[i] = new NextHop();
			str = hops[i].getType().getMark();
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		//testShortestPath();
		//testDirectGraph();
		
		testHopMemory();
		
		System.out.println(System.getProperty("user.dir"));
	}

}
