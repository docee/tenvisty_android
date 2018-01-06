package com.tws.commonlib.task;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/10/11.
 */

public class LocalPichumbImgTask extends AsyncTask<String, Void, String> {
    private ImageView itemView;
    private onCreateVideoThumb te;

    public void setItemView(ImageView view) {
        this.itemView = view;
    }

    public LocalPichumbImgTask(onCreateVideoThumb _te) {
        this.te = _te;
    }

    @Override
    protected String doInBackground(String... strings) {
        String filePath = strings[0];
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inSampleSize = 4;// 1/4宽高
        if (filePath.contains(".mp4")) {
            String thumbPath = filePath + ".jpg";
            File f = new File(thumbPath);
            if (!f.exists()) {
                final Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
                if (bitmap != null) {
                    try {
                        String snapFile = filePath + ".jpg";
                        FileOutputStream fos = new FileOutputStream(snapFile);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fos);
                        fos.flush();
                        fos.close();
                        if (bitmap.isRecycled()) {
                            bitmap.recycle();
                        }
                        if (itemView != null) {
                            if (te != null) {
                                te.onCreated(itemView, snapFile, bitmap);
                            }
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    if (itemView != null) {
                        if (te != null) {
                            te.onCreated(itemView, null, bitmap);
                        }
                    }
                }
                return null;

            }
            else{
                filePath = thumbPath;
            }
        }
        final Bitmap bitmap = BitmapFactory.decodeFile(filePath, bfo);
        if (bitmap != null) {
//        else {
//            File file = new File(filePath);
//            file.delete();
//        }

            if (itemView != null) {
                if (te != null) {
                    te.onCreated(itemView, filePath, bitmap);
                }
            }
        }
        return null;
    }

    public interface onCreateVideoThumb {
        void onCreated(ImageView itemView, String path, Bitmap bmp);
    }
}
