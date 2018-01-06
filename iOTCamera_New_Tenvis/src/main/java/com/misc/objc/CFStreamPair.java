package com.misc.objc;

import java.io.Serializable;

public class CFStreamPair  implements Serializable{
	CFReadStream rs;
	CFWriteStream ws;	
	
	public CFStreamPair(CFWriteStream ws, CFReadStream rs)
	{
		this.rs = rs;
		this.ws = ws;
		//@TODO: when to delete from the MAP?		
	}
	
	public void CloseWriteStream()
	{
		this.ws = null;
	}
	
	public void CloseReadStream()
	{
		this.rs = null;
	}
	
	public boolean IsEmpty()
	{
		return !(this.ws != null || this.rs != null);

	}
}
