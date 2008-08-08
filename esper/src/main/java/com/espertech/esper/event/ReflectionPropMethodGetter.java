/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Property getter for methods using Java's vanilla reflection.
 */
public final class ReflectionPropMethodGetter implements EventPropertyGetter
{
    private final Method method;

    /**
     * Constructor.
     * @param method is the regular reflection method to use to obtain values for a field.
     */
    public ReflectionPropMethodGetter(Method method)
    {
        this.method = method;
    }

    public final Object get(EventBean obj) throws PropertyAccessException
    {
        Object underlying = obj.getUnderlying();

        try
        {
            return method.invoke(underlying, (Object[]) null);
        }
        catch (IllegalArgumentException e)
        {
            throw new PropertyAccessException("Mismatched getter instance to event bean type");
        }
        catch (IllegalAccessException e)
        {
            throw new PropertyAccessException(e);
        }
        catch (InvocationTargetException e)
        {
            throw new PropertyAccessException(e);
        }
    }

    public String toString()
    {
        return "ReflectionPropMethodGetter " +
                "method=" + method.toGenericString();
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
