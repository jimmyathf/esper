/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.collection.Pair;

import java.util.Map;

/**
 * Interface for event types that provide decorating event properties as a name-value map.
 */
public interface DecoratingEventBean
{
    /**
     * Returns decorating properties.
     * @return property name and values
     */
    public Map<String, Object> getDecoratingProperties();
}
