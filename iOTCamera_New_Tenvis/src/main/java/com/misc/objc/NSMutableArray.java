

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
* NSMutableArray extends NSArray to allow modification.
*
* @author michael@mpowers.net
* @author $Author: cgruber $
* @version $Revision: 1.8 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSMutableArray extends NSArray  implements Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -4113125048899908240L;

	/**
    * Returns an NSArray backed by the specified List.
    * This is useful to "protect" an internal representation
    * that is returned by a method of return type NSArray.
    */
    
	public static NSMutableArray mutableArrayBackedByList( List aList )
    {
        return new NSMutableArray( aList, null );
    }
    
    NSMutableArray( List aList, Object ignored ) // differentiates
    {
        super( aList, ignored );
    }
    
	/**
	* Default constructor returns an empty array.
	*/
    public NSMutableArray ()
    {
    	super();
//System.out.println( "NSMutableArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }	

	/**
	* Constructor with a size hint.
	*/
    public NSMutableArray ( int aSize )
    {
    	super();
//System.out.println( "NSMutableArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }	

	/**
	* Produces an array containing only the specified object.
	*/
    public NSMutableArray (Object anObject)
    {
    	super( anObject );
//System.out.println( "NSMutableArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Produces an array containing the specified objects.
	*/
    public NSMutableArray (Object[] anArray)
    {
    	super( anArray );
//System.out.println( "NSMutableArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Produces an array containing the objects in the specified collection.
	*/
    public NSMutableArray (Collection aCollection)
    {
    	super( aCollection );
//System.out.println( "NSMutableArray: " + net.wotonomy.ui.swing.util.StackTraceInspector.getMyCaller() );
    }

	/**
	* Removes the last object from the array.
	*/
    public void removeLastObject ()
    {
		list.remove( count() - 1 );    
    }

	/**
	* Removes the object at the specified index.
	*/
    public void removeObjectAtIndex (int index)
    {
    	list.remove( index );
    }

	/**
	* Adds all objects in the specified collection.
	*/
	public void addObjectsFromArray (Collection aCollection)
    {
    	list.addAll( aCollection );
    }

	/**
	* Removes all objects from the array.
	*/
    public void removeAllObjects ()
    {
    	list.clear();
    }

	/**
	* Removes all objects equivalent to the specified object
	* within the range of specified indices.
	*/
    public void removeObject (Object anObject, NSRange aRange)
    {
    	if ( ( anObject == null ) || ( aRange == null ) ) return;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
	    for ( int i = loc; i < max; i++ )
	    {
	    	if ( anObject.equals( list.get( i ) ) )
		    {
		    	list.remove( i );
			    i = i - 1;
			    max = max - 1;
		    }
	    }
    }

	/**
	* Removes all instances of the specified object within the 
	* range of specified indices, comparing by reference.
	*/
    public void removeIdenticalObject (Object anObject, NSRange aRange)
    {
    	if ( ( anObject == null ) || ( aRange == null ) ) return;
	    
	    int loc = aRange.location();
	    int max = aRange.maxRange();
	    for ( int i = loc; i < max; i++ )
	    {
	    	if ( anObject == list.get( i ) )
		    {
		    	list.remove( i );
			    i = i - 1;
			    max = max - 1;
		    }
	    }
    }

	/**
	* Removes all objects in the specified collection from the array.
	*/
    public void removeObjectsInArray (Collection aCollection)
    {
    	list.removeAll( aCollection );
    }

	/**
	* Removes all objects in the indices within the specified range 
	* from the array.
	*/
    public void removeObjectsInRange (NSRange aRange)
    {
    	if ( aRange == null ) return;
	    
	    for ( int i = 0; i < aRange.length(); i++ )
	    {
	    	list.remove( aRange.location() );
	    }
    }

	/**
	* Replaces objects in the current range with objects from
	* the specified range of the specified array.  If currentRange
	* is larger than otherRange, the extra objects are removed.
	* If otherRange is larger than currentRange, the extra objects
	* are added.
	*/
    public void replaceObjectsInRange (NSRange currentRange, 
    	List otherArray, NSRange otherRange)
	{
		if ( ( currentRange == null ) || ( otherArray == null ) ||
			( otherRange == null ) ) return;
	
		// transform otherRange if out of bounds for array
		if ( otherRange.maxRange() > otherArray.size() )
		{
			// TODO: Test this logic.
			int loc = Math.min( otherRange.location(), otherArray.size() - 1 );
			otherRange = new NSRange( loc, otherArray.size() - loc ); 
		}
		
		Object o;
		List subList = list.subList( 
			currentRange.location(), currentRange.maxRange() );
		int otherIndex = otherRange.location();
		// TODO: Test this logic.
		for ( int i = 0; i < subList.size(); i++ )
		{
			if ( otherIndex < otherRange.maxRange() )
			{ // set object
				subList.set( i, otherArray.get( otherIndex ) ); 
			}
			else
			{ // remove extra elements from currentRange
				subList.remove( i );
				i--; 
			}
			otherIndex++;
		}
		// TODO: Test this logic.
		for ( int i = otherIndex; i < otherRange.maxRange(); i++ )
		{
			list.add( otherArray.get( i ) );
		}
	}

	/**
	* Clears the current array and then populates it with the
	* contents of the specified collection.
	*/
    public void setArray (Collection aCollection)
    {
    	list.clear();
	    list.addAll( aCollection );
    }

	/**
	* Sorts this array using the values from the specified selector.
	*/
//    public void sortUsingSelector (NSSelector aSelector)
//    {
//    	//TODO: implement 
//        throw new net.wotonomy.util.WotonomyException( "Not implemented yet." );
//    }

	/**
	* Removes all objects equivalent to the specified object. 
	*/
    public void removeObject (Object anObject)
    {
        list.remove( anObject );
    }

	/**
	* Removes all occurences of the specified object,
	* comparing by reference.
	*/
    public void removeIdenticalObject (Object anObject)
    {
    	Iterator it = list.iterator();
	    while ( it.hasNext() )
	    {
	    	if ( it.next() == anObject )
		    {
		    	it.remove();
		    }
	    }	
    }

	/**
	* Inserts the specified object into this array at the
	* specified index.
	*/
    public void insertObjectAtIndex (Object anObject, int anIndex)
    {
    	list.add( anIndex, anObject );
    }
    
	/**
	* Replaces the object at the specified index with the 
	* specified object.
	*/
    public void replaceObjectAtIndex (int anIndex, Object anObject)
    {
    	list.set( anIndex, anObject );
    }

	/**
	* Adds the specified object to the end of this array.
	*/
    public void addObject (Object anObject) 
    {
    	list.add( anObject );
    }
    
    public Object clone()
    {
        return new NSMutableArray( list );
    }
    
    public NSArray immutableClone() {
        return new NSArray(this);
    }
	
    public NSMutableArray mutableClone() {
        return new NSMutableArray(this);
    }
	
    // interface List: mutators

	public void add(int index, Object element) 
    {
        list.add( index, element );
    }
    
	public boolean add(Object o)
    {
        return list.add(o);
    }

	public boolean addAll(Collection coll) 
    {
        return list.addAll(coll);
    }

	public boolean addAll(int index, Collection c) 
    {
        return list.addAll( index, c );
    }
    
	public void clear() 
    {
        list.clear();
    }

	public Iterator iterator() 
    {
        return list.iterator();
    }

	public ListIterator listIterator() 
    {
        return list.listIterator();
    }    

	public ListIterator listIterator(int index)
    {
        return list.listIterator();
	}

	public Object remove(int index) 
    {
        return list.remove( index );
    }
    
	public boolean remove(Object o) 
    {
        return list.remove(o);
    }

	public boolean removeAll(Collection coll) 
    {
        return list.removeAll(coll);
    }

	public boolean retainAll(Collection coll) 
    {
        return list.retainAll(coll);
    }

    public Object set(int index, Object element) 
    {
        return list.set( index, element );
    }
    
    public List subList(int fromIndex, int toIndex) 
    {
        return list.subList( fromIndex, toIndex );
    }

	public void release() {
		// TODO Auto-generated method stub
		
	}
    
}

/*
 * $Log: NSMutableArray.java,v $
 * Revision 1.8  2005/07/13 14:12:44  cgruber
 * Add mutableClone() and immutableClone()  per. WebObjects 5.3 conformance.
 *
 * Revision 1.7  2003/08/06 23:07:52  chochos
 * general code cleanup (mostly, removing unused imports)
 *
 * Revision 1.6  2003/01/16 22:47:30  mpowers
 * Compatibility changes to support compiling woextensions source.
 * (34 out of 56 classes compile!)
 *
 * Revision 1.5  2003/01/10 19:16:40  mpowers
 * Implemented support for page caching.
 *
 * Revision 1.4  2002/10/24 21:15:36  mpowers
 * New implementations of NSArray and subclasses.
 *
 * Revision 1.3  2002/10/24 18:16:30  mpowers
 * Now enforcing NSArray's immutable nature.
 *
 * Revision 1.2  2001/01/11 20:34:26  mpowers
 * Implemented EOSortOrdering and added support in framework.
 * Added header-click to sort table columns.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:31  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */


