package com.tws.commonlib.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.zxing.activity.CaptureActivity;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.EventListActivity;
import com.tws.commonlib.activity.LiveViewActivity;
import com.tws.commonlib.activity.aoni.DeviceSetting_AoniActivity;
import com.tws.commonlib.activity.aoni.EventList_AoniActivity;
import com.tws.commonlib.activity.hichip.DeviceSetting_HichipActivity;
import com.tws.commonlib.activity.hichip.EventList_HichipActivity;
import com.tws.commonlib.activity.hichip.LiveView_HichipActivity;
import com.tws.commonlib.activity.setting.DeviceSettingActivity;
import com.tws.commonlib.activity.setting.EditDeviceActivity;
import com.tws.commonlib.activity.setting.ModifyCameraPasswordActivity;
import com.tws.commonlib.adapter.GridViewPagerAdapter;
import com.tws.commonlib.adapter.VideoViewAdapter;
import com.tws.commonlib.base.ConnectionState;
import com.tws.commonlib.base.MyConfig;
import com.tws.commonlib.base.TwsToast;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.CameraState;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.bean.TwsSessionState;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CameraFragment extends BaseFragment implements OnTouchListener,
        OnGestureListener, IIOTCListener {
    static final int MY_CAMERA_REQUEST_CODE = 0;
    static final int PUBLIC_CAMERA_REQUEST_CODE = 1;
    static final int CAMERA_ADD_REQUEST_CODE = 2;
    static final int CAMERA_EVENTLIST_REQUEST_CODE = 3;

    /**
     * 区别是公共摄像机还是自己的摄像机，因为公共摄像机列表继承了该类
     */
    static int CAMERA_OWN; //MY_CAMERA_REQUEST_CODE PUBLIC_CAMERA_REQUEST_CODE

    static final int CPU_CORES_NUM = TwsTools.getCpuCoresNum();//获取CPU的核数
    static final float CPU_FREQUENCE_NUM = TwsTools.getCpuFrequence();//获取CPU的频率
    AlertDialog modifyPasswordAlert;

    /**
     * 每页有几行
     */
    private int rows = 100;
    /**
     * 每页有几列
     */
    private int columns = 1;


    /**
     * 手势相关
     */
    GestureDetector mGestureDetector;


    /**
     * GridViews用于保存多个GridView的列表（每一页一个GridView）
     */
    private List<GridView> gridViews = new ArrayList<GridView>();

    /**
     * 自定义的PagerAdapter，用于ViewPager中显示GridView
     */
    GridViewPagerAdapter pageradapter;


    float ROUND_CORNER_BITMAP = 20.0f;

    private IntentFilter filter;

    private AlertDialog dlg;

    private boolean isShowModifyPwdDlg = false;//是否有修改密码对话框显示

    private int remoteEventCameraIndex = -1;//移动侦测报警通知摄像机
    Button addButton;
    private VideoViewAdapter adapter;
    List<Map<String, Object>> cells = null;
    private CameraBroadcastReceiver receiver;
    LinearLayout ll_container_first_add_tip;
    ViewPager viewpager;
    int ranNum;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case TwsDataValue.HANDLE_MESSAGE_SESSION_STATE: {
                    final IMyCamera camera = (IMyCamera) msg.obj;

                    if (!camera.isExist()) {
                        return;
                    }
                    refreshItems();
                    int resultCode = msg.arg1;
                    if (resultCode == TwsSessionState.CONNECTION_STATE_CONNECTED) {
                        //已经成功建立连接
                        if (MyConfig.isStrictPwd() && camera.getPassword().equals(TwsDataValue.DEFAULT_PASSWORD)) {//检测该摄像机密码格式是否符合要求
                            modifyPasswordHint(camera);
                        }
                    }
                    //收到密码错误，则弹出输入密码的dialog
                    else if (resultCode == TwsSessionState.CONNECTION_STATE_WRONG_PASSWORD) {
                        if (camera.getState() == CameraState.Reseting) {
                            camera.setPassword(TwsDataValue.DEFAULT_PASSWORD);
                            camera.asyncStop(new IMyCamera.TaskExecute() {
                                @Override
                                public void onPosted(IMyCamera c, Object data) {
                                    c.start();
                                }
                            });
                        } else {
                            camera.asyncStop(null);
                            showPasswordWrongHint(camera);
                        }
                    }
                    //收到密码错误，则弹出输入密码的dialog
                    else if (resultCode == TwsSessionState.CONNECTION_STATE_WAKINGUP) {
                       // camera.start();
                    } else if (resultCode == TwsSessionState.CONNECTION_STATE_TIMEOUT) {
                        camera.asyncStop(new IMyCamera.TaskExecute() {
                            @Override
                            public void onPosted(IMyCamera c, Object data) {
                                c.start();
                            }
                        });
                    } else if (resultCode == TwsSessionState.CONNECTION_STATE_CONNECT_FAILED || resultCode ==
                            TwsSessionState.CONNECTION_STATE_DISCONNECTED || resultCode == TwsSessionState.CONNECTION_STATE_UNKNOWN_DEVICE) {
                        if (camera.getState() != CameraState.None) {
                            camera.asyncStop(new IMyCamera.TaskExecute() {
                                @Override
                                public void onPosted(IMyCamera c, Object data) {
                                    c.start();
                                }
                            });
                        } else {

                        }
                    }
                    else if(camera.isSleeping()){
                        if(camera.isWakingUp()){
                            camera.asyncWakeUp(new IMyCamera.TaskExecute() {
                                @Override
                                public void onPosted(final IMyCamera camera, Object data) {
                                    camera.start();
                                }
                            });
                        }
                    }
                    break;
                }

                case TwsDataValue.HANDLE_MESSAGE_IO_RESP:
                    int avIOCtrlMsgType = msg.arg1;
                    byte[] data = msg.getData().getByteArray("data");
                    IMyCamera camera = (IMyCamera) msg.obj;
                    if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT) {//IOTYPE_USER_IPCAM_EVENT_REPORT (0x1FFF)---由IPCamera發往App--當IPCamera發生事件時，即時發送資訊通知App

                        byte[] t = new byte[8];
                        System.arraycopy(data, 0, t, 0, 8);//0-7位存储时间
                        AVIOCTRLDEFs.STimeDay evtTime = new AVIOCTRLDEFs.STimeDay(t);

                        int camChannel = Packet.byteArrayToInt_Little(data, 12);

                        int evtType = Packet.byteArrayToInt_Little(data, 16);
                        //Log.i("++VideoList++", "AVIOCTRLDEFs.IOTYPE_USER_IPCAM_EVENT_REPORT---evtType="+evtType);
                        if (evtType != AVIOCTRLDEFs.AVIOCTRL_EVENT_MOTIONPASS
                                && evtType != AVIOCTRLDEFs.AVIOCTRL_EVENT_IOALARMPASS) {
                            boolean canPush = true;//((MyCamera) camera).shouldPush();
                            if (evtType == AVIOCTRLDEFs.AVIOCTRL_EVENT_BELL_RING || canPush) {
                                TwsTools.showAlarmNotification(getActivity(), camera.getUid(), 2, System.currentTimeMillis());
                            }
                        }

                        //showNotification(camera, camChannel, evtType, evtTime.getTimeInMillis());

                    } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_FIRMWARE_INFO_RESP) {
                        if (data != null && data.length > 0) {
                            String firmver = TwsTools.getString(data);
                            String[] arrFirm = firmver.split("\\.");
                            IMyCamera mCamera = camera;
                            if (arrFirm.length >= 5) {
                                if (mCamera.getCustomTypeVersion() != null && mCamera.getSystemTypeVersion() != null && mCamera.getVendorTypeVersion() != null) {
                                    if (!arrFirm[0].equalsIgnoreCase(mCamera.getCustomTypeVersion()) || !arrFirm[1].equalsIgnoreCase(mCamera.getVendorTypeVersion()) || !(arrFirm[2] + "." + arrFirm[3] + "." + arrFirm[4]).equalsIgnoreCase(mCamera.getSystemTypeVersion())) {
                                        if (CameraFragment.this.getActivity() != null) {
                                            TwsToast.showToast(CameraFragment.this.getActivity(), getString(R.string.toast_updating_succ_finish));
                                        }
                                    }
                                }
                                mCamera.setCustomTypeVersion(arrFirm[0]);
                                mCamera.setVendorTypeVersion(arrFirm[1]);
                                mCamera.setSystemTypeVersion(arrFirm[2] + "." + arrFirm[3] + "." + arrFirm[4]);
                            }
                        }
                    } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_UPGRADE_STATUS) {
                        final View view = getCameraView(camera);
                        final AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus process = new AVIOCTRLDEFs.SMsgAVIoctrlUpgradeStatus(data);
                        if (view != null) {
                            if (CameraFragment.this.getActivity() != null) {
                                CameraFragment.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        TextView txt_state = view.findViewById(R.id.txt_state);
                                        if (txt_state != null) {
                                            txt_state.setBackgroundResource(R.drawable.shape_state_connecting);
                                            txt_state.setText(String.format(getString(R.string.process_upgrading_percent), process.p));
                                        }
                                        if (process.p >= 100) {
                                            TwsToast.showToast(CameraFragment.this.getActivity(), getString(R.string.toast_updating_succ_reboot));
                                            txt_state.setText(getString(R.string.process_rebooting));
                                        }
                                    }
                                });
                            }
                        }
                    }
                    else if(avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_DEVINFO_RESP){
                        if(camera.getSupplier()!= IMyCamera.Supllier.UnKnown){
                            refreshItems();
                        }
                    }
                    else if(avIOCtrlMsgType ==  AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PRAM_RESP){
                        refreshItems();
                    }
//                    //注意！！！由于在NTP同步的时候，设置完同步会去获取时间。这里获取完时间会去设置时间，注意不要死循环了
//                    else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_TIME_INFO_RESP) {
//                        if (camera.isFirstLogin()) {
//                            camera.setFirstLogin(false);
//                            AVIOCTRLDEFs.SMsgAVIoctrlTime time = new AVIOCTRLDEFs.SMsgAVIoctrlTime(data);
//                            if (time.adjustFlg == 0) {
//                                Calendar phoneCal = Calendar.getInstance(TimeZone.getTimeZone("gmt"));
//                                phoneCal.setTimeInMillis(System.currentTimeMillis());
//                                byte[] phoneTime = AVIOCTRLDEFs.STimeDay.parseContent(phoneCal.get(Calendar.YEAR), phoneCal.get(Calendar.MONTH) + 1, phoneCal.get(Calendar.DAY_OF_MONTH),
//                                        phoneCal.get(Calendar.DAY_OF_WEEK), phoneCal.get(Calendar.HOUR_OF_DAY), phoneCal.get(Calendar.MINUTE), phoneCal.get(Calendar.SECOND));
//                                byte[] data2 = AVIOCTRLDEFs.SMsgAVIoctrlTime.parseContent(phoneTime, 0, TwsDataValue.NTP_SERVER, 1);
//                                camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_REQ, data2);
//                            }
//                        }
//                    } else if (avIOCtrlMsgType == AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SET_TIME_INFO_RESP) {
//                       if(Packet.byteArrayToInt_Little(data) == 0){
//                           TwsToast.showToast(CameraFragment.this.getActivity(),"sync time succeed");
//                       }
//                       else{
//                           TwsToast.showToast(CameraFragment.this.getActivity(),"sync time failed");
//                       }
//                    }

                    break;
                case TwsDataValue.HANDLE_MESSAGE_CHANNEL_STATE:

//                    final MyCamera camera = (MyCamera) msg.obj;
//                    int resultCode = msg.arg1;
//                    final int channel = msg.arg2;
//                    if (resultCode == NSCamera.CONNECTION_STATE_TIMEOUT) {
//                        if (!camera.isExist()) {
//                            return;
//                        }
//                        camera.asyncStopChannel(channel, new MyCamera.TaskExecute() {
//                            @Override
//                            public void onPosted(Object data) {
//                                camera.startChannel(channel);
//                            }
//                        });
//                    }
                    break;
            }
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_camera, null);
            ranNum = (int) (Math.random() * 10000);
            initView();
        }
        ViewGroup parent = (ViewGroup) view.getParent();
        if (parent != null) {
            parent.removeView(view);
        }
        isShowModifyPwdDlg = false;
        afterInit();
        return view;
    }

    void refreshTitle() {
        if (view != null) {
            TextView txt_title = view.findViewById(R.id.txt_title);
            if(txt_title != null) {
                txt_title.setText(getString(R.string.title_camera_list) + String.format("(%d)", TwsDataValue.cameraList().size()));
//                if(TwsDataValue.cameraList().size() > 0) {
//                    txt_title.setText(getString(R.string.title_camera_list) + String.format("(%d)", TwsDataValue.cameraList().size()));
//                }
//                else{
//                    txt_title.setText(getString(R.string.title_camera_list));
//                }
            }
        }
    }

    private View view;

    public void initView() {

        //TwsActivity.initSystemBar(CameraFragment.this.getActivity());

        //手势监听
        mGestureDetector = new GestureDetector(this);

        //右上角编辑按钮的监听
        addButton = (Button) view.findViewById(R.id.btnAdd);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CameraFragment.this.getActivity(),
                        CaptureActivity.class);
                startActivityForResult(intent, CAMERA_ADD_REQUEST_CODE);
            }
        });

//		TextView navigationtTextView = (TextView)view.findViewById(R.id.navigationTextView);//
//		navigationtTextView.setText(getText(R.string.my_camera));

        addButton.setVisibility(View.VISIBLE);

        CAMERA_OWN = MY_CAMERA_REQUEST_CODE;//自己摄像机列表的标识
//        for (int i = 0; i < TwsDataValue.cameraList().size(); i++) {
//            NSCamera camera = TwsDataValue.cameraList().get(i);
//            if(camera.connect_state == NSCamera.CONNECTION_STATE_CONNECTED) {
//                camera.stopVideo();
//            }
//        }

        Log.i("==TAG==", "TwsDataValue.getCameraList().size()=" + TwsDataValue.cameraList().size());

    }


    /**
     * 如果是默认密码或者密码格式不符合要求，显示修改摄像机密码提示dialog
     */
    private void modifyPasswordHint(final IMyCamera camera) {
        if (!isShowModifyPwdDlg && CameraFragment.this.getContext() != null) {

            isShowModifyPwdDlg = true;
            if(modifyPasswordAlert != null && modifyPasswordAlert.isShowing()){
                modifyPasswordAlert.dismiss();
            }
            Builder dlgBuilder = new Builder(CameraFragment.this.getContext());
            dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);

            dlgBuilder.setMessage(String.format(getString(R.string.dialog_msg_strict_change_pwd), camera.getNickName()));
            dlgBuilder.setCancelable(false);
            dlgBuilder.setPositiveButton(getText(R.string.process_change),
                    new DialogInterface.OnClickListener() {//点击修改按钮跳转至高级设置界面修改设备密码
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.putExtra(TwsDataValue.EXTRA_KEY_UID, camera.getUid());
                            intent.setClass(CameraFragment.this.getActivity(), ModifyCameraPasswordActivity.class);
                            startActivity(intent);
                        }
                    });
            if (!MyConfig.isStrictPwd()) {//如果不是强制更改密码则显示取消按钮
                dlgBuilder.setNegativeButton(getText(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
            }

            modifyPasswordAlert = dlgBuilder.show();//
            modifyPasswordAlert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {

                }
            });
        }
    }

    /**
     * 获取在界面中该camera的view
     *
     * @param camera
     * @return camera所对应的View
     */
    public View getCameraView(IMyCamera camera) {
        int index = TwsDataValue.cameraList().indexOf(camera);//1.获得该camera在cameraList中的位置
        if (gridViews.size() < index / (rows * columns)) {//超出界限
            return null;
        }
        GridView gridView = gridViews.get(index / (rows * columns));//2.是哪个gridview
        if (gridView.getCount() < index % (rows * columns)) {//gridview 中的items 的位置与camera应在的位置不同
            return null;
        }
        View view = gridView.getChildAt(index % (rows * columns));//3.获取gridview item
        return view;
    }


    /**
     * onPause时：
     * 1.stopAllVideoNoIndex(-1)
     * 2.所有camera camera.unregisterIOTCListener(this);
     * 3.NSNotificationCenter removeObserver(this)
     */
    @Override
    public void onPause() {
        super.onPause();

//		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
//		nc.removeObserver(this);
    }


    /**
     * 设置当前的gridView相关参数
     *
     * @param gridView
     * @param cells
     */
    private void setGridView(GridView gridView, List<Map<String, Object>> cells) {

        gridView.setNumColumns(1);// 2列
        // gridView.setColumnWidth(50);
        gridView.setHorizontalSpacing(0);
        gridView.setVerticalSpacing(0);
        gridView.setPadding(0, 0, 0, 0);
        adapter = new VideoViewAdapter(CameraFragment.this.getActivity(), cells,
                R.layout.camera_main_item, new String[]{"cameraName"},
                new int[]{R.id.txt_nikename});
        gridView.setAdapter(adapter);
        adapter.setOnButtonClickListener(new VideoViewAdapter.OnButtonClickListener() {

            @Override
            public void onButtonClick(int btnId, final IMyCamera camera) {
                Bundle extras = new Bundle();
                extras.putString(TwsDataValue.EXTRA_KEY_UID, camera.getUid());
                Intent intent = new Intent();
                intent.putExtras(extras);
                // TODO Auto-generated method stub
                if (btnId == R.id.btn_item_delete) {
                    CameraFragment.this.deleteCamera(camera);
                } else if (btnId == R.id.btn_item_setting) {
                    if (camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P) {
                        intent.setClass(CameraFragment.this.getActivity(), DeviceSetting_HichipActivity.class);
                    } else {
                        if(camera.getSupplier()== IMyCamera.Supllier.AN){
                            intent.setClass(CameraFragment.this.getActivity(), DeviceSetting_AoniActivity.class);
                        }
                        else {
                            intent.setClass(CameraFragment.this.getActivity(), DeviceSettingActivity.class);
                        }
                    }
                    startActivity(intent);
                } else if (btnId == R.id.btn_item_event) {
                    if (camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P) {
                        intent.setClass(CameraFragment.this.getActivity(), EventList_HichipActivity.class);
                    } else {
                        if(camera.getSupplier()== IMyCamera.Supllier.AN){
                            intent.setClass(CameraFragment.this.getActivity(), EventList_AoniActivity.class);
                        }
                        else{
                            intent.setClass(CameraFragment.this.getActivity(), EventListActivity.class);
                        }
                    }
                    startActivity(intent);
                } else if (btnId == R.id.btn_play) {
//                    if(camera.getP2PType() == IMyCamera.CameraP2PType.TutkP2P) {
//                        camera.sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_SETSTREAMCTRL_REQ, AVIOCTRLDEFs.SMsgAVIoctrlSetStreamCtrlReq.parseContent(0, (byte) (camera.getVideoQuality())));
//                    }
                    //camera.asyncStartVideo(null);
                    if (camera.getEventNum(0) > 0) {
                        NotificationManager manager = (NotificationManager) CameraFragment.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                        int eventnum = camera.clearEventNum(CameraFragment.this.getContext(),0);
                        int intId = camera.getIntId();
                        manager.cancel(camera.getUid(), intId);
                    }

                    intent.setClass(CameraFragment.this.getActivity(), camera.getP2PType() == IMyCamera.CameraP2PType.HichipP2P? LiveView_HichipActivity.class : LiveViewActivity.class);
                    startActivity(intent);
                } else if (btnId == R.id.btn_modifyPassword) {
                    camera.stop();
                    showPasswordWrongHint(camera);
                } else if (btnId == R.id.btn_reconnect) {
                    Log.i("click button1",System.currentTimeMillis()+"");
                    camera.asyncStop(new IMyCamera.TaskExecute() {
                        @Override
                        public void onPosted(IMyCamera c, Object data) {
                            c.start();
                            Log.i("click button2",System.currentTimeMillis()+"");
                        }
                    });
                }
                else if(btnId == R.id.btn_wakeup){
                    Log.i("click button1",System.currentTimeMillis()+"");
                    if(!camera.isWakingUp()){
                        camera.asyncWakeUp(new IMyCamera.TaskExecute() {
                            @Override
                            public void onPosted(final IMyCamera camera, Object data) {
                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        camera.start();
                                        Log.i("click button3",System.currentTimeMillis()+"");
                                    }
                                }, 3000);
                                CameraFragment.this.getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        refreshItems();
                                        Log.i("click button2",System.currentTimeMillis()+"");
                                    }
                                });
                            }
                        });
                    }
                }

                else if (btnId == R.id.img_snapshot) {
                    if (camera.isPasswordWrong()) {
                        camera.stop();
                        showPasswordWrongHint(camera);
                    } else if (camera.isConnected()) {
                        camera.asyncStartVideo(null);

                        if (camera.getEventNum(0) > 0) {
                            NotificationManager manager = (NotificationManager) CameraFragment.this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            int eventnum = camera.clearEventNum(CameraFragment.this.getContext(),0);
                            int intId = camera.getIntId();
                            manager.cancel(camera.getUid(), intId);
                        }
                        intent.setClass(CameraFragment.this.getActivity(), LiveViewActivity.class);
                        startActivity(intent);
                    } else {
                        camera.asyncStop(new IMyCamera.TaskExecute() {
                            @Override
                            public void onPosted(IMyCamera c, Object data) {
                                c.start();
                            }
                        });
                    }
                } else if (btnId == R.id.btn_item_edit) {
                    intent.setClass(CameraFragment.this.getActivity(), EditDeviceActivity.class);
                    startActivity(intent);
                } else if (btnId == R.id.ll_mask_image) {

                    if (camera.isDisconnect()) {
                        if (CameraFragment.this.getContext() != null) {
                            ConnectionState.getInstance(CameraFragment.this.getContext()).CheckConnectState();
                            if (ConnectionState.getInstance(CameraFragment.this.getContext()).isNoneConnected()) {
                                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(CameraFragment.this.getContext());
                                dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                                dlgBuilder.setTitle(R.string.warning);
                                dlgBuilder.setMessage(getString(R.string.dialog_msg_no_network));

                                dlgBuilder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }).show();
                            } else {
                                camera.asyncStop(new IMyCamera.TaskExecute() {
                                    @Override
                                    public void onPosted(IMyCamera c, Object data) {
                                        c.start();
                                    }
                                });
                            }
                        }
                    }
                }

            }

        });

    }

    /**
     * 更新界面
     */
    protected void updatePager() {

        gridViews.clear();
        if (pageradapter != null) {
            pageradapter.notifyDataSetChanged();
        }

        GridView gridView = null;

        //camera_list_count += 1;	//添加按钮

        cells = new ArrayList<Map<String, Object>>();
        gridView = new GridView(CameraFragment.this.getActivity());
        setGridView(gridView, cells);//设置当前gridview的样式
        gridViews.add(gridView);

        int no_in_page;
        IMyCamera camera;
        int camera_list_count = TwsDataValue.cameraList().size();
        for (no_in_page = 0; no_in_page < camera_list_count; no_in_page++) {
            Map<String, Object> cell = new HashMap<String, Object>();
            camera = TwsDataValue.cameraList().get(no_in_page);
            cell.put("cameraUid", camera.getUid());
            cell.put("object", camera);
            cells.add(cell);
        }

        pageradapter = new GridViewPagerAdapter(gridViews);
        viewpager = (ViewPager) view.findViewById(R.id.gridViewPager);
        viewpager.setAdapter(pageradapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (receiver == null) {
            receiver = new CameraBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(TwsDataValue.ACTION_CAMERA_INIT_END);
            filter.addAction(TwsDataValue.ACTION_CAMERA_REFRESH);
            filter.addAction(TwsDataValue.ACTION_CAMERA_ALARM_EVENT);
            filter.addAction(TwsDataValue.ACTION_CAMERA_REFRESH_ONE_ITEM);
            CameraFragment.this.getActivity().registerReceiver(receiver, filter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        TwsDataValue.setTryConnectcamera(null);
        isShowModifyPwdDlg = false;
        refreshTitle();
        refreshItems();
        if(modifyPasswordAlert != null){
            if(modifyPasswordAlert.isShowing()) {
                modifyPasswordAlert.dismiss();
            }
        }
        for (IMyCamera camera : TwsDataValue.cameraList()) {
            if (camera.isConnected() && camera.getPassword().equalsIgnoreCase(IMyCamera.DEFAULT_PASSWORD)) {
                modifyPasswordHint(camera);
                break;
            } else if (camera.isNotConnect()) {
                camera.registerIOTCListener(CameraFragment.this);
                camera.asyncStart(null);
            }
        }
        Bundle bundle = this.getActivity().getIntent().getExtras();
        if (bundle != null) {
//            String remoteEventUID = GCMUtils.GetRemoteEventCamera(this.getContext());
//            if (remoteEventUID != null) {
//                for (NSCamera camera : TwsDataValue.getCameraList()) {
//                    if (camera.uid.equalsIgnoreCase(remoteEventUID)) {
//                        if (!((MyCamera) camera).isPlaying()) {
//                            Intent intent = new Intent();
//                            intent.putExtras(this.getActivity().getIntent().getExtras());
//                            intent.setClass(CameraFragment.this.getActivity(), LiveViewActivity.class);
//                            startActivity(intent);
//                        }
//                    }
//                }
//            }
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
        Log.i(this.getClass().getSimpleName(), "connect state " + camera.getUid() + " " + resultCode);
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = resultCode;
        msg.obj = camera;
        handler.sendMessage(msg);
    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Message msg = handler.obtainMessage();
        msg.what = TwsDataValue.HANDLE_MESSAGE_IO_RESP;
        msg.arg1 = avIOCtrlMsgType;
        Bundle bundle = new Bundle();
        bundle.putByteArray("data", data);
        msg.setData(bundle);
        msg.obj = camera;
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


    /**
     * 滑动的最小间距和速度设置
     *
     * @author Administrator
     */
    public static class SnsConstant {
        private static final int FLING_MIN_DISTANCE = 50;
        private static final int FLING_MIN_VELOCITY = 0;

        public static int getFlingMinDistance() {
            return FLING_MIN_DISTANCE;
        }

        public static int getFlingMinVelocity() {
            return FLING_MIN_VELOCITY;
        }
    }

    /**
     * 显示密码错误的对话框
     *
     * @param camera
     */
    public void showPasswordWrongHint(final IMyCamera camera) {

        Log.i("1233333", "==showPasswordWrongHint==");
        if (CameraFragment.this.getContext() == null) {
            return;
        }
        if (dlg != null && dlg.isShowing()) {
            return;
        }
        Builder builder = new AlertDialog.Builder(CameraFragment.this.getContext());
        dlg = builder.create();
        dlg.setTitle(getString(R.string.camera_state_passwordWrong));
        dlg.setIcon(android.R.drawable.ic_menu_save);
        LayoutInflater inflater = dlg.getLayoutInflater();
        View view = inflater.inflate(R.layout.hint_password_error, null);
        dlg.setView(view);
        dlg.setCanceledOnTouchOutside(false);
        TextView txt_desc =  view.findViewById(R.id.txt_desc);
        txt_desc.setText(String.format(getString(R.string.lab_camera_password_reenter),camera.getNickName()));
        final EditText passwordEditText = (EditText) view.findViewById(R.id.cameraPasswordEditText);
        final Button btnOK = (Button) view.findViewById(R.id.btnOK);
        final Button btnCancel = (Button) view.findViewById(R.id.btnCancel);

        btnOK.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dlg.dismiss();
                dlg = null;
                String newPassword = passwordEditText.getText().toString();
                camera.setPassword(newPassword);
                camera.sync2Db(CameraFragment.this.getContext());
                //camera.stop();
                camera.start();
                //camera.startVideo();
            }
        });

        btnCancel.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                dlg.dismiss();
                dlg = null;
            }
        });

        dlg.show();

    }

    /**
     * 删除设备
     */
    public void deleteCamera(final IMyCamera camera) {
        //显示确认删除该摄像机的提示对话框
        Builder dlgBuilder = new Builder(CameraFragment.this.getActivity());
        dlgBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        dlgBuilder.setTitle(getText(R.string.warning));
        dlgBuilder.setMessage(getText(R.string.dialog_msg_remove_camera_confirm));
        //dlgBuilder.setMessage("UID:"+camera.uid+"\n"+getText(R.string.tips_remove_camera_confirm));
        dlgBuilder.setPositiveButton(getText(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @SuppressLint("RestrictedApi")
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (int i = 0; i < cells.size(); i++) {
                            if (cells.get(i).get("object") == camera) {
                                cells.remove(i);
                            }
                        }
                        camera.closePush(CameraFragment.this.getContext());
                        camera.asyncStop(null);
                        camera.remove(CameraFragment.this.getContext());
                        try {
                            ((FolderFragment) CameraFragment.this.getFragmentManager().getFragments().get(1)).initView();
                        } catch (Exception ex) {

                        }
                        refreshItems();
                        refreshTitle();
                    }
                });
        dlgBuilder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dlgBuilder.show();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onActivityResult");
        //TextView navigationTextView = (TextView)view.findViewById(R.id.navigationTextView);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
        } else if (requestCode == PUBLIC_CAMERA_REQUEST_CODE) {
        } else if (resultCode == CAMERA_ADD_REQUEST_CODE) {

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class CameraBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub

            if (intent.getAction().equals(TwsDataValue.ACTION_CAMERA_INIT_END)) {
                //updatePager();
                refreshItems();
                for (final IMyCamera camera : TwsDataValue.cameraList()) {
                    camera.registerIOTCListener(CameraFragment.this);
                    if (!camera.isConnected() && !camera.isPasswordWrong()) {
                        camera.asyncStop(new IMyCamera.TaskExecute() {
                            @Override
                            public void onPosted(IMyCamera c, Object data) {
                                c.start();
                            }
                        });
                    }
                }
            } else if (intent.getAction().equals(TwsDataValue.ACTION_CAMERA_REFRESH)) {
                // refreshItems();
            } else if (intent.getAction().equals(TwsDataValue.ACTION_CAMERA_REFRESH_ONE_ITEM)) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);
                    if (uid != null) {
                        for (IMyCamera c : TwsDataValue.cameraList()) {
                            if (c.getUid().equals(uid)) {
                                View cameraView = getCameraView(c);//.findViewById(R.id.img_push_alarm)
                                if (cameraView != null) {
                                    if (c.getEventNum(0) > 0) {
                                        cameraView.findViewById(R.id.img_push_alarm).setVisibility(View.VISIBLE);
                                    }
                                    Bitmap snap = c.getSnapshot();
                                    if (snap == null || snap.isRecycled()) {
                                        if (TwsTools.isSDCardValid()) {
                                            try {
                                                BitmapFactory.Options opts = new BitmapFactory.Options();
                                                opts.inJustDecodeBounds = true;
                                                String snapshotPath = TwsTools.getFilePath(uid, TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB) + "/" + TwsTools.getFileNameWithTime(uid, TwsTools.PATH_SNAPSHOT_LIVEVIEW_AUTOTHUMB);
                                                snap = BitmapFactory.decodeFile(snapshotPath, opts);
                                                opts.inSampleSize = opts.outWidth / 640;
                                                if (opts.inSampleSize < 1) {
                                                    opts.inSampleSize = 1;
                                                }
                                                opts.inJustDecodeBounds = false;
                                                snap = BitmapFactory.decodeFile(snapshotPath, opts);
                                                c.setSnapshot(snap);
                                            } catch (OutOfMemoryError error) {

                                            }
                                        }
                                    }
                                    if (snap == null || !snap.isRecycled()) {
                                        ((ImageView) cameraView.findViewById(R.id.img_snapshot)).setImageBitmap(snap);
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
                //refreshItems();
            }
            else if(intent.getAction().equals(TwsDataValue.ACTION_CAMERA_ALARM_EVENT)){
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    String uid = bundle.getString(TwsDataValue.EXTRA_KEY_UID);
                    int evtType = bundle.getInt(TwsDataValue.EXTRA_ALARM_EVENT_ID);
                    if (uid != null) {
                        for (IMyCamera c : TwsDataValue.cameraList()) {
                            if (c.getUid().equals(uid)) {
                                View cameraView = getCameraView(c);//.findViewById(R.id.img_push_alarm)
                                if (cameraView != null) {
                                    if(c.getSupplier() == IMyCamera.Supllier.AN){
                                        //电池事件
                                        if(evtType >= 7 && evtType <= 10){
                                            c.sendIOCtrl(0, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GET_BAT_PRAM_REQ, AVIOCTRLDEFs.SMsgAVIoctrlDeviceInfoReq.parseContent());
                                        }
                                        else if(evtType > 10){
                                            if(!c.isSleeping() && !c.isConnected() && !c.isPasswordWrong()){
                                                c.asyncStop(new IMyCamera.TaskExecute() {
                                                    @Override
                                                    public void onPosted(IMyCamera camera, Object data) {
                                                        camera.start();
                                                    }
                                                });
                                            }
                                        }
                                    }
                                    else{
                                        if (c.getEventNum(0) > 0) {
                                            cameraView.findViewById(R.id.img_push_alarm).setVisibility(View.VISIBLE);
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }

    }

    private void afterInit() {
        updatePager();
        for (IMyCamera camera : TwsDataValue.cameraList()) {
            camera.registerIOTCListener(CameraFragment.this);
            if (camera.isNotConnect()) {
                //camera.start();
            }
        }
    }

    void refreshItems() {
        int no_in_page;
        IMyCamera camera;
        int camera_list_count = TwsDataValue.cameraList().size();
        cells.clear();
        for (no_in_page = 0; no_in_page < camera_list_count; no_in_page++) {
            Map<String, Object> cell = new HashMap<String, Object>();
            camera = TwsDataValue.cameraList().get(no_in_page);
            cell.put("cameraUid", camera.getUid());
            cell.put("object", camera);
            cells.add(cell);
        }
        if (pageradapter != null) {
            pageradapter.notifyDataSetChanged();
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (ll_container_first_add_tip == null) {
            ll_container_first_add_tip = (LinearLayout) view.findViewById(R.id.
                    ll_container_first_add_tip);
            ImageView btn_first_add_camera = (ImageView) view.findViewById(R.id.btn_first_add_camera);
            btn_first_add_camera.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addButton.performClick();
                }
            });
        }
        if (TwsDataValue.cameraList().size() == 0) {
            ll_container_first_add_tip.setVisibility(ViewStub.VISIBLE);
            //viewpager.setVisibility(ViewStub.GONE);
        } else {
            ll_container_first_add_tip.setVisibility(ViewStub.GONE);
            //viewpager.setVisibility(ViewStub.VISIBLE);
        }
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
                           float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean onTouch(View arg0, MotionEvent arg1) {
        // TODO Auto-generated method stub
        return false;
    }
}
