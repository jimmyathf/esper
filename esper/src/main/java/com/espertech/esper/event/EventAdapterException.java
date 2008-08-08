/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.EPException;

/**
 * This exception is thrown to indicate a problem resolving an event type by name.
 */
public class EventAdapterException extends EPException
{
    /**
     * Ctor.
     * @param message - error message
     */
    public EventAdapterException(final String message)
    {
        super(message);
    }

    /**
     * Ctor.
     * @param message - error message
     * @param nested - nested exception
     */
    public EventAdapterException(final String message, Throwable nested)
    {
        super(message, nested);
    }
}
