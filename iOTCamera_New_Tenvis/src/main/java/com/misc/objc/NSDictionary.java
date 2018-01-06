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
* A pure java implementation of NSDictionary that
* implements Map for greater java interoperability.
*
* @author michael@mpowers.net
* @author cgruber@israfil.net
* @author $Author: cgruber $
* @version $Revision: 1.9 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSDictionary extends HashMap<Object, Object>  implements Serializable//implements NSKeyValueCoding
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 7763538465753270479L;
	public static final NSDictionary EmptyDictionary = new NSDictionary();

    /**
    * Default constructor produces an empty dictionary.
    */
    public NSDictionary ()
    {
    	super();
   }

    /**
    * Constructor produces an empty dictionary with an initial capacity.
    */
    public NSDictionary (int initialCapacity)
    {
    	super(initialCapacity);
    }

    /**
    * Produces a dictionary that contains one key referencing one value.
    */
    public NSDictionary (Object key, Object value)
    {
    	super();
	    put( key, value );
    }

    /**
    * Produces a dictionary containing the specified keys and values.
    * An IllegalArgumentException is thrown if the arrays are not 
    * of the same length.
    */
    public NSDictionary (Object[] objects, Object[] keys)
    {
    	super();
	    if ( keys.length != objects.length )
	    {
	    	throw new IllegalArgumentException( "Array lengths do not match." );
	    }

	    for ( int i = 0; i < keys.length; i++ )
	    {
	    	put( keys[i], objects[i] );
	    }
    }

    /**
    * Produces a dictionary that is a copy of the specified map (or dictionary).
    */
    public NSDictionary (Map aMap)
    {
    	super( aMap );
    }

    /**
    * Returns a count of the key-value pairs in this dictionary.
    */
    public int count ()
    {
    	return size();
    }

    /**
    * Returns an NSArray containing all keys in this dictionary.
    */
    public NSArray allKeys ()
    {
    	return new NSArray( keySet() );

    }

    /**
    * Returns an NSArray containing all keys that reference the
    * specified value.
    */
    public NSArray allKeysForObject (Object value)
    {
    	NSMutableArray result = new NSMutableArray();
    	Map.Entry entry;
		Iterator it = entrySet().iterator();

		while ( it.hasNext() )
		{
			entry = (Map.Entry) it.next();

			// handle null values
			if ( ( value == null ) && ( entry.getValue() == null )
			|| ( value.equals( entry.getValue() ) ) )
			{
				// if match, add to result set
				result.addObject( entry.getKey() );
			}
		}
		
		return result;
    }

    /**
    * Returns an NSArray containing all values in this dictionary.
    */
    public NSArray allValues ()
    {
    	return new NSArray( values() );
    }

    /**
    * Returns whether the specified dictionary has the same or
    * equivalent key-value pairs as this dictionary.
    */
    public boolean isEqualToDictionary (NSDictionary aDictionary)
    {
    	return equals( aDictionary );
    }

    /**
    * Returns an array of objects for the specified array of keys.
    * If a key isn't found, the marker parameter will be placed 
    * in the corresponding index(es) in the returned array.
    */
    public NSArray objectsForKeys (NSArray anArray, Object aMarker)
    {
    	NSMutableArray result = new NSMutableArray();
    	if ( anArray == null ) return result;

	    Object value;
	    Enumeration enumeration = anArray.objectEnumerator();
	    while ( enumeration.hasMoreElements() )
	    {
	    	value = objectForKey( enumeration.nextElement() );
		    if ( value == null )
		    {
		    	value = aMarker;
		    }
			result.addObject( value );
	    }
	    return result;
    }

    /**
    * Returns an enumeration over the keys in this dictionary.
    */
    public java.util.Enumeration keyEnumerator ()
    {
    	return new java.util.Enumeration()
	    {
	    	Iterator it = NSDictionary.this.keySet().iterator(); 
	    	public boolean hasMoreElements()
		    {
				return it.hasNext();
		    }
		    public Object nextElement()
		    {
		    	return it.next();
		    }
	    };
    }

    /**
    * Returns an enumeration over the values in this dictionary.
    */
    public java.util.Enumeration objectEnumerator ()
    {
    	return new java.util.Enumeration()
	    {
	    	Iterator it = NSDictionary.this.values().iterator(); 
	    	public boolean hasMoreElements()
		    {
				return it.hasNext();
		    }
		    public Object nextElement()
		    {
		    	return it.next();
		    }
	    };
	}
    
    /**
    * Returns the value for the specified key, or null
    * if the key is not found.
    */
    public Object objectForKey (Object aKey)
    {
    	return get( aKey );
    }
    
    // interface NSKeyValueCoding

//    public Object valueForKey (String aKey)
//    { // System.out.println( "valueForKey: " + aKey + "->" + this );      
//        Object result = objectForKey( aKey );
//        if ( result == null ) 
//            result = NSKeyValueCodingSupport.valueForKey( this, aKey );
//        return result;
//    }

    public void takeValueForKey (Object aValue, String aKey)
    { // System.out.println( "takeValueForKey: " + aKey + " : " + aValue + "->" + this );      
        put( aKey, aValue ); //FIXME: technically cheating since this is a read-only class
    }

//    public Object storedValueForKey (String aKey)
//    {
//        Object result = objectForKey( aKey );
//        if ( result == null ) 
//            result = NSKeyValueCodingSupport.storedValueForKey( this, aKey );
//        return result;
//    }

    public void takeStoredValueForKey (Object aValue, String aKey)
    {
        put( aKey, aValue ); //FIXME: technically cheating since this is a read-only class
    }

//    public Object handleQueryWithUnboundKey (String aKey)
//    {
//    	return NSKeyValueCodingSupport.handleQueryWithUnboundKey( this, aKey );
//    }
//
//    public void handleTakeValueForUnboundKey (Object aValue, String aKey)
//    {
//    	NSKeyValueCodingSupport.handleTakeValueForUnboundKey( this, aValue, aKey );
//    }
//
//    public void unableToSetNullForKey (String aKey)
//    {
//    	NSKeyValueCodingSupport.unableToSetNullForKey( this, aKey );
//    }

    public Object validateTakeValueForKeyPath (Object aValue, String aKey)
    {
    	throw new RuntimeException( "Not implemented yet." );
    }

//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//        Enumeration enumeration = keyEnumerator();
//        boolean quote = false;
//        buf.append(NSPropertyListSerialization.TOKEN_BEGIN[NSPropertyListSerialization.PLIST_DICTIONARY]);
//        while (enumeration.hasMoreElements()) {
//            if (buf.length() == 1)
//                buf.append(' ');
//            Object k = enumeration.nextElement();
//            buf.append(NSPropertyListSerialization.stringForPropertyList(k));
//            buf.append(" = ");
//            k = objectForKey(k);
//            buf.append(NSPropertyListSerialization.stringForPropertyList(k));
//            buf.append("; ");
//        }
//        buf.append(NSPropertyListSerialization.TOKEN_END[NSPropertyListSerialization.PLIST_DICTIONARY]);
//        return buf.toString();
//    }

}

/*
 * $Log: NSDictionary.java,v $
 * Revision 1.9  2005/05/11 15:21:53  cgruber
 * Change enum to enumeration, since enum is now a keyword as of Java 5.0
 *
 * A few other comments in the code.
 *
 * Revision 1.8  2003/08/05 00:50:14  chochos
 * use NSPropertyListSerialization to get the tokens to enclose the string description
 *
 * Revision 1.7  2003/08/04 20:26:10  chochos
 * use NSPropertyListSerialization inside toString()
 *
 * Revision 1.6  2003/08/04 18:49:38  chochos
 * NSDictionary(Object[], Object[]) was taking the parameters in the wrong order; for compatibility with Apple's NSDictionary, objects comes first, then keys.
 *
 * Revision 1.5  2003/08/04 18:26:19  chochos
 * fixed opening '{'
 *
 * Revision 1.4  2003/01/28 22:11:30  mpowers
 * Now implements NSKeyValueCoding.
 *
 * Revision 1.3  2002/06/30 17:58:06  mpowers
 * Add a capacity constructor and static empty dictionary: thanks cgruber.
 *
 * Revision 1.2  2001/02/23 23:43:41  mpowers
 * Removed ill-advised this.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:31  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */


