/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.agg.aggregator;

/**
 * Aggregator for the very last value.
 */
public class AggregatorLastEver implements AggregationMethod
{
    protected final Class type;
    protected Object lastValue;

    /**
     * Ctor.
     * @param type of result
     */
    public AggregatorLastEver(Class type) {
        this.type = type;
    }

    public void clear()
    {
        lastValue = null;
    }

    public void enter(Object object)
    {
        lastValue = object;
    }

    public void leave(Object object)
    {
    }

    public Object getValue()
    {
        return lastValue;
    }

    public Class getValueType()
    {
        return type;
    }
}