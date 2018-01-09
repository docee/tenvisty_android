package com.tws.commonlib.activity.hichip;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;

import com.hichip.content.HiChipDefines;
import com.hichip.tools.Packet;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlListEventReq;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.PlaybackActivity;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.SpinnerButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

import static com.tutk.IOTC.AVIOCTRLDEFs.AVIOCTRL_EVENT_ALL;
import static com.tutk.IOTC.AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONDECT;

public class EventList_HichipActivity extends BaseActivity implements IIOTCListener {

    private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;

    private static final int OPT_MENU_ITEM_SEARCH = 0;
    private static final int REQUEST_CODE_EVENT_DETAIL = 0;
    private static final int REQUEST_CODE_EVENT_SEARCH = 1;
    private static final int SEARCH_WITHIN_AN_HOUR = 0;
    private static final int SEARCH_WITHIN_HALF_A_DAY = 1;
    private static final int SEARCH_WITHIN_A_DAY = 2;
    private static final int SEARCH_WITHIN_A_WEEK = 3;
    public final static String VIDEO_PLAYBACK_START_TIME = "VIDEO START TIME";
    public final static String VIDEO_PLAYBACK_END_TIME = "VIDEO END TIME";
    private List<TWS_HI_P2P_FILE_INFO> list = Collections.synchronizedList(new ArrayList<TWS_HI_P2P_FILE_INFO>());
    private EventListAdapter adapter;

    private HichipCamera mCamera;

    private View loadingView = null;
    private View offlineView = null;
    private View noResultView = null;

    private ListView eventListView = null;
    private String dev_uid;
    private Boolean mIsSearchingEvent = false;

    Boolean isCloudEvent;
    private long startTime_;
    private long stopTime_;
    private boolean supportPlayback = false;
    SpinnerButton spinner_type;
    int searchEventType;
    LinearLayout ll_search_event_time;
    PopupWindow popupWindow;
    String[] arrSearchTimes;
    TextView txt_search_event_time;
    private Calendar mStartSearchCalendar;
    private Calendar mStopSearchCalendar;
    TextView txt_event_day_top;
    int accSelect = -1;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_event_list);
        arrSearchTimes = new String[]{getString(R.string.option_search_within_an_hour), getString(R.string.option_search_within_half_a_day)
                , getString(R.string.option_search_within_a_day)
                , getString(R.string.option_search_within_a_week)
                , getString(R.string.option_search_custom)};
        isCloudEvent = false;

        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                mCamera = (HichipCamera) _camera;
                mCamera.registerIOTCListener(this);
                break;
            }
        }
        adapter = new EventListAdapter(this);
        Bundle bundle = this.getIntent().getExtras();
        dev_uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);

        supportPlayback = true;
        this.setTitle(getResources().getString(R.string.title_event_list));
        initView();


    }

    @Override
    protected void initView() {
        super.initView();

        eventListView = (ListView) findViewById(R.id.lstEventList);
        eventListView.setAdapter(adapter);
        eventListView.setOnItemClickListener(listViewOnItemClickListener);
        eventListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!list.isEmpty()) {
                    txt_event_day_top.setText(list.get(firstVisibleItem).strDate);
                }
                // TwsToast.showToast(EventListActivity.this, "firstVisibleItem:" + firstVisibleItem + ",visibleItemCount:" + visibleItemCount + ",totalItemCount:" + totalItemCount);
                if (firstVisibleItem == 0) {
//                    View firstVisibleItemView = mListView.getChildAt(0);
//                    if (firstVisibleItemView != null && firstVisibleItemView.getTop() == 0) {
//                        Log.d("ListView", "##### 滚动到顶部 #####");
//                    }
                } else if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
//                    View lastVisibleItemView = mListView.getChildAt(mListView.getChildCount() - 1);
//                    if (lastVisibleItemView != null && lastVisibleItemView.getBottom() == mListView.getHeight()) {
//                        Log.d("ListView", "##### 滚动到底部 ######");
//                    }
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //do nothing
            }

        });
        txt_event_day_top = (TextView) findViewById(R.id.txt_event_day_top);
        loadingView = getLayoutInflater().inflate(R.layout.view_loading_events, null);
        offlineView = getLayoutInflater().inflate(R.layout.view_camera_is_offline, null);
        noResultView = getLayoutInflater().inflate(R.layout.view_no_result, null);
        ll_search_event_time = (LinearLayout) findViewById(R.id.ll_search_event_time);
        ll_search_event_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsSearchingEvent) {
                    return;
                }
                popupWindow = showPopupWindow(ll_search_event_time, arrSearchTimes, new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (position == 4) {
                            showCustomSearch();
                        } else {
                            mStartSearchCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
                            mStopSearchCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
                            switch (position) {
                                case SEARCH_WITHIN_AN_HOUR:
                                    mStartSearchCalendar.add(Calendar.HOUR, -1);
                                    break;

                                case SEARCH_WITHIN_HALF_A_DAY:
                                    mStartSearchCalendar.add(Calendar.HOUR_OF_DAY, -12);
                                    break;

                                case SEARCH_WITHIN_A_DAY:
                                    mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -1);
                                    break;

                                case SEARCH_WITHIN_A_WEEK:
                                    mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -7);
                                    break;
                            }

                            long startTime = mStartSearchCalendar.getTimeInMillis();
                            long stopTime = mStopSearchCalendar.getTimeInMillis();
                            searchEventList(startTime, stopTime, searchEventType);
                        }
                        popupWindow.dismiss();
                    }
                }, true);
            }
        });
        txt_search_event_time = (TextView) findViewById(R.id.txt_search_event_time);
        spinner_type = (SpinnerButton) findViewById(R.id.spinner_type);
        spinner_type.setTitles(new String[]{getString(R.string.lab_record_event), getString(R.string.lab_record_time)});
        spinner_type.setSpinnerButtonListener(new SpinnerButton.SpinnerButtonListener() {
            @Override
            public void OnSpinnerButtonClick(int which) {
                if (accSelect == which) {
                    return;
                }
                if (mIsSearchingEvent) {
                    spinner_type.Click(accSelect);
                    return;
                }
                accSelect = which;
                if (which == 0) {
                    searchEventType = HiChipDefines.HI_P2P_EVENT_ALARM;
                } else {
                    searchEventType = HiChipDefines.HI_P2P_EVEN_PLAN;
                }
                searchEventList(mStartSearchCalendar.getTimeInMillis(), mStopSearchCalendar.getTimeInMillis(), 1);
            }
        });
        initEventSearchTime();
        txt_event_day_top.setVisibility(View.GONE);
        spinner_type.Click(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        quit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EVENT_DETAIL) {

            if (data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                quit();
                break;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        Configuration cfg = getResources().getConfiguration();

        if (cfg.orientation == Configuration.ORIENTATION_LANDSCAPE) {

        } else if (cfg.orientation == Configuration.ORIENTATION_PORTRAIT) {

        }
    }

    private AdapterView.OnItemClickListener listViewOnItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

            if (list.size() == 0 || list.size() < position)
                return;

            if (isCloudEvent == false && !supportPlayback) {
                return;
            }

            int pos = position - eventListView.getHeaderViewsCount();

            if (pos < 0)
                return;

            TWS_HI_P2P_FILE_INFO file_info = list.get(position);
            Bundle extras = new Bundle();
            extras.putString(TwsDataValue.EXTRA_KEY_UID, dev_uid);
            byte[] b_startTime = file_info.sStartTime.parseContent();
            // byte[] b_startTime = STimeDay.parseContent(file_info.sStartTime.year,
            // file_info.sStartTime.month, file_info.sStartTime.day,
            // file_info.sStartTime.wday, file_info.sStartTime.hour,
            // file_info.sStartTime.minute, file_info.sStartTime.second);
            extras.putByteArray("st", b_startTime);

            long startTimeLong = file_info.sStartTime.getTimeInMillis();
            long endTimeLong = file_info.sEndTime.getTimeInMillis();

            long pbtime = startTimeLong - endTimeLong;
            extras.putLong("pb_time", pbtime);
            extras.putLong(VIDEO_PLAYBACK_START_TIME, startTimeLong);
            extras.putLong(VIDEO_PLAYBACK_END_TIME, endTimeLong);

            Intent intent = new Intent();
            intent.putExtras(extras);
            intent.setClass(EventList_HichipActivity.this, Playback_HichipActivity.class);
            startActivityForResult(intent, REQUEST_CODE_EVENT_DETAIL);
        }
    };

    private void quit() {
        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mCamera = null;
            if (handler != null && timeoutRun != null) {
                handler.removeCallbacks(timeoutRun);
            }
        }
    }

    private void initEventSearchTime() {
        mStartSearchCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        mStartSearchCalendar.setTime(new Date());
        mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -1);
        mStopSearchCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        mStopSearchCalendar.setTime(new Date());
    }

    private static String getLocalTime(long utcTime, boolean subMonth) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        calendar.setTimeInMillis(utcTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");
        dateFormat.setTimeZone(TimeZone.getDefault());

        if (subMonth)
            calendar.add(Calendar.MONTH, -1);

        return dateFormat.format(calendar.getTime());
    }

    /**
     * 获取时间 小时:分;秒 HH:mm:ss
     *
     * @return
     */
    public static String getTimeShort(Date currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        formatter.setTimeZone(TimeZone.getDefault());
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    Runnable timeoutRun = new Runnable() {

        @Override
        public void run() {

            if (mIsSearchingEvent) {
                mIsSearchingEvent = false;
                eventListView.removeFooterView(loadingView);
                eventListView.removeFooterView(noResultView);
                eventListView.addFooterView(noResultView);
                //Toast.makeText(EventListActivity.this, EventListActivity.this.getText(R.string.tips_search_event_no_result), Toast.LENGTH_SHORT).show();
                eventListView.setEnabled(true);
            }
        }

    };

    private void searchEventList(final long startTime, final long stopTime, int eventType) {
        mIsSearchingEvent = true;
        handler.removeCallbacks(timeoutRun);

        txt_event_day_top.setVisibility(View.GONE);
        list.clear();
        adapter.notifyDataSetChanged();

        eventListView.removeFooterView(noResultView);
        eventListView.removeFooterView(loadingView);
        eventListView.addFooterView(loadingView);

        // set search time to actionbar title
        String startTimeSring = getLocalTime(startTime, false);
        String stopTimeString = getLocalTime(stopTime, false);

        startTime_ = startTime;
        stopTime_ = stopTime;

//        TextView txtSearchTime = (TextView) searchTimeView.findViewById(R.id.txtSearchTimeDuration);
        txt_search_event_time.setText(startTimeSring + " - " + stopTimeString);


        if (mCamera != null && mCamera.isConnected()) {
            eventListView.setEnabled(false);
            searchEventList2(startTime, stopTime, eventType);
            /* timeout for no search result been found */
            handler.postDelayed(timeoutRun, 18000);

        } else {
            TwsToast.showToast(EventList_HichipActivity.this, getString(R.string.toast_connect_drop));
            mIsSearchingEvent = false;
            eventListView.removeFooterView(loadingView);
            eventListView.removeFooterView(noResultView);
            eventListView.addFooterView(noResultView);
            //Toast.makeText(EventListActivity.this, EventListActivity.this.getText(R.string.tips_search_event_no_result), Toast.LENGTH_SHORT).show();
        }

    }

    private void searchEventList2(long startTime, long stopTime, int eventType) {
        eventType = searchEventType;
        byte btEventType;
        com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE timezone = (mCamera).getTimezone();
        com.hichip.content.HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone_ext = (mCamera).getTimezoneExt();
        TimeZone tz = null;
        long offset = 0;
        if (timezone != null && timezone.u32DstMode == 1) {
            int dstMode = timezone.u32DstMode;
            if (dstMode == 1) {
                String[] specifiedIDs = TimeZone.getAvailableIDs(timezone.s32TimeZone * 60 * 60 * 1000);
                for (int i = 0; i < specifiedIDs.length; i++) {
                    if (TimeZone.getTimeZone(specifiedIDs[i]).useDaylightTime() && TimeZone.getTimeZone(specifiedIDs[i]).inDaylightTime(new Date())) {
                        tz = TimeZone.getTimeZone(specifiedIDs[i]);
                        break;
                    }
                }
            }
        } else if (timezone_ext != null && timezone_ext.u32DstMode == 1) {
            tz = TimeZone.getTimeZone(Packet.getString(timezone_ext.sTimeZone));
        }
        if (tz != null && tz.inDaylightTime(new Date())) {
            startTime += 60 * 60 * 1000;
            stopTime += 60 * 60 * 1000;
        }
        switch (eventType) {
            case 0:
                btEventType = HiChipDefines.HI_P2P_EVENT_ALL;
                break;
            case 1:
                btEventType = HiChipDefines.HI_P2P_EVENT_MANUAL;
                break;
            case 2:
                btEventType = HiChipDefines.HI_P2P_EVENT_ALARM;
                break;
            case 3:
                btEventType = HiChipDefines.HI_P2P_EVEN_PLAN;
                break;
            default:
                btEventType = HiChipDefines.HI_P2P_EVENT_ALL;
                break;
        }
        int time = (int) ((TimeZone.getDefault().getOffset(new Date().getTime())) / 3600L / 10L);
        if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST)) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_NODST, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                    .parseContent(0, startTime, stopTime, time, btEventType));
        } else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NEW)) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_NEW, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                    .parseContent(0, startTime, stopTime, time, btEventType));
        } else if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_EXT)) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START_EXT, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                    .parseContent(0, startTime, stopTime, time, btEventType));
        } else {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_PB_QUERY_START, HiChipDefines.HI_P2P_S_PB_LIST_REQ
                    .parseContent(0, startTime, stopTime, time, btEventType));
        }
    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {
        Bundle bundle = new Bundle();
        Message msg = handler.obtainMessage();
        msg.what = resultCode;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int succ, int avIOCtrlMsgType, byte[] data) {
        if (succ == -1) {// IO的错误码
            dismissLoadingProgress();
            TwsToast.showToast(EventList_HichipActivity.this, getString(R.string.toast_connect_drop));
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.arg1 = succ;
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

    public class EventInfo {

        public static final int EVENT_UNREADED = 0;
        public static final int EVENT_READED = 1;
        public static final int EVENT_NORECORD = 2;

        public int EventType;
        public long Time;
        public STimeDay EventTime;
        public int EventStatus;
        public String dropboxPath;
        public String strDate;
        public String strTime;
        public boolean isDateFirstItem;
        private UUID m_uuid = UUID.randomUUID();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));

        public String getUUID() {
            return m_uuid.toString();
        }

        public EventInfo(int eventType, long time, int eventStatus, String dropboxPath_) {

            EventType = eventType;
            Time = time;
            EventStatus = eventStatus;
            dropboxPath = dropboxPath_;
        }

        public EventInfo(int eventType, STimeDay eventTime, int eventStatus) {

            EventType = eventType;
            EventTime = eventTime;
            EventStatus = eventStatus;
            if (eventTime != null) {
                calendar.setTimeInMillis(eventTime.getTimeInMillis());
                strDate = getStringDateShort(calendar.getTime());
                //strTime = eventTime.getLocalTime();
                strTime = getTimeShort(calendar.getTime());
                isDateFirstItem = false;
            }
        }
    }

    public class EventListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public EventListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean isEnabled(int position) {

            if (list.size() == 0) {
                return false;
            } else {
            }
            return super.isEnabled(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final TWS_HI_P2P_FILE_INFO evt = (TWS_HI_P2P_FILE_INFO) getItem(position);

            ViewHolder holder = null;
            String lastDate = "";
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.view_event_list_item_hichip, null);

                holder = new ViewHolder();
                holder.txt_event_day = (TextView) convertView.findViewById(R.id.txt_event_day);
                holder.txt_event_time = (TextView) convertView.findViewById(R.id.txt_event_time);
                holder.txt_event_title = (TextView) convertView.findViewById(R.id.txt_event_title);
                holder.txt_event_type = (TextView) convertView.findViewById(R.id.txt_event_type);
                holder.img_event_image = (ImageView) convertView.findViewById(R.id.img_event_image);
                holder.img_event_type_image = (ImageView) convertView.findViewById(R.id.img_event_type_image);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.txt_event_type.setText(evt.EventType == HiChipDefines.HI_P2P_EVENT_ALARM ? getString(R.string.evttype_motion_detection) : getString(R.string.evttype_plan));
            if (mCamera != null) {
                holder.txt_event_title.setText(mCamera.getNickName());
            }
            if (evt.sStartTime != null) {
                holder.txt_event_time.setText(evt.strTime);

                if (evt.isDateFirstItem && position != 0) {
                    holder.txt_event_day.setText(evt.strDate);
                    holder.txt_event_day.setVisibility(View.VISIBLE);
                } else {

                    holder.txt_event_day.setVisibility(View.GONE);
                }
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
                calendar.setTimeInMillis(evt.sStartTime.getTimeInMillis());

                SimpleDateFormat dateFormat = new SimpleDateFormat();
                dateFormat.setTimeZone(TimeZone.getDefault());
                String timeString = dateFormat.format(calendar.getTime());
                holder.txt_event_time.setText(timeString);
            }

            //holder.indicator.setVisibility(supportPlayback & evt.EventStatus != EventInfo.EVENT_NORECORD ? View.VISIBLE : View.GONE);

            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inSampleSize = 4;// 1/4宽高

            //Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_FILES.get(position),bfo) ;
            Bitmap bitmap = null;
            File rootFolder = new File(Environment
                    .getExternalStorageDirectory().getAbsolutePath()
                    + "/" + MyConfig.getFolderName() + "/");
            File rootFolder1 = new File(rootFolder.getAbsolutePath()
                    + "/Remote/");
            File targetFolder = new File(rootFolder1.getAbsolutePath()
                    + "/" + dev_uid);
            String filenameString = dev_uid + "_" + evt.EventType + evt.sStartTime.year + evt.sStartTime.month + evt.sStartTime.day + evt.sStartTime.wday + evt.sStartTime.hour + evt.sStartTime.minute + evt.sStartTime.second + ".jpg";
            String fullFileNamePath = targetFolder.getAbsolutePath() + "/" + filenameString;
            bitmap = BitmapFactory.decodeFile(fullFileNamePath, bfo);
            if (bitmap != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    holder.img_event_image.setBackground(new BitmapDrawable(mInflater.getContext().getResources(), bitmap));
                } else {
                    holder.img_event_image.setBackgroundResource(R.drawable.view_event_record);
                }
                holder.img_event_image.setImageResource(R.drawable.ic_menu_play_inverse_background);
            } else {
                holder.img_event_image.setBackgroundResource(R.drawable.view_event_record);
                holder.img_event_image.setImageBitmap(null);
            }
            if (bitmap != null) {
                if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
                    holder.img_event_type_image.setImageResource(R.drawable.ic_motion_detection_read);
                } else {

                    holder.img_event_type_image.setImageResource(R.drawable.ic_time_record_read);
                }
                // holder.txt_event_type.setTypeface(null, Typeface.NORMAL);
                // holder.txt_event_type.setTextColor(0xFF999999);
            } else {
                if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
                    holder.img_event_type_image.setImageResource(R.drawable.ic_motion_detection_unread);
                } else {
                    holder.img_event_type_image.setImageResource(R.drawable.ic_time_record_unread);
                }
                // holder.txt_event_type.setTypeface(null, Typeface.BOLD);
                // holder.txt_event_type.setTextColor(0xFF000000);
            }
            return convertView;

        }

        private final class ViewHolder {
            public TextView txt_event_day;
            public TextView txt_event_time;
            public TextView txt_event_title;
            public TextView txt_event_type;
            public ImageView img_event_image;
            public ImageView img_event_type_image;
        }
    }// EventListAdapter


    public static String getStringDateShort(Date currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy");
        formatter.setTimeZone(TimeZone.getDefault());
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            int sessionChannel = bundle.getInt("sessionChannel");

            switch (msg.what) {

                case NSCamera.CONNECTION_STATE_CONNECTING:
                    break;
                case NSCamera.CONNECTION_STATE_WRONG_PASSWORD:
                case NSCamera.CONNECTION_STATE_CONNECT_FAILED:
                case NSCamera.CONNECTION_STATE_DISCONNECTED:
                case NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE:
                case NSCamera.CONNECTION_STATE_TIMEOUT:

                    if (eventListView.getAdapter() != null && eventListView.getFooterViewsCount() == 0) {
                        txt_event_day_top.setVisibility(View.GONE);
                        list.clear();
                        eventListView.addFooterView(offlineView);
                        adapter.notifyDataSetChanged();
                    }

                    break;

                case NSCamera.CONNECTION_STATE_CONNECTED:

                    if (sessionChannel == 0 && eventListView.getAdapter() != null) {
                        eventListView.removeFooterView(offlineView);
                        adapter.notifyDataSetChanged();
                    }

                    break;
                case HiChipDefines.HI_P2P_START_REC_UPLOAD_EXT://下载
                    break;
                case HiChipDefines.HI_P2P_PB_QUERY_START_NODST:
                case HiChipDefines.HI_P2P_PB_QUERY_START:
                    if (data.length >= 12) {
                        byte flag = data[8];// 数据发送的结束标识符
                        int cnt = data[9]; // 当前包的文件个数
                        if (cnt > 0) {
                            for (int i = 0; i < cnt; i++) {
                                int pos = 12;
                                int size = TWS_HI_P2P_FILE_INFO.sizeof();
                                byte[] t = new byte[24];
                                System.arraycopy(data, i * size + pos, t, 0, 24);
                                TWS_HI_P2P_FILE_INFO file_info = new TWS_HI_P2P_FILE_INFO(t);
                                long duration = file_info.sEndTime.getTimeInMillis()
                                        - file_info.sStartTime.getTimeInMillis();
                                if (duration <= 1000 * 1000 && duration > 0) { //1000秒，文件录像一般为15分钟，但是有可能会长一点所有就设置为1000
                                    list.add(file_info);
                                }
                            }
                        }
                        if (flag == 1) {// 表示数据收完了
                            if (!list.isEmpty()) {
                                txt_event_day_top.setText(list.get(0).strDate);
                                txt_event_day_top.setVisibility(View.VISIBLE);
                                Collections.sort(list, new Comparator<HiChipDefines.HI_P2P_FILE_INFO>() {
                                    @Override
                                    public int compare(HiChipDefines.HI_P2P_FILE_INFO eventInfo, HiChipDefines.HI_P2P_FILE_INFO t1) {
                                        long tm1 = eventInfo.sStartTime.getTimeInMillis();
                                        long tm2 = t1.sStartTime.getTimeInMillis();
                                        return tm1 > tm2 ? -1 : (tm1 < tm2 ? 1 : 0);
                                    }
                                });
                                setListDate();
                            }
                            adapter.notifyDataSetChanged();

                            eventListView.removeFooterView(loadingView);
                            eventListView.removeFooterView(noResultView);

                            if (list.size() == 0) {
                                eventListView.addFooterView(noResultView);
                                //Toast.makeText(EventListActivity.this, EventListActivity.this.getText(R.string.tips_search_event_no_result), Toast.LENGTH_SHORT).show();
                            }

                            eventListView.setEnabled(true);
                            mIsSearchingEvent = false;
                        }
                    }
                    break;
            }

            super.handleMessage(msg);
        }
    };


    private void setListDate() {
        for (int i = 0; i < list.size(); i++) {
            if (i == 0) {
                list.get(i).isDateFirstItem = true;
            } else {
                list.get(i).isDateFirstItem = !list.get(i).strDate.equals(list.get(i - 1).strDate);
            }
        }
    }

    private void showCustomSearch() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        final AlertDialog dlg = builder.create();
        dlg.setTitle(getText(R.string.dialog_title_eventsearch));
        dlg.setIcon(android.R.drawable.ic_dialog_info);

        LayoutInflater inflater = dlg.getLayoutInflater();
        View view = inflater.inflate(R.layout.view_search_event_custom, null);
        dlg.setView(view);

        final Button btnStartDate = (Button) view.findViewById(R.id.btnStartDate);
        final Button btnStartTime = (Button) view.findViewById(R.id.btnStartTime);
        final Button btnStopDate = (Button) view.findViewById(R.id.btnStopDate);
        final Button btnStopTime = (Button) view.findViewById(R.id.btnStopTime);
        Button btnOK = (Button) view.findViewById(R.id.btnOK);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        // set button
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        EventList_HichipActivity.this.mStartSearchCalendar = Calendar.getInstance();
        EventList_HichipActivity.this.mStartSearchCalendar.set(Calendar.SECOND, 0);
        EventList_HichipActivity.this.mStopSearchCalendar = Calendar.getInstance();
        EventList_HichipActivity.this.mStopSearchCalendar.set(Calendar.SECOND, 0);

        btnStartDate.setText(dateFormat.format(EventList_HichipActivity.this.mStartSearchCalendar.getTime()));
        btnStartTime.setText(timeFormat.format(EventList_HichipActivity.this.mStartSearchCalendar.getTime()));
        btnStopDate.setText(dateFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));
        btnStopTime.setText(timeFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));


        final DatePickerDialog.OnDateSetListener startDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                EventList_HichipActivity.this.mStartSearchCalendar.set(year, monthOfYear, dayOfMonth,
                        EventList_HichipActivity.this.mStartSearchCalendar.get(Calendar.HOUR_OF_DAY),
                        EventList_HichipActivity.this.mStartSearchCalendar.get(Calendar.MINUTE), 0);

                btnStartDate.setText(dateFormat.format(EventList_HichipActivity.this.mStartSearchCalendar.getTime()));

                // todo:
                // if start time > stop time , then stop time = start time
                //

                if (EventList_HichipActivity.this.mStartSearchCalendar.after(EventList_HichipActivity.this.mStopSearchCalendar)) {

                    EventList_HichipActivity.this.mStopSearchCalendar.setTimeInMillis(EventList_HichipActivity.this.mStartSearchCalendar.getTimeInMillis());
                    btnStopDate.setText(dateFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));
                    btnStopTime.setText(timeFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));
                }
            }
        };

        final DatePickerDialog.OnDateSetListener stopDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                // todo:
                // let tmp = after set stop time
                // if tmp time < start time, do nothing.
                //
                Calendar tmp = Calendar.getInstance();
                tmp.set(year, monthOfYear, dayOfMonth, EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
                        EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

                if (tmp.after(EventList_HichipActivity.this.mStartSearchCalendar) || tmp.equals(EventList_HichipActivity.this.mStartSearchCalendar)) {

                    EventList_HichipActivity.this.mStopSearchCalendar.set(year, monthOfYear, dayOfMonth,
                            EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
                            EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

                    btnStopDate.setText(dateFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));

                }
            }
        };

        final TimePickerDialog.OnTimeSetListener startTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                EventList_HichipActivity.this.mStartSearchCalendar.set(EventList_HichipActivity.this.mStartSearchCalendar.get(Calendar.YEAR),
                        EventList_HichipActivity.this.mStartSearchCalendar.get(Calendar.MONTH),
                        EventList_HichipActivity.this.mStartSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

                btnStartTime.setText(timeFormat.format(EventList_HichipActivity.this.mStartSearchCalendar.getTime()));

                // todo:
                // if start time > stop time , then stop time = start time
                //
                if (EventList_HichipActivity.this.mStartSearchCalendar.after(EventList_HichipActivity.this.mStopSearchCalendar)) {

                    EventList_HichipActivity.this.mStopSearchCalendar.setTimeInMillis(EventList_HichipActivity.this.mStartSearchCalendar.getTimeInMillis());
                    btnStopTime.setText(timeFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));
                }
            }
        };

        final TimePickerDialog.OnTimeSetListener stopTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                // todo:
                // let tmp = after set stop time
                // if tmp time < start time, do nothing.
                //
                Calendar tmp = Calendar.getInstance();
                tmp.set(EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.YEAR), EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
                        EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);

                if (tmp.after(EventList_HichipActivity.this.mStartSearchCalendar) || tmp.equals(EventList_HichipActivity.this.mStartSearchCalendar)) {

                    EventList_HichipActivity.this.mStopSearchCalendar.set(EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.YEAR),
                            EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
                            EventList_HichipActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

                    btnStopTime.setText(timeFormat.format(EventList_HichipActivity.this.mStopSearchCalendar.getTime()));
                }
            }
        };

        btnStartDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dateDialog = new DatePickerDialog(EventList_HichipActivity.this, AlertDialog.THEME_HOLO_LIGHT, startDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DAY_OF_MONTH));
                dateDialog.setTitle(R.string.lab_searchevent_from_date);
                dateDialog.show();
            }
        });

        btnStartTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                TimePickerDialog timeDialog = new TimePickerDialog(EventList_HichipActivity.this, AlertDialog.THEME_HOLO_LIGHT, startTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                timeDialog.setTitle(R.string.lab_searchevent_from_time);
                timeDialog.show();
            }
        });

        btnStopDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dateDialog = new DatePickerDialog(EventList_HichipActivity.this, AlertDialog.THEME_HOLO_LIGHT, stopDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DAY_OF_MONTH));
                dateDialog.setTitle(R.string.lab_searchevent_to_date);
                dateDialog.show();
            }
        });

        btnStopTime.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                TimePickerDialog timeDialog = new TimePickerDialog(EventList_HichipActivity.this, AlertDialog.THEME_HOLO_LIGHT, stopTimeOnTimeSetListener, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
                timeDialog.setTitle(R.string.lab_searchevent_to_time);
                timeDialog.show();
            }
        });

        btnOK.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//20130725 chun
//				DatabaseManager manager = new DatabaseManager(SearchEventActivity.this);
//				manager.addSearchHistory(SearchEventActivity.this.mDevUID, SearchEventActivity.this.mSearchEventType, mStartSearchCalendar.getTimeInMillis(),
//						mStopSearchCalendar.getTimeInMillis());
                long startTime = mStartSearchCalendar.getTimeInMillis();
                long stopTime = mStopSearchCalendar.getTimeInMillis();
                int eventType = 1;
                searchEventList(startTime, stopTime, eventType);
                dlg.dismiss();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.show();
    }

    private static class TWS_HI_P2P_FILE_INFO extends HiChipDefines.HI_P2P_FILE_INFO {

        public String strDate;
        public String strTime;
        public boolean isDateFirstItem;

        public static int sizeof() {
            return 24;
        }

        public TWS_HI_P2P_FILE_INFO(byte[] byt) {
            super(byt);
            if (super.sStartTime != null) {
                strDate = super.sStartTime.toString().substring(0, 10);
                strTime = super.sStartTime.toString().substring(11, 19);
                isDateFirstItem = false;
            }
        }
    }
}
