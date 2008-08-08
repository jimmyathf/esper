/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.core;

/**
 * Base class for stream and property name resolution errors.
 */
public abstract class StreamTypesException extends Exception
{
    /**
     * Ctor.
     * @param msg - message
     */
    public StreamTypesException(String msg)
    {
        super(msg);
    }
}
