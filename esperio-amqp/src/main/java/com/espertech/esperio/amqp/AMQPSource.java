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

package com.espertech.esperio.amqp;

import com.espertech.esper.client.EPException;
import com.espertech.esper.dataflow.annotations.DataFlowContext;
import com.espertech.esper.dataflow.annotations.DataFlowOpPropertyHolder;
import com.espertech.esper.dataflow.interfaces.*;
import com.rabbitmq.client.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

public class AMQPSource implements DataFlowSourceOperator {

    private static final Log log = LogFactory.getLog(AMQPSource.class);

    @DataFlowOpPropertyHolder
    private AMQPSettingsSource settings;

    private transient Connection connection;
    private transient Channel channel;
    private transient QueueingConsumer consumer;
    private transient String consumerTag;

    @DataFlowContext
    protected EPDataFlowEmitter graphContext;

    public AMQPSource() {
    }

    public DataFlowOpInitializeResult initialize(DataFlowOpInitializateContext context) throws Exception {
        return null;
    }

    public void open(DataFlowComponentOpenContext openContext) {
        log.info("Opening AMQP, settings are: " + settings.toString());

        try {
            final ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(settings.getHost());
            if (settings.getPort() > -1) {
                connectionFactory.setPort(settings.getPort());
            }
            if (settings.getUsername() != null) {
                connectionFactory.setUsername(settings.getUsername());
            }
            if (settings.getPassword() != null) {
                connectionFactory.setPassword(settings.getPassword());
            }
            if (settings.getVhost() != null) {
                connectionFactory.setVirtualHost(settings.getVhost());
            }

            connection = connectionFactory.newConnection();
            channel = connection.createChannel();

            channel.basicQos(settings.getPrefetchCount());
            if (settings.getExchange() != null) {
                channel.exchangeDeclarePassive(settings.getExchange());
            }

            final AMQP.Queue.DeclareOk queue;
            if (settings.getQueueName() == null || settings.getQueueName().trim().length() == 0) {
                queue = channel.queueDeclare();
            }
            else {
                // java.lang.String queue,boolean durable,boolean exclusive,boolean autoDelete,java.util.Map<java.lang.String,java.lang.Object> arguments) throws java.io.IOException
                queue = channel.queueDeclare(settings.getQueueName(), settings.isDeclareDurable(), settings.isDeclareExclusive(), settings.isDeclareAutoDelete(), settings.getDeclareAdditionalArgs());
            }
            if (settings.getExchange() != null && settings.getRoutingKey() != null) {
                channel.queueBind(queue.getQueue(), settings.getExchange(), settings.getRoutingKey());
            }

            final String queueName = queue.getQueue();
            log.info("AMQP consuming queue " + queueName + (settings.isLogMessages() ? " with logging" : ""));

            consumer = new QueueingConsumer(channel);
            consumerTag = channel.basicConsume(queueName, settings.isConsumeAutoAck(), consumer);
        }
        catch (IOException e) {
            String message = "AMQP source setup failed: " + e.getMessage();
            log.error(message, e);
            throw new EPException(message);
        }
    }

    public void next() throws InterruptedException {
        if (consumer == null) {
            log.warn("Consumer not started");
        }
        else {
            final QueueingConsumer.Delivery msg = consumer.nextDelivery(settings.getWaitMSecNextMsg());
            if (msg == null) {
                if (settings.isLogMessages() && log.isDebugEnabled()) {
                    log.debug("No message received");
                }
                return;
            }
            final byte[] bytes = msg.getBody();
            Object transformed = settings.getAmqpToObjectTransform().transform(bytes);
            if (settings.isLogMessages() && log.isDebugEnabled()) {
                log.debug("Received " + bytes.length + " bytes, to be processed by " + settings.getAmqpToObjectTransform() + ", output is " + transformed);
            }

            if (transformed instanceof Object[]) {
                graphContext.submit((Object[]) transformed);
            }
            else {
                graphContext.submit(transformed);
            }
        }
    }

    public void close(DataFlowComponentCloseContext openContext) {
        try {
            if (channel != null) {
              if (consumerTag != null) {
                  channel.basicCancel(consumerTag);
              }

              channel.close();
            }
        }
        catch (IOException e) {
            log.warn("Error closing AMQP channel", e);
        }

        try {
            if (connection != null) {
              connection.close();
            }
        }
        catch (IOException e) {
            log.warn("Error closing AMQP connection", e);
        }
    }
}
