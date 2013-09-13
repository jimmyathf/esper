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

package com.espertech.esper.regression.epl;

import com.espertech.esper.client.*;
import com.espertech.esper.client.scopetest.EPAssertionUtil;
import com.espertech.esper.client.scopetest.SupportUpdateListener;
import com.espertech.esper.collection.Pair;
import com.espertech.esper.support.bean.*;
import com.espertech.esper.support.client.SupportConfigFactory;
import com.espertech.esper.support.epl.SupportStaticMethodLib;
import junit.framework.TestCase;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class TestInsertIntoTransposeStream extends TestCase
{
    private EPServiceProvider epService;
    private SupportUpdateListener listener;

    public void setUp()
    {
        Configuration configuration = SupportConfigFactory.getConfiguration();
        epService = EPServiceProviderManager.getDefaultProvider(configuration);
        epService.initialize();
        listener = new SupportUpdateListener();
    }

    protected void tearDown() throws Exception {
        listener = null;
    }

    public void testTransposeFunctionToStreamWithProps()
    {
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBean.class);
        epService.getEPAdministrator().getConfiguration().addPlugInSingleRowFunction("custom", SupportStaticMethodLib.class.getName(), "makeSupportBean");

        String stmtTextOne = "insert into MyStream select 1 as dummy, transpose(custom('O' || theString, 10)) from SupportBean(theString like 'I%')";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        String stmtTextTwo = "select * from MyStream";
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtTextTwo);
        stmt.addListener(listener);
        EventType type = stmt.getEventType();
        assertEquals(Pair.class, type.getUnderlyingType());

        epService.getEPRuntime().sendEvent(new SupportBean("I1", 1));
        EventBean result = listener.assertOneGetNewAndReset();
        Pair underlying = (Pair) result.getUnderlying();
        EPAssertionUtil.assertProps(result, "dummy,theString,intPrimitive".split(","), new Object[]{1, "OI1", 10});
        assertEquals("OI1", ((SupportBean) underlying.getFirst()).getTheString());
    }

    public void testTransposeFunctionToStream()
    {
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBean.class);
        epService.getEPAdministrator().getConfiguration().addPlugInSingleRowFunction("custom", SupportStaticMethodLib.class.getName(), "makeSupportBean");

        String stmtTextOne = "insert into OtherStream select transpose(custom('O' || theString, 10)) from SupportBean(theString like 'I%')";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        String stmtTextTwo = "select * from OtherStream(theString like 'O%')";
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtTextTwo);
        stmt.addListener(listener);
        EventType type = stmt.getEventType();
        assertEquals(SupportBean.class, type.getUnderlyingType());

        epService.getEPRuntime().sendEvent(new SupportBean("I1", 1));
        EventBean result = listener.assertOneGetNewAndReset();
        EPAssertionUtil.assertProps(result, "theString,intPrimitive".split(","), new Object[]{"OI1", 10});
        assertEquals("OI1", ((SupportBean) result.getUnderlying()).getTheString());
    }

    public void testTransposeSingleColumnInsert()
    {
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBean.class);
        epService.getEPAdministrator().getConfiguration().addEventType(SupportBeanNumeric.class);
        epService.getEPAdministrator().getConfiguration().addPlugInSingleRowFunction("customOne", SupportStaticMethodLib.class.getName(), "makeSupportBean");
        epService.getEPAdministrator().getConfiguration().addPlugInSingleRowFunction("customTwo", SupportStaticMethodLib.class.getName(), "makeSupportBeanNumeric");

        // with transpose and same input and output
        String stmtTextOne = "insert into SupportBean select transpose(customOne('O' || theString, 10)) from SupportBean(theString like 'I%')";
        EPStatement stmtOne = epService.getEPAdministrator().createEPL(stmtTextOne);
        assertEquals(SupportBean.class, stmtOne.getEventType().getUnderlyingType());
        stmtOne.addListener(listener);

        epService.getEPRuntime().sendEvent(new SupportBean("I1", 1));
        EventBean resultOne = listener.assertOneGetNewAndReset();
        EPAssertionUtil.assertProps(resultOne, "theString,intPrimitive".split(","), new Object[]{"OI1", 10});
        assertEquals("OI1", ((SupportBean) resultOne.getUnderlying()).getTheString());
        stmtOne.destroy();

        // with transpose but different input and output (also test ignore column name)
        String stmtTextTwo = "insert into SupportBeanNumeric select transpose(customTwo(intPrimitive, intPrimitive+1)) as col1 from SupportBean(theString like 'I%')";
        EPStatement stmtTwo = epService.getEPAdministrator().createEPL(stmtTextTwo);
        assertEquals(SupportBeanNumeric.class, stmtTwo.getEventType().getUnderlyingType());
        stmtTwo.addListener(listener);

        epService.getEPRuntime().sendEvent(new SupportBean("I2", 10));
        EventBean resultTwo = listener.assertOneGetNewAndReset();
        EPAssertionUtil.assertProps(resultTwo, "intOne,intTwo".split(","), new Object[]{10, 11});
        assertEquals(11, (int) ((SupportBeanNumeric) resultTwo.getUnderlying()).getIntTwo());
        stmtTwo.destroy();

        // invalid wrong-bean target
        try {
            epService.getEPAdministrator().createEPL("insert into SupportBeanNumeric select transpose(customOne('O', 10)) from SupportBean");
            fail();
        }
        catch (EPStatementException ex) {
            assertEquals("Error starting statement: Expression-returned event type 'SupportBean' with underlying type 'com.espertech.esper.support.bean.SupportBean' cannot be converted target event type 'SupportBeanNumeric' with underlying type 'com.espertech.esper.support.bean.SupportBeanNumeric' [insert into SupportBeanNumeric select transpose(customOne('O', 10)) from SupportBean]", ex.getMessage());
        }

        // invalid additional properties
        try {
            epService.getEPAdministrator().createEPL("insert into SupportBean select 1 as dummy, transpose(customOne('O', 10)) from SupportBean");
            fail();
        }
        catch (EPStatementException ex) {
            assertEquals("Error starting statement: Cannot transpose additional properties in the select-clause to target event type 'SupportBean' with underlying type 'com.espertech.esper.support.bean.SupportBean', the transpose function must occur alone in the select clause [insert into SupportBean select 1 as dummy, transpose(customOne('O', 10)) from SupportBean]", ex.getMessage());
        }

        // invalid occurs twice
        try {
            epService.getEPAdministrator().createEPL("insert into SupportBean select transpose(customOne('O', 10)), transpose(customOne('O', 11)) from SupportBean");
            fail();
        }
        catch (EPStatementException ex) {
            assertEquals("Error starting statement: A column name must be supplied for all but one stream if multiple streams are selected via the stream.* notation [insert into SupportBean select transpose(customOne('O', 10)), transpose(customOne('O', 11)) from SupportBean]", ex.getMessage());
        }

        // invalid wrong-type target
        try {
            epService.getEPAdministrator().getConfiguration().addEventType("SomeOtherStream", new HashMap<String, Object>());
            epService.getEPAdministrator().createEPL("insert into SomeOtherStream select transpose(customOne('O', 10)) from SupportBean");
            fail();
        }
        catch (EPStatementException ex) {
            assertEquals("Error starting statement: Event type named 'SomeOtherStream' has already been declared with differing column name or type information: Type 'SomeOtherStream' is not compatible [insert into SomeOtherStream select transpose(customOne('O', 10)) from SupportBean]", ex.getMessage());
        }

        // invalid two parameters
        try {
            epService.getEPAdministrator().createEPL("select transpose(customOne('O', 10), customOne('O', 10)) from SupportBean");
            fail();
        }
        catch (EPStatementException ex) {
            assertEquals("Error starting statement: The transpose function requires a single parameter expression [select transpose(customOne('O', 10), customOne('O', 10)) from SupportBean]", ex.getMessage());
        }

        // test not a top-level function or used in where-clause (possible but not useful)
        epService.getEPAdministrator().createEPL("select * from SupportBean where transpose(customOne('O', 10)) is not null");
        epService.getEPAdministrator().createEPL("select transpose(customOne('O', 10)) is not null from SupportBean");
    }

    public void testTransposeEventJoinMap()
    {
        Map<String, Object> metadata = makeMap(new Object[][] {{"id", String.class}});
        epService.getEPAdministrator().getConfiguration().addEventType("AEvent", metadata);
        epService.getEPAdministrator().getConfiguration().addEventType("BEvent", metadata);

        String stmtTextOne = "insert into MyStream select a, b from AEvent.win:keepall() as a, BEvent.win:keepall() as b";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        String stmtTextTwo = "select a.id, b.id from MyStream";
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtTextTwo);
        stmt.addListener(listener);

        Map<String, Object> eventOne = makeMap(new Object[][] {{"id", "A1"}});
        Map<String, Object> eventTwo = makeMap(new Object[][] {{"id", "B1"}});
        epService.getEPRuntime().sendEvent(eventOne, "AEvent");
        epService.getEPRuntime().sendEvent(eventTwo, "BEvent");

        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), "a.id,b.id".split(","), new Object[]{"A1", "B1"});
    }

    public void testTransposeEventJoinPOJO()
    {
        epService.getEPAdministrator().getConfiguration().addEventType("AEvent", SupportBean_A.class);
        epService.getEPAdministrator().getConfiguration().addEventType("BEvent", SupportBean_B.class);

        String stmtTextOne = "insert into MyStream select a.* as a, b.* as b from AEvent.win:keepall() as a, BEvent.win:keepall() as b";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        String stmtTextTwo = "select a.id, b.id from MyStream";
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtTextTwo);
        stmt.addListener(listener);

        epService.getEPRuntime().sendEvent(new SupportBean_A("A1"));
        epService.getEPRuntime().sendEvent(new SupportBean_B("B1"));
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), "a.id,b.id".split(","), new Object[]{"A1", "B1"});
    }

    public void testTransposePOJOPropertyStream()
    {
        epService.getEPAdministrator().getConfiguration().addEventType("Complex", SupportBeanComplexProps.class);

        String stmtTextOne = "insert into MyStream select nested as inneritem from Complex";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        String stmtTextTwo = "select inneritem.nestedValue as result from MyStream";
        EPStatement stmt = epService.getEPAdministrator().createEPL(stmtTextTwo);
        stmt.addListener(listener);

        epService.getEPRuntime().sendEvent(SupportBeanComplexProps.makeDefaultBean());
        EPAssertionUtil.assertProps(listener.assertOneGetNewAndReset(), "result".split(","), new Object[]{"nestedValue"});
    }

    public void testInvalidTranspose()
    {
        Map<String, Object> metadata = makeMap(new Object[][] {
                {"nested", makeMap(new Object[][] {{"nestedValue", String.class}}) }
        });
        epService.getEPAdministrator().getConfiguration().addEventType("Complex", metadata);

        String stmtTextOne = "insert into MyStream select nested as inneritem from Complex";
        epService.getEPAdministrator().createEPL(stmtTextOne);

        try
        {
            String stmtTextTwo = "select inneritem.nestedValue as result from MyStream";
            epService.getEPAdministrator().createEPL(stmtTextTwo);
        }
        catch (Exception ex)
        {
            assertEquals("Error starting statement: Failed to resolve property 'inneritem.nestedValue' to a stream or nested property in a stream [select inneritem.nestedValue as result from MyStream]", ex.getMessage());
        }

        // test invalid unwrap-properties
        epService.getEPAdministrator().getConfiguration().addEventType(E1.class);
        epService.getEPAdministrator().getConfiguration().addEventType(E2.class);
        epService.getEPAdministrator().getConfiguration().addEventType(EnrichedE2.class);

        try {
            epService.getEPAdministrator().createEPL("@Resilient insert into EnrichedE2 " +
                    "select e2.* as event, e1.otherId as playerId " +
                    "from E1.win:length(20) as e1, E2.win:length(1) as e2 " +
                    "where e1.id = e2.id ");
        }
        catch (Exception ex)
        {
            assertEquals("Error starting statement: The 'e2.* as event' syntax is not allowed when inserting into an existing bean event type, use the 'e2 as event' syntax instead [@Resilient insert into EnrichedE2 select e2.* as event, e1.otherId as playerId from E1.win:length(20) as e1, E2.win:length(1) as e2 where e1.id = e2.id ]", ex.getMessage());
        }
    }

    private Map<String, Object> makeMap(Object[][] entries)
    {
        Map result = new HashMap<String, Object>();
        for (Object[] entry : entries)
        {
            result.put(entry[0], entry[1]);
        }
        return result;
    }

    public static class E1 implements Serializable {
        private final String id;
        private final String otherId;

        public E1(String id, String otherId) {
            this.id = id;
            this.otherId = otherId;
        }

        public String getId() {
            return id;
        }

        public String getOtherId() {
            return otherId;
        }
    }

    public static class E2 implements Serializable {
        private final String id;
        private final String value;

        public E2(String id, String value) {
            this.id = id;
            this.value = value;
        }

        public String getId() {
            return id;
        }

        public String getValue() {
            return value;
        }
    }

    public static class EnrichedE2 implements Serializable {
        private final E2 event;
        private final String otherId;

        public EnrichedE2(E2 event, String playerId) {
            this.event = event;
            this.otherId = playerId;
        }

        public E2 getEvent() {
            return event;
        }

        public String getOtherId() {
            return otherId;
        }
    }
}
