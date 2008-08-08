/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.vaevent;

import com.espertech.esper.event.EventBean;
import com.espertech.esper.event.EventPropertyGetter;
import com.espertech.esper.event.EventType;
import com.espertech.esper.event.PropertyAccessException;

/**
 * An event bean that represents multiple potentially disparate underlying events and presents a unified face
 * across each such types or even any type.
 */
public class VariantEventBean implements EventBean, VariantEvent
{
    private final VariantEventType variantEventType;
    private final EventBean underlyingEventBean;

    /**
     * Ctor.
     * @param variantEventType the event type
     * @param underlying the event
     */
    public VariantEventBean(VariantEventType variantEventType, EventBean underlying)
    {
        this.variantEventType = variantEventType;
        this.underlyingEventBean = underlying;
    }

    public EventType getEventType()
    {
        return variantEventType;
    }

    public Object get(String property) throws PropertyAccessException
    {
        EventPropertyGetter getter = variantEventType.getGetter(property);
        if (getter == null)
        {
            return null;
        }
        return getter.get(this);
    }

    public Object getUnderlying()
    {
        return underlyingEventBean.getUnderlying();
    }

    /**
     * Returns the underlying event.
     * @return underlying event
     */
    public EventBean getUnderlyingEventBean()
    {
        return underlyingEventBean;
    }

}
