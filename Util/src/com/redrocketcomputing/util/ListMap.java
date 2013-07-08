package com.redrocketcomputing.util;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * @author stephen
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class ListMap implements Map, Cloneable
{
  static class SimpleEntry implements Entry
  {
    Object key;
    Object value;

    public SimpleEntry(Object key, Object value)
    {
      this.key = key;
      this.value = value;
    }

    public SimpleEntry(Map.Entry e)
    {
      this.key = e.getKey();
      this.value = e.getValue();
    }

    public Object getKey()
    {
      return key;
    }

    public Object getValue()
    {
      return value;
    }

    public Object setValue(Object value)
    {
      Object oldValue = this.value;
      this.value = value;
      return oldValue;
    }

    public boolean equals(Object o)
    {
      if (!(o instanceof Map.Entry))
        return false;
      Map.Entry e = (Map.Entry)o;
      return eq(key, e.getKey()) && eq(value, e.getValue());
    }

    public int hashCode()
    {
      Object v;
      return ((key == null) ? 0 : key.hashCode()) ^ ((value == null) ? 0 : value.hashCode());
    }

    public String toString()
    {
      return key + "=" + value;
    }

    private static boolean eq(Object o1, Object o2)
    {
      return (o1 == null ? o2 == null : o1.equals(o2));
    }
  }

  private transient Set entrySet;

  /**
   *  
   */
  public ListMap()
  {
    // Create the entry set
    entrySet = new ListSet();
  }

  public ListMap(int initialCapacity)
  {
    // Create underlaying list using an ArrayList
    entrySet = new ListSet(initialCapacity);
  }

  public ListMap(Class listClass)
  {
    //create a new instance which has the same type as listClass
    entrySet = new ListSet(listClass);
  }

  public ListMap(Map map)
  {
    // Create new entry set
    this(map.size());

    // Loop through the map build an new map
    for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
    {
      // Extract element
      Map.Entry element = (Map.Entry)iterator.next();

      // Add to new entry set
      entrySet.add(new SimpleEntry(element.getKey(), element.getValue()));
    }
  }

  public ListMap(Map map, Class listClass)
  {
    // Forward to the collection constructor
    this(listClass);

    // Loop through the map build an new map
    for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)
    {
      // Extract element
      Map.Entry element = (Map.Entry)iterator.next();

      // Add to new entry set
      entrySet.add(new SimpleEntry(element.getKey(), element.getValue()));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#entrySet()
   */
  public Set entrySet()
  {
    return entrySet;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Map#put(java.lang.Object, java.lang.Object)
   */
  public Object put(Object key, Object value)
  {
    // Loop through the entry set looking for match keys
    Map.Entry previous = null;
    for (Iterator iterator = entrySet.iterator(); iterator.hasNext();)
    {
      // Extract the entry
      Map.Entry element = (Map.Entry)iterator.next();

      // Check for key match
      if (eq(key, element.getKey()))
      {
        // Save the matching entry
        previous = element;
      }
    }

    // Check for previous mapping
    if (previous == null)
    {
      // Add new entry
      entrySet.add(new SimpleEntry(key, value));
    }
    else
    {
      // Update existing entry
      previous.setValue(value);
    }

    // Return the previous entry
    return previous;
  }

  private final boolean eq(Object x, Object y)
  {
    return (x == null ? y == null : x.equals(y));
  }

  public int size()
  {
    return entrySet().size();
  }

  public boolean isEmpty()
  {
    return size() == 0;
  }

  public boolean containsValue(Object value)
  {
    Iterator i = entrySet().iterator();
    if (value == null)
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (e.getValue() == null)
          return true;
      }
    }
    else
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (value.equals(e.getValue()))
          return true;
      }
    }
    return false;
  }

  public boolean containsKey(Object key)
  {
    Iterator i = entrySet().iterator();
    if (key == null)
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (e.getKey() == null)
          return true;
      }
    }
    else
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (key.equals(e.getKey()))
          return true;
      }
    }
    return false;
  }

  public Object get(Object key)
  {
    Iterator i = entrySet().iterator();
    if (key == null)
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (e.getKey() == null)
          return e.getValue();
      }
    }
    else
    {
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (key.equals(e.getKey()))
          return e.getValue();
      }
    }
    return null;
  }

  public Object remove(Object key)
  {
    Iterator i = entrySet().iterator();
    Entry correctEntry = null;
    if (key == null)
    {
      while (correctEntry == null && i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (e.getKey() == null)
          correctEntry = e;
      }
    }
    else
    {
      while (correctEntry == null && i.hasNext())
      {
        Entry e = (Entry)i.next();
        if (key.equals(e.getKey()))
          correctEntry = e;
      }
    }

    Object oldValue = null;
    if (correctEntry != null)
    {
      oldValue = correctEntry.getValue();
      i.remove();
    }
    return oldValue;
  }

  public void putAll(Map t)
  {
    Iterator i = t.entrySet().iterator();
    while (i.hasNext())
    {
      Entry e = (Entry)i.next();
      put(e.getKey(), e.getValue());
    }
  }

  public void clear()
  {
    entrySet().clear();
  }

  transient volatile Set keySet = null;
  transient volatile Collection values = null;

  public Set keySet()
  {
    if (keySet == null)
    {
      keySet = new AbstractSet()
      {
        public Iterator iterator()
        {
          return new Iterator()
          {
            private Iterator i = entrySet().iterator();

            public boolean hasNext()
            {
              return i.hasNext();
            }

            public Object next()
            {
              return ((Entry)i.next()).getKey();
            }

            public void remove()
            {
              i.remove();
            }
          };
        }

        public int size()
        {
          return ListMap.this.size();
        }

        public boolean contains(Object k)
        {
          return ListMap.this.containsKey(k);
        }
      };
    }
    return keySet;
  }

  public Collection values()
  {
    if (values == null)
    {
      values = new AbstractCollection()
      {
        public Iterator iterator()
        {
          return new Iterator()
          {
            private Iterator i = entrySet().iterator();

            public boolean hasNext()
            {
              return i.hasNext();
            }

            public Object next()
            {
              return ((Entry)i.next()).getValue();
            }

            public void remove()
            {
              i.remove();
            }
          };
        }

        public int size()
        {
          return ListMap.this.size();
        }

        public boolean contains(Object v)
        {
          return ListMap.this.containsValue(v);
        }
      };
    }
    return values;
  }

  public boolean equals(Object o)
  {
    if (o == this)
      return true;

    if (!(o instanceof Map))
      return false;
    Map t = (Map)o;
    if (t.size() != size())
      return false;

    try
    {
      Iterator i = entrySet().iterator();
      while (i.hasNext())
      {
        Entry e = (Entry)i.next();
        Object key = e.getKey();
        Object value = e.getValue();
        if (value == null)
        {
          if (!(t.get(key) == null && t.containsKey(key)))
            return false;
        }
        else
        {
          if (!value.equals(t.get(key)))
            return false;
        }
      }
    }
    catch (ClassCastException unused)
    {
      return false;
    }
    catch (NullPointerException unused)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int h = 0;
    Iterator i = entrySet().iterator();
    while (i.hasNext())
      h += i.next().hashCode();
    return h;
  }

  public String toString()
  {
    StringBuffer buf = new StringBuffer();
    buf.append("{");

    Iterator i = entrySet().iterator();
    boolean hasNext = i.hasNext();
    while (hasNext)
    {
      Entry e = (Entry)(i.next());
      Object key = e.getKey();
      Object value = e.getValue();
      buf.append((key == this ? "(this Map)" : key) + "=" + (value == this ? "(this Map)" : value));

      hasNext = i.hasNext();
      if (hasNext)
        buf.append(", ");
    }

    buf.append("}");
    return buf.toString();
  }

 protected Object clone() throws CloneNotSupportedException
  {
    ListMap result = (ListMap)super.clone();
    result.keySet = null;
    result.values = null;
    return result;
  }
}