

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
* NSArray is an unmodifiable List.  
* Calling the mutator methods of the List interface (add, addAll, 
* set, etc.) on an instance of NSArray will throw an Unsupported
* Operation exception: use a NSMutableArray instead.  This is to
* simulate Objective-C's pattern of exposing mutator methods only
* on mutable subinterface, which is wonderful for communicating
* via interface the contract on returned collections (whether you
* may modify return values) as well as implementing array faults.
*
* @author michael@mpowers.net
* @author $Author: cgruber $
* @version $Revision: 1.16 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSArray implements List, Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 6347896769820805112L;

	/**
    * Actual list that backs this instance.
    */
    List list;
    
	/**
	* Return value when array index is not found.
	*/
    public static final int NotFound = -1;

	/**
	* A constant representing an empty array.
	*/
    public static final NSArray EmptyArray = new NSArray();

    /**
    * Returns an NSArray backed by the specified List.
    * This is useful to "protect" an internal representation
    * that is returned by a method of return type NSArray.
    */
    public static NSArray arrayBackedByList( List aList )
    {
        return new NSArray( aList, null );
    }
    
    NSArray( List aList, Object ignored ) // differentiates
    {
        list = aList;
    }
    
    /**
    * Constructor with a size hint, used by NSMutableArray.
    */
    NSArray( int aSize )
    {
        list = new ArrayList( aSize );
    }
    
	/**
	* Default constructor returns an empty array.
	*/
    public NSArray ()
    {
    	list = new ArrayList();
//if ( ! ( this instanceof NSMutableArray ) )        
//System.out.println( "NSArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }	

	/**
	* Produces an array containing only the specified object.
	*/
    public NSArray (Object anObject)
    {
    	this();
	    list.add( anObject );
//if ( ! ( this instanceof NSMutableArray ) )        
//System.out.println( "NSArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Produces an array containing the specified objects.
	*/
    public NSArray (Object[] anArray)
    {
    	this();
	    for ( int i = 0; i < anArray.length; i++ )
	    {
	    	list.add( anArray[i] );
	    }
//if ( ! ( this instanceof NSMutableArray ) )        
//System.out.println( "NSArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Produces an array containing the objects in the specified collection.
	*/
    public NSArray (Collection aCollection)
    {
        this();
    	Iterator i = aCollection.iterator();
        while ( i.hasNext() ) list.add( i.next() );
//if ( ! ( this instanceof NSMutableArray ) )        
//System.out.println( "NSArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Returns the number of items in this array.
	*/
    public int count ()
    {
    	return list.size();
    }

	/**
	* Returns an array containing all objects in this array 
	* plus the specified object.
	*/
    public NSArray arrayByAddingObject (Object anObject)
    {
    	NSArray result = new NSArray( this );
	    result.protectedAdd( anObject );
	    return result;
    }

	/**
	* Returns an array containing all objects in this array
	* plus all objects in the specified list.
	*/
    public NSArray arrayByAddingObjectsFromArray (Collection aCollection)
    {
    	NSArray result = new NSArray( this );
	    result.protectedAddAll( aCollection );
	    return result;
    }

	/**
	* Returns a string containing the string representations of
	* each element in this array, with each element separated from
	* each neighboring element by the specified string.
	*/
    public String componentsJoinedByString (String separator)
    {
    	StringBuffer buf = new StringBuffer();
	    Iterator it = list.iterator();
	    if ( it.hasNext() )
	    {
	    	buf.append( it.next().toString() );
	    }
	    while ( it.hasNext() )
	    {
	    	buf.append( separator );
		    buf.append( it.next().toString() );
	    }
	    return buf.toString();
    }

	/**
	* Returns whether an equivalent object is contained in this array.
	*/
    public boolean containsObject (Object anObject)
    {
    	return list.contains( anObject );
    }

	/**
	* Returns the first object in this array that is equivalent to
	* an object in the specified list, or null if no objects are
	* in common.
	*/
    public Object firstObjectCommonWithArray (Collection aCollection)
    {
    	if ( aCollection == null ) return null;
	    
	    Object o;
	    Iterator it = list.iterator();
	    while ( it.hasNext() )
	    {
	    	o = it.next();
	    	if ( aCollection.contains( o ) ) return o;
	    }
	    return null;
    }

	/**
	* Returns whether the specified list contains elements equivalent
	* to those in this array in the same order.
	*/
    public boolean isEqualToArray (List aList)
    {
    	return list.equals( aList );
    }

	/**
	* Returns the last object in this array, or null if the array is empty.
	*/
    public Object lastObject ()
    {
    	int i;
	    if ( (i = list.size()) == 0 ) return null;
	    return list.get( i - 1 );
    }

	/**
	* 
	*/
/*
    public NSArray sortedArrayUsingSelector (NSSelector);
*/

	/**
	* Returns an array comprised of only those elements whose
	* indices fall within the specified range.
	*/
    public NSArray subarrayWithRange (NSRange aRange)
    {
    	//TODO: Test this logic.
    	NSArray result = new NSArray();
    	if ( aRange == null ) return result;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
        int count = count();
    	for ( int i = loc; i < max && i < count; i++ )
	    {
	    	result.protectedAdd( list.get( i ) );
	    }
	    return result;
    }

	/**
	* Returns an enumeration over the the elements of the array.
	*/
    public Enumeration objectEnumerator ()
    {
    	//TODO: Test this logic.
    	return new Enumeration()
		{
	    	Iterator it = NSArray.this.iterator(); 
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
	* Returns an enumeration over the elements of the array in reverse order.
	*/
    public java.util.Enumeration reverseObjectEnumerator ()
    {
	    return new java.util.Enumeration()
	    {
	    	ListIterator it = null;
		    public ListIterator getIterator()
		    {
		    	if ( it == null )
			    {
                    it = NSArray.this.listIterator();
                    // zoom to end
                    while ( it.hasNext() ) it.next();
				}
				return it;
		    }
	    	public boolean hasMoreElements()
		    {
				return getIterator().hasPrevious();
		    }
		    public Object nextElement()
		    {
		    	return getIterator().previous();
		    }
	    };
	}

	/**
	* Copies the elements of this array into the specified object array
	* as the array's capacity permits.
	*/
    public void getObjects (Object[] anArray)
    {
    	if ( anArray == null ) return;
	    
    	for ( int i = 0; i < anArray.length; i++ )
	    {
	    	anArray[i] = objectAtIndex( i );
	    }
    }

	/**
	* Copies the elements of this array that fall within the specified range
	* into the specified object array as the array's capacity permits.
	*/
    public void getObjects (Object[] anArray, NSRange aRange)
    {
    	if ( ( anArray == null ) || ( aRange == null ) ) return;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
	    for ( int i = loc; i < max; i++ )
	    {
	    	anArray[ i-loc ] = objectAtIndex( i );
	    }
    }

	/**
	* Returns the index of the first object in the array equivalent
	* to the specified object.  Returns NotFound if the item is not found.
	*/
    public int indexOfObject (Object anObject)
    {
    	int result = list.indexOf( anObject );
	    if ( result == -1 ) return NotFound; // in case this changes
	    return result;
    }

	/**
	* Returns the index of the first object in the array 
	* within the specified range equivalent to the specified object.
	* Returns NotFound if the item is not found.
	*/
    public int indexOfObject (Object anObject, NSRange aRange)
    {
    	if ( ( anObject == null ) || ( aRange == null ) ) return NotFound;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
	    for ( int i = loc; i < max; i++ )
	    {
	    	if ( anObject.equals( list.get(i) ) )
		    {
		    	return i;
		    }
	    }
	    return NotFound;
    }

	/**
	* Returns the index of the specified object if it exists
	* in the array, comparing by reference.
	* Returns NotFound if the item is not found.
	*/
    public int indexOfIdenticalObject (Object anObject)
    {
        int size = list.size();
	    for ( int i = 0; i < size; i++ )
	    {
	    	if ( anObject == list.get(i) )
		    {
		    	return i;
		    }
	    }
	    return NotFound;
    }

	/**
	* Returns the index of the first object in the array 
	* within the specified range equivalent to the specified object.
	*/
    public int indexOfIdenticalObject (Object anObject, NSRange aRange)
    {
    	if ( aRange == null ) return NotFound;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
	    for ( int i = loc; i < max; i++ )
	    {
	    	if ( anObject == list.get(i) )
		    {
		    	return i;
		    }
	    }
	    return NotFound;
    }

	/**
	* Returns the object at the specified index.  Throws an
	* IndexOutOfRange exception if the index is out of range.
	*/
    public Object objectAtIndex (int anIndex)
    {
    	return list.get( anIndex );
    }

	/**
	* Returns an array consisting of strings within the specified string
	* as delimited by the specified separator characters. 
	*/
    public static NSArray componentsSeparatedByString 
    	(String aString, String aSeparator)
    {
    	NSArray result = new NSArray();
	    if ( aString == null ) return result;
	    if ( aSeparator == null ) return new NSArray( aString );
	    
        //FIXME: The spec probably considers the whole
        // string as a separator, unlike string tokenizer.
	    java.util.StringTokenizer tokens = 
	    	new java.util.StringTokenizer( aString, aSeparator );
	    while ( tokens.hasMoreTokens() )
	    {
	    	result.protectedAdd( tokens.nextToken() );
	    }
	    
	    return result;
    }
    
    public Object clone()
    {
        return new NSArray( list );
    }
    
    public NSArray immutableClone()
    {
        return this;
    }
	
    public NSMutableArray mutableClone()
    {
        return new NSMutableArray( this );
    }
    
//    public String toString() {
//        StringBuffer buf = new StringBuffer();
//        buf.append(NSPropertyListSerialization.TOKEN_BEGIN[NSPropertyListSerialization.PLIST_ARRAY]);
//        for (int i = 0; i < count(); i++) {
//            Object x = objectAtIndex(i);
//            buf.append(NSPropertyListSerialization.stringForPropertyList(x));
//            if (i < count() - 1)
//                buf.append(", ");
//        }
//        buf.append(NSPropertyListSerialization.TOKEN_END[NSPropertyListSerialization.PLIST_ARRAY]);
//        return buf.toString();
//    }
    
    // interface List: accessors
    
    public boolean contains(Object o) { return list.contains(o); }
    public boolean containsAll(Collection c) { return list.containsAll(c); }
    public boolean equals(Object o) { return list.equals(o); }
    public Object get(int index) { return list.get(index); }
    public int hashCode() { return list.hashCode(); }
    public int indexOf(Object o) { return list.indexOf(o); }
    public boolean isEmpty() { return list.isEmpty(); }
    public int lastIndexOf(Object o) { return list.lastIndexOf(o); }
	public int size() { return list.size(); }
    public Object[] toArray() { return list.toArray(); }
    public Object[] toArray(Object[] a) { return list.toArray(a); }
    
    // interface List: mutators

	public void add(int index, Object element) 
    {
        throw new UnsupportedOperationException();
    }
    
	public boolean add(Object o)
    {
	    throw new UnsupportedOperationException();
    }

	public boolean addAll(Collection coll) 
    {
	    throw new UnsupportedOperationException();
    }

	public boolean addAll(int index, Collection c) 
    {
	    throw new UnsupportedOperationException();
    }
    
	public void clear() 
    {
        throw new UnsupportedOperationException();
    }

	public Iterator iterator() 
    {
        // make a copy to avoid ConcurrentModificationExceptions
        final Iterator i = new LinkedList( list ).iterator();
	    return new Iterator() 
        {
            public boolean hasNext() {return i.hasNext();}
            public Object next() 	 {return i.next();}
            public void remove() { throw new UnsupportedOperationException(); }
        };
    }

	public ListIterator listIterator() { return listIterator(0); }

	public ListIterator listIterator(final int index) 
    {
        // make a copy to avoid ConcurrentModificationExceptions
        final ListIterator i = new LinkedList( list ).listIterator(index);
	    return new ListIterator() 
        {
            public boolean hasNext()     {return i.hasNext();}
            public Object next()         {return i.next();}
            public boolean hasPrevious() {return i.hasPrevious();}
            public Object previous()     {return i.previous();}
            public int nextIndex()       {return i.nextIndex();}
            public int previousIndex()   {return i.previousIndex();}
            public void remove() { throw new UnsupportedOperationException(); }
            public void set(Object o) { throw new UnsupportedOperationException(); }
            public void add(Object o) { throw new UnsupportedOperationException(); }
	    };
	}

	public Object remove(int index) 
    {
	    throw new UnsupportedOperationException();
    }
    
	public boolean remove(Object o) 
    {
	    throw new UnsupportedOperationException();
    }

	public boolean removeAll(Collection coll) 
    {
	    throw new UnsupportedOperationException();
    }

	public boolean retainAll(Collection coll) 
    {
	    throw new UnsupportedOperationException();
    }

    public Object set(int index, Object element) 
    {
	    throw new UnsupportedOperationException();
    }
    
    public List subList(int fromIndex, int toIndex) 
    {
        return Collections.unmodifiableList(list.subList(fromIndex, toIndex));
    }
    
    /**
    * Provided for the use of subclasses like ArrayFault.
    */
    protected boolean protectedAdd( Object o )
    {
        return list.add( o );
    }
    
    /**
    * Provided for the use of subclasses like ArrayFault.
    */
    protected boolean protectedAddAll( Collection coll )
    {
        return list.addAll( coll );
    }
}

/*
 * $Log: NSArray.java,v $
 * Revision 1.16  2005/07/13 14:12:44  cgruber
 * Add mutableClone() and immutableClone()  per. WebObjects 5.3 conformance.
 *
 * Revision 1.15  2003/08/06 23:07:52  chochos
 * general code cleanup (mostly, removing unused imports)
 *
 * Revision 1.14  2003/08/05 00:48:56  chochos
 * use NSPropertyListSerialization to get the opening and closing tokens for the string representation
 *
 * Revision 1.13  2003/08/04 20:26:10  chochos
 * use NSPropertyListSerialization inside toString()
 *
 * Revision 1.12  2003/08/04 18:18:43  chochos
 * toString() yields strings in the same format as Apple's NSArray
 *
 * Revision 1.11  2003/01/28 19:44:20  mpowers
 * Fixed reverse enumerator.
 *
 * Revision 1.10  2003/01/18 23:49:55  mpowers
 * Added mutableClone().
 *
 * Revision 1.9  2003/01/18 23:30:42  mpowers
 * WODisplayGroup now compiles.
 *
 * Revision 1.8  2003/01/16 22:47:30  mpowers
 * Compatibility changes to support compiling woextensions source.
 * (34 out of 56 classes compile!)
 *
 * Revision 1.7  2003/01/10 19:16:40  mpowers
 * Implemented support for page caching.
 *
 * Revision 1.6  2002/10/24 21:15:36  mpowers
 * New implementations of NSArray and subclasses.
 *
 * Revision 1.5  2002/10/24 18:16:30  mpowers
 * Now enforcing NSArray's immutable nature.
 *
 * Revision 1.4  2002/03/08 19:02:54  mpowers
 * Long-overdue speed optimization of indexOfIdenticalObject.
 *
 * Revision 1.3  2002/02/13 22:02:56  mpowers
 * Fixed: bug in componentsSeparatedByString when separator is null
 * (thanks to Cedrik LIME).
 *
 * Revision 1.2  2001/01/11 20:34:26  mpowers
 * Implemented EOSortOrdering and added support in framework.
 * Added header-click to sort table columns.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:26  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:37  michael
 * Added log to all files.
 *
 *
 */



