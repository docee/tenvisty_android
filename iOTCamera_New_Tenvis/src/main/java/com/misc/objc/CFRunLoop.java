package com.misc.objc;

import java.io.IOException;
import java.io.Serializable;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import android.os.Build;
import android.os.HandlerThread;
import android.util.Log;

public class CFRunLoop extends Thread  {
	//static Map<int, CFRunLoop> runloops = new HashMap<int, CFRunLoop>();
	static CFRunLoop runloopofprocess = null; 
	public static String kCFRunLoopDefaultMode;
	Selector m_selector;
	Thread selThread = null;
	Queue<ChannelEvent> evQ = new ConcurrentLinkedQueue<ChannelEvent>();
	Queue<Runnable> runableQ = new ConcurrentLinkedQueue<Runnable>();
	Queue<TimerEvent> TimerOprQ = new ConcurrentLinkedQueue<TimerEvent>();
	PriorityQueue<CFRunLoopTimer> timerQ = new PriorityQueue<CFRunLoopTimer>();
	
	class TimerEvent
	{
		CFRunLoopTimer timer;
		boolean add;
		
		public TimerEvent(CFRunLoopTimer timer, boolean add)
		{
			this.timer = timer;
			this.add = add;
//			Log.e("timer", timer.toString() + ((add == true)?"add":"delete"));
		}
	}
	
	class ChannelEvent
	{
		SocketChannel sc;
		boolean reg;
		int ops;
		
		public SocketChannel getSc() {
			return sc;
		}

		public boolean isReg() {
			return reg;
		}

		public int getOps() {
			return ops;
		}
		
		public ChannelEvent(SocketChannel sc, boolean reg, int ops)
		{
			this.sc = sc;
			this.reg = reg;
			this.ops = ops;
		}
	}
	
	protected CFRunLoop()
	{
		try {
			m_selector = Selector.open();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		int nselected;
		selThread = Thread.currentThread();
		while(true)
		{	
			long tv = beforeSelect();
			try {
				if(tv < 0)
					nselected = m_selector.selectNow();
				else
					nselected = m_selector.select(tv);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				continue;
			}
			if (nselected > 0) {
				Set<SelectionKey> keyset = m_selector.selectedKeys();
				Iterator<SelectionKey> it = keyset.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove(); // 切记移除
					if (!key.isValid()) {
						continue;
					}

					if (key.isAcceptable()) {
						// @TODO:
						assert (false);
						continue;
					}

					SocketChannel sc = (SocketChannel) key.channel();
					CFWriteStream ws = CFStream.FindWriteStreamByChannel(sc);
					CFReadStream rs = CFStream.FindReadStreamByChannel(sc);

					if (key.isConnectable()) {
						try {
							sc.finishConnect();
							UnRegister((SocketChannel) key.channel(), SelectionKey.OP_CONNECT);
							ws.m_cbClient.excute(rs,
									CFStream.kCFStreamEventOpenCompleted,
									ws.m_cbClient);
							
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							Log.e("EXCEPTION", e.getMessage());
							ws.m_cbClient.excute(rs,
									CFStream.kCFStreamEventErrorOccurred,
									ws.m_cbClient);
						}
					} else if (key.isWritable()) {
						UnRegister((SocketChannel) key.channel(), SelectionKey.OP_WRITE);
						ws.m_cbClient.excute(ws,
								CFStream.kCFStreamEventCanAcceptBytes,
								ws.m_cbClient);
					} else if (key.isReadable()) {
						rs.m_cbClient.excute(rs,
								CFStream.kCFStreamEventHasBytesAvailable,
								rs.m_cbClient);
					}
				}
			}
			
			check_timer();
		}
	}
	
	public static CFRunLoop CFRunLoopGetCurrent()
	{		
		//@TODO: when to delete from the MAP?
		if(runloopofprocess == null)
		{
			runloopofprocess = new CFRunLoop();
			runloopofprocess.start();
		}
		
		return runloopofprocess;		
	}
	
	public void Register(SocketChannel sc, int op)
	{
		evQ.add(new ChannelEvent(sc, true, op));
		if(Thread.currentThread() != selThread)
		{			
			if(m_selector.isOpen())
				m_selector.wakeup();
		}
	}
	
	public void UnRegister(SocketChannel sc, int op)
	{
		evQ.add(new ChannelEvent(sc, false, op));
		if(Thread.currentThread() != selThread)
		{
			if(m_selector.isOpen())
				m_selector.wakeup();
		}
	}
	
	// TODO timeQ is thread-safe?
	public boolean AddTimer(CFRunLoopTimer timer)
	{
		TimerOprQ.add(new TimerEvent(timer, true));
		
		return true;
	}
	
	public boolean DeleteTimer(CFRunLoopTimer timer)
	{
		TimerOprQ.add(new TimerEvent(timer, false));
		
		return true;
	}
	
	public boolean post(Runnable r)
	{
		if(runableQ.add(r))
		{
			if(Thread.currentThread() != selThread)
			{
				if (isEmulator() == false) {
					if(m_selector.isOpen())
						m_selector.wakeup();
				}

			}
			
			return true;
		}
		
		return false;
	}
	
	private boolean isEmulator() {
        return (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
  }
	
	long beforeSelect()
	{
		ChannelEvent ev;
		CFRunLoopTimer timer;
		int ops;
		Runnable r;
		
		handle_timer_op();
		
		while((ev = evQ.poll()) != null)
		{
			SocketChannel sc = ev.getSc();		
			
			if(sc != null && sc.keyFor(m_selector) != null)
			{
				if(!sc.keyFor(m_selector).isValid())
				{
					continue;
				}
				
				ops = sc.keyFor(m_selector).interestOps();
			}
			else
				ops = 0;
			if(ev.isReg())
			{
				ops |= ev.getOps();
			}
			else
			{
				ops &= ~ev.getOps();
			}
			
			if(m_selector.isOpen())
			{
				try {
					sc.register(m_selector, ops);
					//Log.e("EVENT", String.valueOf(ops));
				} catch (ClosedChannelException e) {
					// TODO Auto-generated catch block
					//Log.e("EXCEPTION", e.getMessage());
				}
			}
		}
		
		while((r = runableQ.poll()) != null)
		{
			r.run();
		}
		
		if((timer = timerQ.peek()) != null)
		{
			long next_time = timer.getNext_time();
			long now_time = System.currentTimeMillis();
			if(now_time < next_time)
				return next_time - now_time;
			else
				return -1;
		}
		else
		{
			// wait 500ms at most when add timer 
			return 500;
		}
	}
	
	void handle_timer_op()
	{
		TimerEvent tv;
		CFRunLoopTimer timer;
		while((tv = TimerOprQ.poll()) != null)
		{
			if(tv.add)
			{
				timer = tv.timer;
				timer.setNext_time(System.currentTimeMillis() + timer.getLoop_time());
				if(!timerQ.add(timer))
				{
					Log.e("timer", "failed to add timer");
				}
				else
				{
					Log.i("timer", "add timer success:"+timer.toString());
				}
			}
			else
			{
				if(!timerQ.remove(tv.timer))
				{
					Log.e("timer", "failed to remove timer");
				}
				else
				{
					Log.i("timer", "delete timer success:"+tv.timer.toString());
				}
			}
		}
	}
	
	void check_timer()
	{
		CFRunLoopTimer timer;
		long now_time = System.currentTimeMillis();
		
		handle_timer_op();
		while((timer = timerQ.peek()) != null)
		{
			if((timer.getNext_time() - now_time) < 0)
			{
				timer = timerQ.poll();
				CFRunLoopTimer.CFRunLoopTimerCallBack cb = timer.getCb();
				cb.timeout(timer, null);
				timer.setNext_time(now_time + timer.getLoop_time());
				timerQ.add(timer);
			}
			else
			{
				break;
			}
		}
	}
}
