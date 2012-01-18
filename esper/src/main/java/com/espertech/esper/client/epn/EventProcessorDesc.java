/*
 * *************************************************************************************
 *  Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 *  http://esper.codehaus.org                                                          *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.client.epn;

import java.io.Serializable;

public class EventProcessorDesc implements Serializable {
    private static final long serialVersionUID = 946489949161515812L;
    private EventProcessorState state;
    private EventProcessor processor;

    public EventProcessorDesc(EventProcessorState state, EventProcessor processor) {
        this.state = state;
        this.processor = processor;
    }

    public EventProcessorState getState() {
        return state;
    }

    public EventProcessor getProcessor() {
        return processor;
    }
}
