/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.filter.FilterSpecCompiled;
import com.espertech.esper.epl.spec.ViewSpec;
import com.espertech.esper.epl.spec.StreamSpecBase;

import java.util.List;

/**
 * Specification for building an event stream out of a filter for events (supplying type and basic filter criteria)
 * and views onto these events which are staggered onto each other to supply a final stream of events.
 */
public class FilterStreamSpecCompiled extends StreamSpecBase implements StreamSpecCompiled
{
    private static final long serialVersionUID = 0L;
    private transient FilterSpecCompiled filterSpec;

    /**
     * Ctor.
     * @param filterSpec - specifies what events we are interested in.
     * @param viewSpecs - specifies what view to use to derive data
     * @param optionalStreamName - stream name, or null if none supplied
     * @param isUnidirectional - true to indicate a unidirectional stream in a join, applicable for joins
     */
    public FilterStreamSpecCompiled(FilterSpecCompiled filterSpec, List<ViewSpec> viewSpecs, String optionalStreamName, boolean isUnidirectional)
    {
        super(optionalStreamName, viewSpecs, isUnidirectional);
        this.filterSpec = filterSpec;
    }

    /**
     * Returns filter specification for which events the stream will getSelectListEvents.
     * @return filter spec
     */
    public FilterSpecCompiled getFilterSpec()
    {
        return filterSpec;
    }

    /**
     * Sets a filter specification.
     * @param filterSpec to set
     */
    public void setFilterSpec(FilterSpecCompiled filterSpec)
    {
        this.filterSpec = filterSpec;
    }
}
