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

import java.util.Comparator;
import java.io.Serializable;
import java.lang.reflect.*;

import android.util.Log;
//import net.wotonomy.util.PropertyComparator;

/**
* A pure java implementation of NSSelector.
*
* @author michael@mpowers.net
* @author $Author: mpowers $
* @version $Revision: 1.9 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSSelector implements /*Comparator,*/ Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2065158584206787348L;
	protected NSMutableDictionary methodMap; // map of classes to methods
	protected String methodName;
	protected Class[] parameterTypes;

	/**
	* A marker to indicate object not found.
	*/
	protected static final String NOT_FOUND = "NOT_FOUND";
	
	/**
	* Saves creating a new class array for parameterless method invocation.
	*/
	protected static final Class[] EMPTY_CLASS_ARRAY = new Class[0];
	
	/**
	* Saves creating a new object array for parameterless method invocation.
	*/
	protected static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
	
    /**
    * Constructor specifying a method name and an array of parameter types.
    */
    public NSSelector (String aMethodName, Class[] aParameterTypeArray)
    {
    	methodName = aMethodName;
	    parameterTypes = aParameterTypeArray;
	    methodMap = new NSMutableDictionary();
    }
    
    /**
    * Constructor specifying a method name with no parameters.
    */
    public NSSelector (String aMethodName)
    {
    	this( aMethodName, new Class[]{NSNotification.class} );
    }
    
    /**
    * Constructor for custom subclasses that implement specific operators
    * and that do not use dynamic method invocation.
    */
    protected NSSelector()
    {
    }

    /**
    * Returns the name of the method.
    */
    public String name ()
    {
    	return methodName;
    }

    /**
    * Returns the array of parameter types.
    */
    public Class[] parameterTypes ()
    {
    	return parameterTypes;
    }

    /**
    * A String description of this selector.
    */
    public String toString ()
    {
    	StringBuffer result = new StringBuffer();
	    result.append( "[" + getClass().getName() + ": name = " + name() + ", parameter types = [" );
        if ( parameterTypes != null )
        {
            if ( parameterTypes.length > 0 )
            {
                result.append( parameterTypes[0].toString() );
            }
            for ( int i = 1; i < parameterTypes.length; i++ )
            {
                result.append( ", " );
                result.append( parameterTypes[i].toString() );
            }
        }
	    result.append( "] ]" );
	    return result.toString();
    }

    /**
    * Returns the appropriate method for the specified class.
    */
    public Method methodOnClass (Class aClass) 
    	throws NoSuchMethodException
	{
		Object result = methodMap.objectForKey( aClass );
		
		if ( result == null )
		{
			result = getMethodForClass( aClass );
            if ( result == null )
            {
                result = NOT_FOUND;
            }
			methodMap.setObjectForKey( result, aClass );
		}
		
		if ( result == NOT_FOUND )
		{
			throw new NoSuchMethodException();
		}
		return (Method) result;
	}
	
    /**
    * Returns the appropriate method, or null if not found.
    */
	private Method getMethodForClass( Class aClass )
	{
		Method[] methods = aClass.getMethods();
		for ( int i = 0; i < methods.length; i++ )
		{
			//Log.v("test", methods[i].getName());
			if ( methods[i].getName().equals( name() ) )
			{
				Class[] params = methods[i].getParameterTypes();
				if ( params.length == parameterTypes.length )
				{
					boolean pass = true;
					for ( int j = 0; j < params.length; j++ )
					{
						if ( ! params[j].isAssignableFrom( parameterTypes[j] ) )
						{
							pass = false;
						}
					}	
					if ( pass ) return methods[i];
				}
			}
		}
		return null;
	}

    /**
    * Convenience to get a method for an object.
    */
    public Method methodOnObject (Object anObject)
    	throws NoSuchMethodException
	{
		Method m = methodOnClass( anObject.getClass() );
        if ( m == null ) throw new NoSuchMethodException( name() );
        return m;
	}

    /**
    * Returns whether the class implements the method for this selector.
    */
    public boolean implementedByClass (Class aClass)
    {
    	try
	    {
			methodOnClass( aClass );
			return true;
	    }
	    catch ( NoSuchMethodException exc )
	    {
	    }
	    return false;
    }

    /**
    * Returns whether the object's class implements the method 
    * for this selector.
    */
    public boolean implementedByObject (Object anObject)
    {
    	try
	    {
			methodOnObject( anObject );
			return true;
	    }
	    catch ( NoSuchMethodException exc )
	    {
	    }
	    return false;
    }

    /**
    * Invokes this selector's method on the specified object 
    * using the specified parameters.
    */
    public Object invoke (Object anObject, Object[] parameters) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return methodOnObject( anObject ).invoke( anObject, parameters );
	}

    /**
    * Invokes this selector's method on the specified object 
    * with no parameters.
    */
    public Object invoke (Object anObject) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return invoke( anObject, EMPTY_OBJECT_ARRAY );
	}

    /**
    * Invokes this selector's method on the specified object 
    * with the specified parameter.
    */
    public Object invoke (Object anObject, Object aParameter) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return invoke( anObject, new Object[] { aParameter } );
	}

    /**
    * Invokes this selector's method on the specified object 
    * using the specified two parameters.
    */
    public Object invoke (Object anObject, Object p1, Object p2) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return invoke( anObject, new Object[] { p1, p2 } );
	}
		    
    /**
    * Invokes the method with the specified signature on the specified 
    * object using the specified parameters.
    */
    public static Object invoke 
		(String methodName, Class[] parameterTypes, Object anObject, Object[] parameters) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return new NSSelector( methodName, parameterTypes ).invoke( anObject, parameters );
	}

    /**
    * Invokes the method with the specified signature on the specified object 
    * with no parameters.
    */
    public static Object invoke  
		(String methodName, Object anObject) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return NSSelector.invoke( 
			methodName, EMPTY_CLASS_ARRAY, anObject, EMPTY_OBJECT_ARRAY );
	}

    /**
    * Invokes the method with the specified signature on the specified 
    * object using the specified parameter.
    */
    public static Object invoke  
		(String methodName, Class[] parameterTypes, 
		 Object anObject, Object aParameter) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return NSSelector.invoke( 
			methodName, parameterTypes, anObject, new Object[] { aParameter } );
	}

    /**
    * Invokes the method with the specified signature on the specified 
    * object using the specified two parameters.
    */
    public static Object invoke  
		(String methodName, Class[] parameterTypes, 
		 Object anObject, Object p1, Object p2) 
    	throws IllegalAccessException, IllegalArgumentException, 
	    	InvocationTargetException, NoSuchMethodException
	{
		return NSSelector.invoke( 
			methodName, parameterTypes, anObject, new Object[] { p1, p2 } );
	}

    // interface Comparator

    private Comparator comparator;	
    
    /**
    * Constructor specifying a method name and a comparator.
    * This is not in the spec.
    */
    public NSSelector (String aMethodName, Comparator aComparator)
    {
    	this( aMethodName, EMPTY_CLASS_ARRAY );
        comparator = aComparator;
    }
    
    /**
    * Returns the Comparator used for this selector.
    * This is not in the spec.
    */
//    public Comparator comparator()
//    {
//        if ( comparator == null )
//        {
//            comparator = new PropertyComparator( methodName );
//        }
//        return comparator;
//    }
//    
//    public int compare(Object o1, Object o2)
//    {
//        if ( comparator == null )
//        {
//            comparator = new PropertyComparator( methodName );
//        }
//        return comparator.compare( o1, o2 );
//    }
//    
    public boolean equals(Object obj)
    {
        return ( obj == this );
    }
}

/*
 * $Log: NSSelector.java,v $
 * Revision 1.9  2003/02/12 19:34:35  mpowers
 * Added accessor for comparator.
 *
 * Revision 1.8  2003/02/07 20:23:41  mpowers
 * Provided backwards compatibility for comparators.
 *
 * Revision 1.7  2003/01/22 23:02:25  mpowers
 * Fixed a null pointer error in NSSelector.toString.
 *
 * Revision 1.6  2003/01/18 23:46:58  mpowers
 * EOSortOrdering is now correctly using NSSelectors.
 *
 * Revision 1.4  2001/10/31 15:24:45  mpowers
 * Implicit constructor is now protected.
 *
 * Revision 1.3  2001/02/07 19:25:51  mpowers
 * Fixed: method matching uses isAssignableFrom rather than ==.
 *
 * Revision 1.2  2001/01/08 23:30:16  mpowers
 * Fixed major bug - selectors were not supposed to share a method map.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:45  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:39  michael
 * Added log to all files.
 *
 *
 */



