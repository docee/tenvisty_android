package com.decoder.util;

public class DecEncG711 {



	public static native int Decode(byte[] in, int in_size, byte[] out);
	
	public static native int Encode(byte[] in, int in_size, byte[] out);
	
	static {

		try {
			System.loadLibrary("DecEncG711Android");
		} catch (UnsatisfiedLinkError ule) {
			System.out.println("loadLibrary(DecEncG711Android)," + ule.getMessage());
		}
	}
}
