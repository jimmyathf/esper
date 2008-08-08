/**************************************************************************************
 * Copyright (C) 2006 Esper Team. All rights reserved.                                *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.view.stat.olap;

/**
 * Cell is the analytic values or variable tracked by a cube.
 */
public interface Cell
{
    /**
     * Returns the value.
     * @return double value
     */
    public double getValue();
}
