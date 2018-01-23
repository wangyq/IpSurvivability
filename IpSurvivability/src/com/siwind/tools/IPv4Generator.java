package com.siwind.tools;

import java.util.Random;

public class IPv4Generator{
    private static Random randomGenerator = new Random();
    private int[] cdfPrefix;
    private int maxCDFNum;

    public IPv4Generator(int[] countPrefix){
        cdfPrefix = getCDFPrefix(countPrefix);
        maxCDFNum = cdfPrefix[cdfPrefix.length-1];
    }

    private int nextPrefixLen(){
        int baseLen = 8;
        int rNum = randomGenerator.nextInt(maxCDFNum);
        for( int i=0;i<cdfPrefix.length;i++){
            if( rNum <= cdfPrefix[i]){
                baseLen += i;
                break;
            }
        }
        return baseLen;
    }

    private int nextIp(int prefixLen){
        int ip = 0, maxLen = 32;
        int segLen = 4;
        int curLen = prefixLen;
        while (maxLen>0){
            int curIp = 0;
            if( curLen > 0) {
                int count = (curLen >= segLen) ? (1<<segLen) : (1 << curLen); //
                curIp = randomGenerator.nextInt(count-1) + 1; //[1,count)

                if (curLen < segLen) {
                    curIp = curIp << (segLen - curLen);
                }

                curLen -= segLen;
            }
            //
            ip = (ip << segLen) + curIp;
            maxLen -= segLen;
        }//
        return ip;
    }
    /**
     * get cdf of prefix distribution
     * @param countPrefix
     * @return
     */
    private static int[] getCDFPrefix(int[] countPrefix){
        int[] cdfPrefix = new int[countPrefix.length];
        int previous = 0;

        for(int i=0;i<countPrefix.length;i++){
            cdfPrefix[i] = previous + countPrefix[i];
            previous += countPrefix[i];
        }
        return cdfPrefix;
    }

    /**
     * generate random public IPv4 and its prefix len
     * @return
     */
    public int[] nextIPandPrefixLen(){
        int[] IpAndLen=new int[2];
        int len  = nextPrefixLen();//perfix len
        int ip = 0;
        do{
            ip = nextIp(len);
        }while(!IPv4Util.isIPv4Public(ip));
        IpAndLen[0] = ip; IpAndLen[1] = len;
        return IpAndLen;
    }
}
