/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.epl.spec;

import com.espertech.esper.util.MetaDefItem;

/**
 * Specification for on-trigger statements.
 */
public abstract class OnTriggerDesc implements MetaDefItem
{
    private OnTriggerType onTriggerType;

    /**
     * Ctor.
     * @param onTriggerType the type of on-trigger
     */
    public OnTriggerDesc(OnTriggerType onTriggerType)
    {
        this.onTriggerType = onTriggerType;
    }

    /**
     * Returns the type of the on-trigger statement.
     * @return trigger type
     */
    public OnTriggerType getOnTriggerType()
    {
        return onTriggerType;
    }
}
