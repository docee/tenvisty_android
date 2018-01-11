package com.tws.commonlib.fragment;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tws.commonlib.BuildConfig;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.base.FolderInfoModel;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.ui.RecyclingImageView;
import com.tws.commonlib.util.ImageCache;
import com.tws.commonlib.util.ImageFetcher;
import com.tws.commonlib.util.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FolderFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private View view;
    private final List<FolderInfoModel> sourceList = new ArrayList<FolderInfoModel>();
    private boolean isInit = false;
    private ImageFetcher mImageFetcher;
    private  PictureListAdapter mAdapter;
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private static final String TAG = "FolderFragment";
    public boolean isInited() {
        return isInit;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = TwsTools.dip2px(getActivity(),100);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = TwsTools.dip2px(getActivity(),1);// getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        mAdapter = new PictureListAdapter(getActivity());

        ImageCache.ImageCacheParams cacheParams =
                new ImageCache.ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);

        mImageFetcher.setImageSize(TwsTools.dip2px(getActivity(),100),TwsTools.dip2px(getActivity(),60));
        mImageFetcher.setLoadingImage(R.drawable.default_img);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);
        mImageFetcher.clearCache();
    }
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_folder, null);
            final ListView picture_fragment_camera_list = (ListView) view.findViewById(R.id.picture_fragment_camera_list);
            picture_fragment_camera_list.setAdapter(mAdapter);
            picture_fragment_camera_list.setOnItemClickListener(this);
            picture_fragment_camera_list.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    // Pause fetcher to ensure smoother scrolling when flinging
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                        // Before Honeycomb pause image loading on scroll to help with performance
                        if (!Utils.hasHoneycomb()) {
                            mImageFetcher.setPauseWork(true);
                        }
                    } else {
                        mImageFetcher.setPauseWork(false);
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                }
            });

            // This listener is used to get the final width of the GridView and then calculate the
            // number of columns and the width of each column. The width of each column is variable
            // as the GridView has stretchMode=columnWidth. The column width is used to set the height
            // of each view so we get nice square thumbnails.
//            picture_fragment_camera_list.getViewTreeObserver().addOnGlobalLayoutListener(
//                    new ViewTreeObserver.OnGlobalLayoutListener() {
//                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
//                        @Override
//                        public void onGlobalLayout() {
//                            if (mAdapter.getNumColumns() == 0) {
//                                final int numColumns = (int) Math.floor(
//                                        mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
//                                if (numColumns > 0) {
//                                    final int columnWidth =
//                                            (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
//                                    mAdapter.setNumColumns(numColumns);
//                                    mAdapter.setItemHeight(columnWidth);
//                                    if (BuildConfig.DEBUG) {
//                                        Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
//                                    }
//                                    if (Utils.hasJellyBean()) {
//                                        picture_fragment_camera_list.getViewTreeObserver()
//                                                .removeOnGlobalLayoutListener(this);
//                                    } else {
//                                        picture_fragment_camera_list.getViewTreeObserver()
//                                                .removeGlobalOnLayoutListener(this);
//                                    }
//                                }
//                            }
//                        }
//                    });
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        isInit = true;
        return view;
    }

    private String getImagesPath(String dev_uid) {
        String imagesPath = null;
        if (imagesPath == null) {
            imagesPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_SNAPSHOT_MANUALLY);
        }
        return imagesPath;
    }

    private String getAutoThumbFile(String dev_uid) {
        String imagesPath = null;
        if (imagesPath == null) {
            imagesPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB) + "/" + TwsTools.getFileNameWithTime(dev_uid, TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB);
        }
        return imagesPath;
    }

    private String getVideosPath(String dev_uid) {
        String videosPath = null;
        if (videosPath == null) {
            videosPath = TwsTools.getFilePath(dev_uid, TwsTools.PATH_RECORD_MANUALLY);
        }
        return videosPath;
    }

    FolderInfoModel processFolderInfo(IMyCamera camera) {
        String thumbPath = null;
        int photoCount = 0;
        int videoCount = 0;
        File photoFolder = new File(getImagesPath(camera.getUid()));
        if (photoFolder.exists()) {
            for(File pic : photoFolder.listFiles()){
                String strFileName = pic.getName();
                if(strFileName.length() >= 36 && strFileName.endsWith(".jpg")){
                    photoCount++;
                    if(thumbPath == null) {
                        thumbPath = pic.getAbsolutePath();
                    }
                }
            }
        }
        if(thumbPath == null){
            String autoThumFile = getAutoThumbFile(camera.getUid());
            File fThumb = new File(autoThumFile);
            if(fThumb.exists()){
                thumbPath = autoThumFile;
            }
        }
        File videoFolder = new File(getVideosPath(camera.getUid()));
        if (videoFolder.exists()) {
            for(File fVideo : videoFolder.listFiles()){
                if(fVideo.isFile()) {
                    String strFileName = fVideo.getName();
                    if(strFileName.length() >= 36) {
                        if (strFileName.endsWith(".mp4")) {
                            videoCount++;
                        } else if (thumbPath == null && strFileName.endsWith(".jpg")) {
                            thumbPath = fVideo.getAbsolutePath();
                        }
                    }
                }
                else{
                    for(File fRemoteVideo : fVideo.listFiles()){
                        if(fRemoteVideo.isFile()) {
                            String strFileName = fRemoteVideo.getName();
                            if(strFileName.length() >= 36) {
                                if (strFileName.endsWith(".mp4")) {
                                    videoCount++;
                                } else if (thumbPath == null && strFileName.endsWith(".jpg")) {
                                    thumbPath = fVideo.getAbsolutePath();
                                }
                            }
                        }
                    }
                }
            }
        }
        FolderInfoModel m = new FolderInfoModel(thumbPath, camera.getUid(), camera.getNickName(), photoCount, videoCount);
        return m;
    }

    public void initView() {
        sourceList.clear();
        for (IMyCamera camera : TwsDataValue.cameraList()) {
            sourceList.add(processFolderInfo(camera));
        }

        if (TwsDataValue.cameraList().size() > 0) {
            view.findViewById(R.id.txt_nocamera).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.txt_nocamera).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
        FolderInfoModel m = sourceList.get(position);
//                if (m.videoCount + m.photoCount == 0) {
//                    TwsToast.showToast(FolderFragment.this.getContext(), getString(R.string.tips_no_photo_video));
//                } else {
        Bundle extras = new Bundle();
        extras.putString(TwsDataValue.EXTRA_KEY_UID, m.uid);
        Intent intent = new Intent();
        intent.putExtras(extras);
        intent.setClass(getActivity(), CameraFolderActivity.class);
        startActivity(intent);
    }


    protected class PictureListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        //	public VideoListAdapter(LayoutInflater layoutInflater) {
        //		this.mInflater = layoutInflater;
        //	}

        public PictureListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);

            //		this.mContext = context;
            //		this.mInflater = layoutInflater;
        }

        @Override
        public int getCount() {

            return sourceList.size();
        }

        @Override
        public Object getItem(int position) {

            return sourceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressLint("InflateParams")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            final FolderInfoModel model = sourceList.get(position);
            if (model == null)
                return null;

            ViewHolder holder = null;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.view_list_video_camera, null);
                holder = new ViewHolder();
                //			holder.img = (ImageView) convertView.findViewById(R.id.img);
                holder.txt_name = (TextView) convertView.findViewById(R.id.txt_name);
                holder.txt_count = (TextView) convertView.findViewById(R.id.txt_count);
                holder.img_snap = (RecyclingImageView) convertView.findViewById(R.id.img_snap);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (holder != null) {
                holder.txt_name.setText(model.cameraName);
                holder.txt_count.setText(String.format(getString(R.string.tips_folder_desc), model.photoCount + "", model.videoCount + ""));
                mImageFetcher.loadImage(model.thumbPath, holder.img_snap);

            }

            return convertView;
        }

        public final class ViewHolder {
            //		public ImageView img;
            public TextView txt_name;
            public TextView txt_count;
            public RecyclingImageView img_snap;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        initView();
        mAdapter.notifyDataSetChanged();
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

}
