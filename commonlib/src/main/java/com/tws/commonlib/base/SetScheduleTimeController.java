package com.tws.commonlib.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.text.SimpleDateFormat;
import java.util.Calendar;

;

/**
 * @author Administrator
 *
 */
public class SetScheduleTimeController implements IIOTCListener {
	//计划开始时间
	private Calendar mFromCalendar;
	//计划结束时间
	private Calendar mToCalendar;
	//计划类型,0:关闭，1:全天，2:自定义
	private int selectType = -1;
	private Activity activity;
	private HiChipDefines.HI_P2P_QUANTUM_TIME quantum_time;
//	public static final int HI_P2P_TYPE_ALARM = 0;  /*报警联动计划*/
//	public static final int HI_P2P_TYPE_PLAN  = 1;  /*定时录像计划*/
//	public static final int HI_P2P_TYPE_SNAP  = 2; /*定时抓拍计划*/
	final private int[] ioCmdSet = new int[]{HiChipDefines.HI_P2P_SET_ALARM_SCHEDULE, HiChipDefines.HI_P2P_SET_REC_AUTO_SCHEDULE, HiChipDefines.HI_P2P_SET_SNAP_AUTO_SCHEDULE};
	final private int[] ioCmdGet = new int[]{HiChipDefines.HI_P2P_GET_ALARM_SCHEDULE, HiChipDefines.HI_P2P_GET_REC_AUTO_SCHEDULE, HiChipDefines.HI_P2P_GET_SNAP_AUTO_SCHEDULE};
	private int scheduleType = -1;
	//CheckBox[] weekDayCb;
	boolean[] weekDayChecked = new boolean[]{false, false, false, false, false, false, false};
	private IMyCamera mCamera;
	private OnSchuduleResult onSchuduleResult;
	private String[] arrTitles ;
	public String[] getArrTitles() {
		return arrTitles;
	}
	public void setArrTitles(String[] arrTitles) {
		this.arrTitles = arrTitles;
	}

	private String[] arrDescs ;
	public String[] getArrDescs() {
		return arrDescs;
	}
	public void setArrDescs(String[] arrDescs) {
		this.arrDescs = arrDescs;
	}

	@Override
	public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

	}

	@Override
	public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

	}

	@Override
	public void receiveSessionInfo(IMyCamera camera, int resultCode) {

	}

	@Override
	public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

	}

	@Override
	public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
		if(camera != mCamera)
			return;

		Bundle bundle = new Bundle();
		bundle.putByteArray(TwsDataValue.EXTRAS_KEY_DATA, data);
		Message msg = handler.obtainMessage();
		msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
		msg.obj = camera;
		msg.arg1 = avIOCtrlMsgType;
		msg.arg2 = avChannel;
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	@Override
	public void initSendAudio(IMyCamera paramCamera, boolean paramBoolean) {

	}

	@Override
	public void receiveOriginalFrameData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte1, int paramInt2, byte[] paramArrayOfByte2, int paramInt3) {

	}

	@Override
	public void receiveRGBData(IMyCamera paramCamera, int paramInt1, byte[] paramArrayOfByte, int paramInt2, int paramInt3) {

	}

	@Override
	public void receiveRecordingData(IMyCamera paramCamera, int avChannel, int paramInt1, String path) {

	}

	public interface OnSchuduleResult {
		public void onGetRemoteData(boolean[] weekDayChecked);
		public void onSetRemoteData();
		
		public void onWriteResult(String text, int type);

	}
	public void Displose(){
		mCamera.unregisterIOTCListener(this);
	}
	public SetScheduleTimeController(Activity activity, int scheduleType, IMyCamera mCamera, OnSchuduleResult onSchuduleResult){
		this.activity = activity;
		this.scheduleType = scheduleType;
		this.mCamera = mCamera;
		this.onSchuduleResult = onSchuduleResult;
		mFromCalendar = Calendar.getInstance();
		mToCalendar = Calendar.getInstance();
		//weekDayCb = new CheckBox[7];
		weekDayChecked = new boolean[]{false,false,false,false,false,false,false};
		mCamera.registerIOTCListener(this);
//		weekDayCb[0] = (CheckBox)findViewById(R.id.sunday_cb);
//		weekDayCb[1] = (CheckBox)findViewById(R.id.monday_cb);
//		weekDayCb[2] = (CheckBox)findViewById(R.id.tuesday_cb);
//		weekDayCb[3] = (CheckBox)findViewById(R.id.wednsday_cb);
//		weekDayCb[4] = (CheckBox)findViewById(R.id.thursday_cb);
//		weekDayCb[5] = (CheckBox)findViewById(R.id.friday_cb);
//		weekDayCb[6] = (CheckBox)findViewById(R.id.saturday_cb);
	}
	public void showRecordTypePopView(final String title) {
		if(GetSelectType() == -1){
			return;
		}
		View customView = activity.getLayoutInflater().inflate(R.layout.popview_select_record_type,
				null, false);
		if(this.getArrTitles()!=null && this.getArrDescs()!=null){
			TextView none_title_textview = (TextView)customView.findViewById(R.id.none_title_textview);
			TextView none_desc_textview = (TextView)customView.findViewById(R.id.none_desc_textview);
			TextView allday_desc_textview = (TextView)customView.findViewById(R.id.allday_desc_textview);
			TextView allday_title_textview = (TextView)customView.findViewById(R.id.allday_title_textview);
			TextView custom_title_textview = (TextView)customView.findViewById(R.id.custom_title_textview);
			TextView custom_desc_textview = (TextView)customView.findViewById(R.id.custom_desc_textview);
			none_title_textview.setText(arrTitles[0]);
			none_desc_textview.setText(arrDescs[0]);
			allday_title_textview.setText(arrTitles[1]);
			allday_desc_textview.setText(arrDescs[1]);
			custom_title_textview.setText(arrTitles[2]);
			custom_desc_textview.setText(arrDescs[2]);
		}

		final AlertDialog.Builder dlg = new AlertDialog.Builder(activity);
		final AlertDialog dlgBuilder = dlg.create();
		final LinearLayout[] arrRecordTypeLL=new LinearLayout[3];
		OnClickListener recordTypeListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
					for(int i=0;i<arrRecordTypeLL.length;i++){
						if(arrRecordTypeLL[i] == v ){
							if(i == 2){
								showRecordTimePopView(title);
							}
							else{
								SetSelectType(i);
							}
							break;
						}
					}
				dlgBuilder.dismiss();
			}
		};
		arrRecordTypeLL[0] = (LinearLayout)customView.findViewById(R.id.none_record_ll);
		arrRecordTypeLL[1] = (LinearLayout)customView.findViewById(R.id.allday_record_ll);
		arrRecordTypeLL[2] = (LinearLayout)customView.findViewById(R.id.manually_record_ll);
		for(int i=0;i<arrRecordTypeLL.length;i++){
			arrRecordTypeLL[i].setOnClickListener(recordTypeListener);
		}
		arrRecordTypeLL[GetSelectType()].getChildAt(1).setVisibility(View.VISIBLE);
		dlgBuilder.setView(customView);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dlgBuilder.setTitle(title);

		dlgBuilder.show();
		WindowManager m =this.activity.getWindowManager();
		Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
		WindowManager.LayoutParams p = dlgBuilder.getWindow().getAttributes();  //获取对话框当前的参数值
		//p.height = (int) (d.getHeight() * 0.3);   //高度设置为屏幕的0.3
		p.width = (int) (d.getWidth() * 0.9);    //宽度设置为屏幕的0.8
		dlgBuilder.getWindow().setAttributes(p);     //设置生效
		//dlgBuilder.getWindow().setLayout(activity.getWindowManager().getDefaultDisplay().getWidth(), activity.getWindowManager().getDefaultDisplay().getHeight());
	}

	private void showRecordTimePopView(String title) {
		View customView = activity.getLayoutInflater().inflate(R.layout.popview_select_event_time,
				null, false);

		final AlertDialog.Builder dlg = new AlertDialog.Builder(activity,AlertDialog.THEME_HOLO_LIGHT);
		final AlertDialog dlgBuilder = dlg.create();
		dlgBuilder.setView(customView);
		dlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
		dlgBuilder.setTitle(title);
		final TimePicker fromTimePicker  = (TimePicker) customView.findViewById(R.id.time_from_picker);
		final TimePicker toTimePicker  = (TimePicker) customView.findViewById(R.id.time_to_picker);
		setTimePicker(fromTimePicker,this.mFromCalendar.get(Calendar.HOUR_OF_DAY),this.mFromCalendar.get(Calendar.MINUTE));
		setTimePicker(toTimePicker,this.mToCalendar.get(Calendar.HOUR_OF_DAY),this.mToCalendar.get(Calendar.MINUTE));


//		final Spinner spinEventType = (Spinner) customView.findViewById(R.id.spinner_search_event_video);

		Button btnOK = (Button) customView.findViewById(R.id.seach_event_ok);
		Button btnCancel = (Button) customView.findViewById(R.id.seach_event_cancel);

		btnOK.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//if(toTimePicker.getCurrentHour()*60+(toTimePicker.getCurrentMinute()%2==1?30:0) != fromTimePicker.getCurrentHour()*60+(fromTimePicker.getCurrentMinute()%2==1?30:0)){
					 mToCalendar.set(mToCalendar.get(Calendar.YEAR),mToCalendar.get(Calendar.MONTH),mToCalendar.get(Calendar.DAY_OF_MONTH),toTimePicker.getCurrentHour(),toTimePicker.getCurrentMinute()%2==1?30:0);
					 mFromCalendar.set(mToCalendar.get(Calendar.YEAR),mToCalendar.get(Calendar.MONTH),mToCalendar.get(Calendar.DAY_OF_MONTH),fromTimePicker.getCurrentHour(),fromTimePicker.getCurrentMinute()%2==1?30:0);
					 SetSelectType(2);
					 dlgBuilder.dismiss();
			//	}
//				else{
//					HiToast.showToast(activity, activity.getString(R.string.tips_recordtime_bigger));
//				}
			}
		});

		btnCancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				dlgBuilder.dismiss();
			}
		});
		dlgBuilder.show();
		dlgBuilder.getWindow().setLayout(activity.getWindowManager().getDefaultDisplay().getWidth(), activity.getWindowManager().getDefaultDisplay().getHeight()); 
	}
	private void setTimePicker(final TimePicker timePicker,int hour,int minutes){
        //Calendar calendar = Calendar.getInstance();
       // calendar.setTimeInMillis(System.currentTimeMillis());
        timePicker.setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minutes);
        //如果需要设置分钟的时间间隔则调用该方法
        MyTimePickerDialog.setNumberPickerTextSize(timePicker,new String[]{"00","30","00","30","00","30"});
	}
	public int GetSelectType(){
		return this.selectType;
	}
	public void SetSelectType(int type){
		this.selectType = type;
		setCheckWeekDayVisible();
		setRecordTimeText();
	}
	private void setCheckWeekDayVisible(){
		if(GetSelectType()  != 0){
			//timing_video_recording_weekday.setVisibility(View.GONE);
		}
		else{
			//timing_video_recording_weekday.setVisibility(View.GONE);
		}
	}
	private void setRecordTimeText(){
		int type = GetSelectType();
		String text = "";
		switch(type){
			case 0:
				text = activity.getString(R.string.tips_recordtime_none_title);
				break;
			case 1:
				text = activity.getString(R.string.tips_recordtime_allday_title);
				break;
			case 2:
				final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
				text = timeFormat.format(mFromCalendar.getTime())+" - "+timeFormat.format(mToCalendar.getTime());
				break;
			default:
			break;
		}
		if(onSchuduleResult != null){
			onSchuduleResult.onWriteResult(text,type);
		}
	}
	
	//设置计划时间
	public void SetSchedule(){
		boolean allday = false;
		boolean none = false;
		if(quantum_time == null){
			return;
		}
		if(GetSelectType() == 1){
			allday = true;
		}
		else if(GetSelectType() == 0){
			none = true;
		}
		int fromIndex = mFromCalendar.get(Calendar.HOUR_OF_DAY) * 2 + (mFromCalendar.get(Calendar.MINUTE) == 30 ? 1 : 0);
		int toIndex = mToCalendar.get(Calendar.HOUR_OF_DAY) * 2 + (mToCalendar.get(Calendar.MINUTE) == 30 ? 1 : 0) -1;
//		if(toIndex < 0){
//			toIndex = 0;
//		}
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 48; j++) {
				//weekDayCb[i].isChecked() &&
				if(!none &&  (allday || (toIndex >= fromIndex && j >= fromIndex && j <= toIndex) || (toIndex < fromIndex && j>=fromIndex) || (toIndex < fromIndex && j<=toIndex))){
					quantum_time.sDayData[i][j] = 80;
				}else{
					quantum_time.sDayData[i][j] = 78;
				}
			}
		}
//		for(int i=0;i<7;i++) {
//			for(int j=0;j<48;j++) {
//				quantum_time.sDayData[i][j] = val;
//			}
//		}
		quantum_time.u32QtType = scheduleType;
		mCamera.sendIOCtrl(0,ioCmdSet[scheduleType],quantum_time.parseContent());
	}
	
	public void GetSchedule(){
		mCamera.sendIOCtrl(0,ioCmdGet[scheduleType], new byte[0]);
	}
	


	private Handler handler = new Handler() {
		@SuppressLint("SimpleDateFormat") @Override
		public void handleMessage(Message msg) {
			switch(msg.what) {
			case TwsDataValue.HANDLE_MESSAGE_IO_RESP:
			{
				if(msg.arg2==0) {
					//					MyCamera camera = (MyCamera)msg.obj;
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(TwsDataValue.EXTRAS_KEY_DATA);
					if(msg.arg1 ==  ioCmdGet[scheduleType]) {
						quantum_time = new HiChipDefines.HI_P2P_QUANTUM_TIME(data);
						//重写报警时间区间选择
						int _toIndex  = -1;
						int _fromIndex = -1;
						for(int i=0;i<7;i++) {
							for(int j=0;j<48;j++) {
								if(quantum_time.sDayData[i][j] != 78){
									if(quantum_time.sDayData[i][(48+j-1)%48]!=78 && (_toIndex == -1 || _toIndex ==j-1)){
										_toIndex = j;
									}
									if(quantum_time.sDayData[i][(48+j-1)%48]==78){
										_fromIndex = j;
									}
								}
							}
							break;
						}
						mFromCalendar.set(mFromCalendar.get(Calendar.YEAR), mFromCalendar.get(Calendar.MONTH), mFromCalendar.get(Calendar.DAY_OF_MONTH),_fromIndex/2, _fromIndex%2==0?0:30);
						mToCalendar.set(mFromCalendar.get(Calendar.YEAR), mFromCalendar.get(Calendar.MONTH),mFromCalendar.get(Calendar.DAY_OF_MONTH),(_toIndex+1)/2, _toIndex%2==0?30:0);

//						if(GetSelectType() == -1){
//							SimpleDateFormat formatter = new SimpleDateFormat ("yyyyMMdd");
//
//
//							if(formatter.format(mToCalendar.getTime()).compareTo(formatter.format(mFromCalendar.getTime()))>0){
//								SetSelectType(1);
//							}
//							else if(mToCalendar.after(mFromCalendar)){
//								SetSelectType(2);
//							}
//							else{
//								SetSelectType(0);
//							}
//						}
							if(_fromIndex != -1 && _toIndex ==-1){
								_toIndex = _fromIndex;
							}
							if(GetSelectType() == -1) {
								if (_fromIndex == -1 && _toIndex != -1) {
									SetSelectType(1);
								} else if (_fromIndex != -1 && _toIndex != -1) {
									SetSelectType(2);
								} else {
									SetSelectType(0);
								}
							}
						if(onSchuduleResult!=null){
							onSchuduleResult.onGetRemoteData(weekDayChecked);
						}
							
					}
					else if(msg.arg1 == ioCmdSet[scheduleType])
					{
						TwsToast.showToast(activity, activity.getString(R.string.tips_setting_succ));
						if(onSchuduleResult!=null){
							onSchuduleResult.onSetRemoteData();
						}

					}
				}
			}
			break;
			}
		}
	};

}
