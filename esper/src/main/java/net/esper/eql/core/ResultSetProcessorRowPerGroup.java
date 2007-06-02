/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package net.esper.eql.core;

import java.util.*;

import net.esper.collection.MultiKey;
import net.esper.collection.Pair;
import net.esper.collection.MultiKeyUntyped;
import net.esper.event.EventBean;
import net.esper.event.EventType;
import net.esper.eql.expression.ExprNode;
import net.esper.eql.agg.AggregationService;
import net.esper.view.Viewable;

/**
 * Result set processor for the fully-grouped case:
 * there is a group-by and all non-aggregation event properties in the select clause are listed in the group by,
 * and there are aggregation functions.
 * <p>
 * Produces one row for each group that changed (and not one row per event). Computes MultiKey group-by keys for
 * each event and uses a set of the group-by keys to generate the result rows, using the first (old or new, anyone) event
 * for each distinct group-by key.
 */
public class ResultSetProcessorRowPerGroup implements ResultSetProcessor
{
    private final SelectExprProcessor selectExprProcessor;
    private final OrderByProcessor orderByProcessor;
    private final AggregationService aggregationService;
    private final List<ExprNode> groupKeyNodes;
    private final ExprNode optionalHavingNode;
    private final boolean isOutputLimiting;
    private final boolean isOutputLimitLastOnly;
    private final boolean isSorting;

    // For output rate limiting, keep a representative event for each group for
    // representing each group in an output limit clause
    private final Map<MultiKeyUntyped, EventBean> newEventGroupReps = new LinkedHashMap<MultiKeyUntyped, EventBean>();
    private final Map<MultiKeyUntyped, EventBean> oldEventGroupReps = new LinkedHashMap<MultiKeyUntyped, EventBean>();

    // For sorting, keep the generating events for each outgoing event
    private final Map<MultiKeyUntyped, EventBean[]> newGenerators = new HashMap<MultiKeyUntyped, EventBean[]>();
    private final Map<MultiKeyUntyped, EventBean[]> oldGenerators = new HashMap<MultiKeyUntyped, EventBean[]>();

    /**
     * Ctor.
     * @param selectExprProcessor - for processing the select expression and generting the final output rows
     * @param orderByProcessor - for sorting outgoing events according to the order-by clause
     * @param aggregationService - handles aggregation
     * @param groupKeyNodes - list of group-by expression nodes needed for building the group-by keys
     * @param optionalHavingNode - expression node representing validated HAVING clause, or null if none given.
     * Aggregation functions in the having node must have been pointed to the AggregationService for evaluation.
     * @param isOutputLimiting - true to indicate we are output limiting and must keep producing
     * a row per group even if groups didn't change
     * @param isOutputLimitLastOnly - true if output limiting and interested in last event only
     */
    public ResultSetProcessorRowPerGroup(SelectExprProcessor selectExprProcessor,
                                         OrderByProcessor orderByProcessor,
                                         AggregationService aggregationService,
                                         List<ExprNode> groupKeyNodes,
                                         ExprNode optionalHavingNode,
                                         boolean isOutputLimiting, boolean isOutputLimitLastOnly)
    {
        this.selectExprProcessor = selectExprProcessor;
        this.orderByProcessor = orderByProcessor;
        this.aggregationService = aggregationService;
        this.groupKeyNodes = groupKeyNodes;
        this.optionalHavingNode = optionalHavingNode;
        this.isOutputLimiting = isOutputLimiting;
        this.isOutputLimitLastOnly = isOutputLimitLastOnly;
        this.isSorting = orderByProcessor != null;
    }

    public EventType getResultEventType()
    {
        return selectExprProcessor.getResultEventType();
    }

    public Pair<EventBean[], EventBean[]> processJoinResult(Set<MultiKey<EventBean>> newEvents, Set<MultiKey<EventBean>> oldEvents)
    {
        // Generate group-by keys for all events, collect all keys in a set for later event generation
        Map<MultiKeyUntyped, EventBean[]> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean[]>();
        MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newEvents, keysAndEvents, true);
        MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldEvents, keysAndEvents, false);

        // generate old events
        EventBean[] selectOldEvents = generateOutputEventsJoin(keysAndEvents, optionalHavingNode, oldEventGroupReps, oldGenerators, false);

        // update aggregates
        if (!newEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : newEvents)
            {
                aggregationService.applyEnter(eventsPerStream.getArray(), newDataMultiKey[count]);
                count++;
            }
        }
        if (!oldEvents.isEmpty())
        {
            // apply old data to aggregates
            int count = 0;
            for (MultiKey<EventBean> eventsPerStream : oldEvents)
            {
                aggregationService.applyLeave(eventsPerStream.getArray(), oldDataMultiKey[count]);
                count++;
            }
        }

        // generate new events using select expressions
        EventBean[] selectNewEvents = generateOutputEventsJoin(keysAndEvents, optionalHavingNode, newEventGroupReps, newGenerators, true);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new Pair<EventBean[], EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

    public Pair<EventBean[], EventBean[]> processViewResult(EventBean[] newData, EventBean[] oldData)
    {
        // Generate group-by keys for all events, collect all keys in a set for later event generation
        Map<MultiKeyUntyped, EventBean> keysAndEvents = new HashMap<MultiKeyUntyped, EventBean>();
        MultiKeyUntyped[] newDataMultiKey = generateGroupKeys(newData, keysAndEvents, true);
        MultiKeyUntyped[] oldDataMultiKey = generateGroupKeys(oldData, keysAndEvents, false);

        // generate old events
        EventBean[] selectOldEvents = generateOutputEventsView(keysAndEvents, optionalHavingNode, oldEventGroupReps, oldGenerators, false);

        // update aggregates
        EventBean[] eventsPerStream = new EventBean[1];
        if (newData != null)
        {
            // apply new data to aggregates
            for (int i = 0; i < newData.length; i++)
            {
                eventsPerStream[0] = newData[i];
                aggregationService.applyEnter(eventsPerStream, newDataMultiKey[i]);
            }
        }
        if (oldData != null)
        {
            // apply old data to aggregates
            for (int i = 0; i < oldData.length; i++)
            {
                eventsPerStream[0] = oldData[i];
                aggregationService.applyLeave(eventsPerStream, oldDataMultiKey[i]);
            }
        }

        // generate new events using select expressions
        EventBean[] selectNewEvents = generateOutputEventsView(keysAndEvents, optionalHavingNode, newEventGroupReps, newGenerators, true);

        if ((selectNewEvents != null) || (selectOldEvents != null))
        {
            return new Pair<EventBean[], EventBean[]>(selectNewEvents, selectOldEvents);
        }
        return null;
    }

    private EventBean[] generateOutputEventsView(Map<MultiKeyUntyped, EventBean> keysAndEvents, ExprNode optionalHavingExpr, Map<MultiKeyUntyped, EventBean> groupReps, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData)
    {
        EventBean[] eventsPerStream = new EventBean[1];
        EventBean[] events = new EventBean[keysAndEvents.size()];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[keysAndEvents.size()];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
            currentGenerators = new EventBean[keysAndEvents.size()][];
        }

        int count = 0;
        for (Map.Entry<MultiKeyUntyped, EventBean> entry : keysAndEvents.entrySet())
        {
            // Set the current row of aggregation states
            aggregationService.setCurrentRow(entry.getKey());

            eventsPerStream[0] = entry.getValue();

            // Filter the having clause
            if (optionalHavingExpr != null)
            {
                Boolean result = (Boolean) optionalHavingExpr.evaluate(eventsPerStream, isNewData);
                if (!result)
                {
                    continue;
                }
            }

            events[count] = selectExprProcessor.process(eventsPerStream, isNewData);
            keys[count] = entry.getKey();
            if(isSorting)
            {
                EventBean[] currentEventsPerStream = new EventBean[] { entry.getValue() };
                generators.put(keys[count], currentEventsPerStream);
                currentGenerators[count] = currentEventsPerStream;
            }

            count++;
        }

        // Resize if some rows were filtered out
        if (count != events.length)
        {
            if (count == 0)
            {
                return null;
            }
            EventBean[] out = new EventBean[count];
            System.arraycopy(events, 0, out, 0, count);
            events = out;

            if(isSorting || (isOutputLimiting && !isOutputLimitLastOnly))
            {
                MultiKeyUntyped[] outKeys = new MultiKeyUntyped[count];
                System.arraycopy(keys, 0, outKeys, 0, count);
                keys = outKeys;
            }

            if(isSorting)
            {
                EventBean[][] outGens = new EventBean[count][];
                System.arraycopy(currentGenerators, 0, outGens, 0, count);
                currentGenerators = outGens;
            }
        }

        return applyOutputLimitAndOrderBy(events, currentGenerators, keys, groupReps, generators, isNewData);
    }

    private EventBean[] generateOutputEventsJoin(Map<MultiKeyUntyped, EventBean[]> keysAndEvents, ExprNode optionalHavingExpr, Map<MultiKeyUntyped, EventBean> groupReps, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData)
    {
        EventBean[] events = new EventBean[keysAndEvents.size()];
        MultiKeyUntyped[] keys = new MultiKeyUntyped[keysAndEvents.size()];
        EventBean[][] currentGenerators = null;
        if(isSorting)
        {
            currentGenerators = new EventBean[keysAndEvents.size()][];
        }

        int count = 0;
        for (Map.Entry<MultiKeyUntyped, EventBean[]> entry : keysAndEvents.entrySet())
        {
            aggregationService.setCurrentRow(entry.getKey());
            EventBean[] eventsPerStream = entry.getValue();

            // Filter the having clause
            if (optionalHavingExpr != null)
            {
                Boolean result = (Boolean) optionalHavingExpr.evaluate(eventsPerStream, isNewData);
                if (!result)
                {
                    continue;
                }
            }

            events[count] = selectExprProcessor.process(eventsPerStream, isNewData);
            keys[count] = entry.getKey();
            if(isSorting)
            {
                generators.put(keys[count], eventsPerStream);
                currentGenerators[count] = eventsPerStream;
            }

            count++;
        }

        // Resize if some rows were filtered out
        if (count != events.length)
        {
            if (count == 0)
            {
                return null;
            }
            EventBean[] out = new EventBean[count];
            System.arraycopy(events, 0, out, 0, count);
            events = out;

            if(isSorting || (isOutputLimiting && !isOutputLimitLastOnly))
            {
                MultiKeyUntyped[] outKeys = new MultiKeyUntyped[count];
                System.arraycopy(keys, 0, outKeys, 0, count);
                keys = outKeys;
            }

            if(isSorting)
            {
                EventBean[][] outGens = new EventBean[count][];
                System.arraycopy(currentGenerators, 0, outGens, 0, count);
                currentGenerators = outGens;
            }
        }

        return applyOutputLimitAndOrderBy(events, currentGenerators, keys, groupReps, generators, isNewData);
    }

    private EventBean[] applyOutputLimitAndOrderBy(EventBean[] events, EventBean[][] currentGenerators, MultiKeyUntyped[] keys, Map<MultiKeyUntyped, EventBean> groupReps, Map<MultiKeyUntyped, EventBean[]> generators, boolean isNewData)
    {
        if(isOutputLimiting && !isOutputLimitLastOnly)
        {
            // Update the group representatives
            int count = 0;
            for(MultiKeyUntyped key : keys)
            {
                groupReps.put(key, events[count++]);
            }

            // Update the outgoing events
            events = groupReps.values().toArray(new EventBean[0]);

            // Update the generating events and group-by keys if needed
            if(isSorting)
            {
                currentGenerators = generators.values().toArray(new EventBean[0][]);
                keys = groupReps.keySet().toArray(new MultiKeyUntyped[0]);
            }
        }

        if(isSorting)
        {
            events =  orderByProcessor.sort(events, currentGenerators, keys, isNewData);
        }

        return events;
    }


    private MultiKeyUntyped[] generateGroupKeys(EventBean[] events, Map<MultiKeyUntyped, EventBean> eventPerKey, boolean isNewData)
    {
        if (events == null)
        {
            return null;
        }

        EventBean[] eventsPerStream = new EventBean[1];
        MultiKeyUntyped keys[] = new MultiKeyUntyped[events.length];

        for (int i = 0; i < events.length; i++)
        {
            eventsPerStream[0] = events[i];
            keys[i] = generateGroupKey(eventsPerStream, isNewData);

            if (!eventPerKey.containsKey(keys[i]))
            {
                eventPerKey.put(keys[i], events[i]);
            }
        }

        return keys;
    }

    private MultiKeyUntyped[] generateGroupKeys(Set<MultiKey<EventBean>> resultSet, Map<MultiKeyUntyped, EventBean[]> eventPerKey, boolean isNewData)
    {
        if (resultSet.isEmpty())
        {
            return null;
        }

        MultiKeyUntyped keys[] = new MultiKeyUntyped[resultSet.size()];

        int count = 0;
        for (MultiKey<EventBean> eventsPerStream : resultSet)
        {
            keys[count] = generateGroupKey(eventsPerStream.getArray(), isNewData);

            if (!eventPerKey.containsKey(keys[count]))
            {
                eventPerKey.put(keys[count], eventsPerStream.getArray());
            }

            count++;
        }

        return keys;
    }

    private MultiKeyUntyped generateGroupKey(EventBean[] eventsPerStream, boolean isNewData)
    {
        Object[] keys = new Object[groupKeyNodes.size()];

        int count = 0;
        for (ExprNode exprNode : groupKeyNodes)
        {
            keys[count] = exprNode.evaluate(eventsPerStream, isNewData);
            count++;
        }

        return new MultiKeyUntyped(keys);
    }

    public Iterator<EventBean> getIterator(Viewable parent)
    {
        return new ResultSetRowPerGroupIterator(parent.iterator(), this, aggregationService);
    }

    /**
     * Method to transform an event based on the select expression.
     */
    public static class ResultSetRowPerGroupIterator implements Iterator<EventBean>
    {
        private final Iterator<EventBean> sourceIterator;
        private final ResultSetProcessorRowPerGroup resultSetProcessor;
        private final AggregationService aggregationService;
        private EventBean nextResult;
        private final EventBean[] eventsPerStream;
        private final Set<MultiKeyUntyped> priorSeenGroups;

        public ResultSetRowPerGroupIterator(Iterator<EventBean> sourceIterator, ResultSetProcessorRowPerGroup resultSetProcessor, AggregationService aggregationService)
        {
            this.sourceIterator = sourceIterator;
            this.resultSetProcessor = resultSetProcessor;
            this.aggregationService = aggregationService;
            eventsPerStream = new EventBean[1];
            priorSeenGroups = new HashSet<MultiKeyUntyped>();
        }

        public boolean hasNext()
        {
            if (nextResult != null)
            {
                return true;
            }
            findNext();
            if (nextResult != null)
            {
                return true;
            }
            return false;
        }

        public EventBean next()
        {
            if (nextResult != null)
            {
                EventBean result = nextResult;
                nextResult = null;
                return result;
            }
            findNext();
            if (nextResult != null)
            {
                EventBean result = nextResult;
                nextResult = null;
                return result;
            }
            throw new NoSuchElementException();
        }

        private void findNext()
        {
            while(sourceIterator.hasNext())
            {
                EventBean candidate = sourceIterator.next();
                eventsPerStream[0] = candidate;

                MultiKeyUntyped groupKey = resultSetProcessor.generateGroupKey(eventsPerStream, true);
                aggregationService.setCurrentRow(groupKey);

                Boolean pass = true;
                if (resultSetProcessor.optionalHavingNode != null)
                {
                    pass = (Boolean) resultSetProcessor.optionalHavingNode.evaluate(eventsPerStream, true);
                }
                if (!pass)
                {
                    continue;
                }
                if (priorSeenGroups.contains(groupKey))
                {
                    continue;
                }
                priorSeenGroups.add(groupKey);

                if(resultSetProcessor.selectExprProcessor == null)
                {
                    nextResult = candidate;
                }
                else
                {
                    nextResult = resultSetProcessor.selectExprProcessor.process(eventsPerStream, true);
                }

                break;
            }
        }

        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }
}
