//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.hichip.data;

import android.media.AudioManager;
import android.media.AudioTrack;

import com.decoder.util.DecEncG711;
import com.decoder.util.DecG726;
import com.encoder.util.EncG726;
import com.tutk.IOTC.L;

public class HiAudioPlay {
    private boolean mInitAudio = false;
    private AudioTrack mAudioTrack = null;
    private AudioManager audioManager = null;
    private int audioType;
    private byte[] buff = new byte[320];
    private long[] outLen = new long[1];

    public HiAudioPlay() {
    }

    public boolean init(int audioType) {
        if (!this.mInitAudio) {
            this.audioType = audioType;
            L.i("IOTCamera", "init bruce audiotype " + audioType);
            short sampleRateInHz = 8000;
            byte channelConfig = 2;
            byte audioFormat = 2;
            boolean mMinBufSize = false;
            int mMinBufSize1 = AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat);
            if (mMinBufSize1 != -2 && mMinBufSize1 != -1) {
                try {
                    this.mAudioTrack = new AudioTrack(3, sampleRateInHz, channelConfig, audioFormat, mMinBufSize1, 1);
                } catch (IllegalArgumentException var7) {
                    var7.printStackTrace();
                    return false;
                }

                this.mAudioTrack.setStereoVolume(1.0F, 1.0F);
                this.mAudioTrack.play();
                this.mInitAudio = true;
                if (audioType == 1) {
                    EncG726.g726_enc_state_create((byte) EncG726.G726_16, EncG726.FORMAT_LINEAR);
                }

                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public byte[] audioPlay(byte[] audioData, int sizeInBytes, boolean isplay) {
        if (!this.mInitAudio) {
            return null;
        } else if (this.audioType == 1) {
            DecG726.g726_decode(audioData, sizeInBytes, this.buff, this.outLen);
            if (isplay) {
                this.mAudioTrack.write(this.buff, 0, (int) this.outLen[0]);
            }
            return this.buff;
        } else if (this.audioType == 0) {
            DecEncG711.Decode(this.buff, sizeInBytes, audioData);
            if (isplay) {
                this.mAudioTrack.write(this.buff, 0, 320);
            }

            return this.buff;
        } else {
            return null;
        }
    }

    public void uninit() {
        if (this.mInitAudio) {
            L.i("IOTCamera", "uninit bruce audiotype " + audioType);
            if (this.mAudioTrack != null) {
                this.mAudioTrack.stop();
                this.mAudioTrack.release();
                this.mAudioTrack = null;
            }

            if (this.audioType == 1) {
                EncG726.g726_enc_state_destroy();
            }

            this.mInitAudio = false;
        }

    }
}
