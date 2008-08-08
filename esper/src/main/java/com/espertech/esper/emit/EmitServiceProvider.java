/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.emit;

/**
 * Static factory for implementations of the EmitService interface.
 */
public final class EmitServiceProvider
{
    /**
     * Creates an implementation of the EmitService interface.
     * @return implementation
     */
    public static EmitService newService()
    {
        return new EmitServiceImpl();
    }
}
