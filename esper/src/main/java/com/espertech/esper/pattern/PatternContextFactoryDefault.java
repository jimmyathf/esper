/**************************************************************************************
 * Copyright (C) 2007 Thomas Bernhardt. All rights reserved.                          *
 * http://esper.codehaus.org                                                          *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.pattern;

import com.espertech.esper.core.StatementContext;

/**
 * Default pattern context factory.
 */
public class PatternContextFactoryDefault implements PatternContextFactory
{
    public PatternContext createContext(StatementContext statementContext,
                                        int streamId,
                                        EvalRootNode rootNode)
    {
        PatternStateFactory patternStateFactory = new PatternStateFactoryImpl();

        PatternContext patternContext = new PatternContext(statementContext, streamId, patternStateFactory);

        patternStateFactory.setContext(patternContext);

        return patternContext;
    }
}
