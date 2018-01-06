package com.misc.objc;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.NotYetConnectedException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import com.misc.objc.CFRunLoop;
	
public class CFStream 
{	
	boolean m_bRead = false;
	SocketChannel m_chn = null;
	CFStreamClientCallBack m_cbClient;
	int m_fEvents;
	CFRunLoop m_rl;

	
	public static final int kCFStreamEventNone = 0;
	public static final int kCFStreamEventOpenCompleted = 1;
	public static final int kCFStreamEventHasBytesAvailable = 2;
	public static final int kCFStreamEventCanAcceptBytes = 4;
	public static final int kCFStreamEventErrorOccurred = 8;
	public static final int kCFStreamEventEndEncountered = 16;
	static Map<SocketChannel, CFStreamPair> pairs = new HashMap<SocketChannel, CFStreamPair>();
	
	protected void CFStreamSetTarget(SocketChannel sc, boolean read)
	{
		m_chn = sc;
		m_bRead = read;
	}
	
	public CFStream()
	{
		super();
	}
	
	public class CFStreamClientContext 
	{
		public int version;
		public Object info;
		public Object retain;
		public Object release;
		public Object copyDescription;	
	}
	
//	public class CFRunLoop 
//	{
//		Selector m_selector;
//		
//		
//	}
	static byte[] inet_aton(String ipstr) throws  java.net.UnknownHostException
	{
		int[] addrs = new int[4];
		byte[] byteaddrs = new byte[4];
		
		String[] segments = ipstr.split("\\.");
		if(segments.length != 4)
		{
			throw  new java.net.UnknownHostException(ipstr);
		}
		
		try
		{			
			for(int i = 0; i < 4; i++)
			{
				addrs[i] = Integer.parseInt(segments[i]);
				if(addrs[i] < 0 || addrs[i] > 255)
				{
					throw  new java.net.UnknownHostException(ipstr);
				}
				
				byteaddrs[i] = (byte)addrs[i];
			}
		}
		catch(Exception e)
		{
			throw  new java.net.UnknownHostException(ipstr);
		}
		
		return byteaddrs;
	}
	
	public static void CFStreamCreatePairWithSocketToHost (
			   Object alloc,
			   String host,
			   int port,
			   CFReadStream readStream,
			   CFWriteStream writeStream
			)
	{
		SocketChannel client_channel;
		try {
			client_channel = SocketChannel.open();
			client_channel.configureBlocking(false);
			InetSocketAddress endpoint;
			try
			{				
				InetAddress addr = InetAddress.getByAddress(inet_aton(host));
				endpoint = new InetSocketAddress(addr, port);
			}
			catch (Exception e)
			{
				endpoint = new InetSocketAddress(host, port);
			}
			
			if(!client_channel.connect(endpoint))
			{
				//client_channel.finishConnect();
			}
			
			readStream.CFStreamSetTarget(client_channel, true);
			writeStream.CFStreamSetTarget(client_channel, false);
			pairs.put(client_channel, new CFStreamPair(writeStream, readStream));
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return;		
	}
	
	public static void CFStreamCreatePairWithSocketToHost (
			   Object alloc,
			   InetSocketAddress endpoint,
			   CFReadStream readStream,
			   CFWriteStream writeStream
			) throws IOException
	{
		SocketChannel client_channel;
//		try {
			client_channel = SocketChannel.open();
			client_channel.configureBlocking(false);
			
			if(!client_channel.connect(endpoint))
			{
				//client_channel.finishConnect();
			}
			
			readStream.CFStreamSetTarget(client_channel, true);
			writeStream.CFStreamSetTarget(client_channel, false);
			pairs.put(client_channel, new CFStreamPair(writeStream, readStream));
			
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		return;		
	}
	
	static public CFReadStream FindReadStreamByChannel(SocketChannel sc)
	{
		if(pairs.containsKey(sc))
		{
			return pairs.get(sc).rs;
		}
		else
		{
			return null;
		}
	}
	
	static public CFWriteStream FindWriteStreamByChannel(SocketChannel sc)
	{
		if(pairs.containsKey(sc))
		{
			return pairs.get(sc).ws;
		}
		else
		{
			return null;
		}
	}
	
	public static Boolean CFReadStreamSetClient (
			   CFReadStream stream,
			   int streamEvents,
			   CFStreamClientCallBack clientCB,
			   CFStreamClientContext clientContext
			)
	{
		stream.m_fEvents = streamEvents;
		stream.m_cbClient = clientCB;
		
		return true;		
	}
	
	public static void CFWriteStreamSetClient(CFWriteStream stream,
			   int streamEvents,
			   CFStreamClientCallBack clientCB,
			   CFStreamClientContext clientContext) {
		// TODO Auto-generated method stub
		stream.m_fEvents = streamEvents;
		stream.m_cbClient = clientCB;
	}
	
	public static Boolean CFReadStreamOpen (
			   CFReadStream stream
			)
	{
		int events = 0;
		
		if((stream.m_fEvents & CFStream.kCFStreamEventOpenCompleted) > 0 )
		{
			events |= SelectionKey.OP_CONNECT;
		}
		if((stream.m_fEvents & 
				(CFStream.kCFStreamEventHasBytesAvailable | CFStream.kCFStreamEventErrorOccurred)) > 0)
		{
			events |= SelectionKey.OP_READ;
		}
		
		stream.m_rl.Register(stream.m_chn, events);
		return true;
	}
	
	public static Boolean CFWriteStreamOpen(CFWriteStream stream) {
		int events = 0;			
		if((stream.m_fEvents & 
				(CFStream.kCFStreamEventCanAcceptBytes)) > 0)
		{
			events |= SelectionKey.OP_WRITE;
		}
		
		stream.m_rl.Register(stream.m_chn, events);
		return true;
	}
	
	public static int CFReadStreamRead (
			   CFReadStream stream,
			   byte[] buffer,
			   int bufferLength )
	{
	
		ByteBuffer bb = ByteBuffer.wrap(buffer,0, bufferLength);
		try {
			bufferLength = stream.m_chn.read(bb);
		}catch(NotYetConnectedException e){
//			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			Log.e("EXCEPTION", e.getMessage());
			bufferLength = 0;
		}
		
		if(bufferLength > 0)
		{
			String out = String.format("length:%d, ", bufferLength);			
			
			//Log.v("receive data", out + LibcMisc.array2string(buffer, 0, bufferLength));
		}
		
		return bufferLength;
		
	}
	
	public static int CFReadStreamRead (
			   CFReadStream stream,
			   byte[] buffer,
			   int offset,
			   int bufferLength
			)
	{
		ByteBuffer bb = ByteBuffer.wrap(buffer,offset, bufferLength);
		try {
			bufferLength = stream.m_chn.read(bb);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("EXCEPTION", e.getMessage());
			bufferLength = 0;
		}
		
		if(bufferLength > 0)
		{
			String out = String.format("length:%d, ", bufferLength);			
			
			//Log.v("receive data", out + LibcMisc.array2string(buffer, 0, bufferLength));
		}
		
		return bufferLength;
		
	}
	
	public static int CFWriteStreamWrite (
			   CFWriteStream stream,
			   byte[] buffer,
			   int bufferLength
			)
	{
		int nwriten = 0;
		ByteBuffer bb = ByteBuffer.wrap(buffer,0, bufferLength);
		try {
			nwriten = stream.m_chn.write(bb);
			if(nwriten < bufferLength)
			{
				stream.m_rl.Register(stream.m_chn, SelectionKey.OP_WRITE);
			}
			if(nwriten > 0)
			{
				String out = String.format("length:%d, ", nwriten);			
				
//				Log.v("sending data", out + LibcMisc.array2string(buffer, 0, nwriten));
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.e("EXCEPTION", e.getMessage());
			nwriten = -1;
		}		
		
		return nwriten;
	}
	
	public static void CFReadStreamScheduleWithRunLoop (
			   CFReadStream stream,
			   CFRunLoop runLoop,
			   String runLoopMode
			)
	{
		//stream.m_selector = runLoop.m_selector;
		stream.m_rl = runLoop;
	}

	public static void CFWriteStreamScheduleWithRunLoop(
			CFWriteStream stream, CFRunLoop runLoop,
			String kCFRunLoopDefaultMode) {
		// TODO Auto-generated method stub
		//stream.m_selector = runLoop.m_selector;
		stream.m_rl = runLoop;
	}

	public static void CFReadStreamClose(CFReadStream stream) {
		// TODO Auto-generated method stub
		try {
			int events = 0;			
			
			if((stream.m_fEvents & CFStream.kCFStreamEventOpenCompleted) > 0 )
			{
				events |= SelectionKey.OP_CONNECT;
			}
			if((stream.m_fEvents & 
					(CFStream.kCFStreamEventHasBytesAvailable | CFStream.kCFStreamEventErrorOccurred)) > 0)
			{
				events |= SelectionKey.OP_READ;
			}
			
			stream.m_rl.UnRegister(stream.m_chn, events);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void CFRelease(CFReadStream stream) {
		// TODO Auto-generated method stub
		if(pairs.containsKey(stream.m_chn))
		{
			CFStreamPair pair = pairs.get(stream.m_chn);
			pair.CloseReadStream();
			if(pair.IsEmpty())
			{
				pairs.remove(stream.m_chn);
				try {
					stream.m_chn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static void CFWriteStreamClose(CFWriteStream stream) {
		// TODO Auto-generated method stub
		try {
			int events = 0;			
			if((stream.m_fEvents & 
					(CFStream.kCFStreamEventCanAcceptBytes)) > 0)
			{
				events |= SelectionKey.OP_WRITE;
			}		
			
			stream.m_rl.UnRegister(stream.m_chn, events);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void CFRelease(CFWriteStream stream) {
		// TODO Auto-generated method stub
		if(pairs.containsKey(stream.m_chn))
		{
			CFStreamPair pair = pairs.get(stream.m_chn);
			pair.CloseWriteStream();
			if(pair.IsEmpty())
			{
				pairs.remove(stream.m_chn);
				try {
					stream.m_chn.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public static boolean CFWriteStreamCanAcceptBytes(CFWriteStream stream) {
		// TODO Auto-generated method stub
		return stream.m_chn.isConnected();
	}

	

	
	
	
}
