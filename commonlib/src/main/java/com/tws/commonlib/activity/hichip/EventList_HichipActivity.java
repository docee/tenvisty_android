package com.tws.commonlib.activity.hichip;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.content.res.AppCompatResources;
import android.util.EventLog;
import android.view.Display;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.hichip.content.HiChipDefines;
import com.hichip.tools.Packet;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.AVIOCTRLDEFs.SMsgAVIoctrlListEventReq;
import com.tutk.IOTC.AVIOCTRLDEFs.STimeDay;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.NSCamera;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.activity.PlaybackActivity;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IDownloadCallback;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsSessionState;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.controller.SpinnerButton;
import com.tws.commonlib.task.VideoThumbImgTask;
import com.tws.commonlib.util.ImageCache;
import com.tws.commonlib.util.ImageFetcher;
import com.tws.commonlib.util.ImageWorker;
import com.tws.commonlib.util.Utils;

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

public class EventList_HichipActivity extends BaseActivity implements IIOTCListener, IDownloadCallback {

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

    private List<TWS_HI_P2P_FILE_INFO> downloadlist = Collections.synchronizedList(new ArrayList<TWS_HI_P2P_FILE_INFO>());
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
    boolean checkMode = false;
    NavigationBar title;
    private boolean isDownloading = false;
    private String path;
    int downloadIndex;

    private ImageFetcher mImageFetcher;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_event_list_hichip);
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
                mCamera.registerDownloadListener(this);
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

        mImageThumbSize = TwsTools.dip2px(this, 69);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
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
    public void onResume() {
        super.onResume();
        mCamera.registerDownloadListener(this);
        mImageFetcher.setExitTasksEarly(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        isDownloading = false;
        mCamera.stopDownloadRecording();
        mCamera.unregisterDownloadListener(this);
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    protected void initView() {
        View barView = findViewById(R.id.title_top);
        if (barView != null) {
            title = (NavigationBar) barView;
            title.setTitle(this.getTitle().toString());
            title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
            title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
            title.setRightBtnText(getString(R.string.edit));
            title.setRightBtnBackground(0);
            title.setNavigationButtonLeftListner(new NavigationBar.NavigationBarButtonListener() {

                @Override
                public void OnNavigationButtonClick(int which) {
                    switch (which) {
                        case NavigationBar.NAVIGATION_BUTTON_LEFT:
                            finish();

                            break;
                        case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                            setToolBarVisible();
                            break;
                    }
                }
            });
        }

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
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Utils.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
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
        mImageFetcher.closeCache();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_EVENT_DETAIL) {
            mImageFetcher.setExitTasksEarly(false);
            adapter.notifyDataSetChanged();
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
            if (checkMode) {
                if (file_info.downloadState != 1) {
                    file_info.isSelected = !file_info.isSelected;
                    v.findViewById(R.id.img_select).setSelected(file_info.isSelected);
                } else {
                    TwsToast.showToast(EventList_HichipActivity.this, getString(R.string.tip_alear_dowm));
                }
            } else {
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
                extras.putInt("event_type", file_info.EventType);
                extras.putLong(VIDEO_PLAYBACK_START_TIME, startTimeLong);
                extras.putLong(VIDEO_PLAYBACK_END_TIME, endTimeLong);

                Intent intent = new Intent();
                intent.putExtras(extras);
                intent.setClass(EventList_HichipActivity.this, Playback_HichipActivity.class);
                startActivityForResult(intent, REQUEST_CODE_EVENT_DETAIL);
            }
        }
    };

    public void toolbar_click(View view) {
        if (view.getId() == R.id.img_selectall) {
            view.setSelected(!view.isSelected());
            if (view.isSelected()) {
                ((TextView) findViewById(R.id.txt_selectall)).setTextColor(AppCompatResources.getColorStateList(EventList_HichipActivity.this, R.color.colorPrimary));
            } else {
                ((TextView) findViewById(R.id.txt_selectall)).setTextColor(AppCompatResources.getColorStateList(EventList_HichipActivity.this, R.color.darkergray));
            }
            boolean select = view.isSelected();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).downloadState != 1) {
                    list.get(i).isSelected = select;
                }
            }
            adapter.notifyDataSetChanged();
        } else if (view.getId() == R.id.img_download) {
            downloadlist.clear();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).isSelected && list.get(i).downloadState != 1) {
                    downloadlist.add(list.get(i));
                }
            }
            startMutiDownload();
        }
    }

    public void startMutiDownload() {
        if (TwsTools.isSDCardValid()) {
            downloadIndex = 0;
            if (downloadlist.size() == 0) {
                showAlert(getString(R.string.dialog_msg_record_selectdownload));
            } else {
                showingProgressDialog();
                new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... arg0) {
                        downloadSingle();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        super.onPostExecute(result);
                    }
                }.execute();

            }
        } else {
            TwsToast.showToast(EventList_HichipActivity.this, getString(R.string.tips_setting_failed));
        }
    }

    public void downloadSingle() {
        boolean hasDownloaded = true;
        TWS_HI_P2P_FILE_INFO fileInfo = null;
        String filePath = null;
        String fileName = null;
        while (hasDownloaded && downloadIndex < downloadlist.size()) {
            fileInfo = downloadlist.get(downloadIndex);
            filePath = TwsTools.getFilePath(camera.getUid(), TwsTools.PATH_RECORD_DOWNLAND) + "/";
            fileName = TwsTools.getFileNameWithTime(camera.getUid(), TwsTools.PATH_RECORD_DOWNLAND, fileInfo.sStartTime.getTimeInMillis(), fileInfo.EventType);
            File file = new File(filePath + fileName + ".avi");
            File file2 = new File(filePath + fileName + ".mp4");

            if (file.exists() || file2.exists()) {// 文件已下载过
                hasDownloaded = true;
                fileInfo.downloadState = 1;
                downloadIndex++;
            } else {
                hasDownloaded = false;
            }
        }
        if (!hasDownloaded) {
            if (mCamera != null) {
                mCamera.startDownloadRecording(fileInfo.sStartTime, filePath, fileName);
            }
        } else {
            if (handler != null) {
                handler.sendEmptyMessage(77);
            }
        }
    }
//    private void downloadRecording(int position, final HiChipDefines.HI_P2P_FILE_INFO file_infos) {
//        // HiChipDefines.HI_P2P_FILE_INFO file_infos = file_list.get(position);
//
//        if ( TwsTools.isSDCardValid()) {
//
//            File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
//            File downloadFolder = new File(TwsTools.ONLINE_VIDEO_PATH);
//            File uidFolder = new File(downloadFolder.getAbsolutePath() + "/" + mCamera.getUid() + "");
//            if (!rootFolder.exists()) {
//                rootFolder.mkdirs();
//            }
//            if (!downloadFolder.exists()) {
//                downloadFolder.mkdirs();
//            }
//            if (!uidFolder.exists()) {
//                uidFolder.mkdirs();
//            }
//
//            download_path = uidFolder.getAbsoluteFile() + "/";
//
//            // String[] time=file_info.sStartTime.toString().split(" ");
//
//            // 创建UID文件夹
//            fileName = splitFileName(file_infos.sStartTime.toString());
//
//			/*
//			 * if(oldFile.exists()){ HiToast.showToast(VideoOnlineActivity.this,
//			 * str); return; }
//			 */
//            File file = new File(download_path + fileName + ".avi");
//            File file2 = new File(download_path + fileName + ".mp4");
//
//            if (file.exists() || file2.exists()) {// 文件已下载过
//                // HiToast.showToast(VideoOnlineActivity.this,
//                // getString(R.string.tip_alear_dowm));
//                showPrompt(getString(R.string.tip_alear_dowm));
////                View view = View.inflate(VideoOnlineActivity.this, R.layout.popuwindow_aleary_down, null);
////                AlertDialog.Builder builder = new AlertDialog.Builder(VideoOnlineActivity.this);
////                final AlertDialog dialog = builder.create();
////                dialog.show();
////                dialog.setCancelable(false);
////                dialog.getWindow().setContentView(view);
////                TextView tvKnow = (TextView) dialog.findViewById(R.id.item_tv_know);
////                tvKnow.setOnClickListener(new OnClickListener() {
////                    @Override
////                    public void onClick(View v) {
////                        dialog.dismiss();
////                    }
////                });
//                return;
//            }
//            showLoadingProgress();
////			//因为下载SDK加了耗时操作,所以放在要放在异步里处理
//            new Thread() {
//                public void run() {
//                    mCamera.startDownloadRecording(file_infos.sStartTime, download_path, fileName);
//                }
//
//                ;
//            }.start();
//            //mCamera.startDownloadRecording(file_infos.sStartTime, download_path, fileName);
//        } else {
//            Toast.makeText(EventList_HichipActivity.this, getText(R.string.tips_no_sdcard).toString(), Toast.LENGTH_SHORT)
//                    .show();
//        }
//    }

    private void quit() {
        if (mCamera != null) {
            mCamera.unregisterIOTCListener(this);
            mCamera.unregisterDownloadListener(this);
            //mCamera = null;
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

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
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

    public void releaseBitmap() {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setThumb(null);
            }
        }
    }

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
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg2 = resultCode;
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    TwsToast.showToast(EventList_HichipActivity.this, getString(R.string.toast_connect_drop));
                }
            });
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        msg.arg1 = succ;
        msg.arg2 = avIOCtrlMsgType;
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

    @Override
    public void callbackDownloadState(IMyCamera var1, int total, int curSize, int state, String path) {
        Bundle bundle = new Bundle();
        bundle.putLong("total", total);
        bundle.putLong("curSize", curSize);
        bundle.putString("path", path);

        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_DOWNLOAD_STATE;
        msg.arg1 = state;
        msg.setData(bundle);
        handler.sendMessage(msg);
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
                holder.img_select = (ImageView) convertView.findViewById(R.id.img_select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (!checkMode) {
                holder.img_select.setVisibility(View.GONE);
            } else {
                holder.img_select.setSelected(evt.isSelected);
                holder.img_select.setVisibility(View.VISIBLE);
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
            if (evt.getThumb() == null) {

            }
            if (evt.downloadState == -1) {
                String videoPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_RECORD_DOWNLAND);
                String videoName = TwsTools.getFileNameWithTime(dev_uid, TwsTools.PATH_RECORD_DOWNLAND, evt.sStartTime.getTimeInMillis(), evt.EventType);
                if (new File(videoPath + "/" + videoName + ".mp4").exists() || new File(videoPath + "/" + videoName + ".avi").exists()) {
                    evt.downloadState = 1;
                } else {
                    evt.downloadState = 0;
                }
            }
            if (evt.downloadState == 1) {
                holder.img_select.setSelected(true);
                holder.img_select.setEnabled(false);
            } else {
                holder.img_select.setEnabled(true);
            }
            //holder.indicator.setVisibility(supportPlayback & evt.EventStatus != EventInfo.EVENT_NORECORD ? View.VISIBLE : View.GONE);

            String videoFilenameString = TwsTools.getFileNameWithTime(dev_uid, TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB, evt.sStartTime.getTimeInMillis(), evt.EventType);// dev_uid + "_" + evt.EventType + evt.EventTime.year + evt.EventTime.month + evt.EventTime.day + evt.EventTime.wday + evt.EventTime.hour + evt.EventTime.minute + evt.EventTime.second + ".jpg";
            String videoFullFileNamePath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB) + "/" + videoFilenameString;
            mImageFetcher.loadImage(videoFullFileNamePath, holder.img_event_image, new ImageWorker.OnImageLoadedListener() {
                @Override
                public void onImageLoaded(Object obj,boolean success) {
                    View view = (View)obj;
                    int i=3;
                    while (i>=0 && view != null){
                        view = (View)view.getParent();
                        i--;
                    }
                    ImageView img_event_type = null;
                    ImageView img_play = null;
                    if(view != null){
                        img_event_type = view.findViewById(R.id.img_event_type_image);
                        img_play = view.findViewById(R.id.img_play);
                    }
                    if(img_event_type == null || img_play == null){
                        return;
                    }
                    if(success){

                        if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
                            img_event_type.setImageResource(R.drawable.ic_motion_detection_read);
                        } else {

                            img_event_type.setImageResource(R.drawable.ic_time_record_read);
                        }
                        img_play.setVisibility(View.VISIBLE);
                    }else {
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
//            File f = new File(videoFullFileNamePath);
//            boolean hasThumb = f.exists();
//            if (hasThumb) {
//                if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
//                    holder.img_event_type_image.setImageResource(R.drawable.ic_motion_detection_read);
//                } else {
//
//                    holder.img_event_type_image.setImageResource(R.drawable.ic_time_record_read);
//                }
//                holder.img_play.setVisibility(View.VISIBLE);
//                // holder.txt_event_type.setTypeface(null, Typeface.NORMAL);
//                // holder.txt_event_type.setTextColor(0xFF999999);
//            } else {
//                if (evt.EventType == AVIOCTRL_EVENT_MOTIONDECT) {
//                    holder.img_event_type_image.setImageResource(R.drawable.ic_motion_detection_unread);
//                } else {
//                    holder.img_event_type_image.setImageResource(R.drawable.ic_time_record_unread);
//                }
//                holder.img_play.setVisibility(View.GONE);
//                // holder.txt_event_type.setTypeface(null, Typeface.BOLD);
//                // holder.txt_event_type.setTextColor(0xFF000000);
//            }
            return convertView;

        }

        private final class ViewHolder {
            public TextView txt_event_day;
            public TextView txt_event_time;
            public TextView txt_event_title;
            public TextView txt_event_type;
            public ImageView img_event_image;
            public ImageView img_event_type_image;
            public ImageView img_select;
        }
    }// EventListAdapter


    public static String getStringDateShort(Date currentTime) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
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
                //批量下载结束
                case 77:
                    if (dlgBuilder != null) {
                        dlgBuilder.dismiss();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            showAlertnew(android.R.drawable.ic_dialog_info, getString(R.string.prompt), getString(R.string.dialog_msg_download_complete), getString(R.string.ok), getString(R.string.btn_toView), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == DialogInterface.BUTTON_NEGATIVE) {
                                    } else if (i == DialogInterface.BUTTON_POSITIVE) {

                                        Bundle bundle = new Bundle();
                                        bundle.putString(TwsDataValue.EXTRA_KEY_UID, mCamera.getUid());
                                        bundle.putBoolean("goto", true);
                                        Intent intent = new Intent(EventList_HichipActivity.this, CameraFolderActivity.class);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
                                    }
                                }
                            });
                        }
                    });
                    break;
                case TwsDataValue.HANDLE_MESSAGE_IO_RESP:
                    switch (msg.arg2) {
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
                    break;
                case TwsDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    switch (msg.arg2) {
                        case TwsSessionState.CONNECTION_STATE_CONNECTING:
                            break;
                        case TwsSessionState.CONNECTION_STATE_WRONG_PASSWORD:
                        case TwsSessionState.CONNECTION_STATE_CONNECT_FAILED:
                        case TwsSessionState.CONNECTION_STATE_DISCONNECTED:
                        case TwsSessionState.CONNECTION_STATE_UNKNOWN_DEVICE:
                        case TwsSessionState.CONNECTION_STATE_TIMEOUT:

                            if (eventListView.getAdapter() != null && eventListView.getFooterViewsCount() == 0) {
                                txt_event_day_top.setVisibility(View.GONE);
                                releaseBitmap();
                                list.clear();
                                eventListView.addFooterView(offlineView);
                                adapter.notifyDataSetChanged();
                            }

                            break;

                        case TwsSessionState.CONNECTION_STATE_CONNECTED:

                            if (sessionChannel == 0 && eventListView.getAdapter() != null) {
                                eventListView.removeFooterView(offlineView);
                                adapter.notifyDataSetChanged();
                            }

                            break;
                    }
                    break;


                case TwsDataValue.HANDLE_MESSAGE_DOWNLOAD_STATE:
                    handDownLoad(msg);
                    break;
            }

            super.handleMessage(msg);
        }
    };

    private void handDownLoad(Message msg) {
        Bundle bundle = msg.getData();
        switch (msg.arg1) {
            case DOWNLOAD_STATE_START:
                isDownloading = true;
                path = bundle.getString("path");
                if (popu_tips_down != null) {
                    popu_tips_down.setText(String.format(getString(R.string.tips_downlaoding_video).toString(), "" + (downloadIndex + 1) + "/" + downloadlist.size()));
                }
                if (txt_downloadfile != null) {
                    txt_downloadfile.setText(downloadlist.get(downloadIndex).sStartTime.toString());
                }
                break;
            case DOWNLOAD_STATE_DOWNLOADING:
                if (isDownloading == false) {
                    return;
                }
                float d;
                long total = bundle.getLong("total");
                if (total == 0) {
                    d = bundle.getLong("curSize") * 100 / (1024 * 1024);
                } else {
                    d = bundle.getLong("curSize") * 100 / total;
                }
                if (d >= 100) {
                    d = 99;
                }
                // int rate = Math.round(d);
                int rate = (int) d;
                String rateStr = "";
                if (rate < 10) {
                    rateStr = " " + rate + "%";
                } else {
                    rateStr = rate + "%";
                }
                prs_loading.setProgress(rate);

                rate_loading_video.setText(rateStr);

                break;
            case DOWNLOAD_STATE_END:
                prs_loading.setProgress(100);
                rate_loading_video.setText(100 + "%");
                isDownloading = false;
//                cancel_btn_downloading_video.setText(R.string.ok);
//                goto_btn_downloading_video.setVisibility(View.VISIBLE);
//                popu_tips_down.setText(getString(R.string.tips_down_file_route));

                downloadlist.get(downloadIndex).downloadState = 1;
//                String thumbPath = TwsTools.getFilePath(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB);
//                String thumbFileNanme = TwsTools.getFileNameWithTime(mCamera.getUid(), TwsTools.PATH_SNAPSHOT_PLAYBACK_AUTOTHUMB, downloadlist.get(downloadIndex).sStartTime.getTimeInMillis(), downloadlist.get(downloadIndex).EventType);
//                File thumb = new File(thumbPath + "/" + thumbFileNanme);
//                if (!thumb.exists()) {
//                    Bitmap bmp = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MINI_KIND);
//                    if(bmp != null) {
//                        TwsTools.saveBitmap(bmp, thumbPath + "/" + thumbFileNanme);
//                        bmp.recycle();
//                        bmp = null;
//                        System.gc();
//                    }
//                }
                downloadIndex++;
                downloadSingle();

                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
                break;
            case DOWNLOAD_STATE_ERROR_PATH:
                showAlert(getResources().getString(R.string.toast_connect_drop));
                if (dlgBuilder != null) {
                    dlgBuilder.dismiss();
                }
                dlgBuilder = null;
                break;
            case DOWNLOAD_STATE_ERROR_DATA:
                // +++
                if (mCamera != null && isDownloading) {
                    mCamera.stopDownloadRecording();
                    isDownloading = false;
                    mCamera.disconnect();
                    mCamera.connect();
                }
                break;

        }
    }

    private AlertDialog dlgBuilder;
    private SeekBar prs_loading;
    private TextView rate_loading_video;
    private TextView txt_downloadfile;
    private AlertDialog.Builder dlg;
    private Button cancel_btn_downloading_video, goto_btn_downloading_video;
    private TextView popu_tips_down;
    private HiChipDefines.HI_P2P_FILE_INFO evt;

    protected void showingProgressDialog() {
        View customView = getLayoutInflater().inflate(R.layout.hint_download_record, null, false);
        dlg = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        dlgBuilder = dlg.create();
        dlgBuilder.setView(customView);
        dlgBuilder.setCancelable(false);
        // dlgBuilder.setOnKeyListener(new OnKeyListener() {
        //
        // @Override
        // public boolean onKey(DialogInterface arg0, int keyCode, KeyEvent
        // keyEvent) {
        // switch (keyCode) {
        // case KeyEvent.KEYCODE_BACK:
        // if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
        // cancelDownloadVideo();
        // }
        //
        // break;
        // }
        // return true;
        // }
        //
        // });

		/*
         * mPopupWindow = new PopupWindow(customView); ColorDrawable cd = new
		 * ColorDrawable(-0000); mPopupWindow.setBackgroundDrawable(cd);
		 * mPopupWindow.setOutsideTouchable(false);
		 * mPopupWindow.setFocusable(true);
		 * mPopupWindow.setWidth(LayoutParams.MATCH_PARENT);
		 * mPopupWindow.setHeight(LayoutParams.MATCH_PARENT);
		 */

        dlgBuilder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface d) {
                isDownloading = false;
                if (mCamera != null) {
                    mCamera.stopDownloadRecording();
                }
            }
        });

        prs_loading = (SeekBar) customView.findViewById(R.id.sb_downloading_video);
        prs_loading.setEnabled(false);
        prs_loading.setMax(100);
        prs_loading.setProgress(0);

        rate_loading_video = (TextView) customView.findViewById(R.id.rate_loading_video);
        txt_downloadfile = (TextView) customView.findViewById(R.id.txt_downloadfile);
        popu_tips_down = (TextView) customView.findViewById(R.id.popu_tips_down);
        if (popu_tips_down != null) {
            popu_tips_down.setText(String.format(getString(R.string.tips_downlaoding_video).toString(), "" + (downloadIndex + 1) + "/" + downloadlist.size()));
        }
        if (txt_downloadfile != null) {
            txt_downloadfile.setText(downloadlist.get(downloadIndex).sStartTime.toString());
        }
        cancel_btn_downloading_video = (Button) customView.findViewById(R.id.cancel_btn_downloading_video);
        cancel_btn_downloading_video.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // 显示取消下载的对话框
                // cancelDownloadVideo();
                if (getString(R.string.ok)
                        .equals(cancel_btn_downloading_video.getText().toString().trim())) {
                    dlgBuilder.dismiss();
                } else {
                    dlgBuilder.dismiss();
                    deleteLoadingFile();
                    adapter.notifyDataSetChanged();
                }
            }
        });

        dlgBuilder.show();
        WindowManager m = getWindowManager();
        Display d = m.getDefaultDisplay();  //为获取屏幕宽、高
        android.view.WindowManager.LayoutParams p = dlgBuilder.getWindow().getAttributes();  //获取对话框当前的参数值
        //p.height = (int) (d.getHeight() * 0.3);   //高度设置为屏幕的0.3
        p.width = (int) (d.getWidth() * 0.9);    //宽度设置为屏幕的0.8
        dlgBuilder.getWindow().setAttributes(p);     //设置生效
    }

    private void deleteLoadingFile() {
        if (path != null) {
            File file = new File(path);
            file.delete();
        }
    }


    public void showPrompt(CharSequence message) {

        AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(this);
        dlgBuilder.setIcon(android.R.drawable.ic_dialog_info);
        dlgBuilder.setTitle(R.string.prompt);
        dlgBuilder.setMessage(message);
        dlgBuilder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).show();
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
        View view = inflater.inflate(R.layout.view_search_event_custom, null);
        dlg.setView(view);

        final Button btnStartDate = (Button) view.findViewById(R.id.btnStartDate);
        final Button btnStartTime = (Button) view.findViewById(R.id.btnStartTime);
        final Button btnStopDate = (Button) view.findViewById(R.id.btnStopDate);
        final Button btnStopTime = (Button) view.findViewById(R.id.btnStopTime);
        Button btnOK = (Button) view.findViewById(R.id.btnOK);
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        // set button
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
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
        public boolean isSelected;
        public int downloadState;

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
            downloadState = -1;
        }
    }

    void setToolBarVisible() {
        checkMode = !checkMode;
        title.setRightBtnText(getString(checkMode ? R.string.done : R.string.edit));
        EventList_HichipActivity.this.findViewById(R.id.ll_toolbar_bottom).setVisibility(checkMode ? View.VISIBLE : View.GONE);
        if (findViewById(R.id.img_selectall).isSelected()) {
            findViewById(R.id.img_selectall).performClick();
        } else {
            if (!checkMode && list != null) {
                for (int i = 0; i < list.size(); i++) {
                    list.get(i).isSelected = false;
                }
            }
            adapter.notifyDataSetChanged();
        }
    }
}
