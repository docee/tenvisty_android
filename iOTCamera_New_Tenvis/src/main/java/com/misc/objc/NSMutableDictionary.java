
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
import java.util.*; //collections

/**
* A pure java implementation of NSMutableDictionary that
* implements Map for greater java interoperability.
*
* @author michael@mpowers.net
* @author $Author: cgruber $
* @version $Revision: 1.4 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSMutableDictionary
    extends NSDictionary  implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -6862005019893693505L;

	/**
    * Default constructor produces an empty dictionary.
    */
    public NSMutableDictionary ()
    {
    	super();
    }
    
    /**
    * Default constructor produces an empty dictionary.
    */
    public NSMutableDictionary (int initialSize)
    {
    	super(initialSize);
    }
    
    /**
    * Produces a dictionary that contains one key referencing one value.
    */
    public NSMutableDictionary (Object key, Object value)
    {
    	super( key, value );
    }

    /**
    * Produces a dictionary containing the specified keys and values.
    * An IllegalArgumentException is thrown if the arrays are not 
    * of the same length.
    */
    public NSMutableDictionary (Object[] keys, Object[] values)
    {
    	super( keys, values );
	}

    /**
    * Produces a dictionary that is a copy of the specified map (or dictionary).
    */
    public NSMutableDictionary (Map aMap)
    {
    	super( aMap );
    }	

    /**
    * Removes the key-value pair for the specified key.
    */
    public void removeObjectForKey (Object aKey)
    {
    	remove( aKey );
    }

    /**
    * Copies all mappings from the specified dictionary to this dictionary,
    * replacing any mappings this map had for any keys in the specified map.
    */
    public void addEntriesFromDictionary (Map aMap)
    {
    	putAll( aMap );
    }

    /**
    * Removes all mappings from this dictionary.
    */
    public void removeAllObjects ()
    {
    	clear();
    }

    /**
    * Removes all keys in the specified array from this dictionary. 
    */
    public void removeObjectsForKeys (NSArray anArray)
    {
    	Enumeration enumeration = anArray.objectEnumerator();
	    while ( enumeration.hasMoreElements() )
	    {
	    	removeObjectForKey( enumeration.nextElement() );
	    }
    }

    /**
    * Clears all mappings in this dictionary and then adds all entries
    * in the specified dictionary.
    */
    public void setDictionary (Map aMap)
    {
    	removeAllObjects();
	    addEntriesFromDictionary( aMap );
    }

    /**
    * Sets the value for the specified key.  If the key currently
    * exists to the dictionary, the old value is replaced with the
    * specified value.  An IllegalArgumentException is thrown if 
    * either the key or value is null.
    */
    public void setObjectForKey (Object aValue, Object aKey)
    {
    	if ( ( aKey == null ) || ( aValue == null ) )
	    {
	    	throw new IllegalArgumentException( 
		    	"Cannot use null objects with an NSMutableDictionary." );
	    }
    	put( aKey, aValue );
    }
}

/*
 * $Log: NSMutableDictionary.java,v $
 * Revision 1.4  2005/05/11 15:21:53  cgruber
 * Change enum to enumeration, since enum is now a keyword as of Java 5.0
 *
 * A few other comments in the code.
 *
 * Revision 1.3  2002/06/30 17:16:26  mpowers
 * Added new constructor taking an int: thanks cgruber.
 *
 *
 * Revision 1.2  2001/02/23 23:43:41  mpowers
 * Removed ill-advised this.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:34  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */



