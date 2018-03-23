package com.tws.commonlib.activity.aoni;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
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

import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlListEventReq;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.App;
import com.tws.commonlib.MainActivity;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.PlaybackActivity;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.SpinnerButton;
import com.tws.commonlib.util.ImageCache;
import com.tws.commonlib.util.ImageFetcher;
import com.tws.commonlib.util.ImageWorker;

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

public class EventList_AoniActivity extends BaseActivity implements IIOTCListener {

    private static final int Build_VERSION_CODES_ICE_CREAM_SANDWICH = 14;

    private static final int OPT_MENU_ITEM_SEARCH = 0;
    private static final int REQUEST_CODE_EVENT_DETAIL = 0;
    private static final int REQUEST_CODE_EVENT_SEARCH = 1;
    private static final int SEARCH_WITHIN_A_DAY = 0;
    private static final int SEARCH_WITHIN_2_DAY = 1;
    private static final int SEARCH_WITHIN_3_DAY = 2;
    private static final int SEARCH_WITHIN_A_WEEK = 3;
    private List<EventInfo> list = Collections.synchronizedList(new ArrayList<EventInfo>());
    private List<AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom> listReq = Collections.synchronizedList(new ArrayList<AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom>());

    private EventListAdapter adapter;

    private IMyCamera mCamera;

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
    private ImageFetcher mImageFetcher;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_event_list_aoni);
        arrSearchTimes = new String[]{getString(R.string.option_search_within_a_day), getString(R.string.option_search_within_2_day)
                , getString(R.string.option_search_within_3_day)
                , getString(R.string.option_search_within_a_week)
                , getString(R.string.option_search_custom)};
        isCloudEvent = false;

        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                mCamera = _camera;
                mCamera.registerIOTCListener(this);
                break;
            }
        }
        adapter = new EventListAdapter(this);
        Bundle bundle = this.getIntent().getExtras();
        dev_uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);

        supportPlayback = true;
        this.setTitle(getResources().getString(R.string.title_event_list));
        initImageFetcher();
        initView();


    }

    void initImageFetcher() {

        mImageThumbSize = TwsTools.dip2px(this, 100);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = TwsTools.dip2px(this, 5);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, mImageThumbSize);

        mImageFetcher.setImageSize(TwsTools.dip2px(this, 69), TwsTools.dip2px(this, 52));
        mImageFetcher.setLoadingImage(R.drawable.view_event_record);
        mImageFetcher.addImageCache(this.getSupportFragmentManager(), cacheParams);
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
                            mStartSearchCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            mStartSearchCalendar.set(Calendar.MINUTE, 0);
                            mStartSearchCalendar.set(Calendar.SECOND, 0);
                            mStopSearchCalendar.set(Calendar.HOUR_OF_DAY, 0);
                            mStopSearchCalendar.set(Calendar.MINUTE, 0);
                            mStopSearchCalendar.set(Calendar.SECOND, 0);
                            switch (position) {

                                case SEARCH_WITHIN_A_DAY:
                                    //mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -1);
                                    break;
                                case SEARCH_WITHIN_2_DAY:
                                    mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -1);
                                    break;

                                case SEARCH_WITHIN_3_DAY:
                                    mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -2);
                                    break;


                                case SEARCH_WITHIN_A_WEEK:
                                    mStartSearchCalendar.add(Calendar.DAY_OF_YEAR, -6);
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
                    searchEventType = AVIOCTRL_EVENT_MOTIONDECT;
                } else {
                    searchEventType = AVIOCTRL_EVENT_ALL;
                }
                searchEventList(mStartSearchCalendar.getTimeInMillis(), mStopSearchCalendar.getTimeInMillis(), 1);
            }
        });
        spinner_type.setVisibility(View.GONE);
        initEventSearchTime();
        txt_event_day_top.setVisibility(View.GONE);
        spinner_type.Click(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        quit();
        mImageFetcher.closeCache();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EVENT_DETAIL) {

            mImageFetcher.setExitTasksEarly(false);
            adapter.notifyDataSetChanged();
//            if (data != null) {
//                Bundle extras = data.getExtras();
//                if (extras != null) {
//                    String evtUUID = extras.getString("event_uuid");
//
//                    for (EventInfo evt : list) {
//
//                        if (evt.getUUID().equalsIgnoreCase(evtUUID)) {
//                            evt.EventStatus = EventInfo.EVENT_READED;
//                            adapter.notifyDataSetChanged();
//                            break;
//                        }
//                    }
//                }
//            }
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

            EventInfo evt = list.get(pos);

            if (evt.EventStatus == EventInfo.EVENT_NORECORD)
                return;

            Bundle extras = new Bundle();
            extras.putString(TwsDataValue.EXTRA_KEY_UID, dev_uid);
            extras.putInt("event_type", evt.EventType);
            extras.putLong("event_time", evt.Time);
            extras.putString("event_uuid", evt.getUUID());
            if (evt.EventTime != null) {
                extras.putByteArray("event_time2", evt.EventTime.toByteArray());
            }

            Intent intent = new Intent();
            intent.putExtras(extras);
            intent.setClass(EventList_AoniActivity.this, PlaybackActivity.class);
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
        mStartSearchCalendar.set(Calendar.HOUR_OF_DAY, 0);
        mStartSearchCalendar.set(Calendar.MINUTE, 0);
        mStartSearchCalendar.set(Calendar.SECOND, 0);
        mStopSearchCalendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        mStopSearchCalendar.setTime(new Date());
    }

    private static String getLocalTime(long utcTime, boolean subMonth) {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
        calendar.setTimeInMillis(utcTime);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
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
        releaseBitmap();
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
        if (startTimeSring.equals(stopTimeString)) {
            txt_search_event_time.setText(startTimeSring);
        } else {
            txt_search_event_time.setText(startTimeSring + " - " + stopTimeString);
        }


        if (mCamera != null && mCamera.isConnected()) {
            eventListView.setEnabled(false);
            searchEventList2(startTime, stopTime, eventType);
            /* timeout for no search result been found */
            handler.postDelayed(timeoutRun, 18000);

        } else {
            TwsToast.showToast(EventList_AoniActivity.this, getString(R.string.toast_connect_drop));
            mIsSearchingEvent = false;
            eventListView.removeFooterView(loadingView);
            eventListView.removeFooterView(noResultView);
            eventListView.addFooterView(noResultView);
            //Toast.makeText(EventListActivity.this, EventListActivity.this.getText(R.string.tips_search_event_no_result), Toast.LENGTH_SHORT).show();
        }

    }

    private void searchEventList2(long startTime, long stopTime, int eventType) {
        listReq.clear();
        while (stopTime >= startTime) {
            Date startDate = new Date(startTime);
            Date stopDate = new Date(stopTime);
            startDate.setHours(0);
            startDate.setMinutes(0);
            startDate.setSeconds(0);
            stopDate.setHours(0);
            stopDate.setMinutes(0);
            stopDate.setSeconds(0);
            startTime = startDate.getTime();
            stopTime = stopDate.getTime();
            listReq.add(new AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom(startDate.getYear() + 1900, startDate.getMonth() + 1, startDate.getDate(), (byte) eventType));
            startTime += 24 * 60 * 60 * 1000;
        }
        if (listReq.size() > 0) {
            AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom req = listReq.get(0);
            mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom.parseConent(req.year, req.month, req.day, (byte) 0));
            listReq.remove(0);

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
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
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

        public Bitmap getThumb() {
            return thumb;
        }

        public void setThumb(Bitmap thumb) {
            if (this.thumb != null && !this.thumb.isRecycled()) {
                this.thumb.recycle();
                this.thumb = null;
                System.gc();
            }
            this.thumb = thumb;
        }

        private Bitmap thumb;
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

            final EventInfo evt = (EventInfo) getItem(position);

            ViewHolder holder = null;
            String lastDate = "";
            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.view_event_list_item, null);

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

            holder.txt_event_type.setText(TwsTools.getEventType(EventList_AoniActivity.this, evt.EventType, false));
            if (mCamera != null) {
                holder.txt_event_title.setText(mCamera.getNickName());
            }
            if (evt.EventTime != null) {
                holder.txt_event_time.setText(evt.strTime);

                if (evt.isDateFirstItem && position != 0) {
                    holder.txt_event_day.setText(evt.strDate);
                    holder.txt_event_day.setVisibility(View.VISIBLE);
                } else {

                    holder.txt_event_day.setVisibility(View.GONE);
                }
            } else {
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
                calendar.setTimeInMillis(evt.Time);

                SimpleDateFormat dateFormat = new SimpleDateFormat();
                dateFormat.setTimeZone(TimeZone.getDefault());
                String timeString = dateFormat.format(calendar.getTime());
                holder.txt_event_time.setText(timeString);
            }

            //holder.indicator.setVisibility(supportPlayback & evt.EventStatus != EventInfo.EVENT_NORECORD ? View.VISIBLE : View.GONE);


            //Bitmap bitmap = BitmapFactory.decodeFile(IMAGE_FILES.get(position),bfo) ;
            Bitmap bitmap = evt.getThumb();

            String filenameString = TwsTools.getFileNameWithTime(dev_uid, TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB, evt.EventTime.getTimeInMillis(), evt.EventType);// dev_uid + "_" + evt.EventType + evt.EventTime.year + evt.EventTime.month + evt.EventTime.day + evt.EventTime.wday + evt.EventTime.hour + evt.EventTime.minute + evt.EventTime.second + ".jpg";
            String fullFileNamePath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB) + "/" + filenameString;
            mImageFetcher.loadImage(fullFileNamePath, holder.img_event_image, new ImageWorker.OnImageLoadedListener() {
                @Override
                public void onImageLoaded(Object obj, boolean success) {
                    View view = (View) obj;
                    int i = 3;
                    while (i >= 0 && view != null) {
                        view = (View) view.getParent();
                        i--;
                    }
                    ImageView img_event_type = null;
                    ImageView img_play = null;
                    if (view != null) {
                        img_event_type = view.findViewById(R.id.img_event_type_image);
                        img_play = view.findViewById(R.id.img_play);
                    }
                    if (img_event_type == null || img_play == null) {
                        return;
                    }
                    if (success) {

                        if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
                            img_event_type.setImageResource(R.drawable.ic_motion_detection_read);
                        } else {

                            img_event_type.setImageResource(R.drawable.ic_time_record_read);
                        }
                        img_play.setVisibility(View.VISIBLE);
                    } else {
                        if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
                            img_event_type.setImageResource(R.drawable.ic_motion_detection_unread);
                        } else {
                            img_event_type.setImageResource(R.drawable.ic_time_record_unread);
                        }
                        img_play.setVisibility(View.GONE);
                        // holder.txt_event_type.setTypeface(null, Typeface.BOLD);
                        // holder.txt_event_type.setTextColor(0xFF000000);
                    }
                }
            });
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        formatter.setTimeZone(TimeZone.getDefault());
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    public void releaseBitmap() {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setThumb(null);
            }
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");
            int sessionChannel = bundle.getInt("sessionChannel");

            switch (msg.what) {
                case NSCamera.CONNECTION_STATE_SLEEPING:
                    showAlert(App.getTopActivity(), getString(R.string.alert_camera_wakeup), null, false, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            back2Activity(MainActivity.class);
                        }
                    });
                    break;
                case NSCamera.CONNECTION_STATE_CONNECTING:
                    break;
                case NSCamera.CONNECTION_STATE_WRONG_PASSWORD:
                case NSCamera.CONNECTION_STATE_CONNECT_FAILED:
                case NSCamera.CONNECTION_STATE_DISCONNECTED:
                case NSCamera.CONNECTION_STATE_UNKNOWN_DEVICE:
                case NSCamera.CONNECTION_STATE_TIMEOUT:

                    if (eventListView.getAdapter() != null && eventListView.getFooterViewsCount() == 0) {
                        txt_event_day_top.setVisibility(View.GONE);
                        releaseBitmap();
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

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_RESP:

                    if (data.length >= 12 && mIsSearchingEvent && eventListView.getAdapter() != null) {

                        // int idx = data[8];
                        int end = data[9];
                        int cnt = data[10];
                        int type = data[11];
                        if (cnt > 0) {

                            //List<EventInfo> mlist = Collections.synchronizedList(new ArrayList<EventInfo>());
                            // mlist.clear();
                            int pos = 12;
                            int size = AVIOCTRLDEFs.SAvEvent_Ausdom.getTotalSize();

                            for (int i = 0; i < cnt; i++) {
                                byte[] t = new byte[8];
                                System.arraycopy(data, i * size + pos, t, 0, 8);
                                STimeDay time = new STimeDay(t);

                                byte event = 1;
                                //list.add(new EventInfo(event, time, status));
                                // if (searchEventType == event) {
                                EventInfo evt = new EventInfo(event, time, 0);
                                //mlist.add(evt);
                                list.add(evt);
                                // }
                            }

                            //converList(mlist);

                        }
                        if (end == 1) {
                            if (listReq.size() > 0) {
                                AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom req = listReq.get(0);
                                mCamera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTEVENT_REQ, AVIOCTRLDEFs.SMsgAVIoctrlListEventReq_Ausdom.parseConent(req.year, req.month, req.day, (byte) 0));
                                listReq.remove(0);
                            } else {
                                //setListDate();
                                if (!list.isEmpty()) {
                                    txt_event_day_top.setText(list.get(0).strDate);
                                    txt_event_day_top.setVisibility(View.VISIBLE);
                                    Collections.sort(list, new Comparator<EventInfo>() {
                                        @Override
                                        public int compare(EventInfo eventInfo, EventInfo t1) {
                                            long tm1 = eventInfo.EventTime.getTimeInMillis();
                                            long tm2 = t1.EventTime.getTimeInMillis();
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
                    }

                    break;
            }

            super.handleMessage(msg);
        }
    };

    /**
     * 改变list的顺序，最近的时间排在前面
     *
     * @param mlist
     */
    private void converList(List<EventInfo> mlist) {
        //List<EventInfo> temList=new ArrayList<EventInfo>();
        int listsize = mlist.size();

        for (int i = 0; i < listsize; i++) {
            list.add(0, mlist.get(i));
        }
    }

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
        View view = inflater.inflate(R.layout.view_search_event_custom_aoni, null);
        dlg.setView(view);

        final Button btnStartDate = (Button) view.findViewById(R.id.btnStartDate);
        final Button btnStopDate = (Button) view.findViewById(R.id.btnStopDate);
        Button btnOK = (Button) view.findViewById(R.id.btnOK);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        // set button
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");

        EventList_AoniActivity.this.mStartSearchCalendar = Calendar.getInstance();
        EventList_AoniActivity.this.mStartSearchCalendar.set(Calendar.HOUR_OF_DAY, 0);
        EventList_AoniActivity.this.mStartSearchCalendar.set(Calendar.MINUTE, 0);
        EventList_AoniActivity.this.mStartSearchCalendar.set(Calendar.SECOND, 0);
        EventList_AoniActivity.this.mStopSearchCalendar = Calendar.getInstance();
        EventList_AoniActivity.this.mStopSearchCalendar.set(Calendar.SECOND, 0);
        EventList_AoniActivity.this.mStopSearchCalendar.set(Calendar.HOUR_OF_DAY, 0);
        EventList_AoniActivity.this.mStopSearchCalendar.set(Calendar.MINUTE, 0);

        btnStartDate.setText(dateFormat.format(EventList_AoniActivity.this.mStartSearchCalendar.getTime()));
        btnStopDate.setText(dateFormat.format(EventList_AoniActivity.this.mStopSearchCalendar.getTime()));


        final DatePickerDialog.OnDateSetListener startDateOnDateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                EventList_AoniActivity.this.mStartSearchCalendar.set(year, monthOfYear, dayOfMonth,
                        EventList_AoniActivity.this.mStartSearchCalendar.get(Calendar.HOUR_OF_DAY),
                        EventList_AoniActivity.this.mStartSearchCalendar.get(Calendar.MINUTE), 0);

                btnStartDate.setText(dateFormat.format(EventList_AoniActivity.this.mStartSearchCalendar.getTime()));

                // todo:
                // if start time > stop time , then stop time = start time
                //

                if (EventList_AoniActivity.this.mStartSearchCalendar.after(EventList_AoniActivity.this.mStopSearchCalendar)) {

                    EventList_AoniActivity.this.mStopSearchCalendar.setTimeInMillis(EventList_AoniActivity.this.mStartSearchCalendar.getTimeInMillis());
                    btnStopDate.setText(dateFormat.format(EventList_AoniActivity.this.mStopSearchCalendar.getTime()));
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
                tmp.set(year, monthOfYear, dayOfMonth, EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
                        EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

                if (tmp.after(EventList_AoniActivity.this.mStartSearchCalendar) || tmp.equals(EventList_AoniActivity.this.mStartSearchCalendar)) {

                    EventList_AoniActivity.this.mStopSearchCalendar.set(year, monthOfYear, dayOfMonth,
                            EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.HOUR_OF_DAY),
                            EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.MINUTE), 0);

                    btnStopDate.setText(dateFormat.format(EventList_AoniActivity.this.mStopSearchCalendar.getTime()));

                }
            }
        };

        final TimePickerDialog.OnTimeSetListener startTimeOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                EventList_AoniActivity.this.mStartSearchCalendar.set(EventList_AoniActivity.this.mStartSearchCalendar.get(Calendar.YEAR),
                        EventList_AoniActivity.this.mStartSearchCalendar.get(Calendar.MONTH),
                        EventList_AoniActivity.this.mStartSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);


                // todo:
                // if start time > stop time , then stop time = start time
                //
                if (EventList_AoniActivity.this.mStartSearchCalendar.after(EventList_AoniActivity.this.mStopSearchCalendar)) {

                    EventList_AoniActivity.this.mStopSearchCalendar.setTimeInMillis(EventList_AoniActivity.this.mStartSearchCalendar.getTimeInMillis());
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
                tmp.set(EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.YEAR), EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
                        EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute, 0);

                if (tmp.after(EventList_AoniActivity.this.mStartSearchCalendar) || tmp.equals(EventList_AoniActivity.this.mStartSearchCalendar)) {

                    EventList_AoniActivity.this.mStopSearchCalendar.set(EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.YEAR),
                            EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.MONTH),
                            EventList_AoniActivity.this.mStopSearchCalendar.get(Calendar.DAY_OF_MONTH), hourOfDay, minute);

                }
            }
        };

        btnStartDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dateDialog = new DatePickerDialog(EventList_AoniActivity.this, AlertDialog.THEME_HOLO_LIGHT, startDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DAY_OF_MONTH));
                dateDialog.setTitle(R.string.lab_searchevent_from_date);
                dateDialog.show();
            }
        });


        btnStopDate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                DatePickerDialog dateDialog = new DatePickerDialog(EventList_AoniActivity.this, AlertDialog.THEME_HOLO_LIGHT, stopDateOnDateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
                        .get(Calendar.DAY_OF_MONTH));
                dateDialog.setTitle(R.string.lab_searchevent_to_date);
                dateDialog.show();
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

}
