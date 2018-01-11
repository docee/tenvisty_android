package com.tws.commonlib.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
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
    public LocalPicItemListAdapter(Context context, List<LocalPichModel> _sourceItemList,float ratio) {
        this.activity = (Activity) context;
        this.mInflater = LayoutInflater.from(context);
        this.sourceItemList = _sourceItemList;
        this.checkPicList = new ArrayList<ImageView>();
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
        sourceItemList.add(new LocalPichModel(path));
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
        Bitmap bitmap = null;
        if (model.getThumbBmp() == null) {
            LocalPichumbImgTask task = new LocalPichumbImgTask(new LocalPichumbImgTask.onCreateVideoThumb() {
                @Override
                public void onCreated(final ImageView itemView, final String path, final Bitmap bmp) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (path != null && bmp != null) {
                                if (path.contains(".mp4") || path.contains(TwsDataValue.Remte_RECORDING_DIR)) {
                                    itemView.setImageResource(R.drawable.ic_menu_play_inverse_background);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                        itemView.setBackground(new BitmapDrawable(mInflater.getContext().getResources(), bmp));
                                    } else {
                                        itemView.setImageBitmap(bmp);
                                    }
                                } else {
                                    itemView.setImageBitmap(bmp);
                                }
                            } else {
                                itemView.setImageResource(R.drawable.ic_menu_play_inverse_background);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                    itemView.setBackground(new BitmapDrawable(mInflater.getContext().getResources(), defaultPic));
                                } else {
                                    itemView.setImageBitmap(defaultPic);
                                }
                            }
                        }
                    });
                    model.setThumbBmp(bmp);
                }
            });
            task.setItemView(holder.img_pic);
            task.execute(sourceItemList.get(position).path);
//            BitmapFactory.Options bfo = new BitmapFactory.Options();
//            bfo.inSampleSize = 4;// 1/4宽高
//            bitmap = BitmapFactory.decodeFile(sourceItemList.get(position).path, bfo);
        } else {
            bitmap = model.getThumbBmp();
        }
        holder.img_select.setEnabled(model.checked);
        if (model.isVideo()) {
            holder.img_pic.setImageResource(R.drawable.ic_menu_play_inverse_background);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                holder.img_pic.setBackground(new BitmapDrawable(mInflater.getContext().getResources(), bitmap));
            } else {
                holder.img_pic.setImageBitmap(bitmap);
            }
        } else {
            holder.img_pic.setImageBitmap(bitmap);
        }
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
    }

    public void  release(){
        if(defaultPic != null && !defaultPic.isRecycled()){
            defaultPic.recycle();
            defaultPic = null;
            System.gc();
        }
        if(sourceItemList != null){
            for(LocalPichModel model : sourceItemList){
                model.setThumbBmp(null);
            }
        }
    }

}