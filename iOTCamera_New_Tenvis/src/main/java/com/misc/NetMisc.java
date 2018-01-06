package com.misc;

import java.io.Serializable;

public class NetMisc  implements Serializable {
	public static short ntohs(short value)
	{
		value = (short) (((value  << 8)& 0xFF00) | ((value  >> 8)& 0xFF));
		return value;
	}
	
	public static short htons(short value)
	{
		value = (short) (((value << 8) & 0xFF00) | ((value >> 8)& 0xFF));
		return value;
	}
	
	public static short get_short(byte[] ary, int offset)
	{
		short value;
		
		value = (short) ((ary[offset]&0xFF) | ((ary[offset+1]<<8) & 0xFF00));
		return value;
	}
	
	public static int get_int(byte[] ary, int offset)
	{
		int value;
		
		value = (ary[offset] & 0xFF)
				| ((ary[offset+1]<<8) & 0xFF00)
				| ((ary[offset+2]<<16) & 0xFF0000)
				| ((ary[offset+3]<<24) & 0xFF000000);
		return value;
	}
}
