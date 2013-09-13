/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.type;

/**
 * Hold parameters for timer:at.
 */
public class CronParameter {
    private CronOperatorEnum operator;
    private Integer day, month;

    private static final long serialVersionUID = -4006350378033980878L;

    /**
     * Ctor.
     * @param operator is the operator as text
     * @param day is the day text
     */
    public CronParameter(CronOperatorEnum operator, Integer day) {
        this.operator = operator;
        this.day = day;
    }

    /**
     * Sets the month value.
     * @param month to set
     */
    public void setMonth(int month) {
        this.month = month - 1;
    }

    public String formatted() {
        return operator + "(day " + day + " month " + month + ")";
    }

    public CronOperatorEnum getOperator() {
        return operator;
    }

    public Integer getDay() {
        return day;
    }

    public Integer getMonth() {
        return month;
    }
}
