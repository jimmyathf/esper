/**************************************************************************************
 * Copyright (C) 2008 EsperTech, Inc. All rights reserved.                            *
 * http://esper.codehaus.org                                                          *
 * http://www.espertech.com                                                           *
 * ---------------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the GPL license       *
 * a copy of which has been included with this distribution in the license.txt file.  *
 **************************************************************************************/
package com.espertech.esperio.csv;

/**
 * Coercer for using the constructor to perform the coercion.
 */
public class BasicTypeCoercer extends AbstractTypeCoercer {

    public Object coerce(String property, String source) throws Exception {
		Object[] parameters = new Object[] { source };
		Object value = propertyConstructors.get(property).newInstance(parameters);
		return value;
	}
}
