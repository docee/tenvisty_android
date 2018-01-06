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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
* A pure java implementation of NSData, which
* is basically a wrapper on a byte array.
*
* @author michael@mpowers.net
* @author $Author: cgruber $
* @version $Revision: 1.2 $
*/
public class NSData  implements Serializable
{
    public static final NSData EmptyData = new NSData();

    protected byte[] bytes;

    /**
    * Default constructor creates a zero-data object.
    */
    public NSData ()
    {
    	bytes = new byte[0];
    }

    /**
    * Creates an object containing a copy of the specified bytes.
    */
    public NSData (byte[] data)
    {
    	this( data, 0, data.length );
    }

    /**
    * Creates an object containing a copy of the bytes from the specified 
    * array within the specified range.
    */
    public NSData (byte[] data, int start, int length)
    {
    	bytes = new byte[ length ];
	    for ( int i = 0; i < length; i++ )
	    {
	    	bytes[i] = data[ start+i ];
	    }
    }

    /**
    * Creates an object containing the bytes of the specified string.
    */
    public NSData (String aString)
    {
    	this( aString.getBytes() );
    }

    /**
    * Creates an object containing the contents of the specified file.
    * Errors reading the file will produce an empty or partially blank array.
    */
    public NSData (File aFile)
    {
    	int len = (int) aFile.length();
	    byte[] data = new byte[ len ];
	    try
	    {
	    	new java.io.FileInputStream( aFile ).read( data );
	    }
	    catch ( Exception exc )
	    {	
	    	// produce an empty or partially blank array
	    }
		bytes = data;
    }

    /**
    * Creates an object containing the contents of the specified URL.
    */
    public NSData (java.net.URL aURL)
    {
    	throw new RuntimeException( "Not Implemented" );
    }

    /**
    * Creates an object containing a copy of the contents of the 
    * specified NSData object.
    */
    public NSData (NSData aData)
    {
    	this( aData.bytes() );
    }

	/**
	 * Creates a new NSData object from the bytes in the input stream.
	 * The input stream is read fully and is not closed.
	 * @param stream The stream to read from.
	 * @param chunkSize The buffer size used to read from the stream.
	 * @throws IOException if the stream cannot be read from.
	 */
	public NSData(InputStream stream, int chunkSize) throws IOException {
		super();
		byte[] b = new byte[chunkSize];
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		int read = 0;
		do {
			read = stream.read(b);
			if (read > 0)
				bout.write(b, 0, read);
		} while (read > 0);
		bytes = bout.toByteArray();
	}

    /**
    * Returns the length of the contained data.
    */
    public int length ()
    {
    	return bytes.length;
    }

    /**
    * Returns whether the specified data is equivalent to these data.
    */
    public boolean isEqualToData (NSData aData)
    {
    	if (length() != aData.length())
    		return false;
    	byte[] a = bytes();
    	byte[] b = aData.bytes();
	    
	    for ( int i = 0; i < a.length; i++ ) {
			if ( a[i] != b[i] )
				return false;
		}
		return true;
    }

    /**
    * Return the bytes within the data that fall within the specified range.
    */
    public NSData subdataWithRange (NSRange aRange)
    {
    	int loc = aRange.location();
    	byte[] src = bytes();
    	byte[] data = new byte[ aRange.length() ];
    	System.arraycopy(src, loc, data, 0, data.length);
	    return new NSData( data );
    }

    /**
    * Writes the contents of this data to the specified URL.
    * If atomically is true, then the data is written to a temporary
    * file and then renamed to the name specified by the URL when
    * the data transfer is complete.
    */
    public boolean writeToURL (java.net.URL aURL, boolean atomically)
    {
    	throw new RuntimeException( "Not Implemented" );
    }

    /**
    * Convenience to return the contents of the specified file.
    */
    public static NSData dataWithContentsOfMappedFile (java.io.File aFile)
    {
    	return new NSData( aFile );
    }

    /**
    * Returns a copy of the bytes starting at the specified location 
    * and ranging for the specified length.
    */
    public byte[] bytes (int location, int length)
    {
    	byte[] data = new byte[ length ];
	    for ( int i = 0; i < length; i++ )
	    {
	    	data[i] = bytes[ location + i ];
	    }
	    return data;
    }
    
    /**
    * Returns a copy of the bytes backing this data object.
    * NOTE: This method is not in the NSData spec and is
    * included for convenience only.
    */
    public byte[] bytes()
    {
    	return bytes( 0, length() );
    }

//    public String toString() {
//        String hex = "0123456789ABCDEF";
//        StringBuffer buf = new StringBuffer();
//        buf.append(NSPropertyListSerialization.TOKEN_BEGIN[NSPropertyListSerialization.PLIST_DATA]);
//        for (int i = 0; i < bytes.length; i++) {
//            byte b = bytes[i];
//            buf.append(hex.charAt((b & 0xf0) >> 4));
//            buf.append(hex.charAt(b & 0x0f));
//            if (i % 5 == 4)
//                buf.append(' ');
//        }
//        buf.append(NSPropertyListSerialization.TOKEN_END[NSPropertyListSerialization.PLIST_DATA]);
//        return buf.toString();
//    }

	public boolean isEqual(Object obj) {
		if (obj == this)
			return true;
		if (obj instanceof NSData)
			return isEqualToData((NSData)obj);
		return false;
	}

	public static NSData dataWithBytes(byte[] mutableBytes, int i, long length) {
		// TODO Auto-generated method stub
		return new NSData(mutableBytes, i, (int) length);
	}

	public static NSData dataWithBytesNoCopy(byte[] decoded, long l) {
		// TODO Auto-generated method stub
		if(l == decoded.length)
		{
			NSData ns = new NSData();
			ns.bytes = decoded;
			return ns;
		}
		return null;
	}

}

/*
 * $Log: NSData.java,v $
 * Revision 1.2  2006/02/16 13:15:00  cgruber
 * Check in all sources in eclipse-friendly maven-enabled packages.
 *
 * Revision 1.5  2003/08/19 01:53:52  chochos
 * added constructor with an InputStream
 *
 * Revision 1.4  2003/08/05 00:51:31  chochos
 * get the enclosing tokens from NSPropertyListSerialization
 *
 * Revision 1.3  2003/08/04 22:45:47  chochos
 * toString() prints out the bytes in hex (in property list format)
 *
 * Revision 1.2  2003/08/02 01:52:00  chochos
 * added EmptyData
 *
 * Revision 1.1.1.1  2000/12/21 15:47:26  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */
