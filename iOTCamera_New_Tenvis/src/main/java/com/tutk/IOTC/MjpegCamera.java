package com.tutk.IOTC;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.UnresolvedAddressException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.misc.LibcMisc;
import com.misc.NetMisc;
import com.misc.RefInteger;
import com.misc.objc.CFReadStream;
import com.misc.objc.CFRunLoop;
import com.misc.objc.CFRunLoopTimer;
import com.misc.objc.CFStream;
import com.misc.objc.CFStreamClientCallBack;
import com.misc.objc.CFWriteStream;
import com.misc.objc.NSData;
import com.misc.objc.NSDictionary;
import com.misc.objc.NSMutableArray;
import com.misc.objc.NSMutableData;
import com.misc.objc.NSNotification;
import com.misc.objc.NSNotificationCenter;
import com.misc.objc.NSRange;
import com.tutk.IOTC.SearchMjpegCamera;

/**
 * 摄像头类
 * 
 * @version V1.1,2012-10-30
 * @author Thomas Yi,David.Chen
 * 
 */
public class MjpegCamera extends NSCamera implements Serializable, CFStreamClientCallBack {//implements Serializable用于网络环境下的类传输，当你这个类的对象要被通过stream传送到另一个地方去的时候，就要将该类声明成要序列化的声明完了以后不需要实现任何接口。


	/** 摄像头状态 */
	public enum CAMERA_STATUS {
		DISCONNECTED, DNSRESOLVING, CONNECTING, LOGINING, VERIFYING, WRONG_PASSWORD, CONNECTED,
	}

	/** 视频播放状态 */
	public enum PLAYING_STATUS {
		STOPPED, REQUESTING, PLAYING,
	}

	/** 摄像头报警状态或报警类型 */
	public enum ALARM_STATUS {
		NONE, MOTION_DETECTING, TRIGGER_DETECTING, SOUND_DETECTING, UNKNOWN_ALARM,
	}

	/** 摄像头出错类型 */
	public enum CAMERA_ERROR {
		OK, BAD_PARAMS, BAD_STATUS, INTERNAL_ERROR, SOCKET_ERROR, CANT_CONNECT, PEER_CLOSED, UNKNOWN_ERROR, BAD_ID, MAX_SESSION, BAD_AUTH, TIMEOUT, FORBIDDEN, UNSUPPORT,
	}

	/** PTZ控制命令 */
	public enum PTZ_COMMAND {
		T_UP, T_DOWN, P_LEFT, P_RIGHT, PT_LEFT_UP, PT_RIGHT_UP, PT_LEFT_DOWN, PT_RIGHT_DOWN, PT_CENTER, PT_UP_STOP, PT_DOWN_STOP, PT_LEFT_STOP, PT_RIGHT_STOP, P_PATROL, P_PATROL_STOP, T_PATROL, T_PATROL_STOP, ZOOM_WIDE, ZOOM_TELE, IO_ON, IO_OFF, PT_SET_RESET1, PT_GO_RESET1, PT_SET_RESET2, PT_GO_RESET2, PT_SET_RESET3, PT_GO_RESET3, PT_SET_RESET4, PT_GO_RESET4, PT_SET_RESET5, PT_GO_RESET5, PT_SET_RESET6, PT_GO_RESET6, PT_SET_RESET7, PT_GO_RESET7, PT_SET_RESET8, PT_GO_RESET8, PT_SET_RESET9, PT_GO_RESET9, PT_SET_RESET10, PT_GO_RESET10, PT_SET_RESET11, PT_GO_RESET11, PT_SET_RESET12, PT_GO_RESET12, PT_SET_RESET13, PT_GO_RESET13, PT_SET_RESET14, PT_GO_RESET14, PT_SET_RESET15, PT_GO_RESET15, PT_SET_RESET16, PT_GO_RESET16,
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1693364472586999561L;//序列化运行时使用一个称为 serialVersionUID 的版本号与每个可序列化类相关联,该序列号在反序列化过程中用于验证序列化对象的发送者和接收者是否为该对象加载了与序列化兼容的类

	/**
	 * 构造函数
	 * 
	 * @param id
	 * @param name
	 * @param host
	 * @param port
	 * @param user
	 * @param pass
	 * @param audio_buffer_time
	 */
	public MjpegCamera(String id, String name, String host, String port,
			String user, String pass, int audio_buffer_time) {
		super();
		this.init();
		this.uid = id;
		this.host = host;
		this.port = port;
		this.name = name;
		this.user = user;
		this.pwd = pass;
		socketAddress = null;
		this.audio_buffer_time = String.valueOf(audio_buffer_time);
	}

	public MjpegCamera() {
		this.init();
		
		socketAddress = null;
		this.audio_buffer_time = String.valueOf(100);
	}

	private Handler mChildHandler;
	Thread audioThread = null;
	InetSocketAddress socketAddress = null;
	String audio_buffer_time;

	// modify by thinker on 2013-3-1
	int host_type;

	public String getIdentity() {
		return uid;
	}

	public void setIdentity(String identity) {
		this.uid = identity;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getLANHost() {
		return LANHost;
	}

	public void setLANHost(String LANHost) {
		this.LANHost = LANHost;
	}

	public String getLANPort() {
		return LANPort;
	}

	public void setLANPort(String LANPort) {
		this.LANPort = LANPort;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getDdns() {
		return ddns;
	}

	public void setDdns(String ddns) {
		this.ddns = ddns;
	}

	public String getAudio_buffer_time() {
		return audio_buffer_time;
	}

	public void setAudio_buffer_time(String audio_buffer_time) {
		this.audio_buffer_time = audio_buffer_time;
	}

	public CAMERA_STATUS camera_status;
	public PLAYING_STATUS video_status;
	public PLAYING_STATUS audio_status;
	public PLAYING_STATUS talk_status;
	public ALARM_STATUS alarm_status;
	public CAMERA_ERROR error;
	public boolean started;
	// NSMutableArray[] video_queue;
	// NSMutableArray[] audio_queue;

	CFReadStream op_r_stream;
	CFWriteStream op_w_stream;
	CFReadStream av_r_stream;
	CFWriteStream av_w_stream;

	NSMutableData op_r_data;
	NSMutableData op_w_data;
	NSMutableData av_r_data;
	NSMutableData av_w_data;

	// AudioTrack audio_player;

	transient CFRunLoopTimer timer;
	int resolution;
	int brightness;
	int contrast;
	int mode;
	int flip;
	float volume;

	RefInteger adpcm_decode_sample = new RefInteger(0);
	RefInteger adpcm_decode_index = new RefInteger(0);
	RefInteger adpcm_encode_sample = new RefInteger(0);
	RefInteger adpcm_encode_index = new RefInteger(0);
	int talk_seq;
	int talk_tick;
	private long op_t;
	private long op_r_t;
	private long op_w_t;
	private long local_start_tick;
	private long camera_start_tick;
	private NSMutableArray video_queue;
	private NSMutableArray audio_queue;
	AVIGenerator mAVIRecorder = null;
	boolean iscapturing = false;
	Thread talkThread = null;

	private boolean isConnectLAN;
	private int connect_time_s; //LAN:2s WAN:4s
	static final int LAN_CONNECT_TIME_S = 4;    //内网连接时间
	static final int WAN_CONNECT_TIME_S = 10;   //外网连接时间

	class AvDataBuffer {
		int nTotalWant2RcvLen = 0;
		int nRemainingLen = 0;
		byte[] buffer = null;

		public AvDataBuffer(int nTotalWant2RcvLen) {
			this.nTotalWant2RcvLen = nTotalWant2RcvLen;
			this.nRemainingLen = nTotalWant2RcvLen;
			
			try {
				buffer = new byte[nTotalWant2RcvLen];
			} catch (OutOfMemoryError e) {
				System.gc();
				Log.e(this.getClass().getSimpleName(), "OutOfMemoryError");
			}
		}

		public byte[] getData() {
			return buffer;
		}

		public int getWriteOffset() {
			return nTotalWant2RcvLen - nRemainingLen;
		}

		public int getWriteLength() {
			return nRemainingLen;
		}

		public void refreshWritenLength(int len) {
			nRemainingLen -= len;
		}

	}

	enum AvRcvState {
		AV_RCVSTATE_HDR, AV_RCVSTATE_AUDIO, AV_RCVSTATE_VIDEO
	}

	transient AvDataBuffer av_buffer;
	transient AvRcvState av_state;

	byte[] linkid = null;

	public static boolean searching_flag = false;
	// static PlayAudioThread audio_thread = new PlayAudioThread();

	/** 摄像头搜索完成 */
	static volatile int g_searching = 0;
	// static final int AUDIO_RECORD_BUFFERS_NUMBER = 10;
	// static final int AUDIO_PLAY_BUFFERS_NUMBER = 10;
	/** 音频缓冲,值为{@value} */
	public static final int AUDIO_BUFFER_SIZE = 640;
	static final int AUDIO_PACKET_PAYLOAD_SIZE = 160;
	static final int HEADER_LENGTH = 23;

	static final byte[] MO_I = { 'M', 'O', '_', 'I' };
	static final byte[] MO_O = { 'M', 'O', '_', 'O' };
	static final byte[] MO_V = { 'M', 'O', '_', 'V' };

	static final short OP_LOGIN_REQ = 0;
	static final short OP_LOGIN_RESP = 1;
	static final short OP_VERIFY_REQ = 2;
	static final short OP_VERIFY_RESP = 3;
	static final short OP_VIDEO_START_REQ = 4;
	static final short OP_VIDEO_START_RESP = 5;
	static final short OP_VIDEO_END = 6;
	static final short OP_AUDIO_START_REQ = 8;
	static final short OP_AUDIO_START_RESP = 9;
	static final short OP_AUDIO_END = 10;
	static final short OP_TALK_START_REQ = 11;
	static final short OP_TALK_START_RESP = 12;
	static final short OP_TALK_END = 13;
	static final short OP_DECODER_CONTROL_REQ = 14;
	static final short OP_PARAMS_FETCH_REQ = 16;
	static final short OP_PARAMS_FETCH_RESP = 17;
	static final short OP_PARAMS_CHANGED_NOTIFY = 18;
	static final short OP_PARAMS_SET_REQ = 19;
	static final short OP_ALARM_NOTIOFY = 25;
	static final short OP_KEEP_ALIVE = 255;

	static final short AV_LOGIN_REQ = 0;
	static final short AV_VIDEO_DATA = 1;
	static final short AV_AUDIO_DATA = 2;
	static final short AV_TALK_DATA = 3;

	public static final String ACTION_CAMERA_FOUND = "camera.found";
	public static final String ACTION_CAMERA_EOS = "camera.endofsearch";
	public static final String IPCamera_Search_Ended_Notification = "IPCamera_Search_Ended_Notification";
	public static final String IPCamera_CameraStatusChanged_Notification = "IPCamera_CameraStatusChanged_Notification";
	public static final String IPCamera_VideoStatusChanged_Notification = "IPCamera_VideoStatusChanged_Notification";
	public static final String IPCamera_AudioStatusChanged_Notification = "IPCamera_AudioStatusChanged_Notification";
	public static final String IPCamera_TalkStatusChanged_Notification = "IPCamera_TalkStatusChanged_Notification";
	public static final String IPCamera_AlarmStatusChanged_Notification = "IPCamera_AlarmStatusChanged_Notification";
	public static final String IPCamera_Image_Notification = "IPCamera_Image_Notification";
	public static final String IPCamera_Audio_Notification = "IPCamera_Audio_Notification";
	public static final String IPCamera_CameraParamChanged_Notification = "IPCamera_CameraParamChanged_Notification";

	public static final String WAN_HOST_1 = ".tenvis.info";
	public static final String WAN_HOST_2 = ".mytenvis.com";
	public static final String WAN_HOST_3 = ".mytenvis.org";
	static int[] index_adjust = { -1, -1, -1, -1, 2, 4, 6, 8 };

	/** 
	 * This is the step table. Note that many programs use slight deviations from
	 * this table, but such deviations are negligible:
	 */
	static int[] step_table = { 7, 8, 9, 10, 11, 12, 13, 14, 16, 17, 19, 21,
			23, 25, 28, 31, 34, 37, 41, 45, 50, 55, 60, 66, 73, 80, 88, 97,
			107, 118, 130, 143, 157, 173, 190, 209, 230, 253, 279, 307, 337,
			371, 408, 449, 494, 544, 598, 658, 724, 796, 876, 963, 1060, 1166,
			1282, 1411, 1552, 1707, 1878, 2066, 2272, 2499, 2749, 3024, 3327,
			3660, 4026, 4428, 4871, 5358, 5894, 6484, 7132, 7845, 8630, 9493,
			10442, 11487, 12635, 13899, 15289, 16818, 18500, 20350, 22385,
			24623, 27086, 29794, 32767 };

	protected void adpcm_encode(byte[] raw, int len, byte[] encoded,
			int encoded_offset, RefInteger refsample, RefInteger refindex) {
		short[] pcm = LibcMisc.get_short_array(raw, 0);
		int cur_sample;
		int i;
		int delta;
		int sb;
		int code;
		len >>= 1;
		int pre_sample = refsample.getValue();
		int index = refindex.getValue();

		for (i = 0; i < len; i++) {
			cur_sample = pcm[i]; //
			delta = cur_sample - pre_sample; //
			if (delta < 0) {
				delta = -delta;
				sb = 8; //
			} else {
				sb = 0;
			} //
			code = 4 * delta / step_table[index]; //
			if (code > 7)
				code = 7; //

			delta = (step_table[index] * code) / 4 + step_table[index] / 8; //
			if (sb > 0)
				delta = -delta;
			pre_sample += delta; //
			if (pre_sample > 32767)
				pre_sample = 32767;
			else if (pre_sample < -32768)
				pre_sample = -32768;

			index += index_adjust[code]; //
			if (index < 0)
				index = 0; //
			else if (index > 88)
				index = 88;

			if ((i & 0x01) == 0x01)
				encoded[encoded_offset + (i >> 1)] |= code | sb;
			else
				encoded[encoded_offset + (i >> 1)] = (byte) ((code | sb) << 4); //
		}

		refsample.setValue(pre_sample);
		refindex.setValue(index);
	}

	protected static void adpcm_decode(byte[] raw, int rawpos, int len,
			byte[] decoded, RefInteger refsample, RefInteger refindex) {
		int i;
		int code;
		int sb;
		int delta;
		// short[] pcm = LibcMisc.get_short_array(decoded,0);
		short temp;
		len <<= 1;
		int pre_sample, index;

		pre_sample = refsample.getValue();
		index = refindex.getValue();
		for (i = 0; i < len; i++) {
			if (1 == (i & 0x01))
				code = raw[rawpos + (i >> 1)] & 0x0f; //
			else
				code = raw[rawpos + (i >> 1)] >> 4; //
			if ((code & 8) != 0)
				sb = 1;
			else
				sb = 0;
			code &= 7; //

			delta = (step_table[index] * code) / 4 + step_table[index] / 8; //
			if (sb > 0)
				delta = -delta;
			pre_sample += delta; //
			if (pre_sample > 32767)
				pre_sample = 32767;
			else if (pre_sample < -32768)
				pre_sample = -32768;
			// pcm[i] = ((Integer)pre_sample).shortValue();
			temp = ((Integer) pre_sample).shortValue();
			decoded[i << 1] = (byte) (temp & 0xFF);
			decoded[(i << 1) + 1] = (byte) ((temp & 0xFF00) >> 8);
			index += index_adjust[code];
			if (index < 0)
				index = 0;
			if (index > 88)
				index = 88;
		}

		refsample.setValue(pre_sample);
		refindex.setValue(index);
	}

	/** 摄像头ID,值为{@value} */
	public static final String SEARCHED_CAMERA_ID = "dev_id";
	/** 摄像头IP,值为{@value} */
	public static final String SEARCHED_CAMERA_IP = "dev_ip";
	/** 摄像头端口号,值为{@value} */
	public static final String SEARCHED_CAMERA_PORT = "dev_port";
	/** 摄像头ddns,值为{@value} */
	public static final String SEARCHED_CAMERA_DDNS_URES = "dev_ddns_URES";
	/** 摄像头型号,值为{@value} */
	public static final String SEARCHED_CAMERA_MODEL = "dev_model";
	private static SearchMjpegCamera searchCamera = null;
	public static CAMERA_ERROR search(int timeout) {
		if (timeout < 500) {
			return CAMERA_ERROR.BAD_PARAMS;
		}

		if (searchCamera != null) {
			searchCamera.endSearchCamera();
		}

		searchCamera = new SearchMjpegCamera(timeout);
		searchCamera.start();

		return CAMERA_ERROR.OK;
	}
	
	/**
	 * 初始化
	 */
	protected void init() {
		uid = null;
		name = null;
		host = null;
		port = null;
		LANHost = null;
		LANPort = null;
		user = null;
		pwd = null;
		ddns = "";
		audio_buffer_time = null;
		host_type = 0;

		camera_status = CAMERA_STATUS.DISCONNECTED;
		video_status = PLAYING_STATUS.STOPPED;
		audio_status = PLAYING_STATUS.STOPPED;
		talk_status = PLAYING_STATUS.STOPPED;
		alarm_status = ALARM_STATUS.NONE;
		error = CAMERA_ERROR.OK;

		started = false;

		timer = null;

		// play_clients_number = 0;
		volume = (float) 1.0;
	}

	/**
	 * 开始连接摄像头
	 * 
	 * @return
	 */
	public void start() {
		if ((uid == null) || (name == null) || (host == null)
				|| (port == null) || (user == null) || (pwd == null)
				|| (audio_buffer_time == null))
		{
			Log.e(this.getClass().getSimpleName(), "ipcamera start BAD_PARAMS");
//			return CAMERA_ERROR.BAD_PARAMS;
		}
		if (port.length() != 0) {
			if (host.equals("") || user.equals("") || (Integer.parseInt(port) <= 0)
					|| (Integer.parseInt(port) > 0xffff)
					|| (Integer.parseInt(audio_buffer_time) <= 0)
					|| (Integer.parseInt(audio_buffer_time) > 0xffff)){
				Log.e(this.getClass().getSimpleName(), "ipcamera start BAD_PARAMS");
//				return CAMERA_ERROR.BAD_PARAMS;
			}
				
		}
		
		Log.i(this.getClass().getSimpleName(), "ipcamera start");
		stop();

		isConnectLAN = false;
		connect_time_s = LAN_CONNECT_TIME_S;
		host = LANHost;
		port = LANPort;

		timer = CFRunLoopTimer.CFRunLoopTimerCreate(0, 1000, 0,
				new CFRunLoopTimer.CFRunLoopTimerCallBack() {
					public void timeout(CFRunLoopTimer timer, Object o) {
						handle_timer();//----------------------
					}
				}, MjpegCamera.this);
		CFRunLoopTimer.CFRunLoopAddTimer(CFRunLoop.CFRunLoopGetCurrent(),
				timer, null);
		this.socketAddress = null;
		this.op_t = time(null);

		started = true;

//		return CAMERA_ERROR.OK;
	}

	/**
	 * 摄像头停止
	 */
	public void stop() {
		Log.i(this.getClass().getSimpleName(), "ipcamera stop");
		
		if (timer != null) {
			// timer.cancel();
			CFRunLoopTimer.CFRunLoopRemoveTimer(
					CFRunLoop.CFRunLoopGetCurrent(), timer, null);
			timer = null;
		}

		if (started) {
			CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

				public void run() {

					error = CAMERA_ERROR.OK;

					stop_video();
					stop_audio();
					stop_talk();
					
					on_op_stream_disconnected();
				}
			});
		}

		started = false;
	}

	/**
	 * 播放视频
	 * 
	 * @return
	 */
	public CAMERA_ERROR play_video() {
		// play_clients_number ++;
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;
		if (video_status == PLAYING_STATUS.PLAYING)
			return CAMERA_ERROR.OK;
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				if (video_status == PLAYING_STATUS.PLAYING)
					return;
				add_op_video_start_req();
				video_status = PLAYING_STATUS.REQUESTING;
				NSDictionary params = new NSDictionary();
				params.put("status", video_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_VideoStatusChanged_Notification,
						MjpegCamera.this, params);
			}
		});

		Log.i(this.getClass().getSimpleName(), "ipcamera play_video");
		return CAMERA_ERROR.OK;
	}

	/**
	 * 停止视频
	 */
	public void stop_video() {
		// if (--play_clients_number)
		// return;

		if (camera_status != CAMERA_STATUS.CONNECTED)
			return;
		if (video_status == PLAYING_STATUS.STOPPED)
			return;
		System.out.println("stop_video");
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				if (video_status == PLAYING_STATUS.STOPPED)
					return;
				add_op_video_end();
				error = CAMERA_ERROR.OK;
				video_status = PLAYING_STATUS.STOPPED;
				NSDictionary params = new NSDictionary();
				params.put("status", video_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_VideoStatusChanged_Notification,
						MjpegCamera.this, params);
				// if ((video_status == PLAYING_STATUS.STOPPED)
				// && (audio_status == PLAYING_STATUS.STOPPED)
				// && (talk_status == PLAYING_STATUS.STOPPED))
				// disconnect_av_stream();
			}
		});

	}

	/**
	 * 播放音频
	 * 
	 * @return
	 */
	public CAMERA_ERROR play_audio() {
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;

		if (audio_status == PLAYING_STATUS.PLAYING)
			return CAMERA_ERROR.OK;
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;

				if (audio_status == PLAYING_STATUS.PLAYING)
					return;
				add_op_audio_start_req();
				audio_status = PLAYING_STATUS.REQUESTING;

				// if(!audio_thread.isAlive())
				// audio_thread.start();
				NSDictionary params = new NSDictionary();
				params.put("status", audio_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_AudioStatusChanged_Notification,
						MjpegCamera.this, params);
			}
		});

		return CAMERA_ERROR.OK;
	}

	/**
	 * 停止音频
	 */
	public void stop_audio() {
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return;
		if (audio_status == PLAYING_STATUS.STOPPED)
			return;
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				if (audio_status == PLAYING_STATUS.STOPPED)
					return;
				add_op_audio_end();
				error = CAMERA_ERROR.OK;
				audio_status = PLAYING_STATUS.STOPPED;
				stop_audio_play();
				NSDictionary params = new NSDictionary();
				params.put("status", audio_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_AudioStatusChanged_Notification,
						MjpegCamera.this, params);
				// if ((video_status == PLAYING_STATUS.STOPPED)
				// && (audio_status == PLAYING_STATUS.STOPPED)
				// && (talk_status == PLAYING_STATUS.STOPPED))
				// disconnect_av_stream();
			}
		});

	}

	/**
	 * 开始录像
	 * 
	 * @param filename
	 * @param w
	 *            int 录像视频宽带
	 * @param h
	 *            int 录像视频高度
	 * @return
	 */
	public CAMERA_ERROR start_record(String filename, final int w, final int h) {
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;

		final File file = new File(filename);
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {
			public void run() {
				
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;

				mAVIRecorder = new AVIGenerator(file);//
				
				if (video_status != PLAYING_STATUS.STOPPED) {
					mAVIRecorder.addVideoStream(w, h);
				}

				if (audio_status != PLAYING_STATUS.STOPPED) {
					mAVIRecorder.addAudioStream();
				}

				if (talk_status != PLAYING_STATUS.STOPPED) {
					mAVIRecorder.addTalkStream();
				}

				try {
					mAVIRecorder.startAVI();
				} catch (Exception e) {
					
					e.printStackTrace();
				}
			}
		});

		return CAMERA_ERROR.OK;
	}

	/**
	 * 结束录像
	 */
	public void stop_record() {
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (mAVIRecorder != null)
					try {
						mAVIRecorder.finishAVI();
					} catch (Exception e) {
						
						e.printStackTrace();
					}
				mAVIRecorder = null;
			}
		});
	}

	/**
	 * 开启麦克风讲话
	 * 
	 * @return
	 */
	public CAMERA_ERROR start_talk() {
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;

		if (talk_status == PLAYING_STATUS.PLAYING)
			return CAMERA_ERROR.OK;

		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;

				if (talk_status == PLAYING_STATUS.PLAYING)
					return;
				add_op_talk_start_req();
				talk_status = PLAYING_STATUS.REQUESTING;
				NSDictionary params = new NSDictionary();
				params.put("status", talk_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_TalkStatusChanged_Notification, MjpegCamera.this,
						params);
			}
		});

		return CAMERA_ERROR.OK;
	}

	/**
	 * 停止麦克风
	 */
	public void stop_talk() {
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return;
		if (talk_status == PLAYING_STATUS.STOPPED)
			return;

		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				if (talk_status == PLAYING_STATUS.STOPPED)
					return;
				add_op_talk_end();
				error = CAMERA_ERROR.OK;
				talk_status = PLAYING_STATUS.STOPPED;
				stopMicrophone();
				NSDictionary params = new NSDictionary();
				params.put("status", talk_status);
				NSNotificationCenter.defaultCenter().postNotification(
						IPCamera_TalkStatusChanged_Notification, MjpegCamera.this,
						params);
				// if ((video_status == PLAYING_STATUS.STOPPED)
				// && (audio_status == PLAYING_STATUS.STOPPED)
				// && (talk_status == PLAYING_STATUS.STOPPED))
				// disconnect_av_stream();
			}
		});

	}

	/**
	 * 开启客户端麦克风，传输音频至摄像头
	 */
	protected void startMicrophone() {
		if (iscapturing)
			return;
		iscapturing = true;

		talkThread = new Thread() {
			public void run() {
				int frequency = 8000;
				int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_MONO;
				int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
				/*File file=new File(Environment.getExternalStorageDirectory(), "talkbuffer");
				OutputStream out = null;
				try {
					out = new FileOutputStream(file);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				try {
					// Create a new AudioRecord object to record the audio.
					int bufferSize = AudioRecord.getMinBufferSize(frequency,
							channelConfiguration, audioEncoding) * 2;
					AudioRecord audioRecord = new AudioRecord(
							MediaRecorder.AudioSource.MIC, frequency,
							channelConfiguration, audioEncoding, bufferSize);

					byte[] buffer = new byte[MjpegCamera.AUDIO_BUFFER_SIZE];
					audioRecord.startRecording();

					Log.i("AudioTrack", "Begin Recording");
					while (iscapturing) {
						int bufferReadResult = audioRecord.read(buffer, 0,
								MjpegCamera.AUDIO_BUFFER_SIZE);
						talk(buffer, bufferReadResult);												
						//out.write(buffer);
						

					}
					//out.close();
					audioRecord.stop();
					Log.e("AudioTrack", "Stop Recording");

				} catch (Throwable t) {
					Log.e("AudioRecord", "Recording Failed");
				}
			}
		};

		talkThread.start();
	}

	/**
	 * 关闭客户端麦克风
	 */
	protected void stopMicrophone() {
		iscapturing = false;
		talkThread = null;
	}

	/**
	 * 麦克风音频传输
	 * 
	 * @param data
	 * @param datalen
	 */
	public void talk(final byte[] data, int datalen) {
		System.out.println("talk " + datalen);
		final byte[] chunk = new byte[4 + 4 + 4 + 1 + 4
				+ AUDIO_PACKET_PAYLOAD_SIZE];

		if (talk_status != PLAYING_STATUS.PLAYING)
			return;
		if (datalen != AUDIO_BUFFER_SIZE) {
			Log.e("talk", "error length");
		}
		talk_tick = (int) System.currentTimeMillis();

		int t = time(null);
		LibcMisc.memcpy(chunk, talk_tick, 4);
		LibcMisc.memcpy(chunk, 4, talk_seq, 4);
		talk_seq++;
		LibcMisc.memcpy(chunk, 8, t, 4);
		chunk[12] = 0;
		int length = AUDIO_PACKET_PAYLOAD_SIZE;
		LibcMisc.memcpy(chunk, 13, length, 4);
		final int sample = adpcm_encode_sample.getValue();
		final int index = adpcm_encode_index.getValue();
		adpcm_encode(data, datalen, chunk, 17, adpcm_encode_sample,
				adpcm_encode_index);
		//Log.i("Mjcamera--adpcm_encode(data==", Arrays.toString(data));
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				// @TODO:
				if (talk_status != PLAYING_STATUS.PLAYING)
					return;
				add_av_packet(AV_TALK_DATA, chunk, 4 + 4 + 4 + 1 + 4
						+ AUDIO_PACKET_PAYLOAD_SIZE);
				if (mAVIRecorder != null)
					try {
						byte[] encoded = chunk;
						//Log.i("Mjcamera--encoded = chunk==", Arrays.toString(chunk));
						encoded[HEADER_LENGTH + 13] = (byte) (sample & 0xFF);
						encoded[HEADER_LENGTH + 14] = (byte) ((sample & 0xFF00) >> 8);
						encoded[HEADER_LENGTH + 15] = (byte) (index & 0xFF);
						encoded[HEADER_LENGTH + 16] = 0;
						
						/*mAVIRecorder.addTalk(data, 0,
								data.length);*/
						//LibcMisc.get_short_array(data, 0);
						
						mAVIRecorder.addTalk(encoded, 13,
								AUDIO_PACKET_PAYLOAD_SIZE + 4);
						
						//Log.i("Mjcamera--mAVIRecorder.addTalk(encoded==", Arrays.toString(encoded));
					} catch (Exception e) {
						
						e.printStackTrace();
					}
			}
		});

	}

	/**
	 * PTZ控制
	 * 
	 * @param i
	 * @return
	 */
	public CAMERA_ERROR ptz_control(final int i) {
		final byte ptz_command[] = { 0, 2, 4, 6, 90, 91, 92, 93, 25, 1, 3, 5,
				7, 28, 29, 26, 27, 16, 18, 94, 95, 30, 31, 32, 33, 34, 35, 36,
				37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52,
				53, 54, 55, 56, 57, 58, 59, 60, 61, };
		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;
		// if (command. >= ptz_command.length)
		// return CAMERA_ERROR.BAD_PARAMS;
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {

			public void run() {
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				byte[] req = { ptz_command[i] };
				add_op_decoder_control_req(req);
			}
		});

		return CAMERA_ERROR.OK;
	}

	/**
	 * 视频参数设置
	 * 
	 * @param cmd
	 * @param val
	 * @return
	 */
	public CAMERA_ERROR set_vedio_params(final int cmd, final int val) {

		if (camera_status != CAMERA_STATUS.CONNECTED)
			return CAMERA_ERROR.BAD_STATUS;
		CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {
			public void run() {
				
				if (camera_status != CAMERA_STATUS.CONNECTED)
					return;
				add_op_params_set_req((byte) cmd, (byte) val);
			}
		});
		return CAMERA_ERROR.OK;
	}

	/**
	 * 获取分辨率
	 * 
	 * @return
	 */
	public int get_resolution() {
		return resolution;
	}

	/**
	 * 设置分辨率
	 * 
	 * @param value
	 * @return
	 */
	public CAMERA_ERROR set_resolution(final int value) {
		set_vedio_params(0, value);
		return CAMERA_ERROR.OK;
	}

	public int get_brightness() {
		return brightness;
	}

	public CAMERA_ERROR set_brightness(int value) {
		set_vedio_params(1, value);
		return CAMERA_ERROR.OK;
	}

	public int get_contrast() {
		return contrast;
	}

	public CAMERA_ERROR set_contrast(int value) {
		set_vedio_params(2, value);
		return CAMERA_ERROR.OK;
		// if (camera_status != CAMERA_STATUS.CONNECTED)
		// return CAMERA_ERROR.BAD_STATUS;
		// add_op_params_set_req((byte)2, (byte)value);
		// return CAMERA_ERROR.OK;
	}

	public int get_mode() {
		return mode;
	}

	public CAMERA_ERROR set_mode(int value) {
		set_vedio_params(3, value);
		return CAMERA_ERROR.OK;
		// if (camera_status != CAMERA_STATUS.CONNECTED)
		// return CAMERA_ERROR.BAD_STATUS;
		// add_op_params_set_req((byte)3, (byte)value);
		// return CAMERA_ERROR.OK;
	}

	public int get_flip() {
		return flip;
	}

	public CAMERA_ERROR set_flip(final int value) {
		set_vedio_params(5, value);
		// if (camera_status != CAMERA_STATUS.CONNECTED)
		// return CAMERA_ERROR.BAD_STATUS;
		// CFRunLoop.CFRunLoopGetCurrent().post(new Runnable() {
		//
		// public void run() {
		// 
		// if (camera_status != CAMERA_STATUS.CONNECTED)
		// return;
		// add_op_params_set_req((byte)5, (byte)value);
		// }
		// });

		return CAMERA_ERROR.OK;
	}

	public float get_volume() {
		return volume;
	}

	public void set_volume(float value) {
		volume = value;
		if (audio_status == PLAYING_STATUS.PLAYING) {
			// @TODO:
			// AudioQueueSetParameter(audio_play_queue, kAudioQueueParam_Volume,
			// volume);
		}
		if (talk_status == PLAYING_STATUS.PLAYING) {
			// @TODO:
			// AudioQueueSetParameter(audio_record_queue,
			// kAudioQueueParam_Volume, volume);
		}
	}

	/**
	 * 视频流操作回调方法
	 * 
	 * @param eventType
	 */
	protected void op_stream_callback(int eventType) {
		if (camera_status == CAMERA_STATUS.DISCONNECTED)
			return;

		int ret;
		byte[] buffer = new byte[1024];

		switch (eventType) {
		case CFStream.kCFStreamEventOpenCompleted:
			if (camera_status != CAMERA_STATUS.LOGINING) {
				camera_status = CAMERA_STATUS.LOGINING;
				NSDictionary params = new NSDictionary("status", camera_status);
				NSNotificationCenter.defaultCenter()
						.postNotification(
								IPCamera_CameraStatusChanged_Notification,
								this, params);
				op_t = time(null);
				add_op_login_req();
			}
			break;
		case CFStream.kCFStreamEventHasBytesAvailable:
			if (0 < (ret = CFStream.CFReadStreamRead(op_r_stream, buffer, 1024))) {
				op_r_data.appendBytes(buffer, ret);
				parse_op_packet();
			} else {
				error = CAMERA_ERROR.PEER_CLOSED;
				Log.e(this.getClass().getSimpleName(), "ipcamera op_stream_callback PEER_CLOSED");
				on_op_stream_disconnected();
			}

			break;
		case CFStream.kCFStreamEventCanAcceptBytes:
			if (op_w_data.length() > 0) {
				if (0 < (ret = CFStream.CFWriteStreamWrite(op_w_stream,
						op_w_data.mutableBytes(), op_w_data.length()))) {
					op_w_t = time(null);
					op_w_data.replaceBytesInRange(NSRange.MakeNsRange(0, ret),
							null, 0);
				} else if (ret < 0) {
					Log.e(this.getClass().getSimpleName(), "ipcamera op_stream_callback PEER_CLOSED");
					error = CAMERA_ERROR.PEER_CLOSED;
					on_op_stream_disconnected();
				}
			}

			break;
		case CFStream.kCFStreamEventErrorOccurred:
			if (camera_status == CAMERA_STATUS.CONNECTING){
				Log.e(this.getClass().getSimpleName(), "ipcamera op_stream_callback can not connect");
				error = CAMERA_ERROR.CANT_CONNECT;
			}
			else{
				Log.e(this.getClass().getSimpleName(), "ipcamera op_stream_callback SOCKET_ERROR");
				error = CAMERA_ERROR.SOCKET_ERROR;
			}
				
			on_op_stream_disconnected();
			break;
		case CFStream.kCFStreamEventEndEncountered:
			Log.e(this.getClass().getSimpleName(), "ipcamera op_stream_callback PEER_CLOSED");
			error = CAMERA_ERROR.PEER_CLOSED;
			on_op_stream_disconnected();
			break;
		}
	}

	/**
	 * 音视频流回调方法
	 * 
	 * @param eventType
	 */
	protected void av_stream_callback(int eventType) {
		if ((video_status != PLAYING_STATUS.PLAYING)
				&& (audio_status != PLAYING_STATUS.PLAYING)
				&& (talk_status != PLAYING_STATUS.PLAYING))
			return;

		int ret;
		byte[] buffer = new byte[1024];

		ByteArrayInputStream buf = new ByteArrayInputStream(linkid);
		switch (eventType) {
		case CFStream.kCFStreamEventOpenCompleted:
			try {
				Log.i(this.getClass().getSimpleName(), "send AV_LOGIN_REQ " + getHost() + " " + getIdentity());
				add_av_packet(AV_LOGIN_REQ, linkid, 4);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case CFStream.kCFStreamEventHasBytesAvailable:
			try {
				if (0 < (ret = CFStream.CFReadStreamRead(av_r_stream,
						av_buffer.getData(), av_buffer.getWriteOffset(),
						av_buffer.getWriteLength()))) {
					av_buffer.refreshWritenLength(ret);
					if (av_buffer.getWriteLength() == 0) {
						parse_av_packet();
					}
				}
				else {
					Log.e(this.getClass().getSimpleName(), "ipcamera av_stream_callback PEER_CLOSED");
					error = CAMERA_ERROR.PEER_CLOSED;
//					disconnect_av_stream();
					stop();
					start();
				}
			} catch (NullPointerException e) {
				stop();
				start();
			}
			

			break;
		case CFStream.kCFStreamEventCanAcceptBytes:
			if (av_w_data.length() > 0) {
				if (0 < (ret = CFStream.CFWriteStreamWrite(av_w_stream,
						av_w_data.mutableBytes(), av_w_data.length()))) {
					av_w_data.replaceBytesInRange(NSRange.MakeNsRange(0, ret),
							null, 0);
				}
			}

			break;
		case CFStream.kCFStreamEventErrorOccurred:
			Log.e(this.getClass().getSimpleName(), "ipcamera av_stream_callback SOCKET_ERROR");
			error = CAMERA_ERROR.SOCKET_ERROR;
			disconnect_av_stream();
			break;
		case CFStream.kCFStreamEventEndEncountered:
			Log.e(this.getClass().getSimpleName(), "ipcamera av_stream_callback PEER_CLOSED");
			error = CAMERA_ERROR.PEER_CLOSED;
			disconnect_av_stream();
			break;
		}
	}

	public static int time(Object o) {
		return (int) System.currentTimeMillis() / 1000;
	}

	public static int times(Object o) {
		return (int) System.currentTimeMillis() / 10;
	}

	
	protected void handle_timer() {
		long t = time(null);
//		System.out.println("handle_timer " + video_status.toString() + " "
//				+ camera_status.toString() + " "
//				+ Integer.toString((int) (t - op_t)));
		if (camera_status == CAMERA_STATUS.DISCONNECTED) {

			if (socketAddress == null) {
				host = LANHost;
				port = LANPort;

				camera_status = CAMERA_STATUS.CONNECTING;
				NSDictionary params = new NSDictionary("status", camera_status);
				NSNotificationCenter.defaultCenter()
						.postNotification(
								IPCamera_CameraStatusChanged_Notification,
								this, params);
				System.out.println("LANHost " + LANHost);
				if (LANHost.indexOf(WAN_HOST_1) >= 0
						|| LANHost.indexOf(WAN_HOST_2) >= 0 || LANHost.indexOf(WAN_HOST_3) >= 0) {
					if (LANHost.indexOf("http://") < 0) {
						LANHost = "http://" + LANHost;
					}
					ddns = LANHost;
					System.out.println("connect ddns " + ddns);
					connectDDNS(ddns);
				} else {
					init_op_stream();
				}

			} else if (t - op_t >= connect_time_s) {
				connectLanOrWan();
			}
		} else if ((camera_status == CAMERA_STATUS.LOGINING)
				|| (camera_status == CAMERA_STATUS.VERIFYING)) {
			if (t - op_t > 20) {
				Log.e(this.getClass().getSimpleName(), "ipcamera login verifying timeout " + this.getHost());
				error = CAMERA_ERROR.TIMEOUT;
				on_op_stream_disconnected();
			}
		} else if (camera_status == CAMERA_STATUS.CONNECTED) {
			if (t - op_r_t > 2 * 60) {
				Log.e(this.getClass().getSimpleName(), "ipcamera connect timeout");
				error = CAMERA_ERROR.TIMEOUT;
				on_op_stream_disconnected();
			} else if (t - op_w_t > 1 * 60) {
				add_op_keep_alive();
			}
		} else if (camera_status == CAMERA_STATUS.CONNECTING) {
			if (t - op_t >= connect_time_s) {
				connectLanOrWan();
			}
		}
	}

	private void connectLanOrWan() {
		camera_status = CAMERA_STATUS.CONNECTING;
		NSDictionary params = new NSDictionary("status", camera_status);
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_CameraStatusChanged_Notification, this, params);

		if ((isConnectLAN || ddns.length() == 0)
				&& (LANHost.indexOf(WAN_HOST_1) < 0 && LANHost
						.indexOf(WAN_HOST_2) < 0&& LANHost
						.indexOf(WAN_HOST_3) < 0)){
			System.out.println("connect LAN " + LANHost);

			host = LANHost;
			port = LANPort;

			error = CAMERA_ERROR.TIMEOUT;
			init_op_stream();
		} else {
			System.out.println("connect WAN " + ddns);
			if (LANHost.indexOf(WAN_HOST_1) >= 0 || LANHost
						.indexOf(WAN_HOST_2) >= 0|| LANHost
						.indexOf(WAN_HOST_3) >= 0) {
				connectDDNS(ddns);
			}else {
				host = ddns.replace("http://", "");
				port = LANPort;
				
				error = CAMERA_ERROR.TIMEOUT;
				init_op_stream();
			}
		}

		isConnectLAN = !isConnectLAN;
		if (isConnectLAN) {
	        connect_time_s = LAN_CONNECT_TIME_S;
	    }else{
	        connect_time_s = WAN_CONNECT_TIME_S;
	    }
		op_t = time(null);
	}

	private void connectDDNS(final String ddnsString) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		HttpContext context = new BasicHttpContext();

		try {
			System.out.println("connectDDNS " + ddnsString);
			HttpGet httpget = new HttpGet(ddnsString);
			response = httpClient.execute(httpget, context);
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK)
				try {
					throw new IOException(response.getStatusLine().toString());
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			HttpUriRequest currentReq = (HttpUriRequest) context
					.getAttribute(ExecutionContext.HTTP_REQUEST);
			HttpHost currentHost = (HttpHost) context
					.getAttribute(ExecutionContext.HTTP_TARGET_HOST);
			System.out.println("getDDNS host " + currentHost.getHostName()
					+ ":" + currentHost.getPort());

			host = currentHost.getHostName();
			port = Integer.toString(currentHost.getPort());

			init_op_stream();

		} catch (ClientProtocolException e) {
			System.out.println("no network conncet.");
		} catch (IOException e) {
			System.out.println("no network conncet.");
		} finally {

		}
	}

	/**
	 * 视频流操作初始化
	 */
	protected void init_op_stream() {
		Log.i(this.getClass().getSimpleName(), "ipcamera init_op_stream " + host + " " + port);
		
		op_r_stream = new CFReadStream();
		op_w_stream = new CFWriteStream();

		try {
			socketAddress = new InetSocketAddress(host, Integer.parseInt(port));
			
			
			CFStream.CFStreamCreatePairWithSocketToHost(null, socketAddress,
					op_r_stream, op_w_stream);

			CFStream.CFReadStreamSetClient(op_r_stream,
					CFStream.kCFStreamEventOpenCompleted
							| CFStream.kCFStreamEventHasBytesAvailable
							| CFStream.kCFStreamEventErrorOccurred
							| CFStream.kCFStreamEventEndEncountered, this, null);
			CFStream.CFReadStreamScheduleWithRunLoop(op_r_stream,
					CFRunLoop.CFRunLoopGetCurrent(),
					CFRunLoop.kCFRunLoopDefaultMode);
			CFStream.CFWriteStreamSetClient(op_w_stream,
					CFStream.kCFStreamEventOpenCompleted
							| CFStream.kCFStreamEventCanAcceptBytes
							| CFStream.kCFStreamEventErrorOccurred
							| CFStream.kCFStreamEventEndEncountered, this, null);
			CFStream.CFWriteStreamScheduleWithRunLoop(op_w_stream,
					CFRunLoop.CFRunLoopGetCurrent(),
					CFRunLoop.kCFRunLoopDefaultMode);
			CFStream.CFReadStreamOpen(op_r_stream);
			CFStream.CFWriteStreamOpen(op_w_stream);
			op_r_data = new NSMutableData();
			op_w_data = new NSMutableData();
		} catch (UnresolvedAddressException e) {
			System.out.println("ip地址不正确");
			
		}catch (Exception e) {
			
		}finally{
			
		}
	}

	/**
	 * 视频流初始化
	 */
	protected void init_av_stream() {
		try {
			av_buffer = new AvDataBuffer(23);
			av_state = AvRcvState.AV_RCVSTATE_HDR;
			av_r_stream = new CFReadStream();
			av_w_stream = new CFWriteStream();
			
			socketAddress = new InetSocketAddress(host, Integer.parseInt(port));
			CFStream.CFStreamCreatePairWithSocketToHost(null, socketAddress,
					av_r_stream, av_w_stream);

			CFStream.CFReadStreamSetClient(av_r_stream,
					CFStream.kCFStreamEventOpenCompleted
							| CFStream.kCFStreamEventHasBytesAvailable
							| CFStream.kCFStreamEventErrorOccurred
							| CFStream.kCFStreamEventEndEncountered, this, null);
			CFStream.CFReadStreamScheduleWithRunLoop(av_r_stream,
					CFRunLoop.CFRunLoopGetCurrent(),
					CFRunLoop.kCFRunLoopDefaultMode);
			CFStream.CFWriteStreamSetClient(av_w_stream,
					CFStream.kCFStreamEventOpenCompleted
							| CFStream.kCFStreamEventCanAcceptBytes
							| CFStream.kCFStreamEventErrorOccurred
							| CFStream.kCFStreamEventEndEncountered, this, null);
			CFStream.CFWriteStreamScheduleWithRunLoop(av_w_stream,
					CFRunLoop.CFRunLoopGetCurrent(),
					CFRunLoop.kCFRunLoopDefaultMode);
			CFStream.CFReadStreamOpen(av_r_stream);
			CFStream.CFWriteStreamOpen(av_w_stream);
			av_r_data = new NSMutableData();
			av_w_data = new NSMutableData();
		} catch (UnresolvedAddressException e) {
			System.out.println("ip地址不正确");
		}catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 视频流操作关闭
	 */
	protected void on_op_stream_disconnected() {
		disconnect_av_stream();
		disconnect_op_stream();
	}

	protected void disconnect_op_stream() {
		Log.i(this.getClass().getSimpleName(), "ipcamera disconnect_op_stream");
		if (op_r_stream != null) {
			CFStream.CFReadStreamClose(op_r_stream);
			CFStream.CFRelease(op_r_stream);
			op_r_stream = null;
			
		}
		if (op_r_data != null) {
			op_r_data.release();
			op_r_data = null;
		}
		if (op_w_stream != null) {
			CFStream.CFWriteStreamClose(op_w_stream);
			CFStream.CFRelease(op_w_stream);
			op_w_stream = null;
		}
		if (op_w_data != null) {
			op_w_data.release();
			op_w_data = null;
		}

		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();

		if (alarm_status != ALARM_STATUS.NONE) {
			alarm_status = ALARM_STATUS.NONE;
			NSDictionary params = new NSDictionary("value",
					alarm_status.toString());
			nc.postNotification(IPCamera_AlarmStatusChanged_Notification,
					MjpegCamera.this, params);
		}
		if (camera_status != CAMERA_STATUS.DISCONNECTED) {
			op_t = time(null);
			camera_status = CAMERA_STATUS.DISCONNECTED;
			NSDictionary params = new NSDictionary("status", camera_status);
			nc.postNotification(IPCamera_CameraStatusChanged_Notification,
					MjpegCamera.this, params);
		}
	}

	/**
	 * 关闭所有的流
	 */
	protected void disconnect_all_stream() {
		Log.e("All stream disconnect", "disconnect");
		disconnect_av_stream();
		disconnect_op_stream();
		// start();
	}

	protected void disconnect_av_stream() {
		if (av_r_stream != null) {
			CFStream.CFReadStreamClose(av_r_stream);
			CFStream.CFRelease(av_r_stream);
			av_r_stream = null;
		}
		if (av_r_data != null) {
			av_r_data.release();
			av_r_data = null;
		}
		if (av_w_stream != null) {
			CFStream.CFWriteStreamClose(av_w_stream);
			CFStream.CFRelease(av_w_stream);
			av_w_stream = null;
		}
		if (av_w_data != null) {
			av_w_data.release();
			av_w_data = null;
		}
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();

		if (video_status != PLAYING_STATUS.STOPPED) {
			video_status = PLAYING_STATUS.STOPPED;
			NSDictionary params = new NSDictionary();
			params.put("status", video_status);
			nc.postNotification(IPCamera_VideoStatusChanged_Notification, this,
					params);
		}
		if (audio_status != PLAYING_STATUS.STOPPED) {
			audio_status = PLAYING_STATUS.STOPPED;
			stop_audio_play();
			NSDictionary params = new NSDictionary();
			params.put("status", audio_status);
			nc.postNotification(IPCamera_AudioStatusChanged_Notification,
					MjpegCamera.this, params);
		}
		if (talk_status != PLAYING_STATUS.STOPPED) {
			talk_status = PLAYING_STATUS.STOPPED;
			// stop_audio_record();
			NSDictionary params = new NSDictionary();
			params.put("status", talk_status);
			nc.postNotification(IPCamera_TalkStatusChanged_Notification,
					MjpegCamera.this, params);
		}
	}

	protected void parse_op_packet() {
		int i;
		short command;
		int content_length;

		while (true) {

			if (op_r_data.length() == 0)
				return;

			for (i = 0; i < op_r_data.length() - 3; i++) {
				if (0 == LibcMisc.memcmp(op_r_data.mutableBytes(), i, MO_O, 0,
						4))
					break;
			}

			if (i > 0)
				op_r_data.replaceBytesInRange(NSRange.MakeNsRange(0, i), null,
						0);

			if (op_r_data.length() < HEADER_LENGTH)
				return;

			command = LibcMisc.get_short(op_r_data.mutableBytes(), 4);
			content_length = LibcMisc.get_int(op_r_data.mutableBytes(), 15);

			if (op_r_data.length() < HEADER_LENGTH + content_length)
				return;

			op_r_t = time(null);

			switch (command) {
			case OP_LOGIN_RESP:
				parse_op_login_resp(content_length);
				break;
			case OP_VERIFY_RESP:
				parse_op_verify_resp(content_length);
				break;
			case OP_ALARM_NOTIOFY:
				parse_op_alarm_notify(content_length);
				break;
			case OP_VIDEO_START_RESP:
				parse_op_video_start_resp(content_length);
				break;
			case OP_AUDIO_START_RESP:
				parse_op_audio_start_resp(content_length);
				break;
			case OP_TALK_START_RESP:
				parse_op_talk_start_resp(content_length);
				break;
			case OP_PARAMS_FETCH_RESP:
				parse_op_params_fetch_resp(content_length);
				break;
			case OP_PARAMS_CHANGED_NOTIFY:
				parse_op_params_changed_notify(content_length);
				break;
			}

			if (camera_status != CAMERA_STATUS.DISCONNECTED) {
				op_r_data.replaceBytesInRange(
						NSRange.MakeNsRange(0, HEADER_LENGTH + content_length),
						null, 0);
			} else {
				break;
			}
		}
	}

	/**
	 * 用户登录操作响应
	 * 
	 * @param content_length
	 */
	protected void parse_op_login_resp(int content_length) {
		short result;
		if (content_length != 27) {
			Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_login_resp UNKNOWN_ERROR");
			error = CAMERA_ERROR.UNKNOWN_ERROR;
			on_op_stream_disconnected();
			return;
		}

		result = LibcMisc.get_short(op_r_data.mutableBytes(), HEADER_LENGTH);
		if (result == 0) {
			String str = new String(op_r_data.mutableBytes(),
					HEADER_LENGTH + 2, op_r_data.length() - (HEADER_LENGTH + 2));
			if (str.indexOf(0) != -1)
				str = str.substring(0, str.indexOf(0));
			if (uid.equals("")) {
				uid = str;
			} else {
//				String tempIdentity = identity.substring(2, identity.length() );
//				if (tempIdentity.equals(str) != true) {
//					Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_login_resp id error " 
//							+ "source id " + tempIdentity + " new id " + str);
//					
//					error = CAMERA_ERROR.BAD_ID;
//					on_op_stream_disconnected();
//					return;
//				}
			}

			camera_status = CAMERA_STATUS.VERIFYING;
			add_op_verify_req();
			NSDictionary params = new NSDictionary("status", camera_status);
			NSNotificationCenter.defaultCenter().postNotification(
					IPCamera_CameraStatusChanged_Notification, this, params);
		} else if (result == 2) {
			Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_login_resp MAX_SESSION");
			error = CAMERA_ERROR.MAX_SESSION;
			on_op_stream_disconnected();
			return;
		} else {
			Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_login_resp UNKNOWN_ERROR");
			error = CAMERA_ERROR.UNKNOWN_ERROR;
			on_op_stream_disconnected();
			return;
		}
	}

	/**
	 * 用户登录验证操作响应
	 * 
	 * @param content_length
	 */
	protected void parse_op_verify_resp(int content_length) {
		Log.i(this.getClass().getSimpleName(), "parse_op_verify_resp");
		short result;
		if (content_length < 2) {
			Log.e(this.getClass().getSimpleName(), "ipcamera op_verify_req UNKNOWN_ERROR");
			error = CAMERA_ERROR.UNKNOWN_ERROR;
			on_op_stream_disconnected();
			return;
		}
		result = LibcMisc.get_short(op_r_data.mutableBytes(), HEADER_LENGTH);
		if (result == 0) {
			Log.i(this.getClass().getSimpleName(), "ipcamera CONNECTED");
			camera_status = CAMERA_STATUS.CONNECTED;
			add_op_params_fetch_req();
			NSDictionary params = new NSDictionary("status", camera_status);
			NSNotificationCenter.defaultCenter().postNotification(
					IPCamera_CameraStatusChanged_Notification, this, params);
		} else {
			Log.e(this.getClass().getSimpleName(), "ipcamera op_verify_req user or passwor error");
			camera_status = CAMERA_STATUS.WRONG_PASSWORD;
			NSDictionary params = new NSDictionary("status", camera_status);
			NSNotificationCenter.defaultCenter().postNotification(
					IPCamera_CameraStatusChanged_Notification, this, params);
			
			error = CAMERA_ERROR.BAD_AUTH;
			on_op_stream_disconnected();
			return;
		}
	}

	/**
	 * 摄像头报警通知
	 * 
	 * @param content_length
	 */
	protected void parse_op_alarm_notify(int content_length) {
		if (content_length != 9)
			return;
		byte alarm = op_r_data.mutableBytes()[HEADER_LENGTH];
		if (alarm == 0)
			alarm_status = ALARM_STATUS.NONE;
		else if (alarm == 1)
			alarm_status = ALARM_STATUS.MOTION_DETECTING;
		else if (alarm == 2)
			alarm_status = ALARM_STATUS.TRIGGER_DETECTING;
		else if (alarm == 3)
			alarm_status = ALARM_STATUS.SOUND_DETECTING;
		else
			alarm_status = ALARM_STATUS.UNKNOWN_ALARM;

//		Log.e("CAMERA ALARM STATUS", alarm_status.toString());

		NSDictionary params = new NSDictionary("value", alarm_status.toString());
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_AlarmStatusChanged_Notification, this, params);
	}

	/**
	 * 开始视频流操作响应
	 * 
	 * @param content_length
	 */
	protected void parse_op_video_start_resp(int content_length) {
		if (video_status != PLAYING_STATUS.REQUESTING)
			return;

		while (true) {
			if (content_length < 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera op_video_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				break;
			}

			short result;
			result = LibcMisc
					.get_short(op_r_data.mutableBytes(), HEADER_LENGTH);
			if (result == 0) {
				if (content_length == 2) {
					System.out.println("video_status = PLAYING_STATUS.PLAYING");
					video_status = PLAYING_STATUS.PLAYING;
					NSDictionary params = new NSDictionary();
					params.put("status", video_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_VideoStatusChanged_Notification, this,
							params);
					return;
				} else if (content_length == 6) {
					linkid = new NSData(op_r_data.mutableBytes(),
							HEADER_LENGTH + 2, 4).bytes();
					init_av_stream();
					Log.i(this.getClass().getSimpleName(), "parse_op_video_start_resp");
					video_status = PLAYING_STATUS.PLAYING;
					NSDictionary params = new NSDictionary();
					params.put("status", video_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_VideoStatusChanged_Notification, this,
							params);
					return;
				} else {
					Log.e(this.getClass().getSimpleName(), "ipcamera op_video_start_resp UNKNOWN_ERROR");
					error = CAMERA_ERROR.UNKNOWN_ERROR;
					break;
				}
			} else if (result == 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera op_video_start_resp MAX_SESSION");
				error = CAMERA_ERROR.MAX_SESSION;
				// goto fail;
				break;
			} else if (result == 8) {
				Log.e(this.getClass().getSimpleName(), "ipcamera op_video_start_resp FORBIDDEN");
				error = CAMERA_ERROR.FORBIDDEN;
				// goto fail;
				break;
			} else {
				Log.e(this.getClass().getSimpleName(), "ipcamera op_video_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				// goto fail;
				break;
			}
		}

		video_status = PLAYING_STATUS.STOPPED;
		NSDictionary params = new NSDictionary();
		params.put("status", video_status);
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_VideoStatusChanged_Notification, this, params);
		return;
	}

	/**
	 * 开始音频流操作响应
	 * 
	 * @param content_length
	 */
	protected void parse_op_audio_start_resp(int content_length) {
		if (audio_status != PLAYING_STATUS.REQUESTING)
			return;

		while (true) {
			if (content_length < 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				break;
			}

			short result;
			result = LibcMisc
					.get_short(op_r_data.mutableBytes(), HEADER_LENGTH);
			if (result == 0) {
				adpcm_decode_sample.setValue(0);
				adpcm_decode_index.setValue(0);
				if (content_length == 2) {
					audio_status = PLAYING_STATUS.PLAYING;
					start_audio_play();
					NSDictionary params = new NSDictionary();
					params.put("status", audio_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_AudioStatusChanged_Notification,
							MjpegCamera.this, params);
					return;
				} else if (content_length == 6) {
					linkid = new NSData(op_r_data.mutableBytes(),
							HEADER_LENGTH + 2, 4).bytes();
					init_av_stream();
					audio_status = PLAYING_STATUS.PLAYING;
					start_audio_play();
					NSDictionary params = new NSDictionary();
					params.put("status", audio_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_AudioStatusChanged_Notification,
							MjpegCamera.this, params);
					return;
				} else {
					Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp UNKNOWN_ERROR");
					error = CAMERA_ERROR.UNKNOWN_ERROR;
					break;
				}
			} else if (result == 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp MAX_SESSION");
				error = CAMERA_ERROR.MAX_SESSION;
				break;
			} else if (result == 7) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp UNSUPPORT");
				error = CAMERA_ERROR.UNSUPPORT;
				break;
			} else if (result == 8) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp FORBIDDEN");
				error = CAMERA_ERROR.FORBIDDEN;
				break;
			} else {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_audio_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				break;
			}
		}

		audio_status = PLAYING_STATUS.STOPPED;
		NSDictionary params = new NSDictionary();
		params.put("status", audio_status);
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_AudioStatusChanged_Notification, this, params);
		return;
	}

	/**
	 * 开启客户端麦克风音频传输响应
	 * 
	 * @param content_length
	 */
	protected void parse_op_talk_start_resp(int content_length) {
		if (talk_status != PLAYING_STATUS.REQUESTING)
			return;

		while (true) {
			if (content_length < 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				break;
			}

			short result;
			result = LibcMisc
					.get_short(op_r_data.mutableBytes(), HEADER_LENGTH);
			if (result == 0) {
				talk_tick = talk_seq = 0;
				adpcm_encode_sample.setValue(0);
				adpcm_encode_index.setValue(0);
				if (content_length == 2) {
					talk_status = PLAYING_STATUS.PLAYING;
					startMicrophone();
					NSDictionary params = new NSDictionary();
					params.put("status", talk_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_TalkStatusChanged_Notification, this,
							params);
					return;
				} else if (content_length == 6) {
					linkid = new NSData(op_r_data.mutableBytes(),
							HEADER_LENGTH + 2, 4).bytes();
					init_av_stream();
					talk_status = PLAYING_STATUS.PLAYING;
					startMicrophone();
					NSDictionary params = new NSDictionary();
					params.put("status", talk_status);
					NSNotificationCenter.defaultCenter().postNotification(
							IPCamera_TalkStatusChanged_Notification, this,
							params);
					return;
				} else {
					Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp UNKNOWN_ERROR");
					error = CAMERA_ERROR.UNKNOWN_ERROR;
					break;
				}
			} else if (result == 2) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp MAX_SESSION");
				error = CAMERA_ERROR.MAX_SESSION;
				break;
			} else if (result == 7) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp UNSUPPORT");
				error = CAMERA_ERROR.UNSUPPORT;
				break;
			} else if (result == 8) {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp FORBIDDEN");
				error = CAMERA_ERROR.FORBIDDEN;
				break;
			} else {
				Log.e(this.getClass().getSimpleName(), "ipcamera parse_op_talk_start_resp UNKNOWN_ERROR");
				error = CAMERA_ERROR.UNKNOWN_ERROR;
				break;
			}
		}

		talk_status = PLAYING_STATUS.STOPPED;
		NSDictionary params = new NSDictionary();
		params.put("status", talk_status);
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_TalkStatusChanged_Notification, this, params);
		return;
	}

	protected void parse_op_params_fetch_resp(int content_length) {
		if (content_length < 6)
			return;
		resolution = op_r_data.mutableBytes()[HEADER_LENGTH];
		brightness = op_r_data.mutableBytes()[HEADER_LENGTH + 1];
		if (brightness < 0) {
			brightness += 255;
		}
		contrast = op_r_data.mutableBytes()[HEADER_LENGTH + 2];
		mode = op_r_data.mutableBytes()[HEADER_LENGTH + 3];
		flip = op_r_data.mutableBytes()[HEADER_LENGTH + 5];
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_CameraParamChanged_Notification, this);
		return;
	}

	protected void parse_op_params_changed_notify(int content_length) {
		if (content_length != 2)
			return;

		byte command = op_r_data.mutableBytes()[HEADER_LENGTH];
		byte value = op_r_data.mutableBytes()[HEADER_LENGTH + 1];

		switch (command) {
		case 0:
			resolution = value;
			break;
		case 1:
			brightness = value;
			break;
		case 2:
			contrast = value;
			break;
		case 3:
			mode = value;
			break;
		case 5:
			flip = value;
			break;
		default:
			break;
		}

		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_CameraParamChanged_Notification, this);
		return;
	}

	protected void add_op_packet(short command, byte[] content,
			int content_length) {
		byte[] header = new byte[HEADER_LENGTH];
		LibcMisc.memset(header, (byte) 0, HEADER_LENGTH);
		System.arraycopy(MO_O, 0, header, 0, 4);
		LibcMisc.memcpy(header, 4, command, 2);
		LibcMisc.memcpy(header, 15, content_length, 4);

		op_w_data.appendBytes(header, HEADER_LENGTH);
		if (content_length > 0)
			op_w_data.appendBytes(content, content_length);
		if (CFStream.CFWriteStreamCanAcceptBytes(op_w_stream)) {
			int ret;
			if (0 < (ret = CFStream.CFWriteStreamWrite(op_w_stream,
					op_w_data.mutableBytes(), op_w_data.length()))) {
				op_w_t = time(null);
				op_w_data.replaceBytesInRange(NSRange.MakeNsRange(0, ret),
						null, 0);
			}
		}
	}

	protected void add_op_login_req() {
		Log.i(this.getClass().getSimpleName(), "ipcamera op_login_req " + this.getHost());
		add_op_packet(OP_LOGIN_REQ, null, 0);
	}

	protected void add_op_verify_req() {
		Log.i(this.getClass().getSimpleName(), "ipcamera op_verify_req");
		byte[] content = new byte[26];
		LibcMisc.memset(content, 0, 26);
		try {
			LibcMisc.memcpy(content, 0, user.getBytes("ASCII"), 0,
					user.length() > 12 ? 12 : user.length());
			LibcMisc.memcpy(content, 13, pwd.getBytes("ASCII"), 0,
					pwd.length() > 12 ? 12 : pwd.length());
		} catch (UnsupportedEncodingException e) {
			
			e.printStackTrace();
		}
		add_op_packet(OP_VERIFY_REQ, content, 26);
	}

	protected void add_op_keep_alive() {
		add_op_packet(OP_KEEP_ALIVE, null, 0);
	}

	protected void add_op_video_start_req() {
		byte[] buffer = { 1 };
		add_op_packet(OP_VIDEO_START_REQ, buffer, 1);
	}

	protected void add_op_video_end() {
		add_op_packet(OP_VIDEO_END, null, 0);
	}

	protected void add_op_audio_start_req() {
		byte[] buffer = { 2 };
		add_op_packet(OP_AUDIO_START_REQ, buffer, 1);
	}

	protected void add_op_audio_end() {
		add_op_packet(OP_AUDIO_END, null, 0);
	}

	protected void add_op_talk_start_req() {
		byte[] buffer = { (byte) (Integer.parseInt(audio_buffer_time) / 1000) };
		if (buffer[0] == 0)
			buffer[0] = 1;
		add_op_packet(OP_TALK_START_REQ, buffer, 1);
	}

	protected void add_op_talk_end() {
		add_op_packet(OP_TALK_END, null, 0);
	}

	protected void add_op_decoder_control_req(byte[] command) {
		add_op_packet(OP_DECODER_CONTROL_REQ, command, 1);
	}

	protected void add_op_params_fetch_req() {
		add_op_packet(OP_PARAMS_FETCH_REQ, null, 0);
	}

	protected void add_op_params_set_req(int i, byte value) {
		byte[] buffer = new byte[2];
		buffer[0] = (byte) i;
		buffer[1] = value;
		Log.e("req height", String.valueOf(value));
		add_op_packet(OP_PARAMS_SET_REQ, buffer, 2);
	}

	protected void parse_av_packet() {
		int i;
		short command;
		int content_length;

		switch (av_state) {
		case AV_RCVSTATE_HDR:
			if (0 == LibcMisc.memcmp(av_buffer.getData(), 0, MO_V, 0, 4)) {
				// error state
			}

			command = LibcMisc.get_short(av_buffer.getData(), 4);
			content_length = LibcMisc.get_int(av_buffer.getData(), 15);
			if (command == AV_VIDEO_DATA){
				av_state = AvRcvState.AV_RCVSTATE_VIDEO;
//				Log.i(this.getClass().getSimpleName(), "get video start req");
			}
			else if (command == AV_AUDIO_DATA){
				av_state = AvRcvState.AV_RCVSTATE_AUDIO;
			}
				
			try {
				av_buffer = new AvDataBuffer(content_length);
			} catch (OutOfMemoryError e) {
				System.gc();
				Log.e("MjpegCamera", "OutOfMemoryError");
			}
			
			break;

		case AV_RCVSTATE_AUDIO:
			parse_av_audio_data();
			av_buffer = new AvDataBuffer(23);
			av_state = AvRcvState.AV_RCVSTATE_HDR;
			break;

		case AV_RCVSTATE_VIDEO:
			
			parse_av_video_data();
			av_buffer = new AvDataBuffer(23);
			av_state = AvRcvState.AV_RCVSTATE_HDR;
			break;
		}

		return;
	}

	protected void parse_av_video_data(int content_length) {
		if (video_status != PLAYING_STATUS.PLAYING)
			return;
		if (content_length < 13)
			return;
		long t;
		long tick, length;
		tick = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH);
		t = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH + 4);
		length = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH + 9);

		if (content_length != (13 + length))
			return;

		Integer image_tick;
		NSDictionary params;
		Integer image_t = (int) t;
		NSData image_data = NSData.dataWithBytes(av_r_data.mutableBytes(),
				HEADER_LENGTH + 13, length);
		if (mAVIRecorder != null)
			try {
				mAVIRecorder.addImage(image_data.bytes());
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		
		if ((audio_status != PLAYING_STATUS.PLAYING) && (local_start_tick == 0)) {
			int now_time = times(null);
			local_start_tick = now_time + (Integer
					.parseInt(audio_buffer_time) / 10);
			camera_start_tick = tick;
		}
		if (local_start_tick != 0) {
			image_tick = (int) (local_start_tick + tick - camera_start_tick);
			params = new NSDictionary();
			params.put("t", image_t);
			params.put("tick", image_tick);
			params.put("data", image_data);

			NSNotificationCenter.defaultCenter().postNotification(
					IPCamera_Image_Notification, this, params);
		}
		
	}

	protected void parse_av_video_data() {

		if (video_status != PLAYING_STATUS.PLAYING)
			return;

		long t;
		long tick, length;
		byte[] buffer = av_buffer.getData();
		tick = LibcMisc.get_int(buffer, 0);
		t = LibcMisc.get_int(buffer, 4);
		length = LibcMisc.get_int(buffer, 9);

		if (buffer.length != (13 + length))
			return;

		Integer image_tick;
		NSDictionary params;
		Integer image_t = (int) t;
		NSData image_data = NSData.dataWithBytes(buffer, 13, length);
		if (mAVIRecorder != null)
			try {
				mAVIRecorder.addImage(image_data.bytes());
			} catch (Exception e) {
				
				e.printStackTrace();
			}

		if ((audio_status != PLAYING_STATUS.PLAYING) && (local_start_tick == 0)) {
			int now_time = times(null);
			local_start_tick = now_time + (Integer
					.parseInt(audio_buffer_time) / 10);
			camera_start_tick = tick;
		}

		if (local_start_tick != 0) {
			image_tick = (int) (local_start_tick + tick - camera_start_tick);
			params = new NSDictionary();
			params.put("t", image_t);
			params.put("tick", image_tick);
			params.put("data", image_data);


			NSNotificationCenter.defaultCenter().postNotification(
					IPCamera_Image_Notification, this, params);
		}

	}

	protected void parse_av_audio_data() {
		if (audio_status != PLAYING_STATUS.PLAYING)
			return;

		long t;
		long tick, length;
		short sample_temp;
		byte index_temp;
		byte[] decoded;
		byte[] buffer = av_buffer.getData();

		tick = LibcMisc.get_int(buffer, 0);
		t = LibcMisc.get_int(buffer, 8);
		length = LibcMisc.get_int(buffer, 13);

		if (buffer.length == 20 + length) {
//			System.out.println("parse_av_audio_data ");
			sample_temp = LibcMisc.get_short(buffer, (int) (17 + length));
			index_temp = buffer[(int) (17 + length + 2)];
			adpcm_decode_sample.setValue(sample_temp);
			adpcm_decode_index.setValue(index_temp);
		} else if (buffer.length != 17 + length)
			return;

		decoded = new byte[(int) (length << 2)];
		if (mAVIRecorder != null)
			try {
				byte[] encoded = buffer;
				encoded[HEADER_LENGTH + 13] = (byte) (adpcm_decode_sample
						.getValue() & 0xFF);
				encoded[HEADER_LENGTH + 14] = (byte) ((adpcm_decode_sample
						.getValue() & 0xFF00) >> 8);
				encoded[HEADER_LENGTH + 15] = (byte) (adpcm_decode_index
						.getValue() & 0xFF);
				encoded[HEADER_LENGTH + 16] = 0;
				mAVIRecorder.addAudio(encoded, 13, (int) length + 4);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		adpcm_decode(buffer, 17, (int) length, decoded, adpcm_decode_sample,
				adpcm_decode_index);
		Integer chunk_t = (int) (t);
		int now_time = times(null);
		if (local_start_tick == 0) {
			// struct tms tms_buffer;
			local_start_tick = now_time + (Integer
					.parseInt(audio_buffer_time) / 10);
			camera_start_tick = tick;
		}

		Integer chunk_tick = (int) (local_start_tick + tick - camera_start_tick);
		if ((chunk_tick - now_time) < 0) {
			// local_start_tick = 0;
			// return;
		}
		NSData chunk_data = NSData.dataWithBytes(decoded, 0, (length << 2));
		NSDictionary params = new NSDictionary();
		// params.put("t", chunk_t);
		params.put("t", now_time);
		params.put("tick", chunk_tick);
		params.put("data", chunk_data);

		if (mChildHandler != null) {
			Message msg = mChildHandler.obtainMessage();
			msg.obj = params;
			mChildHandler.sendMessage(msg);
		}
		
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_Audio_Notification, this, params);
	}

	protected void parse_av_audio_data(int content_length) {
		if (audio_status != PLAYING_STATUS.PLAYING)
			return;
		if (content_length < 13)
			return;
		long t;
		long tick, length;
		short sample_temp;
		byte index_temp;
		byte[] decoded;

		tick = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH);
		t = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH + 8);
		length = LibcMisc.get_int(av_r_data.mutableBytes(), HEADER_LENGTH + 13);

		if (content_length == 20 + length) {
			sample_temp = LibcMisc.get_short(av_r_data.mutableBytes(),
					(int) (HEADER_LENGTH + 17 + length));
			index_temp = av_r_data.mutableBytes()[(int) (HEADER_LENGTH + 17
					+ length + 2)];
			adpcm_decode_sample.setValue(sample_temp);
			adpcm_decode_index.setValue(index_temp);
		} else if (content_length != 17 + length)
			return;

		decoded = new byte[(int) (length << 2)];
		if (mAVIRecorder != null)
			try {
				byte[] encoded = av_r_data.mutableBytes();
				encoded[HEADER_LENGTH + 13] = (byte) (adpcm_decode_sample
						.getValue() & 0xFF);
				encoded[HEADER_LENGTH + 14] = (byte) ((adpcm_decode_sample
						.getValue() & 0xFF00) >> 8);
				encoded[HEADER_LENGTH + 15] = (byte) (adpcm_decode_index
						.getValue() & 0xFF);
				encoded[HEADER_LENGTH + 16] = 0;
				mAVIRecorder.addAudio(encoded, HEADER_LENGTH + 13,
						(int) length + 4);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		adpcm_decode(av_r_data.mutableBytes(), HEADER_LENGTH + 17,
				(int) length, decoded, adpcm_decode_sample, adpcm_decode_index);
		Integer chunk_t = (int) (t);
		int now_time = times(null);
		if (local_start_tick == 0) {
			// struct tms tms_buffer;
			local_start_tick = now_time + (Integer
					.parseInt(audio_buffer_time) / 10);
			camera_start_tick = tick;
		}

		Integer chunk_tick = (int) (local_start_tick + tick - camera_start_tick);
		if ((chunk_tick - now_time) < 0) {
			// local_start_tick = 0;
			// return;
		}
		NSData chunk_data = NSData.dataWithBytes(decoded, 0, (length << 2));
		NSDictionary params = new NSDictionary();
		// params.put("t", chunk_t);
		params.put("t", now_time);
		params.put("tick", chunk_tick);
		params.put("data", chunk_data);
		NSNotificationCenter.defaultCenter().postNotification(
				IPCamera_Audio_Notification, this, params);
	}

	protected void add_av_packet(short command, byte[] content,
			int content_length) {
		byte[] header = new byte[HEADER_LENGTH];
		LibcMisc.memset(header, 0, HEADER_LENGTH);
		LibcMisc.memcpy(header, 0, MO_V, 0, 4);
		LibcMisc.memcpy(header, 4, command, 2);
		LibcMisc.memcpy(header, 15, content_length, 4);

		av_w_data.appendBytes(header, HEADER_LENGTH);
		if (content_length > 0)
			av_w_data.appendBytes(content, content_length);
		if (CFStream.CFWriteStreamCanAcceptBytes(av_w_stream)) {
			int ret;
			if (0 < (ret = CFStream.CFWriteStreamWrite(av_w_stream,
					av_w_data.mutableBytes(), av_w_data.length())))
				av_w_data.replaceBytesInRange(NSRange.MakeNsRange(0, ret),
						null, 0);
		}
	}

	private void start_audio_play() {
		local_start_tick = 0;
		audioThread = new Thread(new Runnable() {

			public void run() {
				final SimpleAudioTrack audioTrack;

				try {
					// Create a new AudioTrack object using the same parameters
					// as the
					// AudioRecord
					audioTrack = new SimpleAudioTrack(8000,
							AudioFormat.CHANNEL_CONFIGURATION_MONO,
							AudioFormat.ENCODING_PCM_16BIT);
					// Start playback
					audioTrack.init();
					Log.e("AudioTrack", "Ready");
					// Write the music buffer to the AudioTrack object

				} catch (Throwable t) {
					Log.e("AudioTrack", "Playback Failed");
					return;
				}

				Looper.prepare();
				mChildHandler = new Handler() {
					public void handleMessage(Message msg) {
						NSDictionary audioDictionary = (NSDictionary) msg.obj;

						if (audioDictionary == null) {
							// user ask to stop
							mChildHandler = null;
							Log.e("AudioTrack", "Playback Quit");
							Looper.myLooper().quit();
						} else {
							// play audio data
							int play_time = (Integer) audioDictionary.get(
									"tick");
							int get_time = (Integer) audioDictionary.get("t");
							int now_time = times(null);
							if ((play_time - now_time) < -10) {
								// drop delayed packet
								// Log.e("AudioTrack", "Drop delayed packet " +
								// (now_time - get_time));
								return;
							}
							NSData data = (NSData) audioDictionary.get("data");
							audioTrack.playAudioTrack(data.bytes(), 0,
									data.length());
						}
					}
				};

				Looper.loop();
			}
		});
		audioThread.start();
	}

	private void stop_audio_play() {
		local_start_tick = 0;
		if (mChildHandler != null) {
			Message msg = mChildHandler.obtainMessage();
			msg.obj = null;
			mChildHandler.sendMessage(msg);
		}

		audioThread = null;
	}

	public void excute(CFStream stream, int eventType, Object clientCallBackInfo) {
		try {
			if (stream.equals(op_r_stream) || stream.equals(op_w_stream)) {
				op_stream_callback(eventType);
			} else if (stream.equals(av_r_stream) || stream.equals(av_w_stream)) {
				av_stream_callback(eventType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
