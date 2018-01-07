package com.tws.commonlib.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;

import com.tws.commonlib.R;
import com.tws.commonlib.adapter.LocalPicItemListAdapter;
import com.tws.commonlib.base.DateScrollItem;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.HichipCamera;
import com.tws.commonlib.bean.LocalPichModel;
import com.tws.commonlib.controller.MyGallery;
import com.tws.commonlib.controller.MyImageView;
import com.tws.commonlib.controller.NavigationBar;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

//import cn.sharesdk.framework.ShareSDK;
//import cn.sharesdk.onekeyshare.OnekeyShare;

/**
 * 大图展示，可以左右滑动，手势放大缩小图片；
 * 用到了自定义的Gallery（MyGallery）；
 * 自定义的ImageView（MyImageView）
 *
 * @author Administrator
 */
public class PhotoShowActivity extends BaseActivity implements OnTouchListener {

    // 屏幕宽度
    public static int screenWidth;
    // 屏幕高度
    public static int screenHeight;

    private MyGallery gallery;

    private int mposition;

    private String dir;
    private String accFilename;
    public static List<String> pathsrcs;
    private File[] files;

    private GalleryAdapter galleryAdapter;

    /**
     * 当前显示的图片在图片列表中的位置
     */
    public int currentposition = 0;

    /**
     * 分享
     */
    private static final int OPT_MENU_ITEM_SHARE = Menu.FIRST;

    /**
     * 删除
     */
    private static final int OPT_MENU_ITEM_DELETE = Menu.FIRST + 1;
    NavigationBar title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        needConnect = false;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_show);
        initView();
        Intent i = getIntent();
        Bundle extras = i.getExtras();
        accFilename = extras.getString("filename");//所选文件的具体路径
        dir = extras.getString("dir");//文件夹路径
        pathsrcs = new ArrayList<String>();

        setImagesPath(dir);

        //removeCorruptImage();

        //gallery相关设置
        gallery = (MyGallery) findViewById(R.id.gallery);
        gallery.setVerticalFadingEdgeEnabled(false);// 取消竖直渐变边框
        gallery.setHorizontalFadingEdgeEnabled(false);// 取消水平渐变边框
        galleryAdapter = new GalleryAdapter(this);
        //GalleryAdapter galleryAdapter=new GalleryAdapter(this);
        gallery.setAdapter(galleryAdapter);
        mposition = pathsrcs.indexOf(accFilename);
        gallery.setSelection(mposition + ((pathsrcs.size()) * 2));//改变最开始的position的数值,使得到了第一张左右都能滑动
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;

    }

    @Override
    protected void initView() {
        title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle("");
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT2, android.R.drawable.ic_menu_share);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT, android.R.drawable.ic_menu_delete);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        setResult(RESULT_OK, null);
                        finish();
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        deleteImage(currentposition);
                        break;
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT2:
                        TwsTools.showShare(PhotoShowActivity.this, false, null, false, null, new String[]{pathsrcs.get(currentposition)});
                        break;
                }
            }
        });
    }

    /**
     * 获取该文件夹中的所有文件的具体目录，存入pathsrcs
     *
     * @param path
     */
    public final synchronized void setImagesPath(String path) {
        pathsrcs.clear();
        File folder = new File(path);
        File[] files = folder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().length() == 36 || file.getName().length() == 39;
            }
        });
        for (File f : files) {
            if (f.isDirectory()) {
                File[] pics = f.listFiles(new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return file.getName().length() == 36 || file.getName().length() == 39;
                    }
                });
                for (File pic : pics) {
                    pathsrcs.add(pic.getAbsolutePath());
                }
            } else {
                pathsrcs.add(f.getAbsolutePath());
            }
        }
        Collections.sort(pathsrcs, new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                return -s.compareTo(t1);
            }
        });
       // Collections.reverse(pathsrcs);
    }


    /**
     * 移除IMAGE_FILES中的非图片资源
     */
    public final void removeCorruptImage() {
        Iterator<String> it = pathsrcs.iterator();
        while (it.hasNext()) {
            String path = it.next();
            //Bitmap bitmap = BitmapFactory.decodeFile(path) ;
            Bitmap bitmap = null;
            if (path.endsWith(".jpg")) {
                bitmap = BitmapFactory.decodeFile(path);
            } else if (path.endsWith(".avi")) {
                bitmap = ThumbnailUtils.createVideoThumbnail(path, MediaStore.Images.Thumbnails.MICRO_KIND);
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, 60, 60);
            }
            // XXX: CA's hack, snapshot may fail and create corrupted bitmap
            if (bitmap == null) {
                it.remove();
            }
        }
    }

    /**
     * @param fileName
     * @return boolean
     * @判断是否存在该类型的图片
     */
    private boolean validate(String fileName) {
        int idx = fileName.indexOf(".");
        String subfix = fileName.substring(idx + 1);
        if (fileName.equals("")) {
            return false;
        }
        // subfix.equals()||subfix.equals(".png")||subfix.equals(".jpeg")
        return "jpg".equals(subfix) || "png".equals(subfix) || "gif".equals(subfix);
    }

    float beforeLenght = 0.0f; // 两触点距离
    float afterLenght = 0.0f; // 两触点距离
    boolean isScale = false;
    float currentScale = 1.0f;// 当前图片的缩放比率

    private class GalleryChangeListener implements OnItemSelectedListener {

        @Override
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            currentScale = 1.0f;
            isScale = false;
            beforeLenght = 0.0f;
            afterLenght = 0.0f;

        }

        @Override
        public void onNothingSelected(AdapterView<?> arg0) {
            // TODO Auto-generated method stub

        }

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {//后8位表示动作
            case MotionEvent.ACTION_POINTER_DOWN:// 多点缩放
                beforeLenght = spacing(event);
                if (beforeLenght > 5f) {
                    isScale = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isScale) {
                    afterLenght = spacing(event);
                    if (afterLenght < 5f)
                        break;
                    float gapLenght = afterLenght - beforeLenght;
                    if (gapLenght == 0) {
                        break;
                    } else if (Math.abs(gapLenght) > 5f) {
                        // FrameLayout.LayoutParams params =
                        // (FrameLayout.LayoutParams) gallery.getLayoutParams();
                        float scaleRate = gapLenght / 854;// 缩放比例

                        //缩放动画
                        Animation myAnimation_Scale = new ScaleAnimation(currentScale, currentScale + scaleRate, currentScale, currentScale + scaleRate, Animation.RELATIVE_TO_SELF, 0.5f,
                                Animation.RELATIVE_TO_SELF, 0.5f);

                        myAnimation_Scale.setDuration(100);
                        myAnimation_Scale.setFillAfter(true);
                        myAnimation_Scale.setFillEnabled(true);
                        // gallery.getChildAt(0).startAnimation(myAnimation_Scale);

                        // gallery.startAnimation(myAnimation_Scale);
                        currentScale = currentScale + scaleRate;

                        gallery.getSelectedView().setLayoutParams(new Gallery.LayoutParams((int) (480 * (currentScale)), (int) (854 * (currentScale))));

                        beforeLenght = afterLenght;
                    }
                    return true;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
                isScale = false;
                break;
        }

        return false;
    }

    /**
     * 就算两点间的距离
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }


    /**
     * 在gallery中显示的adapter
     *
     * @author Administrator
     */
    public class GalleryAdapter extends BaseAdapter {

        private Context context;

        public GalleryAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {

            //return pathsrcs.size();
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Bitmap bmp = null;

            if (pathsrcs.get(position % (pathsrcs.size())).endsWith(".jpg")) {
                bmp = BitmapFactory.decodeFile(pathsrcs.get(position % (pathsrcs.size())));
            } else if (pathsrcs.get(position % (pathsrcs.size())).endsWith(".avi")) {
                bmp = ThumbnailUtils.createVideoThumbnail(pathsrcs.get(position % (pathsrcs.size())), MediaStore.Images.Thumbnails.MINI_KIND);
            }

            currentposition = (position % (pathsrcs.size()));//获取当前Gallery的位置在pathsrcs中的具体所对应的位置
            String filePath = pathsrcs.get(currentposition);
            String[] paths = filePath.split("/");
            String fileName = paths[paths.length - 1];
            title.setTitle(fileName.substring((fileName.length() == 36?18:21), fileName.length()) + "(" + (currentposition + 1) + "/" + pathsrcs.size() + ")");
            MyImageView view = new MyImageView(context, bmp.getWidth(), bmp.getHeight());
            view.setLayoutParams(new Gallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

            if (pathsrcs.get(position % (pathsrcs.size())).endsWith(".avi")) {
                view.setLayoutParams(new Gallery.LayoutParams(bmp.getWidth(), bmp.getHeight()));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    view.setBackground(new BitmapDrawable(bmp));
                }
                view.setImageResource(R.drawable.ic_menu_play_inverse_background);
                view.center(true, true);

            } else if (pathsrcs.get(position % (pathsrcs.size())).endsWith(".jpg")) {//设置显示的图片
                view.setImageBitmap(bmp);
            }
            return view;
        }

        /**
         * 删除当前位置的图片
         *
         * @param position
         * @return
         */
        public final boolean deleteImageAtPosition(int position) {
            File file = new File(pathsrcs.get(position));
            boolean deleted = file.delete();
            pathsrcs.remove(position);
            if (pathsrcs.size() == 0) {
                onBackPressed();

            } else {
                this.notifyDataSetChanged();
                gallery.setSelection(position + ((pathsrcs.size()) * 2));
            }

            return deleted;
        }

    }// GalleryAdapter end

    /**
     * 创建删除和分享menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuItem shareMenuItem = menu.add(Menu.NONE, OPT_MENU_ITEM_SHARE, 1, "share");
        shareMenuItem.setIcon(android.R.drawable.ic_menu_share).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        MenuItem deleteMenuItem = menu.add(Menu.NONE, OPT_MENU_ITEM_DELETE, 2, "delete");
        deleteMenuItem.setIcon(android.R.drawable.ic_menu_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == OPT_MENU_ITEM_SHARE) {
            TwsTools.showShare(PhotoShowActivity.this, false, null, false, pathsrcs.get(currentposition), null);
        } else if (id == OPT_MENU_ITEM_DELETE) {
            deleteImage(currentposition);
        }
        return true;
    }

    /**
     * 删除对应位置图片的方法
     *
     * @param position
     */
    private void deleteImage(final int position) {
        showYesNoDialog(R.string.dialog_msg_delete_local_pic_confirm,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        galleryAdapter.deleteImageAtPosition(position);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        //TwsTools.showShare(PhotoShowActivity.this, false, null, false,IMAGE_FILES.get(position));
                        break;

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        // ShareSDK.stopSDK(this);
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK, null);
        super.onBackPressed();
    }

}
