package com.tws.commonlib.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tws.commonlib.R;
import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.LocalPichModel;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.task.LocalPichumbImgTask;
import com.tws.commonlib.task.VideoThumbImgTask;
import com.tws.commonlib.util.ImageFetcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Administrator on 2017/7/14.
 */


public class LocalPicItemListAdapter extends BaseAdapter {

    private LayoutInflater mInflater;


    public LocalPicItemListAdapter(Context context) {
        this.mInflater = LayoutInflater.from(context);
    }


    public boolean checkMode;

    public List<LocalPichModel> sourceItemList;
    public List<ImageView> checkPicList;
    private Activity activity;
    Bitmap defaultPic;
    int width;
    int height;
    int type;

    private ImageFetcher mImageFetcher;
    public void  setImageFetcher(ImageFetcher imageFetcher){
        this.mImageFetcher = imageFetcher;
    }
    public LocalPicItemListAdapter(Context context, List<LocalPichModel> _sourceItemList,float ratio,int type) {
        this.activity = (Activity) context;
        this.mInflater = LayoutInflater.from(context);
        this.sourceItemList = _sourceItemList;
        this.checkPicList = new ArrayList<ImageView>();
        this.type = type;
        defaultPic = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_video_snap);

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        width = (dm.widthPixels - TwsTools.dip2px(context,80))/3;
        height = (int)(width/ratio) ;
    }

    public void addSourceItem(String path) {
        for (LocalPichModel m : sourceItemList) {
            if (m.path.equals(path)) {
                return;
            }
        }
        sourceItemList.add(new LocalPichModel(path,type));
    }


    public void setLayout(int layout) {
        this.layout = layout;
    }

    private int getLayout() {
        return this.layout;
    }

    int layout;

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return sourceItemList.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return sourceItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressLint("InflateParams")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        if (sourceItemList == null || sourceItemList.size() < position) {
            return null;
        }
        final LocalPichModel model = sourceItemList.get(position);
        final String title = sourceItemList.get(position).path;

        if (title == null) {
            return null;
        }
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(layout == 0 ? R.layout.adapter_view_local_pic_subitem : layout, null);
            holder = new ViewHolder();
            holder.img_pic = (ImageView) convertView.findViewById(R.id.img_pic);
            holder.img_select = (ImageView) convertView.findViewById(R.id.img_select);
            holder.img_play = (ImageView)convertView.findViewById(R.id.img_play);
            holder.img_download = convertView.findViewById(R.id.img_download);
            if (checkPicList.size() <= position) {
                checkPicList.add(position, holder.img_select);
            }
            holder.img_select.setEnabled(true);
            convertView.setLayoutParams(new AbsListView.LayoutParams(width, height) );
            //((RelativeLayout)holder.img_select.getParent()).getLayoutParams().height =  convertView.getMeasuredWidth()/(16/9);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        mImageFetcher.loadImage(model.thumbPath,holder.img_pic);
        if(model.isVideo()){
            holder.img_play.setVisibility(View.VISIBLE);
        }
        else{
            holder.img_play.setVisibility(View.GONE);
        }
        if(model.type == 2){
            holder.img_download.setVisibility(View.VISIBLE);
        }
        else{
            holder.img_download.setVisibility(View.GONE);
        }
        holder.img_select.setEnabled(model.checked);
//        if (model.isVideo()) {
//            holder.img_pic.setImageResource(R.drawable.ic_menu_play_inverse_background);
//        } else {
//            //holder.img_pic.setImageResource(0);
//        }
        if (!checkMode) {
            holder.img_select.setVisibility(View.INVISIBLE);
        } else {
            holder.img_select.setVisibility(View.VISIBLE);
        }

        return convertView;
    }

    public final class ViewHolder {
        public ImageView img_pic;
        public ImageView img_select;
        public  ImageView img_play;
        public  ImageView img_download;
    }


}