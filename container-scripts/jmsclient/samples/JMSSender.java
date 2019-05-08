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
 * This class is used to send messages.
 *
 * @author Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 */

public class JMSSender
{
  //Defines the JNDI context factory.
  private final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
  private static String EOL = System.getProperty("line.separator");
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Session session;
  private MessageProducer producer;
  private Destination destination;
  private String url = "t3://domain1-managed-server-2,domain1-managed-server-1:8001";
  private String username="weblogic";
  private String password="welcome1";
  private String connectionFactoryName = "cf1";
  private String destinationName = "dq1";
  private static int messageCount = 10;

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  private void close() throws JMSException {
    connection.close();
  }

  private void send() throws JMSException {
    System.out.println(messageCount + " messages will be sent to " + destinationName);
    for (int i=0; i < messageCount; i++) {
      String text = "loopmsg-" + i;
      System.out.println("  Sending message: "+text);
      TextMessage message = session.createTextMessage();
      message.setText(text);
      producer.send(message);
    }
  }

  private void init() throws JMSException, NamingException {
    Context ctx = getInitialContext(url, username, password);
    connectionFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
    destination = (Destination) ctx.lookup(destinationName);
    connection = connectionFactory.createConnection();
    session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    producer = session.createProducer(destination);
    connection.start();
  }

  /**
   * Get context objects.
   * @exception NamingException if fails to get context objects due to internal error
   */
  private InitialContext getInitialContext(String url, String username, String password) throws NamingException {
    Hashtable<String, String> contextProps = new Hashtable<String, String>();
    contextProps.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
    contextProps.put(Context.PROVIDER_URL, url);
    if (username != null) {
      contextProps.put(Context.SECURITY_PRINCIPAL, username);
    }
    if (password != null) {
      contextProps.put(Context.SECURITY_CREDENTIALS, password);
    }
    return new InitialContext(contextProps);
  }

  private void parse(String [] args) throws Exception{
    String usage =
        EOL +
        "  java samples.JMSSender" + EOL +
        "    -url   URL                URL to contact server.  Default " + url + EOL +
        "    -user  username           Username.  Default anonymous user" + EOL +
        "    -pass  password           Password.  " + EOL +
        "    -cf    connectionfactory  ConnectionFactory.  Default " + connectionFactoryName + EOL +
        "    -dest  destination        Destination.  Default " + destinationName + EOL +
        "    -count messagecount       MessageCount for sending messages.  Default " + messageCount + EOL + EOL
        ;
    int i=0;
    try {
      for (; i < args.length; i++) {
        if (args[i].equals("-url")) {
          url = args[++i];

        } else if (args[i].startsWith("-user")) {
          username = args[++i];

        } else if (args[i].startsWith("-pass")) {
          password = args[++i];

        } else if (args[i].startsWith("-cf")) {
          connectionFactoryName = args[++i];

        } else if (args[i].startsWith("-dest")) {
          destinationName = args[++i];

        } else if (args[i].startsWith("-count")) {
          messageCount = new Integer(args[++i]);

        } else {
          System.out.println(usage);
          System.exit(0);

        }
      }
    } catch (NumberFormatException e) {
      throw new Exception(EOL + "Bad argument for '" + args[--i] + "'" + EOL + usage);
    } catch (ArrayIndexOutOfBoundsException aio) {
      throw new Exception(EOL + "Missing argument for '" + args[--i] + "'" + EOL + usage); 
    }
  }
  public static void main(String[] args) throws Exception {
    JMSSender qs = new JMSSender();
    qs.parse(args);
    qs.init();
    qs.send();
    qs.close();
  }
}
