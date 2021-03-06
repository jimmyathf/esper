/*
 * *************************************************************************************
 *  Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 *  http://esper.codehaus.org                                                          *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 * *************************************************************************************
 */

package com.espertech.esper.epl.enummethod.eval;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.epl.expression.ExprEvaluator;
import com.espertech.esper.epl.expression.ExprEvaluatorContext;
import com.espertech.esper.event.arr.ObjectArrayEventBean;
import com.espertech.esper.event.arr.ObjectArrayEventType;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;

public class EnumEvalTakeWhileLastIndexScalar extends EnumEvalBaseScalarIndex implements EnumEval {

    public EnumEvalTakeWhileLastIndexScalar(ExprEvaluator innerExpression, int streamNumLambda, ObjectArrayEventType evalEventType, ObjectArrayEventType indexEventType) {
        super(innerExpression, streamNumLambda, evalEventType, indexEventType);
    }

    public Object evaluateEnumMethod(EventBean[] eventsLambda, Collection target, boolean isNewData, ExprEvaluatorContext context) {
        if (target.isEmpty()) {
            return target;
        }

        ObjectArrayEventBean evalEvent = new ObjectArrayEventBean(new Object[1], evalEventType);
        ObjectArrayEventBean indexEvent = new ObjectArrayEventBean(new Object[1], indexEventType);

        if (target.size() == 1) {
            Object item = target.iterator().next();

            evalEvent.getProperties()[0] = item;
            eventsLambda[streamNumLambda] = evalEvent;

            indexEvent.getProperties()[0] = 0;
            eventsLambda[streamNumLambda + 1] = indexEvent;

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                return Collections.emptyList();
            }
            return Collections.singletonList(item);
        }

        int size = target.size();
        Object[] all = new Object[size];
        int count = 0;
        for (Object item : target) {
            all[count++] = item;
        }

        ArrayDeque<Object> result = new ArrayDeque<Object>();
        int index = 0;
        for (int i = all.length - 1; i >= 0; i--) {

            evalEvent.getProperties()[0] = all[i];
            eventsLambda[streamNumLambda] = evalEvent;

            indexEvent.getProperties()[0] = index++;
            eventsLambda[streamNumLambda + 1] = indexEvent;

            Object pass = innerExpression.evaluate(eventsLambda, isNewData, context);
            if (pass == null || (!(Boolean) pass)) {
                break;
            }
            result.addFirst(all[i]);
        }

        return result;
    }    
}
