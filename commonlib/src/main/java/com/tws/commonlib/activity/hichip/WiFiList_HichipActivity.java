package com.tws.commonlib.activity.hichip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hichip.content.HiChipDefines;
import com.tutk.IOTC.AVIOCTRLDEFs;
import com.tutk.IOTC.Camera;
import com.tutk.IOTC.L;
import com.tutk.IOTC.Packet;
import com.tws.commonlib.R;
import com.tws.commonlib.activity.BaseActivity;
import com.tws.commonlib.activity.setting.WiFiSetActivity;
import com.tws.commonlib.base.TwsTools;
import com.tws.commonlib.bean.IIOTCListener;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * 添加摄像机的界面
 *
 * @author Administrator
 */
public class WiFiList_HichipActivity extends BaseActivity implements IIOTCListener {

    private String dev_uid;
    private IMyCamera camera;
    String newPassword;
    ListView listview_wifilist;
    private ProgressBar progress_loading;
    private LinearLayout ll_fail_wifi_search;
    WiFiListAdapter adapter;
    String connctedSsid;
    int connectStatus;
    private final static int REQUEST_SET_WIFI = 0x999;
    private static List<AVIOCTRLDEFs.SWifiAp> m_wifiList = new ArrayList<AVIOCTRLDEFs.SWifiAp>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_wifi_list);
        dev_uid = this.getIntent().getExtras().getString(TwsDataValue.EXTRA_KEY_UID);
        for (IMyCamera _camera : TwsDataValue.cameraList()) {
            if (_camera.getUid().equalsIgnoreCase(dev_uid)) {
                camera =  _camera;
                break;
            }
        }
        this.setTitle(getResources().getString(R.string.title_setting_wifi));
        initView();
        camera.registerIOTCListener(this);
    }

    @Override
    protected void initView() {
        super.initView();
        final NavigationBar title = (NavigationBar) findViewById(R.id.title_top);
        title.setButton(NavigationBar.NAVIGATION_BUTTON_RIGHT, R.drawable.ic_refresh);
        title.setNavigationBarButtonListener(new NavigationBar.NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case NavigationBar.NAVIGATION_BUTTON_RIGHT:
                        searchWifi();
                        break;
                }
            }
        });
        listview_wifilist = (ListView) findViewById(R.id.listview_wifilist);
        ll_fail_wifi_search = (LinearLayout) findViewById(R.id.ll_fail_wifi_search);
        progress_loading = (ProgressBar) findViewById(R.id.progress_loading);
        adapter = new WiFiListAdapter(this);
        listview_wifilist.setAdapter(adapter);
        listview_wifilist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AVIOCTRLDEFs.SWifiAp selectedWifi = m_wifiList.get(position);
                if (selectedWifi.status != 1) {
                    Intent intent = new Intent();
                    intent.setClass(WiFiList_HichipActivity.this, WiFiSet_HichipActivity.class);
                    Bundle extras = new Bundle();
                    extras.putByte("enctype", selectedWifi.enctype);
                    extras.putByte("mode", selectedWifi.mode);
                    extras.putString(TwsDataValue.EXTRA_KEY_UID, dev_uid);
                    extras.putByteArray("ssid", selectedWifi.ssid);
                    intent.putExtras(extras);
                    startActivityForResult(intent, REQUEST_SET_WIFI);
                }
            }
        });
        searchWifi();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SET_WIFI) {
            if (resultCode == RESULT_OK) {
                //searchWifi();
            }
            adapter.notifyDataSetChanged();
        }

    }

    void searchWifi() {
        progress_loading.setVisibility(View.VISIBLE);
        listview_wifilist.setVisibility(View.GONE);
        ll_fail_wifi_search.setVisibility(View.GONE);
        camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, HiChipDefines.HI_P2P_GET_WIFI_LIST, new byte[0]);
       // camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_REQ, AVIOCTRLDEFs.SMsgAVIoctrlGetWifiReq.parseContent());
    }

    @Override
    public void receiveFrameData(IMyCamera camera, int avChannel, Bitmap bmp) {

    }

    @Override
    public void receiveFrameInfo(IMyCamera camera, int avChannel, long bitRate, int frameRate, int onlineNm, int frameCount, int incompleteFrameCount) {

    }

    @Override
    public void receiveSessionInfo(IMyCamera camera, int resultCode) {

    }

    @Override
    public void receiveChannelInfo(IMyCamera camera, int avChannel, int resultCode) {

    }

    @Override
    public void receiveIOCtrlData(IMyCamera camera, int avChannel, int avIOCtrlMsgType, byte[] data) {
        Bundle bundle = new Bundle();
        bundle.putInt("sessionChannel", avChannel);
        bundle.putByteArray("data", data);

        Message msg = new Message();
        msg.what = avIOCtrlMsgType;
        msg.arg1 = avChannel;
        msg.setData(bundle);
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

    public void doClickLL(View view) {
        ((LinearLayout) view).getChildAt(1).requestFocus();
    }


    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {

            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray("data");

            switch (msg.what) {

                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_RESP: {
                    dismissLoadingProgress();

                    m_wifiList.clear();
                    AVIOCTRLDEFs.SWifiAp connecttedAp = null;
                    if(msg.arg1 == 0){
                        int cnt = com.hichip.tools.Packet.byteArrayToInt_Little(data, 0);
                        int size = HiChipDefines.SWifiAp.getTotalSize();

                        if (cnt > 0 && data.length >= 40) {
                            int pos = 4;
                            for (int i = 0; i < cnt; i++) {
                                byte[] bty_ssid = new byte[32];
                                System.arraycopy(data, i * size + pos, bty_ssid, 0, 32);
                                byte mode = data[i * size + pos + 32];
                                byte enctype = data[i * size + pos + 33];
                                byte signal = data[i * size + pos + 34];
                                byte status = data[i * size + pos + 35];
                                if (connecttedAp == null && status != 0) {
                                    connecttedAp = new AVIOCTRLDEFs.SWifiAp(bty_ssid, mode, enctype, signal, status);
                                } else {
                                    m_wifiList.add(new AVIOCTRLDEFs.SWifiAp(bty_ssid, mode, enctype, signal, status));
                                }
                            }
                        }
                    }
                    Collections.sort(m_wifiList, new Comparator<AVIOCTRLDEFs.SWifiAp>() {
                        @Override
                        public int compare(AVIOCTRLDEFs.SWifiAp sWifiAp, AVIOCTRLDEFs.SWifiAp t1) {
                            return (sWifiAp.signal > t1.signal ? -1 : (sWifiAp.signal == t1.signal ? 0 : 1));
                        }
                    });
                    if (connecttedAp != null) {
                        m_wifiList.add(0, connecttedAp);
                    }
                    progress_loading.setVisibility(View.GONE);
                    if (m_wifiList.size() == 0) {
                        ll_fail_wifi_search.setVisibility(View.VISIBLE);
                    } else {
                        listview_wifilist.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();

                    break;
                }
                case AVIOCTRLDEFs.IOTYPE_USER_IPCAM_GETWIFI_RESP: {
                    byte[] ssid = new byte[32];
                    System.arraycopy(data, 0, ssid, 0, 32);

                    // byte mode = data[64];
                    // byte enctype = data[65];
                    // byte signal = data[66];
                    connectStatus = data[67];
                    connctedSsid = TwsTools.getString(ssid);
                    //camera.sendIOCtrl(Camera.DEFAULT_AV_CHANNEL, AVIOCTRLDEFs.IOTYPE_USER_IPCAM_LISTWIFIAP_REQ, AVIOCTRLDEFs.SMsgAVIoctrlListWifiApReq.parseContent());
                    L.i(WiFiList_HichipActivity.class, "IOTYPE_USER_IPCAM_LISTWIFIAP_REQ");
                    break;
                }
            }
            super.handleMessage(msg);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (camera != null) {
            camera.unregisterIOTCListener(this);
        }
    }

    public class WiFiListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public WiFiListAdapter(Context context) {
            this.mInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return m_wifiList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return m_wifiList.get(position);
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
            final AVIOCTRLDEFs.SWifiAp wifi = m_wifiList.get(position);

            if (wifi == null)
                return null;

            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.view_search_wifi_result, null);

                holder = new ViewHolder();
                holder.txt_ssid = (TextView) convertView.findViewById(R.id.txt_ssid);
                holder.txt_encrypt = (TextView) convertView.findViewById(R.id.txt_encrypt);
                holder.imageview_signal = (ImageView) convertView.findViewById(R.id.imageview_signal);
                //  holder.txt_desc = (TextView) convertView.findViewById(R.id.txt_desc);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (wifi.status == 1) {

            } else {
            }

            if (holder != null) {
                String strSsid = TwsTools.getString(wifi.ssid);
                holder.txt_ssid.setText(strSsid);
                Drawable ic_wifi = null;
                int wifi_signal_id = 0;
                if (wifi.signal > 90) {
                    wifi_signal_id = wifi.enctype == 1 ? R.drawable.ic_signal_wifi_4_bar_black_24dp : R.drawable.ic_signal_wifi_4_bar_lock_black_24dp;
                } else if (wifi.signal > 60) {
                    wifi_signal_id = wifi.enctype == 1 ? R.drawable.ic_signal_wifi_3_bar_black_24dp : R.drawable.ic_signal_wifi_3_bar_lock_black_24dp;
                } else if (wifi.signal > 30) {
                    wifi_signal_id = wifi.enctype == 1 ? R.drawable.ic_signal_wifi_2_bar_black_24dp : R.drawable.ic_signal_wifi_2_bar_lock_black_24dp;
                } else {
                    wifi_signal_id = wifi.enctype == 1 ? R.drawable.ic_signal_wifi_1_bar_black_24dp : R.drawable.ic_signal_wifi_1_bar_lock_black_24dp;
                }
                holder.imageview_signal.setImageResource(wifi_signal_id);
                //if (strSsid.equals(connctedSsid)) {
                    if (wifi.status == 1 || wifi.status == 3) {
                        holder.txt_encrypt.setText(getString(R.string.tips_wifi_connected));
                        holder.txt_ssid.setTextColor(ContextCompat.getColor(WiFiList_HichipActivity.this, R.color.colorPrimaryDarkest));
                    } else if (wifi.status == 2) {
                        holder.txt_encrypt.setText(getString(R.string.tips_wifi_wrong_password));
                        holder.txt_ssid.setTextColor(ContextCompat.getColor(WiFiList_HichipActivity.this, R.color.darkred));
                    } else if (wifi.status == 4) {
                        holder.txt_encrypt.setText(getString(R.string.tips_wifi_saved));
                        holder.txt_ssid.setTextColor(ContextCompat.getColor(WiFiList_HichipActivity.this, R.color.colorPrimaryDarkest));
                    }
                //}
                else {
                    holder.txt_ssid.setTextColor(ContextCompat.getColor(WiFiList_HichipActivity.this, R.color.black));
                    if (wifi.enctype == 1) {
                        holder.txt_encrypt.setText(getString(R.string.tips_wifi_open));
                    } else {
                        holder.txt_encrypt.setText(getString(R.string.tips_wifi_encrypted));
                    }
                }
                // holder.txt_desc.setText(wifi.status == 1?"connected":"1");

            }
            return convertView;

        }

        public final class ViewHolder {
            //			public ImageView img;
            public TextView txt_ssid;
            public TextView txt_encrypt;
            //public ImageView img_lock;
            // public TextView txt_desc;
            //			public TextView txt_;
            public ImageView imageview_signal;

        }


    }
}
