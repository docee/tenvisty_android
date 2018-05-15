package com.tws.commonlib.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.tws.commonlib.R;
import com.tws.commonlib.adapter.DateScrollItemListAdapter;
import com.tws.commonlib.adapter.LocalPicItemListAdapter;
import com.tws.commonlib.base.DateScrollItem;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.LocalPichModel;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;
import com.tws.commonlib.controller.SpinnerButton;
import com.tws.commonlib.ui.ImageDetailActivity;
import com.tws.commonlib.util.ImageCache;
import com.tws.commonlib.util.ImageFetcher;
import com.tws.commonlib.util.Utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class CameraFolderActivity extends BaseActivity {

    private String dev_uid;
    private String imagesPath;
    private String videosPath;
    private String downloadVideosPath;
    private ListView listviewItemList;
    private DateScrollItemListAdapter adapter;
    int firstVisibleItem;
    int accSelect = -1;
    IMyCamera mCamera;
    private ImageFetcher mImageFetcher;
    private static final String IMAGE_CACHE_DIR = "thumbs";

    private int mImageThumbSize;
    private int mImageThumbSpacing;

    private String getImagesPath() {
        if (imagesPath == null) {
            dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
            imagesPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_SNAPSHOT_MANUALLY);
        }
        return imagesPath;
    }

    private String getVideosPath() {
        if (videosPath == null) {
            dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
            videosPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_RECORD_MANUALLY);
        }
        return videosPath;
    }

    private String getDownloadVideosPath() {
        if (downloadVideosPath == null) {
            dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
            downloadVideosPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_RECORD_DOWNLAND);
        }
        return downloadVideosPath;
    }

    private LinearLayout ll_videos;
    private LinearLayout ll_photos;
    private LinearLayout ll_no_photos;
    private LinearLayout ll_no_videos;
    private LinearLayout ll_first_top;
    /**
     * The Constant DEFAULT_LIST_SIZE.
     */
    private static final int DEFAULT_LIST_SIZE = 20;

    final List<DateScrollItem> adapterSource = new ArrayList<DateScrollItem>();
    NavigationBar title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        needConnect = false;
        super.onCreate(savedInstanceState);
        //System.gc();
        setContentView(R.layout.activity_camera_folder);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        //鏍规嵁浼犺繃鏉ョ殑UID鎵惧埌鐩稿簲鐨凜amera
        for (IMyCamera camera : TwsDataValue.cameraList()) {
            if (dev_uid.equalsIgnoreCase(camera.getUid())) {
                mCamera = camera;// 鎵惧埌camera
                break;
            }
        }
        initImageFetcher();
        initView();
        //.initSDK(this);
    }


    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        renderData();
    }

    @Override
    public void onPause() {
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }


    void initImageFetcher() {

        mImageThumbSize = TwsTools.dip2px(this, 100);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = TwsTools.dip2px(this, 5);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(this, IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(this, mImageThumbSize);
        DisplayMetrics dm = new DisplayMetrics();
        (this).getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = (dm.widthPixels - TwsTools.dip2px(this, 80)) / 3;
        int height = (int) (width / camera.getVideoRatio(this));
        mImageFetcher.setImageSize(width, height);
        mImageFetcher.setLoadingImage(R.drawable.default_img);
        mImageFetcher.addImageCache(this.getSupportFragmentManager(), cacheParams);
    }

    private void renderData() {
        // if (which == 0) {
        refreshSource();
        if (adapterSource.size() > 0) {
            listviewItemList.setVisibility(View.VISIBLE);
        } else {
            listviewItemList.setVisibility(View.GONE);
            if (accSelect == 1) {
                ll_no_videos.setVisibility(View.VISIBLE);
                ll_no_photos.setVisibility(View.GONE);
            } else {
                ll_no_videos.setVisibility(View.GONE);
                ll_no_photos.setVisibility(View.VISIBLE);
            }
        }
        firstVisibleItem = 0;
        if (adapterSource.size() > 0) {
            ll_first_top.setVisibility(View.VISIBLE);
        } else {
            ll_first_top.setVisibility(View.GONE);
        }
        setToolBarVisible();
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initView() {
        title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle(mCamera.getNickName());
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT);
        title.setRightBtnText(getString(R.string.edit));
        title.setRightBtnBackground(0);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        if (adapter != null && adapter.isCheckMode()) {
                            adapter.setCheckMode(false);
                        } else {
                            finish();
                        }
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        if (adapter != null) {
                            adapter.setCheckMode(!adapter.isCheckMode());
                        }
                        setToolBarVisible();
                        break;
                }
            }
        });
        ll_no_videos = (LinearLayout) findViewById(R.id.ll_no_videos);
        ll_no_photos = (LinearLayout) findViewById(R.id.ll_no_photos);
        ll_first_top = (LinearLayout) findViewById(R.id.ll_first_top);
        SpinnerButton spinnerButton = (SpinnerButton) findViewById(R.id.spinner_type);
        spinnerButton.setTitles(new String[]{getString(R.string.spinner_photos), getString(R.string.spinner_videos)});
        if (this.getIntent().getBooleanExtra("goto", false)) {
            accSelect = 1;
            spinnerButton.Click(1);
        } else {
            accSelect = 0;
            spinnerButton.Click(0);
        }
        spinnerButton.setSpinnerButtonListener(new SpinnerButton.SpinnerButtonListener() {
            @Override
            public void OnSpinnerButtonClick(int which) {
                if (accSelect == which) {
                    return;
                }
                adapter.setCheckMode(false);
                accSelect = which;
                renderData();
            }
        });

        CameraFolderActivity.this.findViewById(R.id.img_group_check).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (adapterSource.size() > firstVisibleItem) {
                    DateScrollItem model = adapterSource.get(firstVisibleItem);
                    model.checked = !model.checked;
                    model.img_group_check.setSelected(model.checked);
                    // model.img_group_check.setImageResource(model.checked ? R.drawable.ic_check_circle_24dp_checked : R.drawable.ic_check_circle_24dp_unchecked);
                    LocalPicItemListAdapter picAdapter = (LocalPicItemListAdapter) model.subAdatper;
                    ((ImageView) view).setImageResource(model.checked ? R.drawable.ic_check_circle_24dp_checked : R.drawable.ic_check_circle_24dp_unchecked);
                    for (int i = 0; i < picAdapter.sourceItemList.size(); i++) {
                        LocalPichModel m = picAdapter.sourceItemList.get(i);
                        m.checked = model.checked;
                        picAdapter.checkPicList.get(i).setEnabled(m.checked);
                    }
                    picAdapter.notifyDataSetChanged();
                }
            }
        });
        adapter = new DateScrollItemListAdapter(CameraFolderActivity.this, adapterSource);

        adapter.setImageFetcher(mImageFetcher);
        adapter.setStateChangedListner(new DateScrollItemListAdapter.onStateChangedListner() {
            @Override
            public void onCheck(boolean b) {
                setToolBarVisible();
            }
        });
        adapter.setOnclickListner(new DateScrollItemListAdapter.onItemClickLinstener() {
            @Override
            public void onClick(View view, int position, int subPosition, long viewid) {
                if (!adapter.isCheckMode()) {
                    String fileName = ((LocalPichModel) ((DateScrollItem) adapter.getItem(position)).subAdatper.getItem(subPosition)).path;
                    if (accSelect == 0) {
                        Intent intent = new Intent(CameraFolderActivity.this, PhotoShowActivity.class);
                        intent.putExtra("filename", fileName);
                        intent.putExtra("dir", getImagesPath());
                        if (Utils.hasJellyBean()) {
                            // makeThumbnailScaleUpAnimation() looks kind of ugly here as the loading spinner may
                            // show plus the thumbnail image in GridView is cropped. so using
                            // makeScaleUpAnimation() instead.
                            ActivityOptions options = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
                            CameraFolderActivity.this.startActivity(intent, options.toBundle());
                        } else {
                            startActivity(intent);
                        }
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            playbackInAndroid7(fileName);
                        } else {
                            playbackRecording(fileName);
                        }
                    }
                }
            }
        });
        listviewItemList = (ListView) findViewById(R.id.listviewItemList);

        listviewItemList.setAdapter(adapter);
        listviewItemList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int _firstVisibleItem, int visibleItemCount, int totalItemCount) {
                firstVisibleItem = _firstVisibleItem;
                if (!adapterSource.isEmpty()) {
                    DateScrollItem model = adapterSource.get(_firstVisibleItem);
                    ((TextView) CameraFolderActivity.this.findViewById(R.id.txt_date)).setText(adapterSource.get(firstVisibleItem).date);
                    ((TextView) CameraFolderActivity.this.findViewById(R.id.txt_title)).setText(adapterSource.get(firstVisibleItem).title);
                    ((ImageView) findViewById(R.id.img_group_check)).setSelected(model.checked);
                    //((ImageView) findViewById(R.id.img_group_check)).setImageResource(model.checked ? R.drawable.ic_check_circle_24dp_checked : R.drawable.ic_check_circle_24dp_unchecked);
                }
                // TwsToast.showToast(EventListActivity.this, "firstVisibleItem:" + firstVisibleItem + ",visibleItemCount:" + visibleItemCount + ",totalItemCount:" + totalItemCount);
                if (firstVisibleItem == 0) {
//                        Log.d("ListView", "##### 滚动到顶部 #####");
                } else if ((firstVisibleItem + visibleItemCount) == totalItemCount) {
//                        Log.d("ListView", "##### 滚动到底部 ######");
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
//        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            ((ImageView)(findViewById(R.id.img_delete))).setImageTintList(CameraFolderActivity.this.getColorStateList(R.color.select_tint_color));
//        }
//        //}
    }

    void setToolBarVisible() {
//        if (accSelect == 1) {
//            CameraFolderActivity.this.findViewById(R.id.ll_toolbar_bottom).findViewById(R.id.ll_share).setVisibility(View.GONE);
//        } else {
        CameraFolderActivity.this.findViewById(R.id.ll_toolbar_bottom).findViewById(R.id.ll_share).setVisibility(View.VISIBLE);
        //}

        title.setRightBtnText(getString(adapter.isCheckMode() ? R.string.done : R.string.edit));
        CameraFolderActivity.this.findViewById(R.id.img_group_check).setVisibility(adapter.isCheckMode() ? View.VISIBLE : View.INVISIBLE);
        CameraFolderActivity.this.findViewById(R.id.ll_toolbar_bottom).setVisibility(adapter.isCheckMode() ? View.VISIBLE : View.GONE);
    }

    private void playbackInAndroid7(String path) {

        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);
        File file = new File(path);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri contentUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".FileProvider",
                file);
        intent.setDataAndType(contentUri, "video/*");
        startActivity(intent);
    }

    private void playbackRecording(String path) {
        Intent it = new Intent(Intent.ACTION_VIEW);
        String bpath = "file://" + path;
        Uri uri = Uri.parse(bpath);
        it.setDataAndType(uri, "video/*");
        Log.v("", "bpath:" + uri);
        startActivity(it);
    }

    public void doClickLL(View view) {

    }

    public void toolbar_click(View view) {
        if (view.getId() == R.id.img_share) {
            final List<LocalPichModel> deleteFiles = new ArrayList<LocalPichModel>();
            for (DateScrollItem item : adapterSource) {
                for (LocalPichModel pic : ((LocalPicItemListAdapter) (item.subAdatper)).sourceItemList) {
                    if (pic.checked) {
                        deleteFiles.add(pic);
                    }
                }
            }
            String[] pics = new String[deleteFiles.size()];
            for (int i = 0; i < deleteFiles.size(); i++) {
                LocalPichModel m = deleteFiles.get(i);
                pics[i] = m.path;
            }
            if (deleteFiles.size() > 0) {
                String[] paths = new String[deleteFiles.size()];
                for (int i = 0; i < deleteFiles.size(); i++) {
                    paths[i] = deleteFiles.get(i).path;
                }
                TwsTools.showShare(CameraFolderActivity.this, false, null, false, null, paths);
            }
        } else if (view.getId() == R.id.img_delete) {
            final List<LocalPichModel> deleteFiles = new ArrayList<LocalPichModel>();
            for (DateScrollItem item : adapterSource) {
                for (LocalPichModel pic : ((LocalPicItemListAdapter) (item.subAdatper)).sourceItemList) {
                    if (pic.checked) {
                        deleteFiles.add(pic);
                    }
                }
            }
            if (deleteFiles.size() > 0) {
                showYesNoDialog(R.string.dialog_msg_delete_local_pic_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                for (int j = 0; j < adapterSource.size(); j++) {
                                    DateScrollItem item = adapterSource.get(j);
                                    LocalPicItemListAdapter subAdapter = ((LocalPicItemListAdapter) (item.subAdatper));
                                    boolean hasDelete = false;
                                    for (int k = 0; k < subAdapter.sourceItemList.size(); k++) {
                                        LocalPichModel pic = subAdapter.sourceItemList.get(k);
                                        if (pic.checked) {
                                            File f = new File(pic.path);
                                            if (f.exists()) {
                                                f.delete();
                                            }
                                            if (pic.thumbPath != null && pic.thumbPath != pic.path) {
                                                File fThumb = new File(pic.thumbPath);
                                                if (fThumb.exists()) {
                                                    fThumb.delete();
                                                }
                                            }
                                            subAdapter.sourceItemList.remove(pic);
                                            k--;
                                            subAdapter.notifyDataSetChanged();
                                        }
                                    }
                                    if (subAdapter.sourceItemList.size() == 0) {
//                                        if(((ImageView) findViewById(R.id.img_group_check)).isSelected()){
//                                            ((ImageView) findViewById(R.id.img_group_check)).setSelected(false);
//                                        }
                                        adapterSource.remove(item);
                                        j--;
                                        adapter.notifyDataSetChanged();
                                    }
                                }
                                if (adapterSource.size() > 0) {
                                    listviewItemList.setVisibility(View.VISIBLE);
                                    ll_first_top.setVisibility(View.VISIBLE);
                                } else {
                                    adapter.setCheckMode(false);
                                    setToolBarVisible();
                                    ll_first_top.setVisibility(View.GONE);
                                    listviewItemList.setVisibility(View.GONE);
                                    if (accSelect == 1) {
                                        ll_no_videos.setVisibility(View.VISIBLE);
                                        ll_no_photos.setVisibility(View.GONE);
                                    } else {
                                        ll_no_videos.setVisibility(View.GONE);
                                        ll_no_photos.setVisibility(View.VISIBLE);
                                    }
                                }
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                break;
                        }
                    }
                });
            } else {
                showAlert(getString(R.string.alert_delete_pic_none));
            }
        }
    }

    void refreshSource() {
        adapterSource.clear();
        String dir = null;
        String ends = null;
        if (accSelect == 0) {
            dir = getImagesPath();
            ends = ".jpg";
        } else if (accSelect == 1) {
            dir = getVideosPath();
            ends = ".mp4";
        }
        File folder = new File(dir);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File f : files) {
                    //下载的录像
                    if (f.isDirectory()) {
                        File[] pics = f.listFiles();
                        for (File pic : pics) {
                            String fileName = pic.getName();
                            if (fileName.endsWith(ends)) {
                                try {
                                    String[] paras = fileName.split("_");
                                    String date = paras[1].substring(4, 6) + "/" + paras[1].substring(6, 8);
                                    String title = paras[1].substring(0, 8);
                                    Date time = TwsTools.Str2Date(paras[1], null);
                                    int type = 2;
                                    DateScrollItem sItem = findSouceItem(time, type);
                                    if (sItem == null) {
                                        LocalPicItemListAdapter adapter = new LocalPicItemListAdapter(this, new ArrayList<LocalPichModel>(), this.mCamera.getVideoRatio(CameraFolderActivity.this), type);
                                        adapter.checkMode = CameraFolderActivity.this.adapter.isCheckMode();
                                        adapter.setImageFetcher(mImageFetcher);
                                        sItem = new DateScrollItem(time, date, title, adapter, type);
                                        adapterSource.add(sItem);
                                    }
                                    sItem.isRemoteRecord = true;
                                    ((LocalPicItemListAdapter) sItem.subAdatper).addSourceItem(pic.getAbsolutePath());
                                } catch (Exception ex) {

                                }
                            }
                        }
                    }
                    //手动抓拍、录像
                    else {
                        String fileName = f.getName();
                        if (fileName.endsWith(ends)) {
                            try {
                                String[] paras = f.getName().split("_");
                                String date = paras[1].substring(4, 6) + "/" + paras[1].substring(6, 8);
                                String title = paras[1].substring(0, 8);
                                Date time = TwsTools.Str2Date(paras[1], null);
                                int type = paras[1].endsWith(".mp4") ? 1 : 0;
                                DateScrollItem sItem = findSouceItem(time, type);
                                if (sItem == null) {
                                    LocalPicItemListAdapter adapter = new LocalPicItemListAdapter(this, new ArrayList<LocalPichModel>(), this.mCamera.getVideoRatio(CameraFolderActivity.this), type);
                                    adapter.setImageFetcher(mImageFetcher);
                                    adapter.checkMode = CameraFolderActivity.this.adapter.isCheckMode();
                                    sItem = new DateScrollItem(time, date, title, adapter, type);
                                    adapterSource.add(sItem);
                                }
                                ((LocalPicItemListAdapter) sItem.subAdatper).addSourceItem(f.getAbsolutePath());
                            } catch (Exception ex) {

                            }
                        }
                    }
                }
            }
        }
        for (DateScrollItem item : adapterSource) {
            Collections.sort(((LocalPicItemListAdapter) item.subAdatper).sourceItemList, new Comparator<LocalPichModel>() {
                @Override
                public int compare(LocalPichModel localPichModel, LocalPichModel t1) {
                    return -localPichModel.path.compareTo(t1.path);
                    // return 0;
                }
            });
        }
        Collections.sort(adapterSource, new Comparator<DateScrollItem>() {
            @Override
            public int compare(DateScrollItem dateScrollItem, DateScrollItem t1) {
                return -dateScrollItem.time.compareTo(t1.time);
            }
        });
    }

    DateScrollItem findSouceItem(Date date, int type) {
        DateScrollItem result = null;
        for (DateScrollItem item : adapterSource) {
            if (item.type == type && TwsTools.Date2Str(item.time, "yyyyMMdd").equals(TwsTools.Date2Str(date, "yyyyMMdd"))) {
                result = item;
                break;
            }
        }
        return result;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
            case RESULT_OK:
                //accSelect = -1;
                //renderData();
                //imageAdapter.notifyDataSetChanged();
                break;

            default:
                break;
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (adapter != null && adapter.isCheckMode()) {
                adapter.setCheckMode(false);
                title.setRightBtnText(getString(adapter.isCheckMode() ? R.string.done : R.string.edit));
                return true;
            } else {
                CameraFolderActivity.this.finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

}
