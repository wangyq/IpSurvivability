package com.siwind.tools;

import java.security.InvalidParameterException;

/**
 * Created by wang on 16-1-20.
 */
public final class IPv4Util {

    private final static int IPV4_BYTE_SIZE = 4;

    /**
     * ipInt -> byte[]
     * @param ipInt
     * @return byte[]
     */
    public static byte[] intToBytes(int ipInt) {
        byte[] ipAddr = new byte[IPV4_BYTE_SIZE];
        ipAddr[0] = (byte) ((ipInt >>> 24) & 0xFF);
        ipAddr[1] = (byte) ((ipInt >>> 16) & 0xFF);
        ipAddr[2] = (byte) ((ipInt >>> 8) & 0xFF);
        ipAddr[3] = (byte) (ipInt & 0xFF);
        return ipAddr;
    }
    /**
     * byte[] -> int
     * @param bytes
     * @return int
     */
    public static int bytesToInt(byte[] bytes) {
        int addr = bytes[3] & 0xFF;
        addr |= ((bytes[2] << 8) & 0xFF00);
        addr |= ((bytes[1] << 16) & 0xFF0000);
        addr |= ((bytes[0] << 24) & 0xFF000000);
        return addr;
    }

    public static int NetmaskToMasklen(int ipmask){
        int len = 0;
        while( ((ipmask>>>31) & 0x1) !=0  ){
            ipmask = (ipmask<<1);
            len++;
        }
        return len;
    }

    public static int NetmaskFromMasklen(int masklen){
        if( masklen<=0 ) return 0;
        if( masklen>=32 ) return 0xFFFFFFFF; //32-bit
        return  ((1<<masklen)-1)<<(32-masklen);
    }

    /**
     *
     * @param strIP
     * @param ips
     * @return
     */
    public static boolean IPandMaskfromStr(String strIP, int[] ips){
        if( !IPandMasklenfromStr(strIP,ips)) return false;
        ips[1] = NetmaskFromMasklen(ips[1]);
        return true;
    }
    /**
     *
     * @param strIP , such as 192.168.0.16/24, 172.16/16, 10/24, ..., etc.
     * @param ips
     * @return
     */
    public static boolean IPandMasklenfromStr(String strIP, int[] ips){
        if( strIP==null || strIP.trim().isEmpty() ){
            return false;
        }
        strIP = strIP.trim();
        int pos = strIP.indexOf("/");
        if( pos == -1 ) return false;

        String str1 = strIP.substring(0,pos);
        String str2 = strIP.substring(pos+1,strIP.length());

        int i=0;
        pos = 0;
        for( i=0;i<3;i++){
            pos = str1.indexOf(".",pos);
            if( pos == -1 ) break;
            pos ++; //move to next char
        }
        if( i==0 ) str1 += ".0.0.0";
        else if( i==1 ) str1 += ".0.0";
        else if( i==2 ) str1 += ".0";

        if( !IPfromStr(str1,ips)){
            return false;
        }

        //
        ips[1] = Integer.parseInt(str2);
        if( ips[1]<0 || ips[1]>32 ) return false;

        ips[0] &= NetmaskFromMasklen(ips[1]);

        return true;
    }

    /**
     *
     * @param ip
     * @return
     */
    public static String IP2Str(int ip){
        return new StringBuffer().append((ip>>>24) & 0xFF).append('.').append(
                (ip>>>16) & 0xFF).append('.').append((ip>>>8) & 0xFF)
                .append('.').append((ip) & 0xFF).toString();
    }

    /**
     *
     * @param strIP
     * @return
     */
    private static boolean IPfromStr(String strIP,int[] retIP){
        if( (strIP==null) || strIP.trim().isEmpty() ){
            throw new InvalidParameterException("IP address is null!");
        }
        strIP = strIP.trim();  //is right?

        boolean bOK = false;

        int iFirst = 0, iLast = 0, iPos = 0;
        int segment = 0, num = 0;
        int ip = 0;
        iPos = iFirst + iLast;
        do {
            if ((strIP.charAt(iPos)>='0') && (strIP.charAt(iPos)<='9')) {
                segment = segment * 10 + (strIP.charAt(iPos)-'0');
                iLast ++; //move next
            } else if (strIP.charAt(iPos) == ('.')) {
                if ((segment < 0) || (segment > 255)) {
                    break;
                }
                ip = (ip << 8) + segment;

                segment = 0;
                iFirst = iPos + 1;
                iLast = 0;
                num++;
            } else {
                break; //wrong ip address!
            }
            //next position.
            iPos = iFirst + iLast;

            if (iPos >= strIP.length()) { //finished now!
                if ((segment < 0) || (segment > 255)) {
                    break;
                }
                num++;
                if (num != 4) break;

                ip = (ip << 8) + segment; //update ip value!
                bOK = true;
                break;
            }

            //check index if right!
            if ((iLast > 3) || (num > 4)) {
                break;
            }
        } while (true);

        retIP[0] = ip; //return ip address!
        return bOK;
    }

    /**
     *
     * @param strIP
     * @return
     */
    public static boolean isIPv4Valid(String strIP){
        int[] ips = new int[1];
        return IPfromStr(strIP,ips);
    }

    public static int IPfromStr(String strIP){
        int[] ips = new int[1];
//        if( !IPfromStr(strIP,ips)){
//            return new InvalidParameterException("");
//        }
        IPfromStr(strIP,ips);
        return ips[0];
    }

    public static boolean checkMatchIPwithMaskLen(int ip, int masklen){

        int maskip = NetmaskFromMasklen(masklen);
        return ip == (ip&maskip);
    }
}
