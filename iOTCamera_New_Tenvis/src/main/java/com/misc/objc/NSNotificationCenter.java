

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
import java.lang.ref.*;
import java.util.*; //collections
//import net.wotonomy.util.WotonomyException;

/**
* NSNotificationCenter broadcasts NSNotifications to
* registered observers.  Observers can register for all
* notifications of a specific type, or all notifications
* about a specific object, or both.  Observers specify 
* the method that will be called when they are notified.
* A global notification center can be accessed with 
* defaultCenter(), but other centers can be created and
* used independently of the default center. <br><br>
*
* This implementation uses weak references for observers
* and observables.  The advantage to this approach is
* that you do not need to explicitly unregister observers
* or observables; they will be unregistered when they are
* garbage-collected.  Note that you will need to retain
* a reference to any objects you register or they may
* become unregistered if no other object references them.
*
* @author michael@mpowers.net
* @author $Author: mpowers $
* @version $Revision: 1.11 $
*/
@SuppressWarnings({"rawtypes", "unchecked"})
public class NSNotificationCenter  implements Serializable
{
    /** 
    * Null marker class simplifies equals() logic
    * for CompoundKey class below.  
    */
    public static final Object NullMarker = new Object();    
  
    private static NSNotificationCenter defaultCenter = null;
    
    /**
    * A Map of (name,object) pairs to a List 
    * of (observer,selector) pairs.
    */
    private Hashtable observers; // thread-safe
    
    /**
    * Default constructor creates a new notification center.
    */
    public NSNotificationCenter()
    {
        observers = new Hashtable();
    }
    
    /**
    * Returns the system default center, creating one
    * if it has not yet been created.
    */
    static public NSNotificationCenter defaultCenter()
    {
        if ( defaultCenter == null )
        {
            defaultCenter = new NSNotificationCenter();   
        }
        return defaultCenter;
    }
	
	/**
	* Addes the specified observer to the notification queue for
	* notifications with the specified name or the specified 
	* object or both.
	* @param anObserver The observer that wishes to be notified.
	* @param aSelector The selector that will be invoked.  
	* Must have exactly one argument, to which a notification
	* will be passed.
	* @param notificationName The name of the notifications for
	* which the observer will be notified.  If null, will notify
	* only based on matching anObject.
	* @param anObject The object of the notifications for which
	* the observer will be notified.  If null, will notify 
	* only based on matching notificationName.
	*/
	public void addObserver(
		Object anObserver, NSSelector aSelector,
		String notificationName, Object anObject )
    {
        // remove freed objects
        processKeyQueue();
        
        Object name = notificationName;
        if ( name == null )
        {
            name = NullMarker;
        }
        if ( anObject == null )
        {
            anObject = NullMarker;
        }
        Object key = new CompoundKey( name, anObject );
        Object value = new CompoundValue( anObserver, aSelector );
        List list = (List) observers.get( key );
        if ( list == null )
        {
            // create new list with value and put it in map
            list = new Vector(); // thread-safe
            list.add( value );
            observers.put( new CompoundKey( 
                name, anObject, keyQueue ), list );
        }
        else
        {
            // add only if not already in list
            if ( ! list.contains( value ) )
            {
                list.add( value );   
            }
        }
    }
	
	/**
	* Posts the specified notification.  Notifies all registered
	* observers that match either the notification name or 
	* the notification object, or both.
	* @param aNotification The notification that will be passed
	* to the observers selector.
	*/
	public void postNotification(
		NSNotification aNotification )
    {
        List mergedList = new LinkedList();
        Object key, observerList;
        
        Object name = aNotification.name();
        Object object = aNotification.object();
        
        if ( name != null ) 
        {
            if ( object != null ) 
            { // both are specified
                observerList = observers.get( new CompoundKey( name, object ) );
                if ( observerList != null )
                {
                    mergedList.addAll( (List) observerList );
                }
                observerList = observers.get( new CompoundKey( name, NullMarker ) );
                if ( observerList != null )
                {
                    mergedList.addAll( (List) observerList );
                }
                observerList = observers.get( new CompoundKey( NullMarker, object ) );
                if ( observerList != null )
                {
                    mergedList.addAll( (List) observerList );
                }
            }
            else 
            { // object is null
                observerList = observers.get( new CompoundKey( name, NullMarker ) );
                if ( observerList != null )
                {
                    mergedList.addAll( (List) observerList );
                }
            }
        }
        else
        if ( object != null ) 
        { // name is null
            observerList = observers.get( new CompoundKey( NullMarker, object ) );
            if ( observerList != null )
            {
                mergedList.addAll( (List) observerList );
            }
        }

        key = new CompoundKey(
            NullMarker, NullMarker );
        observerList = observers.get( key );
        if ( observerList != null )
        {
            mergedList.addAll( (List) observerList );
        }

        CompoundValue value;
        Iterator it = mergedList.iterator();
        while ( it.hasNext() )
        {
            value = (CompoundValue) it.next();
            if ( value.get() == null )
            {
                it.remove();
            }
            else
            {
                try
                {
                    value.selector().invoke( 
                        value.get(), 
                        new Object[] { aNotification } );
                }
                catch ( Exception exc )
                {
                   // WotonomyException w = new WotonomyException(
                   //     "Error notifying object: " + value.get() + " : " + aNotification, exc );
//                    throw w;
//                    exc.printStackTrace();
postNotification( "Error notifying object", this, new NSDictionary( "exception", exc ) );
                }
            }
        }
        
    }
	
	/**
	* Posts a notification created from the specified name
	* and object.  Calls postNotification( NSNotification ).
	* @param notificationName a String key to distinguish
	* this notification.
	* @param anObject any object, by convention this is 
	* the originator of the notification.
	*/
	public void postNotification(
		String notificationName, Object anObject )
    {
        postNotification( new NSNotification( 
            notificationName, anObject ) );
    }
	
	/**
	* Posts a notification created from the specified name,
	* object, and info.  Calls postNotification( NSNotification ).
	* @param notificationName a String key to distinguish
	* this notification.
	* @param anObject any object, by convention this is 
	* the originator of the notification.
	* @param userInfo a Map containing information specific
	* to the originator of the notification and that may
	* be of interest to a knowledgable observer.
	*/
	public void postNotification(
		String notificationName, Object anObject, Map userInfo )
    {
        postNotification( new NSNotification( 
            notificationName, anObject, userInfo ) );
    }
	
	/**
	* Unregisters the specified observer from all notification
	* queues for which it is registered. 
	* @param anObserver The observer to be unregistered.
	*/
	public void removeObserver(
		 Object anObserver )
    {
        // remove freed objects
        processKeyQueue();
        
        Iterator it = new LinkedList( observers.keySet() ).iterator();
        while ( it.hasNext() )
        {
            removeObserver( anObserver, it.next() );
        }
    }
	
	/**
	* Unregisters the specified observer from all notifications
	* queues associated with the specified name or object or both.
	* @param anObserver The observer to be unregistered, if null
	* will unregister all observers for the specified notification
	* name and object.
	* @param notificationName The name of the notification for which
	* the observer will be unregistered, if null will unregister
	* the specified observer for all notifications with the 
	* specified object.
	* @param anObject The object for the notification for which
	* the observer will be unregistered, if null will unregister 
	* the specified observer for all objects with the specified
	* notification.
	*/
	public void removeObserver(
		Object anObserver, String notificationName, Object anObject )
    {
        // remove freed objects
        processKeyQueue();
        
        // get key matches
        List keys = matchingKeys( notificationName, anObject );
        
        // remove specified observer from each matching key
        Iterator it = keys.iterator();
        while ( it.hasNext() )
        {
            removeObserver( anObserver, it.next() );               
        }
    }
    
    /**
    * Returns all keys that match the specified name and object,
    * but in this case null parameters are considered wildcards.
    * Pass NullMarkers if you want to explicitly match nulls.
    */
    private List matchingKeys( String name, Object object )
    {
        List result = new LinkedList();

        boolean willAdd;
        CompoundKey key;
        Iterator it = observers.keySet().iterator();
        while ( it.hasNext() )
        {
            key = (CompoundKey) it.next();
            willAdd = false;
            if ( ( name == null ) || ( name == key.name() ) )
            {
                if ( ( object == null ) || ( object == key.get() ) )
                {
                    willAdd = true;   
                }
            }
            if ( willAdd )
            {
                result.add( key );   
            }
        }
        return result;
    }
    
    /**
    * Removes the specified observer from the list referenced
    * by the specified key in the observer map.
    */
    private void removeObserver( 
        Object anObserver, Object key )
    {
        // if observer null, remove all observers for key
        if ( anObserver == null )
        {
            observers.remove( key );
            return;
        }
        
        List list = (List) observers.get( key );
        if ( list == null ) return;

        // remove specified observer from list
        Object observer;
        Iterator it = list.iterator();
        while ( it.hasNext() )
        {
            observer = ((CompoundValue)it.next()).get();
            if ( ( observer == null ) || ( anObserver == observer ) )
            {
                // remove if match or freed object
                it.remove();   

                // do not return; process entire list
            }
        }
        if ( list.size() == 0 )
        {
            observers.remove( key );       
        }
    }

    /* Reference queues for cleared WeakKeys */
    private ReferenceQueue keyQueue = new ReferenceQueue();
       
    /**
    * Removes any keys whose object has been garbage collected.
    * (Garbage collected values are removed as they are encountered.)
    */       
    private void processKeyQueue() 
    {
        CompoundKey ck;
        while ((ck = (CompoundKey)keyQueue.poll()) != null) 
        {
            //System.out.println( "EOObserverCenter.processQueue: removing object" );
            observers.remove(ck);
        }
    }
   
    /**
    * Key combining a name with an object.
    * The object is weakly referenced, and keys
    * are deallocated by reference queue.
    * equals() compares by reference.
    */ 
    private static class CompoundKey extends WeakReference
    {
        private Object name;
        private int hashCode;
        
        /**
        * Creates compound key.  
        * Neither name nor object may be null.
        * Use NullMarker to represent null
        * in either name or object.
        */
        public CompoundKey ( 
            Object aName, Object anObject )
        {
            super( anObject );
            name = aName;
            hashCode = aName.hashCode() + anObject.hashCode();
        }
        
        /**
        * Creates compound key with queue.
        * Neither name nor object may be null.
        * Use NullMarker to represent null
        * in either name or object.
        */
        public CompoundKey ( 
            Object aName, Object anObject, ReferenceQueue aQueue )
        {
            super( anObject, aQueue );
            name = aName;
            hashCode = aName.hashCode() + anObject.hashCode();
        }
        
        public Object name()
        {
            return name;
        }
        
        public int hashCode()
        {
            return hashCode;
        }
        
        public boolean equals( Object anObject )
        {
            if ( this == anObject ) return true;
            // assumes only used with other compound keys
            CompoundKey key = (CompoundKey) anObject;
            if ( name == key.name || ( name != null && name.equals( key.name ) ) )
            {
                Object object = get();
                if ( object != null )
                {
                    // compares by reference
                    if ( object == ( key.get() ) )
                    {
                        return true;   
                    }
                }
            }
            return false;
        }
        
        public String toString()
        {
            return "[CompoundKey:"+name()+":"+get()+"]";   
        }
    }
	
    /**
    * Value combining an object with a selector.
    * The object is weakly referenced, and null
    * values are not allowed.
    */ 
    private static class CompoundValue extends WeakReference
    {
        private NSSelector selector;
        private int hashCode;
        
        public CompoundValue( Object anObject, NSSelector aSelector )
        {
            super( anObject );
            hashCode = anObject.hashCode();
            selector = aSelector;
        }
        
        public NSSelector selector()
        {
            return selector;
        }
        
        public int hashCode()
        {
            return hashCode;
        }
        
        public boolean equals( Object anObject )
        {
            if ( this == anObject ) return true;
            // assumes only used with other compound values
            CompoundValue value = (CompoundValue) anObject;
            if ( selector == value.selector || 
               ( selector != null && selector.equals( value.selector ) ) )
            {
                Object object = get();
                if ( object != null )
                {
                    if ( object == value.get() )
                    {
                        return true;   
                    }
                }
            }
            return false;
        }

        public String toString()
        {
            return "[CompoundValue:"+get()+":"+selector().name()+"]";   
        }
    }
/*
    public static void main( String[] argv )
    {
        Object aSource = "aSource";
        Object bSource = "bSource";
        
        Object oneTest = new OneTest();
        Object twoTest = new TwoTest();
        NSSelector notifyMeOnce = 
            new NSSelector( "notifyMeOnce", 
            new Class[] { NSNotification.class } );
        NSSelector notifyMeTwice = 
            new NSSelector( "notifyMeTwice", 
            new Class[] { NSNotification.class } );
            
        NSNotificationCenter.defaultCenter().addObserver(
            oneTest, notifyMeOnce, "aMessage", null );
            
        NSNotificationCenter.defaultCenter().addObserver(
            oneTest, notifyMeOnce, null, aSource );
            
        NSNotificationCenter.defaultCenter().addObserver(
            twoTest, notifyMeOnce, "aMessage", aSource );
            
        NSNotificationCenter.defaultCenter().addObserver(
            twoTest, notifyMeTwice, null, null );
            
        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", bSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", bSource );
        System.out.println( "---" );
        
        NSNotificationCenter.defaultCenter().removeObserver( 
            oneTest, null, aSource );

        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", bSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", bSource );
        System.out.println( "---" );

        NSNotificationCenter.defaultCenter().removeObserver( 
            null );

        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "aMessage", bSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", aSource );
        System.out.println();
        NSNotificationCenter.defaultCenter().postNotification(
            "bMessage", bSource );
        System.out.println( "---" );
    }
    
    static private class OneTest
    {
        public void notifyMeOnce( NSNotification aNotification )
        {
            System.out.println( "OneTest.notifyMeOnce: " + aNotification );   
        }
    }

    static private class TwoTest
    {
        public void notifyMeOnce( NSNotification aNotification )
        {
            System.out.println( "TwoTest.notifyMeOnce: " + aNotification );   
        }
        public void notifyMeTwice( NSNotification aNotification )
        {
            System.out.println( "TwoTest.notifyMeTwice: " + aNotification );   
        }
    }
*/
}



/*
 * $Log: NSNotificationCenter.java,v $
 * Revision 1.11  2003/06/03 14:51:15  mpowers
 * Added commented-out println for debugging.
 *
 * Revision 1.10  2003/03/27 21:46:00  mpowers
 * Better handling for null parameters on subscribe.
 * Better handling for null parameters on post.
 *
 * Revision 1.9  2003/01/28 19:44:38  mpowers
 * Now comparing strings by value not reference.
 *
 * Revision 1.8  2001/06/29 16:14:23  mpowers
 * Fixed a javac compiler error that jikes allowed: shoe's on the other foot!
 *
 * Revision 1.7  2001/06/07 22:09:03  mpowers
 * Exceptions during a notification are no longer being thrown
 * so we can assure that all notifications get handled.
 * Instead, we're printing stack traces...
 *
 * Revision 1.6  2001/04/09 21:41:50  mpowers
 * Better debugging.
 *
 * Revision 1.5  2001/03/15 21:09:06  mpowers
 * Fixed notifications with null objects.
 *
 * Revision 1.4  2001/02/21 21:18:34  mpowers
 * Clarified need to retain references.
 *
 * Revision 1.3  2001/02/21 18:31:07  mpowers
 * Finished and tested implementation of NSNotificationCenter.
 *
 * Revision 1.1.1.1  2000/12/21 15:47:39  mpowers
 * Contributing wotonomy.
 *
 * Revision 1.3  2000/12/20 16:25:38  michael
 * Added log to all files.
 *
 *
 */



