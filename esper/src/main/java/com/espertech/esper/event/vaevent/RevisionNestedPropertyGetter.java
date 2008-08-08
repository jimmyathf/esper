/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.vaevent;

import com.espertech.esper.event.EventPropertyGetter;
import com.espertech.esper.event.EventAdapterService;
import com.espertech.esper.event.EventBean;

/**
 * A getter that works on POJO events residing within a Map as an event property.
 */
public class RevisionNestedPropertyGetter implements EventPropertyGetter
{
    private final EventPropertyGetter revisionGetter;
    private final EventPropertyGetter nestedGetter;
    private final EventAdapterService eventAdapterService;

    /**
     * Ctor.
     * @param revisionGetter getter for revision value
     * @param nestedGetter getter to apply to revision value
     * @param eventAdapterService for handling object types
     */
    public RevisionNestedPropertyGetter(EventPropertyGetter revisionGetter, EventPropertyGetter nestedGetter, EventAdapterService eventAdapterService) {
        this.revisionGetter = revisionGetter;
        this.eventAdapterService = eventAdapterService;
        this.nestedGetter = nestedGetter;
    }

    public Object get(EventBean obj)
    {
        Object result = revisionGetter.get(obj);
        if (result == null)
        {
            return result;
        }

        // Object within the map
        EventBean event = eventAdapterService.adapterForBean(result);
        return nestedGetter.get(event);
    }

    public boolean isExistsProperty(EventBean eventBean)
    {
        return true; // Property exists as the property is not dynamic (unchecked)
    }
}
