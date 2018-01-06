/*
Wotonomy: OpenStep design patterns for pure Java applications.
Copyright (C) 2000 Blacksmith, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, see http://www.gnu.org
 */

package com.misc.objc;

import java.io.Serializable;

import android.util.Log;

/**
 * A pure java implementation of NSMutableData, which is basically an editable
 * wrapper for a byte array.
 * 
 * @author michael@mpowers.net
 * @author $Author: cgruber $
 * @version $Revision: 1.1 $
 */
public class NSMutableData extends NSData implements Serializable {
	/**
	 * Default constructor creates a zero-data object.
	 */
	public NSMutableData() {
		super();
	}

	/**
	 * Creates an object containing the contents of the specified URL.
	 */
	public NSMutableData(java.net.URL aURL) {
		super(aURL);
	}

	/**
	 * Creates an object containing a copy of the contents of the specified
	 * NSData object.
	 */
	public NSMutableData(NSData aData) {
		super(aData);
	}

	/**
	 * Creates an object containing the specified number of bytes initialized to
	 * all zeroes.
	 */
	public NSMutableData(int size) {
		super(new byte[size]); // inits to zeroes
	}

	/**
	 * Sets the length of the data to the specified length. If shorter, the data
	 * is truncated. If longer, the extra bytes are initialized to zeroes.
	 */
	public void setLength(int length) {
		if (length < 0) {
			Log.e("length", String.valueOf(length));
		}
		byte[] data = new byte[length]; // inits to zeroes
		int limit = length;
		if (limit > bytes.length)
			limit = bytes.length;
		for (int i = 0; i < limit; i++) {
			data[i] = this.bytes[i];
		}
		this.bytes = data;
	}

	/**
	 * Appends the specified data to the end of this data.
	 */
	public void appendData(NSData aData) {
		int len = aData.length();
		byte[] data = new byte[bytes.length + len];

		int i;
		for (i = 0; i < bytes.length; i++) {
			data[i] = bytes[i];
		}

		byte[] src = aData.bytes(0, len);
		for (int j = 0; j < len; j++) {
			data[i + j] = src[j];
		}

		bytes = data;
	}

	public void appendByte(byte b) {
		setLength(bytes.length + 1);
		bytes[bytes.length - 1] = b;
	}

	public void appendBytes(byte[] b) {
		int origLen = bytes.length;
		setLength(origLen + b.length);
		for (int i = 0; i < b.length; i++)
			bytes[i + origLen] = b[i];
	}

	public void appendBytes(byte[] b, int n) {
		int origLen = bytes.length;
		setLength(origLen + n);
		for (int i = 0; i < n; i++)
			bytes[i + origLen] = b[i];
	}

	/**
	 * Increases the size of the byte array by the specified amount.
	 */
	public void increaseLengthBy(int increment) {
		setLength(length() + increment);
	}

	/**
	 * Sets the bytes in the array within the specified range to zero.
	 */
	public void resetBytesInRange(NSRange aRange) {
		int loc = aRange.location();
		int max = aRange.maxRange();
		for (int i = loc; i < max; i++) {
			bytes[i] = 0;
		}
	}

	public void replaceBytesInRange(NSRange aRange, byte[] replacementBytes,
			int replacementLength) {
		int i, newlen, shrinklen;
		int loc = aRange.location();
		int max = aRange.maxRange();
		int origLen = bytes.length;
		newlen = origLen + replacementLength - aRange.length();
		if (newlen > origLen)
			setLength(newlen);
		shrinklen = origLen - aRange.length();
		if (aRange.length() > replacementLength) {
			// copy from the beginning(move ahead)
			for (i = 0; i < shrinklen; i++) {
				bytes[loc + replacementLength + i] = bytes[max + i];
			}
		} else if (aRange.length() < replacementLength) {
			// copy from the end(move backward)
			for (i = 1; i <= shrinklen; i++) {
				bytes[newlen - i] = bytes[origLen - i];
			}
		}

		// insert the replacements
		for (i = 0; i < replacementLength; i++) {
			bytes[loc + i] = replacementBytes[i];
		}

		if (newlen < origLen)
			setLength(newlen);
	}

	public enum NSDataSearchOptions {
		NSDataSearchBackwards, NSDataSearchAnchored
	}

    public NSRange rangOfData(NSData dataToFind, NSDataSearchOptions mask,
			NSRange searchRange) {
		byte[] a = bytes();
		byte[] b = dataToFind.bytes();
		
		NSRange range = new NSRange(0, 0);
		if (mask == NSDataSearchOptions.NSDataSearchBackwards) {
			for (int i = searchRange.loc; i < searchRange.loc + searchRange.len - dataToFind.length(); i++) {
				Boolean isFind = true;
				for (int j = 0; j < dataToFind.length(); j++) {
					if (a[j+i] != b[j]) {
						isFind = false;
						break;
					}
				}
				if (isFind == true) {
					range.loc = i;
					range.len = dataToFind.length();
					break;
				}
			}
		}
		return range;
	}

	/**
	 * Copies the data in the specified object to this object, completely
	 * replacing the previous contents.
	 */
	public void setData(NSData aData) {
		bytes = aData.bytes(0, aData.length());
	}

	public byte[] mutableBytes() {
		// TODO Auto-generated method stub
		return bytes;
	}

	public void release() {
		// TODO Auto-generated method stub
		bytes = null;
	}
}

/*
 * $Log: NSMutableData.java,v $ Revision 1.1 2006/02/16 12:47:16 cgruber Check
 * in all sources in eclipse-friendly maven-enabled packages.
 * 
 * Revision 1.2 2003/08/06 23:57:13 chochos appendByte(), appendBytes()
 * 
 * Revision 1.1.1.1 2000/12/21 15:47:34 mpowers Contributing wotonomy.
 * 
 * Revision 1.3 2000/12/20 16:25:38 michael Added log to all files.
 */
