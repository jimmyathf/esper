/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join;

import com.espertech.esper.collection.MultiKey;
import com.espertech.esper.collection.UniformPair;
import com.espertech.esper.epl.join.table.EventTable;
import com.espertech.esper.event.EventBean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Implements the function to determine a join result for a unidirectional stream-to-window joins,
 * in which a single stream's events are ever only evaluated using a query strategy.
 */
public class JoinSetComposerStreamToWinImpl implements JoinSetComposer
{
    private final EventTable[][] repositories;
    private final int streamNumber;
    private final QueryStrategy queryStrategy;

    private Set<MultiKey<EventBean>> emptyResults = new LinkedHashSet<MultiKey<EventBean>>();
    private Set<MultiKey<EventBean>> newResults = new LinkedHashSet<MultiKey<EventBean>>();

    /**
     * Ctor.
     * @param repositories - for each stream an array of (indexed/unindexed) tables for lookup.
     * @param streamNumber is the undirectional stream
     * @param queryStrategy is the lookup query strategy for the stream
     */
    public JoinSetComposerStreamToWinImpl(EventTable[][] repositories, int streamNumber, QueryStrategy queryStrategy)
    {
        this.repositories = repositories;
        this.streamNumber = streamNumber;
        this.queryStrategy = queryStrategy;
    }

    public void init(EventBean[][] eventsPerStream)
    {
        for (int i = 0; i < eventsPerStream.length; i++)
        {
            if ((eventsPerStream[i] != null) && (i != streamNumber))
            {
                for (int j = 0; j < repositories[i].length; j++)
                {
                    repositories[i][j].add((eventsPerStream[i]));
                }
            }
        }
    }

    public void destroy()
    {
        for (int i = 0; i < repositories.length; i++)
        {
            for (EventTable table : repositories[i])
            {
                table.clear();
            }
        }
    }

    public UniformPair<Set<MultiKey<EventBean>>> join(EventBean[][] newDataPerStream, EventBean[][] oldDataPerStream)
    {
        newResults.clear();

        // add new data to indexes
        for (int i = 0; i < newDataPerStream.length; i++)
        {
            if ((newDataPerStream[i] != null) && (i != streamNumber))
            {
                for (int j = 0; j < repositories[i].length; j++)
                {
                    repositories[i][j].add((newDataPerStream[i]));
                }
            }
        }

        // remove old data from indexes
        // adding first and then removing as the events added may be remove right away
        for (int i = 0; i < oldDataPerStream.length; i++)
        {
            if ((oldDataPerStream[i] != null) && (i != streamNumber))
            {
                for (int j = 0; j < repositories[i].length; j++)
                {
                    repositories[i][j].remove(oldDataPerStream[i]);
                }
            }
        }

        // join new data
        if (newDataPerStream[streamNumber] != null)
        {
            queryStrategy.lookup(newDataPerStream[streamNumber], newResults);
        }

        return new UniformPair<Set<MultiKey<EventBean>>>(newResults, emptyResults);
    }

    public Set<MultiKey<EventBean>> staticJoin()
    {
        throw new UnsupportedOperationException("Iteration over a unidirectional join is not supported");
    }
}
