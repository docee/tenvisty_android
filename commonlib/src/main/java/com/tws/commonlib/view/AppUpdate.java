package com.tws.commonlib.view;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 该类作为版本升级更新所用
 * @author TOOTU
 *
 */
public class AppUpdate {


	public String udversionString = "";
	public String udflagString = "";
	public String uddescriptionString = "";
	public String udappsrcString = "";

	public long filelength=0;
	public int downedlength=0;
	public Boolean flagCancel=false;
	public int fileLength = 0;
	/**
	 * 获取最新版本的信息json数据包并转为String类型
	 * 网络访问需放入子线程中执行
	 * @param serverPath 访问服务器进行查询所需的地址
	 * @return  String类型的查询结果
	 * @throws Exception
	 */
	public static String getUpdataVerJSON(String serverPath) throws Exception{
		StringBuilder newVerJSON = new StringBuilder();
        String result = null;
        URL url = null;
        HttpURLConnection connection = null;
        InputStreamReader in = null;
        try {
            url = new URL(serverPath);
            connection = (HttpURLConnection) url.openConnection();
            in = new InputStreamReader(connection.getInputStream());
            BufferedReader bufferedReader = new BufferedReader(in);
            StringBuffer strBuffer = new StringBuffer();
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                strBuffer.append(line);
            }
            result = strBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return result;
	}


	/**
	 * 检测网络是否可用
	 * @param context
	 * @return true or false
	 */
	 //check the Network is available
	public static boolean isNetworkAvailable(Context context) {
		// TODO Auto-generated method stub
    	try{

    		ConnectivityManager cm = (ConnectivityManager)context
    				.getSystemService(Context.CONNECTIVITY_SERVICE);
    		NetworkInfo netWorkInfo = cm.getActiveNetworkInfo();
    		return (netWorkInfo != null && netWorkInfo.isAvailable());//检测网络是否可用
    	}catch(Exception e){
    		e.printStackTrace();
    		return false;
    	}
	}

	/**
	 * 解析getUpdataVerJSON所获取的json字符串
	 * @param updataVerJSONString
	 * @return 完成解析为true
	 */
	public boolean decUpdataVerJSON(String updataVerJSONString) {
		JSONArray updatajsonArray = null;
		JSONObject updatajsonobj;
		JSONObject updatajsonobj1 = null;

		try {
			updatajsonArray = new JSONArray(updataVerJSONString);
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return false;
		}

		if (updatajsonArray.length()>0) {
			//JSONObject obj;
			try {
				updatajsonobj = updatajsonArray.getJSONObject(0);
				updatajsonobj1=updatajsonobj.getJSONObject("file");

			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return false;
			}
			try {

				udversionString=updatajsonobj1.getString("Version");
				udflagString=updatajsonobj1.getString("IsNecessary");
				uddescriptionString=updatajsonobj1.getString("Description");
				//udappsrcString=HOST_URL+updatajsonobj1.getString("Src");//
				udappsrcString=updatajsonobj1.getString("Src");//
			} catch (Exception e) {
				// TODO: handle exception
				Log.e("Splash", e.getMessage());
				udversionString="";
				return false;
			}
		}

		return true;

	}

	/**
	 * 下载文件，注意异常的处理以及中断下载后的处理
	 * @param url 文件下载地址
	 * @param currentdownHandler  交互所需的handler
	 * @param apkname  文件的名字 如 Weijia.apk
	 */
	public void  downAppFile(final Context context,final String url,final Handler currentdownHandler,final String apkname) {
		//downProgressDialog.show();//显示下载进度ProgressDialog
		new Thread(){
			@Override
			public void run() {

				String dir = context.getExternalCacheDir().toString()+ File.separator + "_update";
				File fDir = new File(dir);
				HttpClient client=new DefaultHttpClient();
				HttpGet get = new HttpGet(url);
				String tmpApkName="update_"+apkname+".tmp";

				byte[] buf=new byte[1024*8];
				int ch=-1;
				HttpResponse response;
				Message downloadMessage=new Message();
				try {
					if(!fDir.exists()){
						fDir.mkdirs();
					}

					File tmpFile=new File(fDir,tmpApkName);
					File file=new File(fDir,apkname);
					FileOutputStream fileOutputStream = null;
	            	downedlength = (int)tmpFile.length();
	            	//已经下载完则执行结束
	            	if(file.exists()){
						Message downloadMessage2=new Message();
						downloadMessage2.what=2;
						Bundle bundle = new Bundle();
						bundle.putString("path", file.toString());
						downloadMessage2.setData(bundle);
						currentdownHandler.sendMessage(downloadMessage2);
						return;
	            	}
	            	//删除其他版本文件
	            	else if(!tmpFile.exists() && fDir.listFiles() != null){
	            		for(File f:fDir.listFiles()){
	            			f.delete();
	            		}
	            	}
					fileOutputStream = new FileOutputStream(tmpFile,true);

					get.addHeader("Range", "bytes="+downedlength+"-");
					response = client.execute(get);
					fileLength = Integer.parseInt(response.getFirstHeader("Content-range").getValue().split("/")[1]);
					HttpEntity entity = response.getEntity();
					filelength= entity.getContentLength();//filelength
					//Log.isLoggable("DownTag", (int)filelength);
					downloadMessage.what=0;
					currentdownHandler.sendMessage(downloadMessage);
				//	Log.i("currentdownHandler.sendMessage", "downloadMessage.what=0;");
					InputStream is = entity.getContent();
					if (is==null) {
						throw new RuntimeException("isStream is null");
					}

					do {
						ch=is.read(buf);//读取了多长downedlength
						if (ch <= 0)
						break;
						fileOutputStream.write(buf, 0, ch);//
						fileOutputStream.flush();
						downedlength += ch;
						Message downloadMessage1=new Message();
						downloadMessage1.what=1;
						currentdownHandler.sendMessage(downloadMessage1);
						//Log.i("currentdownHandler.sendMessage", "downloadMessage.what=1;");
					} while (!flagCancel);

					is.close();
					fileOutputStream.close();

					if (!flagCancel) {	//如果是正常下载完成而非下载被中断
					//	downProgressDialog.dismiss();
						tmpFile.renameTo(file);
						Message downloadMessage2=new Message();
						Bundle bundle = new Bundle();
						bundle.putString("path", file.toString());
						downloadMessage2.setData(bundle);
						downloadMessage2.what=2;
						currentdownHandler.sendMessage(downloadMessage2);
						//Log.i("currentdownHandler.sendMessage", "downloadMessage.what=2;");
					//	haveDownload();
					}else {//如果是中断，做相应的处理
						Message downloadMessage3=new Message();
						downloadMessage3.what=3;
						currentdownHandler.sendMessage(downloadMessage3);
						//Log.i("currentdownHandler.sendMessage", "中断下载downloadMessage.what=3;");
					}

				} catch (ClientProtocolException e) {//网络异常要做的处理
					// TODO: handle exception
					Message downloadMessage4=new Message();
					downloadMessage4.what=4;
					currentdownHandler.sendMessage(downloadMessage4);
					//Log.i("currentdownHandler.sendMessage", "ClientProtocolException 中断下载downloadMessage.what=3;");
					e.printStackTrace();
				} catch (IOException e) {//IO操作异常要做的处理
					Message downloadMessage5=new Message();
					downloadMessage5.what=5;
					currentdownHandler.sendMessage(downloadMessage5);
					// TODO Auto-generated catch block
					//Log.i("currentdownHandler.sendMessage", "IOException 中断下载downloadMessage.what=3;");
					e.printStackTrace();
				}

			}
		}.start();
	}


	public String getUdversionString() {
		return udversionString;
	}

	public String getUdflagString() {
		return udflagString;
	}

	public String getUddescriptionString() {
		return uddescriptionString;
	}

	public String getUdappsrcString() {
		return udappsrcString;
	}


	public long getFilelength() {
		return fileLength;
	}


	public int getDownedlength() {
		return downedlength;
	}


	public Boolean getFlagCancel() {
		return flagCancel;
	}


	public void setFlagCancel(Boolean flagCancel) {
		this.flagCancel = flagCancel;
	}



}
