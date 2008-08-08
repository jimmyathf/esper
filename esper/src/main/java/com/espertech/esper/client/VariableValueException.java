/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.client;

/**
 * Indicates that a variable value could not be assigned.
 */
public class VariableValueException extends EPException
{
    /**
     * Ctor.
     * @param message supplies exception details
     */
    public VariableValueException(final String message)
    {
        super(message);
    }
}
