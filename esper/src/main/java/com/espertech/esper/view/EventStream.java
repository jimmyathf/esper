/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view;

import com.espertech.esper.event.EventBean;

/**
 * A streams is a conduct for incoming events. Incoming data is placed into streams for consumption by queries.
 */
public interface EventStream extends Viewable
{
    /**
     * Insert a new event onto the stream.
     * @param event to insert
     */
    public void insert(EventBean event);
}
