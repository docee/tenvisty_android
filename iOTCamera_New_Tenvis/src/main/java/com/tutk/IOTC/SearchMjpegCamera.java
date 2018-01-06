/* 
 CopyRright (c)2012-xxxx:  
 Title: SearchMjpegCamera.java                         
 Project:  IPCamera
 Package:com.IPCamera                                                           
 Author锟斤拷      sanmao and baolei               
 Create Date锟斤拷2012-05-08
 Modified By锟斤拷  sunnyfans                                         
 Modified Date:  2012-10-06                                   
 Why & What is modified锟斤拷add note   
 Version: 10-06                       
 */
package com.tutk.IOTC;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;





import android.util.Log;

import com.misc.LibcMisc;
import com.misc.NetMisc;
import com.misc.objc.NSDictionary;
import com.misc.objc.NSNotificationCenter;
import com.tutk.IOTC.MjpegCamera.CAMERA_ERROR;

/**
 * SearchMjpegCamera class is use to search all the cameras on the local network(Lan)
 * 
 * @version Revision: 10-06
 */
public class SearchMjpegCamera extends Thread
{
	private static final String BROADCAST_ADDRESS = "255.255.255.255";
	private static final int PORT = 10000;

	/* the len of the buf used to receive msg from device */
	private static final int RECEIVE_BUF_LEN = 256;
	/* the min msg len obey the protocol */
	private static final int MIN_MSG_LEN = 41;
	
	private static final int MJPEG_MSG_LEN = 27;
	
	List<String> id_list = new ArrayList<String>();
	
//	Context context;

	private int timeout;

	/**
	 * Constructor with a parameter
	 * 
	 * @param timeout
	 *            : the int type timout is use to jump the block of method
	 *            DatagramSocket.receive(DatagramPacket pack) , the timeout in
	 *            milliseconds or 0 for no timeout
	 * */

	public SearchMjpegCamera(int timeout)
	{
		this.timeout = timeout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		int search_times = 2;

		InetAddress adress = null;
		DatagramPacket searchPacket = null;
		DatagramPacket searchPacket2 = null;
		DatagramSocket searchBroadcast = null;

		/* the broadcast search msg */
		byte[] searchReqMsg =
		{ (byte) 0xb4, (byte) 0x9a, 0x70, 0x4d, 0x00 };
		
		byte[] searchReqMsg2 = new byte[MJPEG_MSG_LEN];
		//System.out.printf("LibcMisc.memset(searchReqMsg2, 0, 27);-0-"+searchReqMsg2);
		//Log.i("LibcMisc.memset(searchReqMsg2, 0, 27)-0-", Arrays.toString(searchReqMsg2));
		LibcMisc.memset(searchReqMsg2, 0, 27);
		//System.out.printf("LibcMisc.memset(searchReqMsg2, 0, 27);-1-"+searchReqMsg2);
		//Log.i("LibcMisc.memset(searchReqMsg2, 0, 27)-1-", Arrays.toString(searchReqMsg2));
		byte[] temp = {'M','O','_','I'};			
		System.arraycopy(temp, 0, searchReqMsg2, 0, temp.length);
		searchReqMsg2[4] = 0;
		searchReqMsg2[15] = 4;
		searchReqMsg2[26] = 1;

		byte[] searchResultBuf = new byte[RECEIVE_BUF_LEN];

		CAMERA_ERROR error = CAMERA_ERROR.OK;

		try
		{
			searchBroadcast = new DatagramSocket();
			searchBroadcast.setSoTimeout(timeout);
			searchBroadcast.setBroadcast(true);

			adress = InetAddress.getByName(BROADCAST_ADDRESS);
			searchPacket = new DatagramPacket(searchReqMsg,
					searchReqMsg.length, adress, PORT);
			searchPacket2 = new DatagramPacket(searchReqMsg2,
					searchReqMsg2.length, adress, PORT);

		}
		catch (Exception e)
		{
			e.printStackTrace();
			error = CAMERA_ERROR.SOCKET_ERROR;
			handle_search_ended(error);
			return;
		}

		

		while (search_times > 0)
		{
			search_times--;

			try
			{
				searchBroadcast.send(searchPacket);
				searchBroadcast.send(searchPacket2);

			}
			catch (IOException e)
			{
				e.printStackTrace();
				error = CAMERA_ERROR.SOCKET_ERROR;
				break;
			}

			while (true)
			{
				DatagramPacket searchResultPacket = new DatagramPacket(
						searchResultBuf, searchResultBuf.length);
				try
				{
					searchBroadcast.receive(searchResultPacket);

				}
				catch (IOException e)
				{
					error = CAMERA_ERROR.OK;
					break;
				}
				/* verify the receive msg header */
				int length = searchResultPacket.getLength();
				
				String id = null;
				if ((length < MIN_MSG_LEN)
						|| (!((searchResultBuf[0] == (byte) 0xb4)
								&& (searchResultBuf[1] == (byte) 0x9a)
								&& (searchResultBuf[2] == (byte) 0x70)
								&& (searchResultBuf[3] == (byte) 0x4d) 
								&& (searchResultBuf[4] == (byte) 0x01))))
						
				{
					if(new String(searchResultBuf, 0, 4).equals("MO_I"))
					{
						String ip = searchResultPacket.getAddress().getHostName();
						String ddnsUserString = "";
						try {
							id = new String(searchResultBuf, 23, 57 - 23 + 1, "ASCII");
							if(id.indexOf(0) != -1)
								id =id.substring(0,id.indexOf(0));
							
							ddnsUserString = new String(searchResultBuf, 153, 64, "ASCII");
							ddnsUserString = interceptChar0Before(ddnsUserString);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
							id = null;
						}
//						System.out.println("mjpeg camera "+id);
						mjpegCameraHandler(searchResultBuf,id,ip,ddnsUserString);
					}else		
						continue;
				}else
				{

					 id = get_id(searchResultBuf);
				}
				

			}
		}

		
		handle_search_ended(error);

		if (searchBroadcast != null)
		{  
			searchBroadcast.close();
		}

		id_list.clear();

	}
	
	public void endSearchCamera() {
		timeout = 0;
	}

	private void mjpegCameraHandler(byte[] searchResultBuf ,String id,String ip,String ddnsUserString) {
		
		if (id_list.contains(id))
		{
			return;
		}
		id_list.add(id);
		String dev_port = null ;
		short val_s;
		int dd ;
		val_s = LibcMisc.get_short(searchResultBuf, 85);
		val_s = NetMisc.ntohs(val_s);
		dd= val_s & 0xFFFF;
		dev_port = String.valueOf(dd);
		
		/* add to filter the 0 ip */
		if (ip.equals("0.0.0.0")) {
			return;
		}
		
		NSDictionary params = new NSDictionary();
		params.put(MjpegCamera.SEARCHED_CAMERA_ID, id);
		params.put(MjpegCamera.SEARCHED_CAMERA_IP, ip);
		params.put(MjpegCamera.SEARCHED_CAMERA_PORT, dev_port);	
		params.put(MjpegCamera.SEARCHED_CAMERA_DDNS_URES, ddnsUserString);

		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.postNotification(MjpegCamera.ACTION_CAMERA_FOUND, null,
				params);
	
	}

	/**
	 * The method is called when receiving a broadcast msg from camera device.
	 * use to parse the id in this msg
	 * 
	 * @param searchResultData
	 *            the msg buffer
	 * @return the device id parse from the msg
	 */
	private String get_id(byte[] searchResultData)
	{
		String string_id = null;
		string_id = new String(searchResultData, 5 + 1, searchResultData[5]);
		return string_id;
	}


	/**
	 * The method is called when broadcast search end use to send msg to notify
	 * the searching process end
	 * 
	 * @param error
	 *            CAMERA_ERROR type ,to send out to notify whether the search
	 *            process come mistakes
	 */
	private void handle_search_ended(CAMERA_ERROR error)
	{
		MjpegCamera.searching_flag = false;
		NSDictionary params = new NSDictionary();
		params.put("error", error);
		NSNotificationCenter nc = NSNotificationCenter.defaultCenter();
		nc.postNotification(MjpegCamera.IPCamera_Search_Ended_Notification, null,
				params);

	}

	 /** 
     * 截取掉C中\0之前的字符串。即只截取\0前的字符 
     * 
     * @param s 
     * @return 
     */  
    public static String interceptChar0Before(String s){  
        if(s == null){  
            return null;  
        }  
        char[] chars = s.toCharArray();  
        StringBuffer sb = new StringBuffer();  
        for(char c : chars){  
            Character ch = c;  
            if(0 == ch.hashCode()){ //如果到了字符串结束，则跳出循环   
                break;  
            }else{  
                sb.append(c);  
            }  
        }  
        return sb.toString();  
    }  
}