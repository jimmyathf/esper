/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.join.assemble;

import com.espertech.esper.epl.join.rep.Node;
import com.espertech.esper.event.EventBean;
import com.espertech.esper.util.IndentWriter;

import java.util.List;

/**
 * Assembly node for an event stream that is a root with a one optional child node below it.
 */
public class RootOptionalAssemblyNode extends BaseAssemblyNode
{
    private final int numStreams;
    private boolean haveChildResults;

    /**
     * Ctor.
     * @param streamNum - is the stream number
     * @param numStreams - is the number of streams
     */
    public RootOptionalAssemblyNode(int streamNum, int numStreams)
    {
        super(streamNum, numStreams);
        this.numStreams = numStreams;
    }

    public void init(List<Node>[] result)
    {
        haveChildResults = false;
    }

    public void process(List<Node>[] result)
    {
        // If we don't have child results, post an empty row
        if (!haveChildResults)
        {
            EventBean[] row = new EventBean[numStreams];
            parentNode.result(row, streamNum, null, null);
        }
    }

    public void result(EventBean[] row, int fromStreamNum, EventBean myEvent, Node myNode)
    {
        parentNode.result(row, streamNum, null, null);

        // record the fact that a row that was generated by a child
        haveChildResults = true;
    }

    public void print(IndentWriter indentWriter)
    {
        indentWriter.println("RootOptionalAssemblyNode streamNum=" + streamNum);
    }
}
