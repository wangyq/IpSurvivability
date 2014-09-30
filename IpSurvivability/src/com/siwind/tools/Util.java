package com.siwind.tools;

public class Util {

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
}
