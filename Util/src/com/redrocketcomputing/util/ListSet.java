package com.redrocketcomputing.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ListSet implements Set, Cloneable
{
  private transient List list;

  public ListSet()
  {
    // Create underlaying list using an ArrayList
    list = new ArrayList();
  }

  public ListSet(int initialCapacity)
  {
    // Create underlaying list using an ArrayList
    list = new ArrayList(initialCapacity);
  }

  public ListSet(Class listClass)
  {
    try
    {
      //create a new instance which has the same type as listClass
      list = (List)listClass.newInstance();
    }
    catch (IllegalAccessException e)
    {
      throw new IllegalArgumentException(e.toString());
    }
    catch (InstantiationException e)
    {
      throw new IllegalArgumentException(e.toString());
    }
  }

  public ListSet(Set set)
  {
    list = new ArrayList((Collection)set);
  }

  public ListSet(Collection collection)
  {
    // Use default list type
    this(collection.size());

    // Add all enties in the collection
    for (Iterator iter = collection.iterator(); iter.hasNext();)
    {
      add(iter.next());
    }
  }

  public ListSet(Collection collection, Class listClass)
  {
    // Use the specified list class type
    this(listClass);

    // Add all entries in the collection
    for (Iterator iter = collection.iterator(); iter.hasNext();)
    {
      add(iter.next());
    }
  }

  public ListSet(Set set, Class listClass)
  {
    // Forward to the collection constructor
    this((Collection)set, listClass);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#iterator()
   */
  public Iterator iterator()
  {
    // Return the lists iterator
    return list.iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#size()
   */
  public int size()
  {
    // Return the lists size
    return list.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(Object o)
  {
    // Loop through exist elements
    for (Iterator iterator = list.iterator(); iterator.hasNext();)
    {
      // Extract the element
      Object element = iterator.next();

      // Check for equals
      if (eq(o, element))
      {
        // Duplicate
        return false;
      }
    }

    // Add the element
    list.add(o);

    // We added
    return true;
  }

  private final boolean eq(Object x, Object y)
  {
    return (x == null ? y == null : x.equals(y));
  }

  public boolean equals(Object o)
  {
    if (o == this)
      return true;

    if (!(o instanceof Set))
      return false;
    Collection c = (Collection)o;
    if (c.size() != size())
      return false;
    try
    {
      return containsAll(c);
    }
    catch (ClassCastException unused)
    {
      return false;
    }
    catch (NullPointerException unused)
    {
      return false;
    }
  }

  public int hashCode()
  {
    int h = 0;
    Iterator i = iterator();
    while (i.hasNext())
    {
      Object obj = i.next();
      if (obj != null)
        h += obj.hashCode();
    }
    return h;
  }

  public boolean removeAll(Collection c)
  {
    boolean modified = false;

    if (size() > c.size())
    {
      for (Iterator i = c.iterator(); i.hasNext();)
        modified |= remove(i.next());
    }
    else
    {
      for (Iterator i = iterator(); i.hasNext();)
      {
        if (c.contains(i.next()))
        {
          i.remove();
          modified = true;
        }
      }
    }
    return modified;
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public boolean contains(Object o)
  {
    Iterator e = iterator();
    if (o == null)
    {
      while (e.hasNext())
        if (e.next() == null)
          return true;
    }
    else
    {
      while (e.hasNext())
        if (o.equals(e.next()))
          return true;
    }
    return false;
  }

  public Object[] toArray()
  {
    Object[] result = new Object[size()];
    Iterator e = iterator();
    for (int i = 0; e.hasNext(); i++)
      result[i] = e.next();
    return result;
  }

  public Object[] toArray(Object a[])
  {
    int size = size();
    if (a.length < size)
      a = (Object[])java.lang.reflect.Array.newInstance(a.getClass().getComponentType(), size);

    Iterator it = iterator();
    for (int i = 0; i < size; i++)
      a[i] = it.next();

    if (a.length > size)
      a[size] = null;

    return a;
  }

  public boolean remove(Object o)
  {
    Iterator e = iterator();
    if (o == null)
    {
      while (e.hasNext())
      {
        if (e.next() == null)
        {
          e.remove();
          return true;
        }
      }
    }
    else
    {
      while (e.hasNext())
      {
        if (o.equals(e.next()))
        {
          e.remove();
          return true;
        }
      }
    }
    return false;
  }

  public boolean containsAll(Collection c)
  {
    Iterator e = c.iterator();
    while (e.hasNext())
      if (!contains(e.next()))
        return false;

    return true;
  }

  public boolean addAll(Collection c)
  {
    boolean modified = false;
    Iterator e = c.iterator();
    while (e.hasNext())
    {
      if (add(e.next()))
        modified = true;
    }
    return modified;
  }

  public boolean retainAll(Collection c)
  {
    boolean modified = false;
    Iterator e = iterator();
    while (e.hasNext())
    {
      if (!c.contains(e.next()))
      {
        e.remove();
        modified = true;
      }
    }
    return modified;
  }

  public void clear()
  {
    Iterator e = iterator();
    while (e.hasNext())
    {
      e.next();
      e.remove();
    }
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("[");

    Iterator i = iterator();
    boolean hasNext = i.hasNext();
    while (hasNext)
    {
      Object o = i.next();
      buf.append(o == this ? "(this Collection)" : String.valueOf(o));
      hasNext = i.hasNext();
      if (hasNext)
        buf.append(", ");
    }

    buf.append("]");
    return buf.toString();
  }
}