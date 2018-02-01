package com.siwind.trie;

import javax.print.attribute.standard.MediaSize;
import java.util.ArrayList;

public class SchemeVSTS {
    protected String Name="";
    protected int[] scheme;
    protected int a = 0; //alpha
    protected int b = 0; //beta

    public SchemeVSTS(String Name) throws Exception {
        makeScheme(Name);
    }

    public String getName(){
        return Name;
    }
    public int[] getScheme(){
        return scheme;
    }
    public int getAlpha(){
        return a;
    }
    public int getBeta(){
        return b;
    }
    /**
     * "8-8-8-8"
     * @param strName
     */
    protected void makeScheme(String strName) throws Exception {
        if( strName == null ) return;
        strName = strName.trim();
        String[] ss = strName.split("-");

        int count = 0;
        int[] s = new int[ss.length];
        for( int i=0;i<ss.length;i++ ){
            //System.out.print("[" + ss[i]+"] ");
            s[i] = Integer.parseInt(ss[i]);
            count += s[i];
        }

        if( count != 32 ) throw new Exception("Wrong Scheme format! = "+strName);
        this.scheme = s;
        this.a = this.scheme.length;
        this.b = this.scheme[0];
        this.Name = strName;
    }

    /**
     *
     * @param strNames
     * @return
     * @throws Exception
     */
    public static ArrayList<SchemeVSTS> buildSchemes(String strNames)throws Exception{

        if( strNames == null ) return null;
        strNames = strNames.trim();
        String[] sstr = strNames.split("\\s+");

        ArrayList<SchemeVSTS> schs = new ArrayList<>();
        for(int i=0;i<sstr.length;i++){
            String str = sstr[i].trim();
            schs.add(new SchemeVSTS(str));
        }

        return schs;
    }

    public static ArrayList<SchemeVSTS> makeAllSchemes()throws Exception{
        String strNames = "";
//        strNames =
//                "14-6-4-8 16-4-4-8 18-4-4-6 20-4-4-4  " +
//                "14-6-4-4-4 16-4-4-4-4 18-2-2-2-8 20-2-2-4-4 " +
//                "14-6-2-2-4-4 16-4-2-2-4-4 18-2-2-2-4-4 20-2-2-2-2-4";

        //strNames = "16-16 8-16-8 8-8-8-8 8-8-4-4-8 8-4-4-4-4-8 8-4-4-4-2-2-8  8-4-4-2-2-2-2-8 8-4-2-2-2-2-2-2-8 8-2-2-2-2-2-2-2-2-8";
        //strNames = "8-16-8 8-8-8-8 8-8-4-4-8 8-4-4-4-4-8 8-4-4-4-2-2-8  8-4-4-2-2-2-2-8 8-4-2-2-2-2-2-2-8 8-2-2-2-2-2-2-2-2-8 8-2-2-2-2-2-2-2-2-2-6 8-2-2-2-2-2-2-2-2-2-2-4";
        //strNames = "12-12-8 12-6-6-8 12-6-4-2-8 12-4-4-2-2-8 12-4-2-2-2-2-8 12-2-2-2-2-2-2-8 12-2-2-2-2-2-2-2-6 12-2-2-2-2-2-2-2-2-4 12-2-2-2-2-2-2-2-2-2-2";
        //strNames = "16-8-8 16-4-4-8 16-4-2-2-8 16-2-2-2-2-8 16-2-2-2-2-2-6  16-2-2-2-2-2-2-4 16-2-2-2-2-2-2-2-2";
        //strNames = "20-4-8 20-2-2-8 20-2-2-2-6 20-2-2-2-2-4 20-2-2-2-2-2-2";
        //strNames = "22-2-8 22-2-2-6 22-2-2-2-4 22-2-2-2-2-2";
        //strNames = "24-8 24-2-6 24-2-2-4 24-2-2-2-2";

        //alpha = 8
        //strNames = "8-4-4-2-2-2-2-8 10-4-2-2-2-2-2-8 12-2-2-2-2-2-2-8 14-2-2-2-2-2-2-6 16-2-2-2-2-2-2-4 18-2-2-2-2-2-2-2";
        //alpha = 7
        //strNames = "8-4-4-4-2-2-8 10-4-4-2-2-2-8 12-4-2-2-2-2-8 14-2-2-2-2-2-8 16-2-2-2-2-2-6 18-2-2-2-2-2-4 20-2-2-2-2-2-2";
        //alpha = 6
        //strNames = "8-4-4-4-4-8 10-4-4-4-2-8 12-4-4-2-2-8 14-4-2-2-2-8 16-2-2-2-2-8 18-2-2-2-2-6 20-2-2-2-2-4 22-2-2-2-2-2";
        //alpha = 5
        //strNames = "8-6-4-6-8 10-4-4-6-8 12-4-4-4-8 14-4-4-2-8 16-4-2-2-8 18-2-2-2-8 20-2-2-2-6 22-2-2-2-4 24-2-2-2-2"; //alpha=5
        //alpha = 4
        //strNames = "8-8-8-8 10-8-6-8 12-6-6-8 14-6-4-8 16-4-4-8 18-4-2-8 20-2-2-8 22-2-2-6 24-2-2-4";
        //alpha = 3
        strNames = "8-16-8 10-14-8 12-12-8 14-10-8 16-8-8 18-6-8 20-4-8 22-2-8 24-2-6";


        return buildSchemes(strNames);
    }

}
