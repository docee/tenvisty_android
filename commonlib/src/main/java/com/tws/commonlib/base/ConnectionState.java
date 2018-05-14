package com.tws.commonlib.base;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;


import com.hichip.HiSmartWifiSet;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ConnectionState {

	private List<INetworkChangeCallback> mNetworkChangeCallback = null;
	List<ScanResult> ScanResultlist;
	private Context context;
	private ConnectionState(Context context){
		this.context = context;
	    this.mNetworkChangeCallback = Collections.synchronizedList(new Vector<INetworkChangeCallback>());
	}
	
	private static ConnectionState sSingleton = null;      
	public static synchronized ConnectionState getInstance(Context context) {  
	        if (sSingleton == null) {  
	            sSingleton = new ConnectionState(context);
	        }  
	        return sSingleton;  
	}
	
	public static int  WIFI_CONNECT_STATE = 0;
	public static int  MOBILE_CONNECT_STATE = 1;
	public static int  NONE_CONNECT_STATE = 2;
	
	private int _connectState = -1;
	public void setConnectState(int _connectState) {
		this._connectState = _connectState;
		if(_connectState == WIFI_CONNECT_STATE){
			WifiState ws = checkWifiState();
			setWifiState(ws);
		}
		else{
			setWifiState(null);
		}
		synchronized (this.mNetworkChangeCallback) {
			 for (int i = 0; i < this.mNetworkChangeCallback.size(); i++) {
			    INetworkChangeCallback listener = (INetworkChangeCallback)this.mNetworkChangeCallback.get(i);
			    listener.receiveNetworkType(_connectState);
			 }
		}
	}

	public boolean isWifiConnected() {
		if(_connectState == -1){
			checkWifiState();
		}
		return this._connectState == WIFI_CONNECT_STATE;
	}
	
	public boolean isMobileConnected() {
		return this._connectState == MOBILE_CONNECT_STATE;
	}
	
	public boolean isNoneConnected() {
		return this._connectState == NONE_CONNECT_STATE;
	}
	
	public boolean is24GWifi(){
		if(getWifiState() != null){
			return getWifiState().frequency >= 2400 && getWifiState().frequency <= 2500;
		}
		return false;
	}
	
	public boolean isWpa(){
		if(getWifiState() != null){
			return getWifiState().capabilities.contains("WPA");
		}
		return false;
	}

	public boolean hasDetectSsid(String ssid){
		WifiState ws = checkWifiState(ssid);
		return ws != null;
	}
	
	public String getSsid(){
		if(getWifiState() != null){
			return getWifiState().ssid;
		}
		return null;
	}
	
	
	private WifiState wifiState;
	

	public WifiState getWifiState() {
		if(wifiState == null){
			wifiState = checkWifiState();
		}
		return wifiState;
	}

	private void setWifiState(WifiState wifiState) {
		this.wifiState = wifiState;
	}

	private WifiState checkWifiState(){
		ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService(Application.CONNECTIVITY_SERVICE);
		//int authMode = -1;
		ScanResultlist = null;
		if(connManager != null){
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(mWifi.isConnected()){
					this._connectState = WIFI_CONNECT_STATE;
					WifiManager	mWifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
		        	WifiInfo WifiInfo = mWifiManager.getConnectionInfo();
					List<WifiConfiguration> wifiConfigList = mWifiManager.getConfiguredNetworks();
				//当前连接SSID
				String currentSSid =WifiInfo.getSSID();
				if(currentSSid == "<unknown ssid>"){
					currentSSid = mWifi.getExtraInfo();
				}
				int icLen = currentSSid.length();
				if (currentSSid.startsWith("\"") && currentSSid.endsWith("\""))
				{
					currentSSid = currentSSid.substring(1, icLen - 1);
				}
				for (WifiConfiguration wifiConfiguration : wifiConfigList) {
					//配置过的SSID
					String configSSid = wifiConfiguration.SSID;
					int iLen = configSSid.length();
					if (configSSid.startsWith("\"") && configSSid.endsWith("\""))
					{
						configSSid = configSSid.substring(1, iLen - 1);
					}


					//比较networkId，防止配置网络保存相同的SSID
					if (currentSSid.equals(configSSid)&&WifiInfo.getNetworkId()==wifiConfiguration.networkId) {
						WifiState ws = null;
						if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
							ws = new WifiState(WifiInfo.getFrequency(),getSecurity(wifiConfiguration)>1?"WPA":"",currentSSid);
						}
						else{
							ScanResultlist = mWifiManager.getScanResults();
							for (ScanResult AccessPoint:ScanResultlist){
								 if (AccessPoint.BSSID.equalsIgnoreCase(WifiInfo.getBSSID()))
								 {
									 ws = new WifiState(AccessPoint.frequency,AccessPoint.capabilities,currentSSid);
								 }
							}
						}
						return ws;
						//int security =  getSecurity(wifiConfiguration);
					}
				}
			}
		}
		return null;
	}
	private WifiState checkWifiState(String currentSSid){
		ConnectivityManager connManager = (ConnectivityManager) this.context.getSystemService(Application.CONNECTIVITY_SERVICE);
		//int authMode = -1;
		if(connManager != null){
			NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if(mWifi.isConnected()){
				this._connectState = WIFI_CONNECT_STATE;
				WifiManager	mWifiManager = (WifiManager) this.context.getSystemService (Context.WIFI_SERVICE);
				if(ScanResultlist==null) {
					ScanResultlist = mWifiManager.getScanResults();
				}
				WifiState ws = null;
				for (ScanResult AccessPoint:ScanResultlist){
					if (AccessPoint.BSSID.equalsIgnoreCase("\""+currentSSid+"\""))
					{
						ws = new WifiState(AccessPoint.frequency,AccessPoint.capabilities,currentSSid);
						break;
					}
				}
				return ws;
			}
		}
		return null;
	}
	/**
	 * These values are matched in string arrays -- changes must be kept in sync
	 */
	static final int SECURITY_NONE = 0;
	static final int SECURITY_WEP = 1;
	static final int SECURITY_PSK = 2;
	static final int SECURITY_EAP = 3;

	int getSecurity(WifiConfiguration config) {
		if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_PSK)) {
			return SECURITY_PSK;
		}
		if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.WPA_EAP) || config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.IEEE8021X)) {
			return SECURITY_EAP;
		}
		return (config.wepKeys[0] != null) ? SECURITY_WEP : SECURITY_NONE;
	}
	
	class WifiState{
		WifiState(int freq,String capa,String ssid){
			this.frequency = freq;
			this.capabilities = capa;
			this.ssid = ssid;
		}
		String ssid;
		
		public String getSsid() {
			return ssid;
		}
//		public void setSsid(String ssid) {
//			this.ssid = ssid;
//		}
		int frequency;
		public int getFrequency() {
			return frequency;
		}
//		public void setFrequency(int frequency) {
//			this.frequency = frequency;
//		}
		String capabilities;
		public String getCapabilities() {
			return capabilities;
		}
//		public void setCapabilities(String capabilities) {
//			this.capabilities = capabilities;
//		}
	}
	
	
	public boolean isSupportedSsid(){
		 if(this.getSsid() == null){
			 return false;
		 }
		 String unSupportedChars = getNotSupportedChar(this.getSsid());
		 return unSupportedChars.trim().length() == 0;
	}
	
	public String getNotSupportedChar(String text){
		if(text == null || text.trim().length() == 0){
			return "";
		}
		String containsNotSupportChars = "";
		int preChar = 0;
		for(int i = 0; i < text.length(); i++){
			int chr = text.charAt(i);
			if(chr >= 32 && chr < 127){
				//
				if(chr == '`'){
					if(containsNotSupportChars.indexOf(chr) == -1){
						containsNotSupportChars += "` ";
					}
				}
				if(chr == '\"'){
					if(containsNotSupportChars.indexOf(chr) == -1){
						containsNotSupportChars += "\" ";
					}
				}
				
				if(i == 0 && chr == '\\'){
					if(containsNotSupportChars.indexOf(chr) == -1){
						containsNotSupportChars += "\\ ";
					}
				}
				if(i == text.length() - 1 && chr == '\\'){
					if(containsNotSupportChars.indexOf(chr) == -1){
						containsNotSupportChars += "\\ ";
					}
				}
				if(chr == '\\' && preChar == '\\'){
					if(containsNotSupportChars.indexOf("\\\\") == -1){
						containsNotSupportChars += "\\\\ ";
					}
				}else if((chr == '(' || chr == '[' || chr == '{') && preChar == '$'){
					if(containsNotSupportChars.indexOf("$"+text.charAt(i)) == -1){
						containsNotSupportChars += "$"+ text.charAt(i) +" ";
					}
				}else{
					preChar = chr;
				}
				
			}else{
				if(chr != 0 && containsNotSupportChars.indexOf(chr) == -1){
					containsNotSupportChars += text.charAt(i)+" ";
				}
			}
		}
		return containsNotSupportChars;
	}
	public boolean isSupportedWifi(){
		return this.is24GWifi();// this.isWifiConnected()&&this.isSupportedSsid()&&this.is24GWifi()&&this.isWpa();
	}
	
	  public boolean registerIOSessionListener(INetworkChangeCallback listener) {
		    boolean result = false;
		    if (!this.mNetworkChangeCallback.contains(listener)) {
		      this.mNetworkChangeCallback.add(listener);
		      result = true;
		    }
		    return result;
	  }

	public boolean unregisterIOSessionListener(INetworkChangeCallback listener) {
		    boolean result = false;
		    if (this.mNetworkChangeCallback.contains(listener)) {
		      this.mNetworkChangeCallback.remove(listener);
		      result = true;
		    }
		    return result;
		  }

		  public  void  CheckConnectState(){
			  NetworkInfo.State wifiState = null;
			  NetworkInfo.State mobileState = null;
			  ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			  if(cm == null) {
				  wifiState = NetworkInfo.State.DISCONNECTED;
				  mobileState = NetworkInfo.State.DISCONNECTED;
			  }
			  else{
				  if (cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI) == null) {
					  wifiState = NetworkInfo.State.DISCONNECTED;
				  } else {
					  wifiState = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
				  }
				  if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) == null) {
					  mobileState = NetworkInfo.State.DISCONNECTED;
				  } else {
					  mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
				  }
			  }
			  if (wifiState != null && mobileState != null
					  && NetworkInfo.State.CONNECTED != wifiState
					  && NetworkInfo.State.CONNECTED == mobileState) {
				  // 手机网络连接成功
				  this.setConnectState(ConnectionState.MOBILE_CONNECT_STATE);
			  } else if (wifiState != null && mobileState != null
					  && NetworkInfo.State.CONNECTED != wifiState
					  && NetworkInfo.State.CONNECTED != mobileState) {
				  // 手机没有任何的网络
				  this.setConnectState(ConnectionState.NONE_CONNECT_STATE);
			  } else if (wifiState != null && NetworkInfo.State.CONNECTED == wifiState) {

				  // 无线网络连接成功
				  this.setConnectState(ConnectionState.WIFI_CONNECT_STATE);
			  }
		  }
	public int GetAutoMode() {
		int authMode = -1;
		if (getWifiState() != null) {
			boolean WpaPsk = getWifiState().capabilities.contains("WPA-PSK");
			boolean Wpa2Psk = getWifiState().capabilities.contains("WPA2-PSK");
			boolean Wpa = getWifiState().capabilities.contains("WPA-EAP");
			boolean Wpa2 = getWifiState().capabilities.contains("WPA2-EAP");
			boolean Tkip = getWifiState().capabilities.contains("TKIP");
			boolean AES = getWifiState().capabilities.contains("CCMP");

			if (getWifiState().capabilities.contains("WEP")) {
				authMode = HiSmartWifiSet.AuthModeWEPOPEN;
			} else if (WpaPsk && Wpa2Psk && AES && Tkip) {    /**********
			 *
			 * WPAPSK WPA2PSK Auto
			 *
			 **********/
				authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKTKIPAES;
			} else if (WpaPsk && Wpa2Psk && Tkip) {
				authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKTKIP;
			} else if (WpaPsk && Wpa2Psk && AES) {
				authMode = HiSmartWifiSet.AuthModeWPA1PSKWPA2PSKAES;
			} else if (Wpa2Psk && AES && Tkip) {
				authMode = HiSmartWifiSet.AuthModeWPA2PSKTKIPAES;
			} else if (Wpa2Psk && Tkip) {
				authMode = HiSmartWifiSet.AuthModeWPA2PSKTKIP;
			} else if (Wpa2Psk && AES) {
				authMode = HiSmartWifiSet.AuthModeWPA2PSKAES;
			} else if (WpaPsk && AES && Tkip) {
				authMode = HiSmartWifiSet.AuthModeWPAPSKTKIPAES;
			} else if (WpaPsk && Tkip) {
				authMode = HiSmartWifiSet.AuthModeWPAPSKTKIP;
			} else if (WpaPsk && AES) {
				authMode = HiSmartWifiSet.AuthModeWPAPSKAES;
			} else if (Wpa && Wpa2) {

				/**********
				 *
				 * WPA-EAP  WPA-EAP2 AUTO
				 *
				 **********/
				authMode = HiSmartWifiSet.AuthModeWPA1EAPWPA2EAP;
			} else if (Wpa2) {
				/**********
				 *
				 * WPA-EAP2
				 *
				 **********/
				authMode = HiSmartWifiSet.AuthModeWPA2EAP;
			} else if (Wpa) {

				/**********
				 *
				 * WPA-EAP
				 *
				 **********/
				authMode = HiSmartWifiSet.AuthModeWPAEAP;
			} else {

				/**********
				 *
				 * Open
				 *
				 **********/
				authMode = HiSmartWifiSet.AuthModeOpen;
			}
		}
		return authMode;
	}
}
