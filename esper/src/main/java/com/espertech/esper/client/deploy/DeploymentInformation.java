package com.espertech.esper.client.deploy;

import java.util.Calendar;

/**
 * Available information about deployment made.
 */
public class DeploymentInformation
{
    private String deploymentId;
    private Module module;
    private Calendar addedDate;
    private Calendar lastUpdateDate;
    private DeploymentInformationItem[] items;
    private DeploymentState state;

    /**
     * Ctor.
     * @param deploymentId deployment id
     * @param lastUpdateDate date of last update to state
     * @param items module statement-level details
     * @param module the module
     */
    public DeploymentInformation(String deploymentId, Module module, Calendar addedDate, Calendar lastUpdateDate, DeploymentInformationItem[] items, DeploymentState state)
    {
        this.deploymentId = deploymentId;
        this.module = module;
        this.lastUpdateDate = lastUpdateDate;
        this.addedDate = addedDate;
        this.items = items;
        this.state = state;
    }

    /**
     * Returns the deployment id.
     * @return deployment id
     */
    public String getDeploymentId()
    {
        return deploymentId;
    }


    /**
     * Returns the last update date, i.e. date the information was last updated with new state.
     * @return last update date
     */
    public Calendar getLastUpdateDate()
    {
        return lastUpdateDate;
    }

    /**
     * Returns deployment statement-level details: Note that for an newly-added undeployed modules
     * not all statement-level information is available and therefore returns an empty array.
     * @return statement details or empty array for newly added deployments
     */
    public DeploymentInformationItem[] getItems() {
        return items;
    }

    public DeploymentState getState()
    {
        return state;
    }

    public Calendar getAddedDate()
    {
        return addedDate;
    }

    public Module getModule()
    {
        return module;
    }

    public String toString() {
        return "id '" + deploymentId + "' " +
               " added on " + addedDate.getTime().toString();
    }
}
