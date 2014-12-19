package com.siwind.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * 提供数据文件的读取
 * @author wang
 *
 */
public class MyFileReader {

	final static String COMMENT_CHAR = ";"; //can place anywhere
	private int index = -1;
	private ArrayList<String> lines = null;
	
	private MyFileReader(ArrayList<String> ll){  // ll must be valid!
		lines = ll;
		if( lines.size() > 0 ){
			index = 0;
		}
	}
	/**
	 * 
	 * @return
	 */
	public static MyFileReader readFile(String strFilename){
		String strLine =  "";
		MyFileReader  ff = null;
		ArrayList<String> lines =  new ArrayList<String>();
		try {
			String strCurDir = System.getProperty("user.dir");  // current directory!
			String strFile = strCurDir + File.separator + strFilename;

			BufferedReader bufferedReader = new BufferedReader(new FileReader(strFile));
			
			int last = -1; //last comment char
			
			while ((strLine = bufferedReader.readLine()) != null) {
				
				last = strLine.indexOf(COMMENT_CHAR);
				if( last == 0 ){
					continue;    //find comment!
				}
				else if( last != -1 ){//find!
					strLine = strLine.substring(0, last);
				}
				strLine = strLine.trim();  //trim empty character!
				if( strLine.isEmpty() ) continue;   //empty line
				
				lines.add(strLine);         //find a valid line								
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if( lines.size() > 0 ){
			ff = new MyFileReader(lines);
		}
		return ff;
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean hasNext(){
		if( index == -1 ) return false;
		if( index >= lines.size() ) return false;
		return true;
	}
	
	/**
	 * 
	 * @return
	 */
	public String next(){
		String str = null;
		if( hasNext() ) {
			str = lines.get(index);
			index ++;
		}
		return str;
	}
}
