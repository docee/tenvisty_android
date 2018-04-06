package com.tws.commonlib.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tutk.IOTC.NSCamera.CAMERA_MODEL;
import com.tutk.IOTC.st_LanSearchInfo;
import com.tws.commonlib.R;
import com.tws.commonlib.base.SearchLanAsync;
import com.tws.commonlib.bean.IMyCamera;
import com.tws.commonlib.bean.TwsDataValue;
import com.tws.commonlib.controller.NavigationBar;

import java.util.ArrayList;
import java.util.List;

//import com.tenvis.P2P.global.SmartLink;


/**
 * 搜索局域网内摄像机列表，并显示
 *
 * @author Administrator
 */
public class SearchCameraActivity extends BaseActivity {
    private final static int GO_TO_ADD_MANUALLY = 0;
    private final static int GO_TO_ADD_ONEKEY_WIFI = 1;
    private List<SearchResult> list = new ArrayList<SearchResult>();
    private SearchResultListAdapter adapter = null;
    private ProgressBar progressBar = null;
    private ProgressBar progressBar_whole = null;
    private ListView lstSearchResult = null;
    private final int searchTime = 2000;
    SearchLanAsync searchLan;
    boolean isSearching;
    LinearLayout lay_fail_lan_search;
    //private WifiManager mWifiManager = null;
    //private ConnectivityManager connManager = null;

    /**
     * 如果在局域网里，没有搜索到设备，显示具有链接的文字，链接到购物界面（根据是否有购物界面进行显示）
     */
    //WifiManager.MulticastLock lock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_camera);
        this.setTitle(getResources().getString(R.string.title_seach_camera));
        initView();
    }//oncreate

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
                        research(title.GetView(which));
                        break;
                }
            }
        });
        lay_fail_lan_search = (LinearLayout) findViewById(R.id.lay_fail_lan_search);
        isSearching = false;
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar_whole = (ProgressBar) findViewById(R.id.progressBar_whole);
        lstSearchResult = (ListView) findViewById(R.id.cameraListView);
        adapter = new SearchResultListAdapter(this.getLayoutInflater());
        lstSearchResult.setAdapter(adapter);
        lstSearchResult.setOnItemClickListener(new OnItemClickListener() {//点击事件监听
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                SearchResult searchResult = list.get(position);
                //如果该摄像机已经在自己摄像机列表中，则弹出相关提示对话框
                for (IMyCamera camera : TwsDataValue.cameraList()) {
                    if (camera.getUid().equalsIgnoreCase(searchResult.uid)) {
                        Toast.makeText(SearchCameraActivity.this.getApplicationContext(), getText(R.string.toast_add_camera_duplicated_search), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                Intent intentFrom = SearchCameraActivity.this.getIntent();
                if(intentFrom != null){
                    String from = intentFrom.getStringExtra(TwsDataValue.EXTRAS_KEY_FROM);
                    if(from != null && from.equals(SaveCameraActivity.class.getName())){
                        setResult(RESULT_OK,new Intent().putExtra(TwsDataValue.EXTRA_KEY_UID,searchResult.uid));
                        finish();
                        return;
                    }
                }
                //否则跳转至添加摄像机的界面
                Bundle extras = new Bundle();
                extras.putString(TwsDataValue.EXTRA_KEY_UID, searchResult.uid);
                Intent intent = new Intent();
                intent.putExtras(extras);
                intent.setClass(SearchCameraActivity.this, SaveCameraActivity.class);
                startActivity(intent);
            }
        });
        research(title.GetView(NavigationBar.NAVIGATION_BUTTON_RIGHT));
    }

    /**
     * 搜索摄像机
     *
     * @param view
     */
    public void research(View view) {
        //如果是点击右上角的搜索按钮，该按钮暂时设置不可用，等搜索完后设置为可用
        if (isSearching) {
            return;
        }
        if (searchLan != null) {
            searchLan.stopSearch();
        }
        isSearching = true;
        lay_fail_lan_search.setVisibility(View.GONE);
        list.clear();//列表清空
        adapter.notifyDataSetChanged();
        //MjpegCamera.search(searchTime);//非P2P摄像机搜索
        progressBar_whole.setVisibility(View.VISIBLE);//进度条显示
        progressBar.setVisibility(View.INVISIBLE);//进度条显示
        searchLan = new SearchLanAsync(new SearchLanAsync.ISearchResultListener() {
            @Override
            public void onReceiveSearchResult(st_LanSearchInfo resp, int status) {
                if (status == 2) {
                    isSearching = false;
                    SearchCameraActivity.this.progressBar_whole.setVisibility(View.VISIBLE);//进度条显示
                    SearchCameraActivity.this.progressBar.setVisibility(View.INVISIBLE);//进度条显示
                } else if (status == 1 && resp != null) {
                    String uidString = new String(resp.UID).trim();
                    String ipString = new String(resp.IP).trim();
                    String portString = Integer.toString(resp.port);
                    Boolean isFindSameDevice = false;
                    for (SearchResult searchResult : list) {
                        if (searchResult.uid.equals(uidString)) {//如果列表中已经有设备有相同的IP
                            isFindSameDevice = true;
                            break;
                        }
                    }
                    if (!isFindSameDevice) {
                        list.add(new SearchResult(uidString, ipString, portString, "", CAMERA_MODEL.CAMERA_MODEL_H264));
                        adapter.notifyDataSetChanged();
                    }
                    SearchCameraActivity.this.progressBar_whole.setVisibility(View.INVISIBLE);//进度条显示
                    SearchCameraActivity.this.progressBar.setVisibility(View.VISIBLE);//进度条显示
                }
                //搜索结束
                else if (status == 0) {
                    if (list.size() == 0) {
                        lay_fail_lan_search.setVisibility(View.VISIBLE);
                    }
                    searchLan = null;
                    isSearching = false;
                    SearchCameraActivity.this.progressBar_whole.setVisibility(View.INVISIBLE);//进度条显示
                    SearchCameraActivity.this.progressBar.setVisibility(View.INVISIBLE);//进度条显示
                }
            }
        });
        searchLan.beginSearch();
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				//lock.acquire();
//				st_LanSearchInfo[] arrResp = Camera.SearchLAN();//P2P摄像机搜索
//				//lock.release();
//				if (arrResp != null && arrResp.length > 0) {//搜索到P2P设备
//					for (st_LanSearchInfo resp : arrResp) {
//
//						String uidString = new String(resp.UID).trim();
//						String ipString = new String(resp.IP).trim();
//						String portString = Integer.toString(resp.port);
//						Boolean isFindSameDevice = false;
//						for (SearchResult searchResult : list) {
//							if (searchResult.ip.equals(ipString)) {//如果列表中已经有设备有相同的IP
//								if (searchResult.cameraModel == CAMERA_MODEL.CAMERA_MODEL_MJPEG) {
//									list.remove(searchResult);
//								}else {
//									isFindSameDevice = true;
//								}
//								break;
//							}
//						}
//						if (!isFindSameDevice) {
//							list.add(new SearchResult(uidString, ipString, portString,"", CAMERA_MODEL.CAMERA_MODEL_H264));
//						}
//					}
//				}else if (arrResp == null&&list.size()==0) {//如果搜索结果为空并且结果列表为空则没有搜索到设备
//					SearchCameraActivity.this.runOnUiThread(new Runnable() {
//						@Override
//						public void run() {
//
//
//						}
//					});
//				}
//
//				SearchCameraActivity.this.runOnUiThread(new Runnable() {
//					@Override
//					public void run() {
//						if (button != null) {
//							button.setEnabled(true);
//						}
//						adapter.notifyDataSetChanged();
//						dialogProgress.setVisibility(View.INVISIBLE);
//					}
//				});
//
//			}
//		}).start();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (searchLan != null) {
            searchLan.stopSearch2();
        }
        isSearching = false;
    }

    /**
     * 显示搜索出来的摄像机表Adapter
     *
     * @author Administrator
     */
    private class SearchResultListAdapter extends BaseAdapter {

        private LayoutInflater mInflater;

        public SearchResultListAdapter(LayoutInflater inflater) {

            this.mInflater = inflater;
        }

        @Override
        public int getCount() {

            return list.size();
        }

        @Override
        public Object getItem(int position) {

            return list.get(position);
        }

        @Override
        public long getItemId(int position) {

            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final SearchResult result = (SearchResult) getItem(position);
            ViewHolder holder = null;

            if (convertView == null) {

                convertView = mInflater.inflate(R.layout.item_list_search_camera, null);
                holder = new ViewHolder();
                holder.uid = (TextView) convertView.findViewById(R.id.uid);
                holder.ip = (TextView) convertView.findViewById(R.id.ip);
                convertView.setTag(holder);

            } else {

                holder = (ViewHolder) convertView.getTag();
            }


            holder.uid.setText(result.uid);
            if (result.ddnsUser.length() != 0) {
                holder.uid.setText(result.ddnsUser);
                //Log.i("result.ddnsUser", "--->>>>"+result.ddnsUser);
            }
            holder.ip.setText(result.ip);
            if (result.hasAdded) {
                holder.uid.setTextColor(Color.rgb(206, 206, 206));
                holder.ip.setTextColor(Color.rgb(206, 206, 206));
            } else {
                holder.uid.setTextColor(Color.rgb(122, 122, 122));
                holder.ip.setTextColor(Color.rgb(153, 153, 153));
            }
            return convertView;
        }// getView()

        public final class ViewHolder {
            public TextView uid;
            public TextView ip;
        }
    }


    /**
     * 保存每个单独的搜索结果信息
     *
     * @author Administrator
     */
    private class SearchResult {

        public String uid;
        public String ip;
        public String port;
        public String ddnsUser;
        public CAMERA_MODEL cameraModel;
        public boolean hasAdded;

        public SearchResult(String uid_, String ip_, String port_, String ddnsUser_, CAMERA_MODEL cameraModel_) {

            uid = uid_;
            ip = ip_;
            port = port_;
            ddnsUser = ddnsUser_;
            cameraModel = cameraModel_;
            for (IMyCamera camera : TwsDataValue.cameraList()) {
                if (camera.getUid().equalsIgnoreCase(uid_)) {
                    hasAdded = true;
                    break;
                }
            }
        }
    }

    /**
     * 跳转到购物界面
     *
     * @param v
     */
    public void toShopping(View v) {
//		Intent intent = new Intent(SearchCameraActivity.this,
//				ShoppingActivity.class);
//		startActivity(intent);
    }

}
