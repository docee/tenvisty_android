

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
* An NSNotification is a generic message that can be
* dispatched by the NSNotificationCenter.
*
* @author michael@mpowers.net
* @author $Author: mpowers $
* @version $Revision: 1.7 $
*/
@SuppressWarnings({"rawtypes"})
public class NSNotification  implements Serializable
{
    public static boolean showStack = false;
    
    protected String name;
    protected Object object;
    protected Map userInfo;
    
    // for debugging only
    private Throwable stackTrace;

    /**
    * Default constructor creates a new notification
    * with no name, object, or info dictionary.
    */
    public NSNotification ()
    {
        this( null, null, null );
    }

    /**
    * Constructor specifying name and object.
    */
    public NSNotification ( String aName, Object anObject )
    {	
        this( aName, anObject, null );
    }

    /**
    * Constructor specifying name, object, and a Map
    * containing application specific information.
    */
    public NSNotification ( 
        String aName, Object anObject, Map aUserInfo )
    {
        name = aName;
        object = anObject;
        if ( showStack ) stackTrace = new RuntimeException();
        userInfo = aUserInfo;
    }

    /**
    * Returns the name of this notification.
    */ 
    public String name ()
    {
        return name;
    }

    /**
    * Returns the object of this notification.
    */
    public Object object ()
    {
        return object;
    }

    /**
    * Returns an NSDictionary that is a copy of 
    * the map containing application specific 
    * information relating to this notification,
    * or null if no such data exists.
    */ 
    public NSDictionary userInfo ()
    {
        if ( userInfo == null ) return null;
        return new NSDictionary( userInfo );
    }
    
    /**
    * Returns a Map containing application specific 
    * information relating to this notification,
    * or null if no such data exists.
    * Note: this method is not in the spec.
    */ 
    public Map userInfoMap ()
    {
        return userInfo;
    }
    
    /**
    * Returns the stack trace when this notification was generated,
    * or null if showStack is false, which is the default.
    * NOTE: This method is not part of the specification.
    */
    public Throwable stackTrace()
    {
        return stackTrace;   
    }
    
    /**
    * Returns a human-readable string representation.
    */
    public String toString()
    {
        return "[ " + name() + " : " + object() + " : " + userInfo() + " ]";
    }
}

/*
 * $Log: NSNotification.java,v $
 * Revision 1.7  2002/10/24 18:16:04  mpowers
 * No longer generating stack trace by default.
 *
 * Revision 1.6  2002/06/21 22:02:47  mpowers
 * Oops: Fixed NPE.
 *
 * Revision 1.5  2002/06/21 21:50:41  mpowers
 * Added a method to get the map directly from the notification.
 * Changed the internal representation to a map not a dictionary.
 * We had been creating a new dictionary with each creation.
 * This also allows people to modify the contents of the userInfo.
 *
 * Revision 1.4  2001/04/09 21:41:49  mpowers
 * Better debugging.
 *
 * Revision 1.3  2001/02/21 18:31:07  mpowers
 * Finished and tested implementation of NSNotificationCenter.
 *
 * Revision 1.2  2001/02/20 23:57:03  mpowers
 * Implemented NSNotificationCenter.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:36  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */



