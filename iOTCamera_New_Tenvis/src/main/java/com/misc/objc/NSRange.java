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

/**
* A pure java implementation of NSRange.
* An NSRange represents a range of numbers
* having a starting location and spanning a
* length.
*
* @author michael@mpowers.net
* @author $Author: cgruber $
* @version $Revision: 1.2 $
*/
public class NSRange implements Cloneable,Serializable
{
    /**
    * An empty range.
    */
    public static final NSRange ZeroRange = new NSRange();
    
    protected int loc;
    protected int len;

    /**
    * Default constructor produces an empty range.
    */
    public NSRange ()
    {
    	this( 0, 0 ); 
    }
    
    public static NSRange MakeNsRange(int location, int length)
    {
    	return new NSRange(location, length);
    }
    
    /**
    * Produces a range with the specified location and length.
    */
    public NSRange (int location, int length)
    {
    	loc = location;
	    len = length;
    }
    
    /**
    * Produces a range that has the same location and length as 
    * the specified range.
    */
    public NSRange (NSRange aRange)
    {
    	this( aRange.location(), aRange.length() );
    }

    /**
    * Returns the location of this range.
    */
    public int location ()
    {
    	return loc;
    }
    
    /**
    * Returns the length of this range.
    */
    public int length ()
    {
    	return len;
    }
    
    /**
    * Returns the maximum extent of the range.  This number is 
    * one more than the last position in the range.
    */
    public int maxRange ()
    {
    	return location() + length();
    }
    
    /**
    * Returns whether this is an empty range, therefore
    * whether the length is zero.
    */
    public boolean isEmpty ()
    {
    	return ( length() == 0 );
    }
    
    /**
    * Returns whether the specified location is contained
    * within this range.
    */
    public boolean locationInRange (int location)
    {
    	if ( location < location() ) return false;
        return location < maxRange();
    }
    
    /**
    * Returns whether the specified range is equal to this range.
    */
    public boolean isEqualToRange (NSRange aRange)
    {
    	if ( aRange == null ) return false;
    	return ( ( aRange.location() == location() ) 
		    &&   ( aRange.length() == length() ) );
    }

    /**
    * Returns whether the specified object is equal to this range.
	*/
    public boolean equals (Object anObject)
    {
    	if ( anObject instanceof NSRange ) 
	    	return isEqualToRange( (NSRange) anObject );
		return false;
    }

    /**
    * Returns a hashCode. 
	*/
    public int hashCode ()
    {
    	// TODO: Test this logic.
    	return ( location() << 2 ) & length(); // bitwise ops never my forte 
    }

    /**
    * Returns a string representation of this range.
	*/
    public String toString ()
    {
    	return "[NSRange: location = " + location() 
	    	+ "; length = " + length() + "]";
    }

    /**
    * Returns the union of this range and the specified range, if any.
    * Gaps are filled, so the result is the smallest starting position
    * and the largest ending position.
	*/
    public NSRange rangeByUnioningRange (NSRange aRange)
    {
    	if ( aRange == null ) return this;
    
    	// TODO: Test this logic.    
		int resultLoc = Math.min( this.location(), aRange.location() );
		int resultLen = Math.max( this.location() + this.length(), 
			aRange.location() + aRange.length() ) - resultLoc;
		return new NSRange( resultLoc, resultLen );
    }	

    /**
    * Returns the intersection of this range and the specified range,
    * if any.  If no intersection, returns an empty range.
	*/
    public NSRange rangeByIntersectingRange (NSRange aRange)
    {
    	// TODO: Test this logic.    
    	if ( ! intersectsRange( aRange ) ) return ZeroRange;
	    int start = Math.max( this.location(), aRange.location() );
	    int end = Math.min( this.location() + this.length(), 
	    	aRange.location() + aRange.length() );
		return new NSRange( start, end - start );
    }

    /**
    * Returns whether the specified range overlaps
    * at any point with this range.
	*/
    public boolean intersectsRange (NSRange aRange)
    {
    	// TODO: Test this logic.    
    	if ( aRange == null ) return false;
	    if ( ( this.location() >= aRange.location() ) 
		&&   ( this.location() < aRange.location() + aRange.length() ) )
			return true;
        return (aRange.location() >= this.location())
                && (aRange.location() < this.location() + this.length());
    }

    /**
    * Returns whether this range is completely 
    * contained within the specified range.
	*/
    public boolean isSubrangeOfRange (NSRange aRange)
    {
    	// TODO: Test this logic.    
    	if ( aRange == null ) return false;
        return (this.location() >= aRange.location())
                && (this.maxRange() <= aRange.maxRange());
    }

    /**
    * Eliminates any intersections between this range and the specified
    * range.  This produces two ranges, either of which may be empty.
    * These two ranges are returned by modifying the supplied second
    * and third parameters.
	*/
//    public void subtractRange (NSRange aRange, 
//    	NSMutableRange firstResult, NSMutableRange secondResult)
//    {
//    	if ( aRange == null ) return;
//	    
//	    // TODO: Test this logic.
//	    // no intersection: return this and aRange without calculation
//	    if ( ! intersectsRange( aRange ) ) 
//	    { 
//	    	if ( firstResult != null )
//		    {
//                firstResult.setLocation( this.location() );
//                firstResult.setLength( this.length() );
//	    	}
//		    if ( secondResult != null )
//		    {
//                secondResult.setLocation( aRange.location() );
//                secondResult.setLength( aRange.location() );
//	    	}
//		    return;
//	    }
//	    
//	    // TODO: Test this logic.
//	    // this range is completely contained by other range
//	    if ( isSubrangeOfRange( aRange ) )
//	    { 
//			if ( firstResult != null )
//		    {
//                firstResult.setLocation( aRange.location() );
//                firstResult.setLength( this.location() - aRange.location() );
//	    	}
//		    if ( secondResult != null )
//		    {
//                secondResult.setLocation( this.maxRange() );
//                secondResult.setLength( 
//					aRange.maxRange() - this.maxRange() - 1 ); // test this
//	    	}
//		    return;
//	    }
//
//	    // TODO: Test this logic.
//	    // other range is completely contained by this range
//	    if ( aRange.isSubrangeOfRange( this ) )
//	    { 
//			if ( firstResult != null )
//		    {
//                firstResult.setLocation( this.location() );
//                firstResult.setLength( aRange.location() - this.location() );
//	    	}
//		    if ( secondResult != null )
//		    {
//                secondResult.setLocation( aRange.maxRange() );
//                secondResult.setLength( 
//					this.maxRange() - aRange.maxRange() - 1 ); // test this
//	    	}
//		    return;
//	    }
//	    
//	    // TODO: Test this logic.
//	    // ranges intersect: remove only the intersection
//	    
//	    NSRange firstRange, secondRange;
//	    if ( this.location() <= aRange.location() )
//	    {
//	    	firstRange = this;
//		    secondRange = aRange;	
//	    }
//	    else
//	    {
//	    	firstRange = aRange;
//		    secondRange = this;	
//	    }
//	    
//        if ( firstResult != null )
//        {
//            firstResult.setLocation( firstRange.location() );
//            firstResult.setLength( 
//	    		secondRange.location() - firstRange.location() );
//        }
//        if ( secondResult != null )
//        {
//            secondResult.setLocation( firstRange.maxRange() );
//            secondResult.setLength(
//                secondRange.maxRange() - aRange.maxRange() - 1 ); // test this
//        }
//        return;
//
//    }

    /**
    * Returns a copy of this range.
	*/
    public Object clone ()
    {
    	return new NSRange( location(), length() );
    }

    /**
    * Parses a range from a string of the form "{x,y}" where
    * x is the location and y is the length.  If not parsable,
    * an IllegalArgumentException is thrown.
	*/
    public static NSRange fromString (String aString)
    {
    	// TODO: Test this logic.    
    	try
	    {
            java.util.StringTokenizer tokens =
                new java.util.StringTokenizer( aString, "{,}" );
			int loc = Integer.parseInt( tokens.nextToken() );
			int len = Integer.parseInt( tokens.nextToken() );
			return new NSRange( loc, len );
	    }
	    catch ( Exception exc )
	    {
	    	throw new IllegalArgumentException( exc.toString() );
	    }
    }
    
}

/*
 * $Log: NSRange.java,v $
 * Revision 1.2  2006/02/16 13:15:00  cgruber
 * Check in all sources in eclipse-friendly maven-enabled packages.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:42  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */