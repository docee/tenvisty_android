package com.misc;

import java.io.Serializable;

public class RefInteger implements Serializable{
	public RefInteger(int value) {
		super();
		this.value = value;
	}

	private int value;
	
	

	/**
	 * @return the value
	 */
	public int getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(int value) {
		this.value = value;
	}
}
