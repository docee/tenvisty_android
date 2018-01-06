package com.tutk.IOTC;

import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

/**
 * 闊抽绫�
 * @version V1.1 2012-10-30
 * @author David.Chen
 */
public class SimpleAudioTrack {
	/**閲囨牱鐜*/
	int mFrequency;
	/**澹伴亾*/
	int mChannel;
	/**閲囨牱绮惧害*/
	int mSampBit; 

	AudioTrack mAudioTrack;

	public SimpleAudioTrack(int frequency, int channel, int sampbit) {
		mFrequency = frequency;
		mChannel = channel;
		mSampBit = sampbit;
	}

	public void init() {
		if (mAudioTrack != null) {
			release();
		}

		// 锟斤拷霉锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷小锟斤拷锟斤拷锟斤拷锟叫�
		int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel,
				mSampBit);

		// STREAM_ALARM锛氳鍛婂０
		// STREAM_MUSCI锛氶煶涔愬０锛屼緥濡俶usic绛�
		// STREAM_RING锛氶搩澹�
		// STREAM_SYSTEM锛氱郴缁熷０闊�
		// STREAM_VOCIE_CALL锛氱數璇濆０闊�
		mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, mFrequency,
				mChannel, mSampBit, minBufSize, AudioTrack.MODE_STREAM);
		// AudioTrack涓湁MODE_STATIC鍜孧ODE_STREAM涓ょ鍒嗙被銆�
		// STREAM鐨勬剰鎬濇槸鐢辩敤鎴峰湪搴旂敤绋嬪簭閫氳繃write鏂瑰紡鎶婃暟鎹竴娆′竴娆″緱鍐欏埌audiotrack涓�
		// 杩欎釜鍜屾垜浠湪socket涓彂閫佹暟鎹竴鏍凤紝搴旂敤灞備粠鏌愪釜鍦版柟鑾峰彇鏁版嵁锛屼緥濡傞�杩囩紪瑙ｇ爜寰楀埌PCM鏁版嵁锛岀劧鍚巜rite鍒癮udiotrack銆�
		// 杩欑鏂瑰紡鐨勫潖澶勫氨鏄�鏄湪JAVA灞傚拰Native灞備氦浜掞紝鏁堢巼鎹熷け杈冨ぇ銆�
		// 鑰孲TATIC鐨勬剰鎬濇槸涓�紑濮嬪垱寤虹殑鏃跺�锛屽氨鎶婇煶棰戞暟鎹斁鍒颁竴涓浐瀹氱殑buffer锛岀劧鍚庣洿鎺ヤ紶缁檃udiotrack锛�
		// 鍚庣画灏变笉鐢ㄤ竴娆℃寰梬rite浜嗐�AudioTrack浼氳嚜宸辨挱鏀捐繖涓猙uffer涓殑鏁版嵁銆�
		// 杩欑鏂规硶瀵逛簬閾冨０绛夊唴瀛樺崰鐢ㄨ緝灏忥紝寤舵椂瑕佹眰杈冮珮鐨勫０闊虫潵璇村緢閫傜敤銆�

		mAudioTrack.play();
	}

	public void release() {
		if (mAudioTrack != null) {
			mAudioTrack.stop();
			mAudioTrack.release();
		}
	}

	public void playAudioTrack(byte[] data, int offset, int length) {
		if (data == null || data.length == 0) {
			return;
		}

		try {
			mAudioTrack.write(data, offset, length);
		} catch (Exception e) {
			// TODO: handle exception
			Log.i("MyAudioTrack", "catch exception...");
		}
	}

	public int getPrimePlaySize() {
		int minBufSize = AudioTrack.getMinBufferSize(mFrequency, mChannel,
				mSampBit);

		return minBufSize * 2;
	}
}
