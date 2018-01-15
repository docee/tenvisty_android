package com.tws.commonlib.wificonfig;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.hichip.tools.HiSinVoiceData;
import com.libra.sinvoice.SinVoicePlayer;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2017/8/15.
 */

public class HiVoiceConfig extends BaseConfig implements SinVoicePlayer.Listener {

    private static int sinVioceSeq;
    private static final int PACK_LEN = 35;
    private SinVoicePlayer mSinVoicePlayer = null;
    private boolean isSinVioce = false;
    private int list_index = 0;
    private static final byte cmdSSID = 0;
    private static final byte cmdKEY = 1;
    public List<int[]> dataList = new ArrayList();

    static {
        System.loadLibrary("sinvoice_no_sign");
        sinVioceSeq = 0;
    }

    private boolean isStopped;
    private boolean isInited;
    private static HiVoiceConfig instance;

    public synchronized static HiVoiceConfig singleInstance() {
        if (instance == null) {
            instance = new HiVoiceConfig();
            instance.isInited = false;
        }
        return instance;
    }

    @Override
    public void set(String ssid, String pwd) {
        super.set(ssid,pwd);
        byte[] SSID = null;
        byte[] KEY = null;

        try {
            SSID = ssid.getBytes("UTF8");
            KEY = pwd.getBytes("UTF8");
        } catch (UnsupportedEncodingException var6) {
            var6.printStackTrace();
        }

        this.dataList.clear();
        this.packSinVoiceData(sinVioceSeq, 0, SSID);
        this.packSinVoiceData(sinVioceSeq, 1, KEY);
    }

    private void init() {
        sinVioceSeq = sinVioceSeq > 3?0:++sinVioceSeq;
        this.mSinVoicePlayer = new SinVoicePlayer();
        this.mSinVoicePlayer.init(this.context);
        this.mSinVoicePlayer.setListener(this);
        isInited = true;
    }
    public void startSinVoice() {
        if(this.dataList.size() >= 2) {
            this.mSinVoicePlayer.play((int[])this.dataList.get(0), ((int[])this.dataList.get(0)).length, false, 2000);
            this.isSinVioce = true;
        }

    }
    public void stopSinVoice() {
        this.isSinVioce = false;
        this.mSinVoicePlayer.stop();
    }

    @Override
    public void runConfig() {
        isStopped = false;
        if (!isInited) {
            init();
        }
        startSinVoice();

    }

    @Override
    public void stopConfig() {
        isStopped = true;
        stopSinVoice();
    }
    private void packSinVoiceData(int seq, int cmd, byte[] data) {
        int len = data.length;
        int pack_count = len / 32 + 1;
        int data_pos = 0;
        Log.v("hichip", "len:" + len + "   pack_count:" + pack_count);

        for(int item = 0; item < pack_count; ++item) {
            byte check = 0;
            byte pack_head = 0;
            byte data_len = 0;
            pack_head = (byte)((byte)((3 & seq) << 6) | (byte)((3 & cmd) << 4) | (byte)(15 & item));
            data_len = (byte)(255 & len);
            int pack_len = 32 >= len - data_pos?len - data_pos + 3:35;
            byte[] data_pack = new byte[pack_len];
            data_pack[0] = pack_head;
            data_pack[1] = data_len;
            System.arraycopy(data, data_pos, data_pack, 3, pack_len - 3);
            data_pos = data_pos + pack_len - 3;
            check = (byte)(data_pack[0] ^ data_pack[1]);
            int[] tokens = new int[pack_len];

            for(int j = 0; j < pack_len; ++j) {
                tokens[j] = data_pack[j];
                if(j > 2) {
                    check ^= data_pack[j];
                }
            }

            data_pack[2] = check;
            tokens[2] = data_pack[2];
            this.dataList.add(tokens);
            if(data_pos >= len) {
                break;
            }
        }

    }

    @Override
    public void onSinVoicePlayStart() {

    }

    @Override
    public void onSinVoicePlayEnd() {
        if(this.isSinVioce) {
            this.list_index = this.list_index < this.dataList.size() - 1?++this.list_index:0;
            this.mSinVoicePlayer.play((int[])this.dataList.get(this.list_index), ((int[])this.dataList.get(this.list_index)).length, false, 2000);
        }
    }

    @Override
    public void onSinToken(int[] ints) {

    }
}
