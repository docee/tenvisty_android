package com.google.zxing.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.camera.CameraManager;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.decoding.CaptureActivityHandler;
import com.google.zxing.decoding.InactivityTimer;
import com.google.zxing.decoding.RGBLuminanceSource;
import com.google.zxing.qrcode.QRCodeReader;
import com.google.zxing.view.ViewfinderView;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.AddCameraInputUidActivity;
import com.tws.commonlib.activity.AddCameraNavigationTypeActivity;
import com.tws.commonlib.activity.SaveCameraActivity;
import com.tws.commonlib.activity.SearchCameraActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;


/**
 * Initial the camera
 *
 * @author Ryan.Tang
 */
public class CaptureActivity extends AppCompatActivity implements Callback {

    private static final int REQUEST_CODE_SCAN_GALLERY = 100;

    private CaptureActivityHandler handler;
    private ViewfinderView viewfinderView;
    private ImageView back;
    private boolean hasSurface;
    private Vector<BarcodeFormat> decodeFormats;
    private String characterSet;
    private InactivityTimer inactivityTimer;
    private MediaPlayer mediaPlayer;
    private boolean playBeep;
    private static final float BEEP_VOLUME = 0.10f;
    private boolean vibrate;
    private ProgressDialog mProgress;
    private String photo_path;
    private Bitmap scanBitmap;
    //	private Button cancelScanButton;
    public static final int RESULT_CODE_QR_SCAN = 0xA1;
    public static final int RESULT_CODE_ADD_MANUALLY = 0xA2;
    public static final int RESULT_CODE_INPUT_UID_MANUALLY = 0xA3;
    public static final int RESULT_CODE_SEARCH_LAN = 0xA4;
    public static final String INTENT_EXTRA_KEY_QR_SCAN = "qr_scan_result";
    private Camera camera;
    private Camera.Parameters params;
    boolean isOpen;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        //ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
        CameraManager.init(getApplication());
        viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_content);

//		cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_scan_qrcode));
        title.setButton(NavigationBar.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_LEFT:
                        finish();
                        break;
                }
            }
        });
        findViewById(R.id.image_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isOpen) {
                    openLight();
                    findViewById(R.id.image_flash).setBackgroundResource(R.drawable.btn_scan_flash_on);
                } else {
                    closeLight();
                    findViewById(R.id.image_flash).setBackgroundResource(R.drawable.btn_scan_flash_off);
                }
            }
        });
        TextView txt_inputuid = (TextView) findViewById(R.id.txt_inputuid);
        txt_inputuid.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        txt_inputuid.getPaint().setAntiAlias(true);//抗锯齿
        txt_inputuid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inactivityTimer.onActivity();
//                setResult(RESULT_CODE_INPUT_UID_MANUALLY);
//                CaptureActivity.this.finish();
                Intent intent2 = new Intent();
                intent2.setClass(CaptureActivity.this, AddCameraInputUidActivity.class);
                startActivity(intent2);
            }
        });
        findViewById(R.id.image_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inactivityTimer.onActivity();
                Intent intent2 = new Intent();
                intent2.setClass(CaptureActivity.this, SearchCameraActivity.class);
                startActivity(intent2);
            }
        });
        Intent intent = this.getIntent();
        if(intent != null ){
            String from = intent.getStringExtra(TwsDataValue.EXTRAS_KEY_FROM);
            if(from != null && from.equals(SaveCameraActivity.class.getName())){
                txt_inputuid.setVisibility(View.GONE);
            }
        }
        //添加toolbar
//        addToolbar();
    }

    private void openLight() //开闪光灯
    {
        try {
            camera = CameraManager.getCamera(); //我们先前在CameraManager类中添加的静态方法
            params = camera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(params);
            camera.startPreview();
            isOpen = true;
        } catch (Exception ex) {

        }
    }

    private void closeLight() //关闪光灯
    {
        try {
            if (params != null && isOpen) {
                params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(params);
                isOpen = false;
            }
        } catch (Exception ex) {

        }
    }

    private void addToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        ImageView more = (ImageView) findViewById(R.id.scanner_toolbar_more);
//        assert more != null;
//        more.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.scanner_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()){
//            case R.id.scan_local:
//                //打开手机中的相册
//                Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT); //"android.intent.action.GET_CONTENT"
//                innerIntent.setType("image/*");
//                Intent wrapperIntent = Intent.createChooser(innerIntent, "选择二维码图片");
//                this.startActivityForResult(wrapperIntent, REQUEST_CODE_SCAN_GALLERY);
//                return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        if (requestCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN_GALLERY:
                    //获取选中图片的路径
                    Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor.moveToFirst()) {
                        photo_path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
                    }
                    cursor.close();

                    mProgress = new ProgressDialog(CaptureActivity.this);
                    mProgress.setMessage("正在扫描...");
                    mProgress.setCancelable(false);
                    mProgress.show();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Result result = scanningImage(photo_path);
                            if (result != null) {
//                                Message m = handler.obtainMessage();
//                                m.what = R.id.decode_succeeded;
//                                m.obj = result.getText();
//                                handler.sendMessage(m);
                                Intent resultIntent = new Intent();
                                Bundle bundle = new Bundle();
                                bundle.putString(INTENT_EXTRA_KEY_QR_SCAN, result.getText());
//                                Logger.d("saomiao",result.getText());
//                                bundle.putParcelable("bitmap",result.get);
                                resultIntent.putExtras(bundle);
                                CaptureActivity.this.setResult(RESULT_CODE_QR_SCAN, resultIntent);

                            } else {
                                Message m = handler.obtainMessage();
                                m.what = R.id.decode_failed;
                                m.obj = "Scan failed!";
                                handler.sendMessage(m);
                            }
                        }
                    }).start();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 扫描二维码图片的方法
     *
     * @param path
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 200);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        try {
            scanBitmap = BitmapFactory.decodeFile(path, options);
        } catch (OutOfMemoryError error) {
            return null;
        }
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!TwsTools.checkPermission(this, Manifest.permission.CAMERA)){
            TwsTools.showAlertDialog(this);
        }
        else{
            SurfaceView surfaceView = (SurfaceView) findViewById(R.id.scanner_view);
            SurfaceHolder surfaceHolder = surfaceView.getHolder();
            if (hasSurface) {
                initCamera(surfaceHolder);
            } else {
                surfaceHolder.addCallback(this);
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }
            decodeFormats = null;
            characterSet = null;

            playBeep = true;
            AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
            if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                playBeep = false;
            }
            initBeepSound();
            vibrate = true;
            closeLight();
            //quit the scan view
//		cancelScanButton.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				CaptureActivity.this.finish();
//			}
//		});
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        if (handler != null) {
            handler.removeMessages(R.id.restart_preview);
        }
        super.onDestroy();
    }

    /**
     * Handler scan result
     *
     * @param result
     * @param barcode
     */
    public void handleDecode(Result result, Bitmap barcode) {
        inactivityTimer.onActivity();
        playBeepSoundAndVibrate();
        String resultString = result.getText();
        //FIXME
        if (TextUtils.isEmpty(resultString)) {
            Toast.makeText(CaptureActivity.this, "Scan failed!", Toast.LENGTH_SHORT).show();
        } else {
            resultString = TwsTools.takeInnerUid(resultString);

            if (resultString == null) {
                Toast.makeText(CaptureActivity.this, getString(R.string.alert_invalid_uid_qrcode), Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessageDelayed(R.id.restart_preview, 5000);
                return;
            }
            else{
                boolean duplicated = false;
                for (IMyCamera camera_ : TwsDataValue.cameraList()) {
                    if (resultString.equalsIgnoreCase(camera_.getUid())) {
                        duplicated = true;
                        break;
                    }
                }
                if (duplicated) {
                    Toast.makeText(CaptureActivity.this, getString(R.string.alert_camera_exist), Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessageDelayed(R.id.restart_preview, 5000);
                    return;
                }
            }

            Intent intent = new Intent();
            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, resultString);
            intent.setClass(this, AddCameraNavigationTypeActivity.class);
            startActivity(intent);
//            Intent resultIntent = new Intent();
//            Bundle bundle = new Bundle();
//            bundle.putString(INTENT_EXTRA_KEY_QR_SCAN, resultString);
            System.out.println("sssssssssssssssss scan 0 = " + resultString);
            // 不能使用Intent传递大于40kb的bitmap，可以使用一个单例对象存储这个bitmap
//            bundle.putParcelable("bitmap", barcode);
//            Logger.d("saomiao",resultString);
//            resultIntent.putExtras(bundle);
//            this.setResult(RESULT_CODE_QR_SCAN, resultIntent);
        }
       // CaptureActivity.this.finish();
    }

    private void initCamera(final SurfaceHolder surfaceHolder) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... arg0) {
                if (isCancelled()) {
                    return null;
                }
                try {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            try {
                                CameraManager.get().openDriver(surfaceHolder);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }  catch (RuntimeException e) {
                    return null;
                }
                if (handler == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            handler = new CaptureActivityHandler(CaptureActivity.this, decodeFormats,
                                    characterSet);
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
            }
        }.execute();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;

            initCamera(holder);
        }

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;

    }

    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    public Handler getHandler() {
        return handler;
    }

    public void drawViewfinder() {
        viewfinderView.drawViewfinder();

    }

    private void initBeepSound() {
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it
            // too loud,
            // so we now play on the music stream.
            setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(beepListener);

            AssetFileDescriptor file = getResources().openRawResourceFd(
                    R.raw.beep);
            try {
                mediaPlayer.setDataSource(file.getFileDescriptor(),
                        file.getStartOffset(), file.getLength());
                file.close();
                mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
                mediaPlayer.prepare();
            } catch (IOException e) {
                mediaPlayer = null;
            }
        }
    }

    private static final long VIBRATE_DURATION = 200L;

    private void playBeepSoundAndVibrate() {
        if (playBeep && mediaPlayer != null) {
            mediaPlayer.start();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(VIBRATE_DURATION);
        }
    }

    /**
     * When the beep has finished playing, rewind to queue up another one.
     */
    private final OnCompletionListener beepListener = new OnCompletionListener() {
        public void onCompletion(MediaPlayer mediaPlayer) {
            mediaPlayer.seekTo(0);
        }
    };

}