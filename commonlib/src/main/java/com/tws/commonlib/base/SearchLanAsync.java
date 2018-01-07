package com.tws.commonlib.base;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.tutk.IOTC.Camera;
import com.tutk.IOTC.st_LanSearchInfo;
import com.tws.commonlib.bean.MyCamera;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import com.hichip.tools.HiSearchSDK;
import com.hichip.tools.HiSearchSDK.HiSearchResult;
import com.tws.commonlib.bean.TwsDataValue;

/**
 * Created by Administrator on 2017/9/23.
 */

public class SearchLanAsync {

    private List<st_LanSearchInfo> deviceList = Collections.synchronizedList(new ArrayList<st_LanSearchInfo>());
    ThreadSearch searchThread;
    ISearchResultListener _listener;
    volatile int searchCount;
    volatile long maxWaitTime;
    volatile long beginTime;
    volatile SearchState state;
    private HiSearchSDK searchSDK;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_MESSAGE_SCAN_RESULT_BEGIN:
                    if (_listener != null) {
                        _listener.onReceiveSearchResult(null, 2);
                    }
                    break;
                case HANDLE_MESSAGE_SCAN_RESULT:
                    if (_listener != null) {
                        if(msg.obj instanceof HiSearchResult){
                            HiSearchResult hiresult = (HiSearchResult) msg.obj;
                            if (hiresult != null) {
                                for (st_LanSearchInfo info : deviceList) {
                                    if (TwsTools.getString(info.UID).equalsIgnoreCase(hiresult.uid)) {
                                        return;
                                    }
                                }
                                st_LanSearchInfo result = new st_LanSearchInfo();
                                result.IP =  hiresult.ip.getBytes();
                                result.UID = hiresult.uid.getBytes();
                                deviceList.add(result);
                                _listener.onReceiveSearchResult(result, 1);
                            }
                        }
                        else if(msg.obj instanceof st_LanSearchInfo){
                            st_LanSearchInfo result = (st_LanSearchInfo) msg.obj;
                            if (result != null) {
                                for (st_LanSearchInfo info : deviceList) {
                                    if (btsCmp(info.UID, result.UID, 20)) {
                                        return;
                                    }
                                }
                                deviceList.add(result);
                                _listener.onReceiveSearchResult(result, 1);
                            }
                        }
                    }
                    break;
                case HANDLE_MESSAGE_SCAN_RESULT_END:
                    if (_listener != null) {
                        _listener.onReceiveSearchResult(null, 0);
                    }
                    break;
            }

        }
    };

    public static boolean btsCmp(byte[] data1, byte[] data2, int len) {
        if (data1 == null && data2 == null) {
            return true;
        }
        if (data1 == null || data2 == null) {
            return false;
        }
        if (data1 == data2) {
            return true;
        }
        boolean bEquals = true;
        int i;
        for (i = 0; i < data1.length && i < data2.length && i < len; i++) {
            if (data1[i] != data2[i]) {
                bEquals = false;
                break;
            }
        }
        return bEquals;
    }

    private static final int HANDLE_MESSAGE_SCAN_RESULT = 0xAA01;
    private static final int HANDLE_MESSAGE_SCAN_RESULT_BEGIN = 0xAA02;
    private static final int HANDLE_MESSAGE_SCAN_RESULT_END = 0xAA03;

    public SearchLanAsync(ISearchResultListener listener) {
        this._listener = listener;
        searchCount = 0;
        maxWaitTime = 5000;
        state = SearchState.Stopped;
        searchSDK = new HiSearchSDK(new HiSearchSDK.ISearchResult() {
            @Override
            public void onReceiveSearchResult(HiSearchResult hiSearchResult) {
                if(SearchLanAsync.this.state == SearchState.Searching && (System.currentTimeMillis() - SearchLanAsync.this.beginTime) < SearchLanAsync.this.maxWaitTime){
                    String temp = hiSearchResult.uid.substring(0, 4);
                    if (!TextUtils.isEmpty(temp)) {
                        Message msg = handler.obtainMessage();
                        msg.obj = hiSearchResult;
                        msg.what = HANDLE_MESSAGE_SCAN_RESULT;
                        SearchLanAsync.this.handler.sendMessage(msg);
                    }
                }
                else{
                    SearchLanAsync.this.searchSDK.stop();
                }
            }
        });
    }

    public synchronized void beginSearch() {
        if (searchThread == null) {
            state = SearchState.Searching;
            beginTime = System.currentTimeMillis();
            searchCount = 1;
            deviceList.clear();
            searchThread = new ThreadSearch();
            searchThread.startThread();
            searchSDK.search2();
        }
    }

    public synchronized void stopSearch() {
        if (searchThread != null && searchThread.isRunning) {
            searchThread.stopThread();
            searchThread = null;
            if(_listener != null){
                _listener.onReceiveSearchResult(null, 0);
                _listener = null;
            }
        }
        if (state == SearchState.Searching) {
            state = SearchState.Stopping;
        }
        searchSDK.stop();
    }

    public SearchState getState() {
        return state;
    }

    public interface ISearchResultListener {
        void onReceiveSearchResult(st_LanSearchInfo var1, int status);
    }

    public enum SearchState {
        Searching,
        Stopping,
        Stopped
    }

    private class ThreadSearch extends BaseThread {
        private ThreadSearch() {
        }

        public void run() {
            Message beginMsg = SearchLanAsync.this.handler.obtainMessage();
            beginMsg.what = HANDLE_MESSAGE_SCAN_RESULT_BEGIN;
            SearchLanAsync.this.handler.sendMessage(beginMsg);
            while (SearchLanAsync.this.state == SearchState.Searching && (System.currentTimeMillis() - SearchLanAsync.this.beginTime) < SearchLanAsync.this.maxWaitTime) {
                SearchLanAsync.this.searchCount++;
                st_LanSearchInfo[] result = Camera.SearchLAN(SearchLanAsync.this.searchCount / 2 < 1 ? 1 : SearchLanAsync.this.searchCount / 2 * 150);
                if (result != null) {
                    for (int i = 0; i < result.length; i++) {
                        Message var7 = SearchLanAsync.this.handler.obtainMessage();
                        var7.what = HANDLE_MESSAGE_SCAN_RESULT;
                        var7.obj = result[i];
                        SearchLanAsync.this.handler.sendMessage(var7);
                    }
                }
            }
            SearchLanAsync.this.state = SearchState.Stopped;

            Message var7 = SearchLanAsync.this.handler.obtainMessage();
            var7.what = HANDLE_MESSAGE_SCAN_RESULT_END;
            SearchLanAsync.this.handler.sendMessage(var7);
        }
    }
}