/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esper.core.start;

import com.espertech.esper.core.context.factory.StatementAgentInstanceFactorySelectResult;
import com.espertech.esper.core.context.factory.StatementAgentInstancePreload;
import com.espertech.esper.core.context.mgr.ContextManagedStatementSelectDesc;
import com.espertech.esper.core.context.stmt.AIRegistryExpr;
import com.espertech.esper.core.context.stmt.AIRegistryPrevious;
import com.espertech.esper.core.context.stmt.AIRegistryPrior;
import com.espertech.esper.core.context.stmt.AIRegistrySubselect;
import com.espertech.esper.core.context.subselect.SubSelectStrategyFactoryDesc;
import com.espertech.esper.core.context.subselect.SubSelectStrategyHolder;
import com.espertech.esper.core.context.util.AgentInstanceContext;
import com.espertech.esper.core.context.util.ContextMergeView;
import com.espertech.esper.core.context.util.StatementAgentInstanceUtil;
import com.espertech.esper.core.service.EPServicesContext;
import com.espertech.esper.core.service.StatementContext;
import com.espertech.esper.epl.agg.AggregationService;
import com.espertech.esper.epl.expression.*;
import com.espertech.esper.epl.spec.StatementSpecCompiled;
import com.espertech.esper.view.ViewProcessingException;
import com.espertech.esper.view.Viewable;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Starts and provides the stop method for EPL statements.
 */
public class EPStatementStartMethodSelect extends EPStatementStartMethodBase
{
    private static final Log log = LogFactory.getLog(EPStatementStartMethodSelect.class);

    public EPStatementStartMethodSelect(StatementSpecCompiled statementSpec, EPServicesContext services, StatementContext statementContext) {
        super(statementSpec, services, statementContext);
    }

    public EPStatementStartResult startInternal(boolean isNewStatement, boolean isRecoveringStatement, boolean isRecoveringResilient) throws ExprValidationException, ViewProcessingException {

        final String contextName = statementSpec.getOptionalContextName();
        AgentInstanceContext defaultAgentInstanceContext = getDefaultAgentInstanceContext();
        final EPStatementStartMethodSelectDesc selectDesc = EPStatementStartMethodSelectUtil.prepare(statementSpec, services, statementContext, isRecoveringResilient, defaultAgentInstanceContext, queryPlanLogging, null, null, null);

        // Determine context
        EPStatementStopMethod stopStatementMethod;
        EPStatementDestroyMethod destroyStatementMethod;
        Viewable finalViewable;
        AggregationService aggregationService;
        Map<ExprSubselectNode, SubSelectStrategyHolder> subselectStrategyInstances;
        Map<ExprPriorNode, ExprPriorEvalStrategy> priorStrategyInstances;
        Map<ExprPreviousNode, ExprPreviousEvalStrategy> previousStrategyInstances;
        List<StatementAgentInstancePreload> preloadList = Collections.emptyList();

        // With context - delegate instantiation to context
        if (statementSpec.getOptionalContextName() != null) {

            // use statement-wide agent-instance-specific aggregation service
            aggregationService = statementContext.getStatementAgentInstanceRegistry().getAgentInstanceAggregationService();

            // use statement-wide agent-instance-specific subselects
            AIRegistryExpr aiRegistryExpr = statementContext.getStatementAgentInstanceRegistry().getAgentInstanceExprService();

            subselectStrategyInstances = new HashMap<ExprSubselectNode, SubSelectStrategyHolder>();
            for (Map.Entry<ExprSubselectNode, SubSelectStrategyFactoryDesc> entry : selectDesc.getSubSelectStrategyCollection().getSubqueries().entrySet()) {
                AIRegistrySubselect specificService = aiRegistryExpr.allocateSubselect(entry.getKey());
                entry.getKey().setStrategy(specificService);

                Map<ExprPriorNode, ExprPriorEvalStrategy> subselectPriorStrategies = new HashMap<ExprPriorNode, ExprPriorEvalStrategy>();
                for (ExprPriorNode subselectPrior : entry.getValue().getPriorNodesList()) {
                    AIRegistryPrior specificSubselectPriorService = aiRegistryExpr.allocatePrior(subselectPrior);
                    subselectPriorStrategies.put(subselectPrior, specificSubselectPriorService);
                }

                Map<ExprPreviousNode, ExprPreviousEvalStrategy> subselectPreviousStrategies = new HashMap<ExprPreviousNode, ExprPreviousEvalStrategy>();
                for (ExprPreviousNode subselectPrevious : entry.getValue().getPrevNodesList()) {
                    AIRegistryPrevious specificSubselectPreviousService = aiRegistryExpr.allocatePrevious(subselectPrevious);
                    subselectPreviousStrategies.put(subselectPrevious, specificSubselectPreviousService);
                }

                subselectStrategyInstances.put(entry.getKey(), new SubSelectStrategyHolder(specificService, aggregationService, subselectPriorStrategies, subselectPreviousStrategies));
            }

            // use statement-wide agent-instance-specific "prior"
            priorStrategyInstances = new HashMap<ExprPriorNode, ExprPriorEvalStrategy>();
            for (ExprPriorNode priorNode : selectDesc.getViewResourceDelegateUnverified().getPriorRequests()) {
                AIRegistryPrior specificService = aiRegistryExpr.allocatePrior(priorNode);
                priorStrategyInstances.put(priorNode, specificService);
            }

            // use statement-wide agent-instance-specific "previous"
            previousStrategyInstances = new HashMap<ExprPreviousNode, ExprPreviousEvalStrategy>();
            for (ExprPreviousNode previousNode : selectDesc.getViewResourceDelegateUnverified().getPreviousRequests()) {
                AIRegistryPrevious specificService = aiRegistryExpr.allocatePrevious(previousNode);
                previousStrategyInstances.put(previousNode, specificService);
            }

            ContextMergeView mergeView = new ContextMergeView(selectDesc.getResultSetProcessorPrototypeDesc().getResultSetProcessorFactory().getResultEventType());
            finalViewable = mergeView;

            ContextManagedStatementSelectDesc statement = new ContextManagedStatementSelectDesc(statementSpec, statementContext, mergeView, selectDesc.getStatementAgentInstanceFactorySelect(),
                    selectDesc.getResultSetProcessorPrototypeDesc().getAggregationServiceFactoryDesc().getExpressions(),
                    selectDesc.getSubSelectStrategyCollection());
            services.getContextManagementService().addStatement(contextName, statement);
            stopStatementMethod = new EPStatementStopMethod(){
                public void stop() {
                    services.getContextManagementService().stoppedStatement(contextName, statementContext.getStatementName(), statementContext.getStatementId());
                    selectDesc.getStopMethod().stop();
                }
            };

            destroyStatementMethod = new EPStatementDestroyMethod(){
                public void destroy() {
                    services.getContextManagementService().destroyedStatement(contextName, statementContext.getStatementName(), statementContext.getStatementId());
                }
            };
        }
        // Without context - start here
        else {
            final StatementAgentInstanceFactorySelectResult resultOfStart = selectDesc.getStatementAgentInstanceFactorySelect().newContext(defaultAgentInstanceContext);
            finalViewable = resultOfStart.getFinalView();
            stopStatementMethod = new EPStatementStopMethod() {
                public void stop() {
                    StatementAgentInstanceUtil.stopSafe(resultOfStart.getStopCallback(), statementContext);
                    selectDesc.getStopMethod().stop();
                }
            };
            destroyStatementMethod = null;
            aggregationService = resultOfStart.getOptionalAggegationService();
            subselectStrategyInstances = resultOfStart.getSubselectStrategies();
            priorStrategyInstances = resultOfStart.getPriorNodeStrategies();
            previousStrategyInstances = resultOfStart.getPreviousNodeStrategies();
            preloadList = resultOfStart.getPreloadList();
        }

        // assign strategies to expression nodes
        EPStatementStartMethodHelperAssignExpr.assignExpressionStrategies(selectDesc, aggregationService, subselectStrategyInstances, priorStrategyInstances, previousStrategyInstances);

        // execute preload if any
        for (StatementAgentInstancePreload preload : preloadList) {
            preload.executePreload();
        }

        return new EPStatementStartResult(finalViewable, stopStatementMethod, destroyStatementMethod);
    }
}
