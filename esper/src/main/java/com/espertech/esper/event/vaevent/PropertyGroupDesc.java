/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.event.vaevent;

import com.espertech.esper.event.EventType;

import java.util.Arrays;
import java.util.Map;

/**
 * For use with building groups of event properties to reduce overhead in maintaining versions.
 */
public class PropertyGroupDesc {

    private final int groupNum;
    private final Map<EventType, String> types;
    private final String[] properties;

    /**
     * Ctor.
     * @param groupNum the group number
     * @param aliasTypeSet the event types and their aliases whose totality of properties fully falls within this group.
     * @param properties is the properties in the group
     */
    public PropertyGroupDesc(int groupNum, Map<EventType, String> aliasTypeSet, String[] properties) {
        this.groupNum = groupNum;
        this.types = aliasTypeSet;
        this.properties = properties;
    }

    /**
     * Returns the group number.
     * @return group number
     */
    public int getGroupNum() {
        return groupNum;
    }

    /**
     * Returns the types.
     * @return types
     */
    public Map<EventType, String> getTypes() {
        return types;
    }

    /**
     * Returns the properties.
     * @return properties
     */
    public String[] getProperties() {
        return properties;
    }

    public String toString()
    {
        return "groupNum=" + groupNum +
               " properties=" + Arrays.toString(properties) +
               " aliasTypes=" + types.toString();
    }
}
