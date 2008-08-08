/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event;

import com.espertech.esper.client.ConfigurationEventTypeLegacy;
import com.espertech.esper.client.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Interface for a factory for obtaining {@link BeanEventType} instances.
 */
public interface BeanEventTypeFactory
{
    /**
     * Returns the bean event type for a given class assigning the given alias.
     * @param alias is the alias
     * @param clazz is the class for which to generate an event type
     * @return is the event type for the class
     */
    public BeanEventType createBeanType(String alias, Class clazz);

    /**
     * Returns the default property resolution style.
     * @return property resolution style
     */
    public Configuration.PropertyResolutionStyle getDefaultPropertyResolutionStyle();
}
