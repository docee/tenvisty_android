package com.tws.commonlib.wificonfig;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tutk.IOTC.TwsThread;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;

import voice.StringEncoder;
import voice.decoder.DataDecoder;
import voice.decoder.VoiceRecognizer;
import voice.encoder.DataEncoder;
import voice.encoder.VoicePlayer;
import voice.encoder.VoicePlayerListener;

/**
 * Created by Administrator on 2017/8/15.
 */

public class FaceberConfig extends BaseConfig {

    static {
        Log.d("tag", "install  voiceRecog.so");
        System.loadLibrary("voiceRecog");
    }
    /* 用于 udpReceiveAndTcpSend 的3个变量 */
    Socket socket = null;
    MulticastSocket ms = null;
    DatagramSocket ds = null;
    DatagramPacket dp;
    udpReceiveAndtcpSend udpReceiver;

    private boolean isStopped;
    private boolean isInited;
    VoicePlayer player;//声波通讯播放器
    VoiceRecognizer recognizer;//声波通讯识别器
    private static FaceberConfig instance;
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            player.play(DataEncoder.encodeSSIDWiFi(ssid, pwd), 1, 1000);
        }
    };

    public synchronized static FaceberConfig singleInstance() {
        if (instance == null) {
            instance = new FaceberConfig();
            instance.isInited = false;
        }
        return instance;
    }

    private void init() {
        StringEncoder se = new StringEncoder() {
            public byte[] string2Bytes(String _s) {
                try {
                    return _s.getBytes("utf-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }

            public String bytes2String(byte[] _bytes, int _off, int _len) {
                try {
                    return new String(_bytes, _off, _len, "utf-8");
                } catch (UnsupportedEncodingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                return null;
            }
        };
        DataEncoder.setStringEncoder(se);
        DataDecoder.setStringDecoder(se);
        //创建声波通讯播放器
        player = new VoicePlayer();
        int freqs[] = new int[19];
        int baseFreq = 4000;
        for (int i = 0; i < freqs.length; i++) {
            freqs[i] = baseFreq + i * 150;
        }
        player.setFreqs(freqs);
        player.setListener(new VoicePlayerListener() {
            @Override
            public void onPlayStart(VoicePlayer voicePlayer) {

            }

            @Override
            public void onPlayEnd(VoicePlayer voicePlayer) {
                if(!isStopped) {
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendEmptyMessageDelayed(1,5000);
                }
            }
        });
        isInited = true;
    }

    @Override
    public void runConfig() {
        isStopped = false;
        if (!isInited) {
            init();
        }
        if(udpReceiver == null) {
            udpReceiver = new udpReceiveAndtcpSend();
        }
        udpReceiver.startThread();
        player.play(DataEncoder.encodeSSIDWiFi(ssid, pwd), 1, 1000);

    }

    @Override
    public void stopConfig() {
        isStopped = true;
        player.stop();
        if(handler.hasMessages(1)){
            handler.removeMessages(1);
        }
        if(ds!=null) {
            ds.close();
            if(ds.isConnected()) {
                ds.disconnect();
            }
        }
        if(udpReceiver != null) {
            udpReceiver.stopThread();
            udpReceiver = null;
        }
    }

    private class udpReceiveAndtcpSend extends TwsThread {
        @Override
        public void run() {
            byte[] data = new byte[1024];
            try {
                ds = new DatagramSocket(8601);
//                InetAddress groupAddress = InetAddress.getByName("224.0.0.1");
//                ms = new MulticastSocket(8601);
//                ms.joinGroup(groupAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }

            while (isRunning) {
                try {
                    dp = new DatagramPacket(data, data.length);
                    if (ds != null)
                        ds.receive(dp);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (dp.getAddress() != null) {
                    final String quest_ip = dp.getAddress().toString();

                    final String codeString = new String(data, 0, dp.getLength());
                    if(listener != null){
                        JSONObject arrJson = null;
                        try {
                            arrJson = new JSONObject(codeString);
                            String jsonc = arrJson.getString("SmLinkReport");
                            JSONObject conJson = new JSONObject(jsonc);
                            String status = conJson.getString("Status");
                            String ip = conJson.getString("IP");
                            String uid = conJson.getString("DID");
                            if(uid != null){
                                uid = uid.trim();
                            }
                            listener.OnReceived(status,ip,uid);
                        } catch (JSONException e) {
                            listener.OnReceived(codeString,codeString,codeString);
                            e.printStackTrace();
                        }
                    }
                }
            }
            if(ds != null) {
                ds.close();
                if(ds.isConnected()) {
                    ds.disconnect();
                }
            }
        }
    }

}
