package samples;

import java.util.Hashtable;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 *  * This class is used to send messages.
 *   *
 *    * @author Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 *     */

public class JMSSender
{
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private MessageProducer producer;
  private Destination destination;

  /**
 *    * Closes JMS objects.
 *       * @exception JMSException if JMS fails to close objects due to internal error
 *          */
  private void close() throws JMSException {
    connection.close();
  }

  private void send() throws JMSException {
    System.out.println(Const.msgCount + " messages will be sent to " + Const.destName);
    for (int i=0; i < Const.msgCount; i++) {
      String text = "loopmsg-" + i;
      System.out.println("  Sending message: "+text);
      TextMessage message = session.createTextMessage();
      message.setText(text);
      producer.send(message);
    }
  }

  private void init() throws JMSException, NamingException {
    Context ctx = getInitialContext(Const.url, Const.username, Const.password);
    connectionFactory = (ConnectionFactory) ctx.lookup(Const.cfName);
    destination = (Destination) ctx.lookup(Const.destName);
    connection = connectionFactory.createConnection();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = session.createProducer(destination);
    connection.start();
  }

  /**
 *    * Get context objects.
 *       * @exception NamingException if fails to get context objects due to internal error
 *          */
  private InitialContext getInitialContext(String url, String username, String password) throws NamingException {
    Hashtable<String, String> contextProps = new Hashtable<String, String>();
    contextProps.put(Context.INITIAL_CONTEXT_FACTORY, Const.JNDI_FACTORY);
    contextProps.put(Context.PROVIDER_URL, Const.url);
    if (username != null) {
      contextProps.put(Context.SECURITY_PRINCIPAL, Const.username);
    }
    if (password != null) {
      contextProps.put(Context.SECURITY_CREDENTIALS, Const.password);
    }
    return new InitialContext(contextProps);
  }

  public static void main(String[] args) throws Exception {
    JMSSender qs = new JMSSender();
    Const.parse(args, "java samples.JMSSender");
    qs.init();
    qs.send();
    qs.close();
  }
}
