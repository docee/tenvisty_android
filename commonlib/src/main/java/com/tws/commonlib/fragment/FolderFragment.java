package com.tws.commonlib.fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tws.commonlib.R;
import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.base.FolderInfoModel;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class FolderFragment extends BaseFragment {
    private View view;
    private final List<FolderInfoModel> sourceList = new ArrayList<FolderInfoModel>();
    private boolean isInit = false;

    public boolean isInited() {
        return isInit;
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_folder, null);
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
        File[] photos = null;
        if (photoFolder.exists()) {
            photos = photoFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || ((file.getName().length() == 36) || (file.getName().length() == 39));
                }
            });
            if (photos != null) {
                photoCount = photos.length;
            } else {
                photoCount = 0;
            }
        } else {
            photoCount = 0;
        }
        File[] videos = null;
        File videoFolder = new File(getVideosPath(camera.getUid()));
        if (videoFolder.exists()) {
            videos = videoFolder.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    return file.isDirectory() || ((file.getName().length() == 36) || (file.getName().length() == 39));
                }
            });
            if (videos != null) {
                videoCount = videos.length;
            } else {
                videoCount = 0;
            }
        } else {
            videoCount = 0;
        }
        if (photoCount > 0) {
            thumbPath = photos[0].getAbsolutePath();
        } else if (videoCount > 0) {
            for (File v : videos) {
                if (new File(v.getAbsoluteFile() + ".jpg").exists()) {
                    thumbPath = v.getAbsoluteFile() + ".jpg";
                    break;
                }
            }
            if (thumbPath == null) {
                final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videos[0].getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
                if (bitmap != null) {
                    try {
                        String snapFile = videos[0].getAbsolutePath() + ".jpg";
                        FileOutputStream fos = new FileOutputStream(snapFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                        fos.flush();
                        fos.close();
                        if (bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        thumbPath = snapFile;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        FolderInfoModel m = new FolderInfoModel(thumbPath, camera.getUid(), camera.getNickName(), photoCount, videoCount);
        return m;
    }

    public void initView() {
        sourceList.clear();
        ListView picture_fragment_camera_list = (ListView) view.findViewById(R.id.picture_fragment_camera_list);

        for (IMyCamera camera : TwsDataValue.cameraList()) {
            sourceList.add(processFolderInfo(camera));
        }

        picture_fragment_camera_list.setAdapter(new PictureListAdapter(getActivity()));
        picture_fragment_camera_list.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position,
                                    long arg3) {
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
                //}
            }
        });
        if (TwsDataValue.cameraList().size() > 0) {
            view.findViewById(R.id.txt_nocamera).setVisibility(View.GONE);
        } else {
            view.findViewById(R.id.txt_nocamera).setVisibility(View.VISIBLE);
        }
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
                holder.img_snap = (ImageView) convertView.findViewById(R.id.img_snap);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (holder != null) {
                holder.txt_name.setText(model.cameraName);
                holder.txt_count.setText(String.format(getString(R.string.tips_folder_desc), model.photoCount + "", model.videoCount + ""));
                if (model.thumbPath != null) {
                    BitmapFactory.Options bfo = new BitmapFactory.Options();
                    bfo.inSampleSize = 4;// 1/4宽高
                    try {
                        Bitmap bmp = BitmapFactory.decodeFile(model.thumbPath, bfo);
                        holder.img_snap.setImageBitmap(bmp);
                    }catch (OutOfMemoryError error){
                        holder.img_snap.setImageResource(R.drawable.default_img);
                    }
                } else {
                    holder.img_snap.setImageResource(R.drawable.default_img);
                }
            }

            return convertView;
        }

        public final class ViewHolder {
            //		public ImageView img;
            public TextView txt_name;
            public TextView txt_count;
            public ImageView img_snap;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

}
