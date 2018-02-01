package com.siwind.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Util {

	public static String getCurTimeString(){
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");//
		return df.format(new Date());
	}
	/**
	 * 返回当前目录, 目录最后带反斜杠
	 * @return
	 */
	public static String getCurDir(){
		String strCurDir = System.getProperty("user.dir") ;
		
		//检查是否最后有一个 斜杠
		if( strCurDir.charAt(strCurDir.length()-1) != '/' 
			&& strCurDir.charAt(strCurDir.length()-1) != '\\' ){
			strCurDir += "/";
		}

		return strCurDir;
	}

	/**
	 *
	 * @param strFile
	 * @return
	 */
	public static String getCurFilePath(String strFile){
		String strRes = "";

		if( strFile == null || strFile.isEmpty() ) return strRes;

		strFile = strFile.trim();
		if( !strFile.startsWith("/")) {
			strRes = getCurDir() + strFile;
		}
		return strRes;
	}
	/**
	 * split string with any blank char(such as whitespace, tab,...)
	 * @param str
	 * @return
     */
	public static String[] splitSpace(String str){
		if( str == null ) return null;
		return str.trim().split("\\s+");
	}
}
