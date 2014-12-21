package com.siwind.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 提供数据文件的读取
 * @author wang
 *
 */
public class FileUtil {

	final static String COMMENT_CHAR = ";"; //can place anywhere

	/**
	 * 
	 * @param line
	 * @param comments
	 * @return
	 */
	protected static int indexOfComment(String line, String comments){
		int index = -1 ;
		if( (line == null) || line.isEmpty() ) return index;
		if( comments == null ) return index;
		
		if( comments.length() == 1 ) {
			index = line.indexOf(comments);
		} else{
			for(int i=0;i<line.length();i++){
				char ch = line.charAt(i);
				index = comments.indexOf(ch);
				if( index != -1 ) break;   //find comment char now!
			}
		}
		
		return index;
	}
	
	/**
	 * 
	 * @param strFilename
	 * @return
	 */
	public static ArrayList<String> getFileContents(String strFilename){
		return getFileContents(strFilename, COMMENT_CHAR);
	}
	/**
	 * 
	 * @return
	 */
	public static ArrayList<String> getFileContents(String strFilename, String comments){
		String strLine =  "";
		FileUtil  ff = null;
		ArrayList<String> lines =  new ArrayList<String>();
		BufferedReader bufferedReader = null;
		try {
			//String strCurDir = System.getProperty("user.dir");  // current directory!
			String strCurDir = FileUtil.class.getResource("/").getFile();

			String strFile = strCurDir + File.separator + strFilename;

			bufferedReader = new BufferedReader(new FileReader(strFile));
			
			int last = -1; //last comment char
			
			while ((strLine = bufferedReader.readLine()) != null) {
				
				//last = strLine.indexOf(COMMENT_CHAR);
				last = indexOfComment(strLine,comments);
				
				if( last != -1 ){//find comment!
					strLine = strLine.substring(0, last);
				}
				strLine = strLine.trim();  //trim empty character!
				if( strLine.isEmpty() ) continue;   //empty line
				
				lines.add(strLine);         //find a valid line								
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			if( bufferedReader != null ){
				try {
					bufferedReader.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return lines;
	}
	

}
