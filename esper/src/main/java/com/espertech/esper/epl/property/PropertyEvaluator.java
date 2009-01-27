package com.espertech.esper.epl.property;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.EventType;

/**
 * Interface for a function that evaluates the properties of an eventand returns event representing the properties.
 */
public interface PropertyEvaluator
{
    /**
     * Returns the result events based on property values, or null if none found.
     * @param event to inspect
     * @return events representing property(s)
     */
    public EventBean[] getProperty(EventBean event);

    /**
     * Returns the result type of the events generated by evaluating a property expression.
     * @return result event type
     */
    public EventType getFragmentEventType();

    public boolean compareTo(PropertyEvaluator otherFilterPropertyEval);
}
