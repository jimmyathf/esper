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

package com.espertech.esper.core.context.mgr;

public class ContextStatePathKey implements Comparable {
    private final String contextName;
    private final int level;
    private final int parentPath;
    private final int subPath;

    public ContextStatePathKey(String contextName, int level, int parentPath, int subPath) {
        this.contextName = contextName;
        this.level = level;
        this.parentPath = parentPath;
        this.subPath = subPath;
    }

    public String getContextName() {
        return contextName;
    }

    public int getLevel() {
        return level;
    }

    public int getParentPath() {
        return parentPath;
    }

    public int getSubPath() {
        return subPath;
    }

    public int compareTo(Object o) {
        if (o.getClass() != ContextStatePathKey.class) {
            throw new IllegalArgumentException("Cannot compare " + ContextStatePathKey.class.getName() + " to " + o.getClass().getName());
        }
        ContextStatePathKey other = (ContextStatePathKey) o;
        if (!this.getContextName().equals(other.getContextName())) {
            return this.getContextName().compareTo(other.getContextName());
        }
        if (this.getLevel() != other.getLevel()) {
            return this.getLevel() < other.getLevel() ? -1 : 1;
        }
        if (this.getParentPath() != other.getParentPath()) {
            return this.getParentPath() < other.getParentPath() ? -1 : 1;
        }
        if (this.getSubPath() != other.getSubPath()) {
            return this.getSubPath() < other.getSubPath() ? -1 : 1;
        }
        return 0;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ContextStatePathKey that = (ContextStatePathKey) o;

        if (level != that.level) return false;
        if (parentPath != that.parentPath) return false;
        if (subPath != that.subPath) return false;
        if (!contextName.equals(that.contextName)) return false;

        return true;
    }

    public int hashCode() {
        int result = contextName.hashCode();
        result = 31 * result + level;
        result = 31 * result + parentPath;
        result = 31 * result + subPath;
        return result;
    }
}
