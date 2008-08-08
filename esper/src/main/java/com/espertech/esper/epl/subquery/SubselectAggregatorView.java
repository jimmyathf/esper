/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.subquery;

import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.ExprNode;
import com.espertech.esper.event.EventBean;
import com.espertech.esper.event.EventType;
import com.espertech.esper.view.ViewSupport;

import java.util.Iterator;

/**
 * View handling the insert and remove stream generated by a subselect
 * for application to aggregation state.
 */
public class SubselectAggregatorView extends ViewSupport
{
    private final AggregationService aggregationService;
    private final ExprNode optionalFilterExpr;

    /**
     * Ctor.
     * @param aggregationService for aggregating
     * @param optionalFilterExpr for filtering the view-posted events before aggregation
     */
    public SubselectAggregatorView(AggregationService aggregationService, ExprNode optionalFilterExpr) {
        this.aggregationService = aggregationService;
        this.optionalFilterExpr = optionalFilterExpr;
    }

    public void update(EventBean[] newData, EventBean[] oldData) {
        EventBean[] eventsPerStream = new EventBean[1];

        if (newData != null)
        {
            for (EventBean event : newData)
            {
                eventsPerStream[0] = event;

                boolean isPass = filter(eventsPerStream, true);
                if (isPass)
                {
                    aggregationService.applyEnter(eventsPerStream, null);
                }
            }
        }

        if (oldData != null)
        {
            for (EventBean event : oldData)
            {
                eventsPerStream[0] = event;
                boolean isPass = filter(eventsPerStream, false);
                if (isPass)
                {
                    aggregationService.applyLeave(eventsPerStream, null);
                }
            }
        }
    }

    public EventType getEventType() {
        return this.getParent().getEventType();
    }

    public Iterator<EventBean> iterator() {
        return this.getParent().iterator();
    }

    private boolean filter(EventBean[] eventsPerStream, boolean isNewData)
    {
        if (optionalFilterExpr == null)
        {
            return true;
        }

        Boolean result = (Boolean) optionalFilterExpr.evaluate(eventsPerStream, isNewData);
        if (result == null)
        {
            return false;
        }
        return result;
    }
}
