package com.misc;

import java.io.Serializable;

public class LibcMisc  implements Serializable {
	public static void memcpy(byte[] dst, int offset, int src, int len)
	{
		byte[] byte_src = new byte[4];
		byte_src[3] = (byte) ((src & 0xFF000000)>>24);
		byte_src[2] = (byte) ((src & 0x00FF0000)>>16);
		byte_src[1] = (byte) ((src & 0x0000FF00)>>8);
		byte_src[0] = (byte) ((src & 0x000000FF));
		
		System.arraycopy(byte_src, 0, dst, offset, len);
	}
	
	public static void memcpy(byte[] dst, int offset, short src, int len)
	{
		byte[] byte_src = new byte[4];
		byte_src[0] = (byte) ((src & 0x000000FF));
		byte_src[1] = (byte) ((src & 0x0000FF00)>>8);
		
		System.arraycopy(byte_src, 0, dst, offset, len);
	}
	
	public static void memcpy(byte[] dst, int src, int len)
	{
		memcpy(dst, 0, src, len);
	}
	
	public static void memcpy(byte[] src, int srcpos, byte[] dst, int dstpos, int len)
	{
		for(int i = 0; i < len; i++)
		{
			src[srcpos + i] = dst[dstpos + i];
		}
		return;
	}

	public static short get_short(byte[] ary, int offset) {
		short value;
		
		value = (short) ((ary[offset] & 0xFF) | ((ary[offset+1]<<8) & 0xFF00));
		return value;
	}
	
	public static short[] get_short_array(byte[] ary, int offset)
	{
		short[] value = new short[(ary.length - offset) >> 1];
		
		for(int i = 0; i < (ary.length - offset); i += 2){
			value[i >> 1] = (short) (((ary[offset + i]) & 0xFF) | ((ary[offset + i + 1] << 8) & 0xFF00));			
		}

		return value;
	}

	public static int get_int(byte[] ary, int offset) {
		int value;
		
		value = (ary[offset]&0xFF) | ((ary[offset+1]<<8) & 0xFF00)
				| ((ary[offset+2]<<16)&0xFF0000) | ((ary[offset+3]<<24) & 0xFF000000);
		return value;
	}
	
	public static int memcmp(byte[] src, int srcpos, byte[] dst, int dstpos, int len)
	{
		for(int i = 0; i < len; i++)
		{
			if(src[srcpos + i] < dst[dstpos + i])
			{
				return -1;
			}
			else if((src[srcpos + i] == dst[dstpos + i]))
			{
				continue;
			}
			else
			{
				return 1;
			}
		}
		return 0;
	}
	
	public static void memset(byte[] content, int j, int len)
	{
		for(int i = 0; i < len; i++)
		{
			content[i] = (byte)j;
		}
		
		return;
	}
	
	public static String array2string(byte[] data, int offset, int len)
	{
		String out = new String();
		
		for(int i = offset; i < (offset + len); i++)
		{
			out += String.format("%02x", data[i]);
		}
		
		return out;
	}
	

	/**
	 * SearchMjpegCamera.java get search camera params
	 * @param array
	 * @param offset
	 * @return
	 */
	public static String get_string(byte[] array,int offset)
    {
    	return ""+(array[offset]&0x00ff)+"."+(array[offset+1]&0x00ff)+"."
				+(array[offset+2]&0x00ff)+"."+(array[offset+3]&0x00ff);
    }
}
