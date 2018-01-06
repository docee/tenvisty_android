package com.hichip.coder;

/**
 * Created by Administrator on 2017/7/21.
 */

public class EncMp4 {
    public static final int CHIP_TYPE_HISI = 0;
    public static final int CHIP_TYPE_GOKE = 1;
    public static final int MP4FRMTYPE_I = 0;
    public static final int MP4FRMTYPE_P = 1;
    public static final int MP4FRMTYPE_AUDIO = 2;
    public static final int MP4FRMTYPE_BUTT = 3;

    static {
        try {
            System.loadLibrary("EncMp4");
        } catch (UnsatisfiedLinkError var1) {
            System.out.println("loadLibrary(EncMp4)," + var1.getMessage());
        }

    }

    public EncMp4() {
    }

    public static native int HIEncMp4init(int[] var0, int var1, int var2, String var3, int var4);

    public static native int HIEncMp4write(int var0, byte[] var1, int var2, int var3, int var4);

    public static native int HIEncMp4deinit(int var0);
}
