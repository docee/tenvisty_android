package com.tws.commonlib.task;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.tws.commonlib.activity.CameraFolderActivity;
import com.tws.commonlib.bean.MyCamera;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/10/11.
 */

public class VideoThumbImgTask extends AsyncTask<String, Void, String> {
    private ImageView itemView;
    private onCreateVideoThumb te;

    public void setItemView(ImageView view) {
        this.itemView = view;
    }

    public VideoThumbImgTask(onCreateVideoThumb _te) {
        this.te = _te;
    }

    @Override
    protected String doInBackground(String... strings) {
        String filePath = strings[0];
        final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
        if (bitmap != null) {
            try {
                String snapFile =  filePath+ ".jpg";
                FileOutputStream fos = new FileOutputStream(snapFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, fos);
                fos.flush();
                fos.close();
                if (bitmap.isRecycled()) {
                    bitmap.recycle();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//        else {
//            File file = new File(filePath);
//            file.delete();
//        }

        if (itemView != null) {
            if (te != null) {
                te.onCreated(itemView, bitmap);
            }
        }
        return null;
    }

    public interface onCreateVideoThumb {
        void onCreated(ImageView itemView, Bitmap bmp);
    }
}
