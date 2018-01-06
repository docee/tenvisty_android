package com.misc.objc;


import java.io.Serializable;

import android.util.Log;

public class CFRunLoopTimer implements Serializable,Comparable<CFRunLoopTimer>{
	public interface CFRunLoopTimerCallBack{
		void timeout(CFRunLoopTimer timer, Object arg);
		
	}
	
	long start_time;
	int flags;
	long loop_time;
	long next_time;
	CFRunLoopTimerCallBack cb;
	Object arg;
	
	public long getStart_time() {
		return start_time;
	}

	public int getFlags() {
		return flags;
	}

	public long getLoop_time() {
		return loop_time;
	}
	
	public long getNext_time() {
		return next_time;
	}

	public void setNext_time(long next_time) {
		this.next_time = next_time;
	}

	public CFRunLoopTimerCallBack getCb() {
		return cb;
	}
	
	/**
	 * @return the arg
	 */
	public Object getArg() {
		return arg;
	}

	/**
	 * @param arg the arg to set
	 */
	public void setArg(Object arg) {
		this.arg = arg;
	}

	protected CFRunLoopTimer(long start_time, int flags, long loop_time, CFRunLoopTimerCallBack cb, Object arg)
	{
		this.start_time = start_time;
		this.flags = flags;
		this.loop_time = loop_time;
		this.cb = cb;
		this.arg = arg;
	}
	
	public static CFRunLoopTimer CFRunLoopTimerCreate (
			   long start_time,
			   long interval,
			   int flags,			   
			   CFRunLoopTimerCallBack callout,
			   Object arg
			)
	{
		return new CFRunLoopTimer(start_time, flags, interval, callout, arg);
	}
	
	public static void CFRunLoopAddTimer (
			   CFRunLoop rl,
			   CFRunLoopTimer timer,
			   String mode
			)
	{
		
		rl.AddTimer(timer);		
	}
	
	public static void CFRunLoopRemoveTimer (
			   CFRunLoop rl,
			   CFRunLoopTimer timer,
			  String mode
			)
	{
//		IpCamera camera = (IpCamera) timer.getArg();
		rl.DeleteTimer(timer);
	}

	public int compareTo(CFRunLoopTimer o) {
		if((o.next_time - next_time) > 0)
			return -1;
		else if(o.next_time == next_time)
			return 0;
		else
			return 1;
	}
}
