package com.tutk.IOTC;

/**
 * Created by Administrator on 2017/10/17.
 */

public class HI_P2P_S_AVFrame {
    public int u32AVFrameFlag;
    public int u32AVFrameLen;
    public int u32AVFramePTS;
    public int u32VFrameType;

    public HI_P2P_S_AVFrame() {
    }

    public HI_P2P_S_AVFrame(byte[] byt) {
        if(byt.length >= 16) {
            this.u32AVFrameFlag = Packet.byteArrayToInt_Little(byt, 0);
            this.u32AVFrameLen = Packet.byteArrayToInt_Little(byt, 4);
            this.u32AVFramePTS = Packet.byteArrayToInt_Little(byt, 8);
            this.u32VFrameType = Packet.byteArrayToInt_Little(byt, 12);
        }

    }

    public void setData(byte[] byt) {
        if(byt.length >= 16) {
            this.u32AVFrameFlag = Packet.byteArrayToInt_Little(byt, 0);
            this.u32AVFrameLen = Packet.byteArrayToInt_Little(byt, 4);
            this.u32AVFramePTS = Packet.byteArrayToInt_Little(byt, 8);
            this.u32VFrameType = Packet.byteArrayToInt_Little(byt, 12);
        }

    }

    public static int sizeof() {
        return 16;
    }

    public static byte[] parseContent(int AVFrameFlag, int AVFrameLen, int AVFramePTS, int AVFrameType) {
        byte[] result = new byte[16];
        byte[] b_AVFrameFlag = Packet.intToByteArray_Little(AVFrameFlag);
        byte[] b_AVFrameLen = Packet.intToByteArray_Little(AVFrameLen);
        byte[] b_AVFramePTS = Packet.intToByteArray_Little(AVFramePTS);
        byte[] b_AVFrameType = Packet.intToByteArray_Little(AVFrameType);
        System.arraycopy(b_AVFrameFlag, 0, result, 0, 4);
        System.arraycopy(b_AVFrameLen, 0, result, 4, 4);
        System.arraycopy(b_AVFramePTS, 0, result, 8, 4);
        System.arraycopy(b_AVFrameType, 0, result, 12, 4);
        return result;
    }
}