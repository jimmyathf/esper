package com.espertech.esper.collection;

import com.espertech.esper.event.EventBean;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An iterator over an array of iterables.
 */
public final class IterablesArrayIterator implements Iterator<EventBean>
{
    private final Iterable<EventBean>[] array;
    private int index;
    private Iterator<EventBean> currentIterator;

    /**
     * Constructor - takes a list of Iterable that supply the iterators to iterate over.
     * @param iterables is a list of Iterable instances for which iterators to iterator over
     */
    public IterablesArrayIterator(Iterable<EventBean>[] iterables)
    {
        array = iterables;
        nextIterable();
    }

    public EventBean next()
    {
        if (currentIterator == null)
        {
            throw new NoSuchElementException();
        }
        if (currentIterator.hasNext())
        {
            return currentIterator.next();
        }

        nextIterable();

        if (currentIterator == null)
        {
            throw new NoSuchElementException();
        }
        return currentIterator.next();
    }

    public boolean hasNext()
    {
        if (currentIterator == null)
        {
            return false;
        }

        if (currentIterator.hasNext())
        {
            return true;
        }

        nextIterable();

        if (currentIterator == null)
        {
            return false;
        }

        return true;
    }

    public void remove()
    {
        throw new UnsupportedOperationException();
    }

    private void nextIterable()
    {
        while (index < array.length)
        {
            Iterable<EventBean> iterable = array[index];
            currentIterator = iterable.iterator();
            index++;
            if (currentIterator.hasNext())
            {                
                return;
            }
        }

        currentIterator = null;
    }
}
