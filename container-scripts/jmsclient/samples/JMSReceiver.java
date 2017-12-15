package samples;

import java.util.Hashtable;
import java.util.List;
import java.util.Scanner;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import weblogic.jms.extensions.DestinationAvailabilityListener;
import weblogic.jms.extensions.DestinationDetail;
import weblogic.jms.extensions.JMSDestinationAvailabilityHelper;
import weblogic.jms.extensions.RegistrationHandle;

/**
 * This class demonstrates setting up JMS asynchronous message consumers 
 * on each member of a WebLogic distributed destination in response to JMSDestinationAvailabilityHelper events.
 * This is similar to the internal behavior of a WebLogic Message Driven bean.
 *
 * @author Copyright (c) 2017, Oracle and/or its affiliates. All rights reserved.
 */

public class JMSReceiver implements DestinationAvailabilityListener, ExceptionListener, MessageListener{
  //Defines the JNDI context factory.
  private final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
  // Defines the JMS destination availability helper
  private final static JMSDestinationAvailabilityHelper JMSDAHELPER = JMSDestinationAvailabilityHelper.getInstance();
  private static String EOL = System.getProperty("line.separator");

  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Destination destination;
  private RegistrationHandle handler;
  private String url = "t3://wls-subdomain:8011";
  private String username;
  private String password;
  private String connectionFactoryName = "cf2";
  private String destinationName = "dq2";
  private Hashtable<String, Session> receivers = new Hashtable<String, Session>();

  /**
   * Closes JMS objects.
   * @exception JMSException if JMS fails to close objects due to internal error
   */
  private void close() throws JMSException
  {
    handler.unregister();
    synchronized (receivers){
      receivers.clear();
    }
    connection.close();
  }


  /**
   * Creates all the necessary objects for receiving messages.
   *
   * @param   ctx JNDI initial context
   * @exception NamingException if operation cannot be performed
   * @exception JMSException if JMS fails to initialize due to internal error
   */
  private void init() throws NamingException, JMSException {
    Context ctx = getInitialContext(url, username, password);
    connectionFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
    destination = (Destination) ctx.lookup(destinationName);
    connection = connectionFactory.createConnection();
    connection.setExceptionListener(this);
    connection.start();
  }

  /**
   * Register dahelper listener for distributed destinations.
   *
   * @param   destName distributed destination name
   * @exception NamingException if operation cannot be performed
   */
  private void initDAHelper() throws NamingException {
    handler = JMSDAHELPER.register(getContextProps(url, username, password), destinationName, this);
  }

  private void parse(String [] args) throws Exception{
    String usage =
        EOL +
        "  java samples.JMSReceiver" + EOL +
        "    -url   URL                URL to contact server.  Default " + url + EOL +
        "    -user  username           Weblogic Username.  Default anonymous user" + EOL +
        "    -pass  password           Weblogic Password.  " + EOL +
        "    -cf    connectionfactory  ConnectionFactory.  Default " + connectionFactoryName + EOL +
        "    -dest  destination        Destination.  Default " + destinationName + EOL + EOL + 
        "  Press the enter key to exit the program." + EOL + EOL
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

        } else if (args[i].equals("-help")) {
          System.out.println(usage);
          System.exit(0);

        } else {
          throw new Exception(EOL + "Unknown argument '" + args[i] + "'"
              + EOL + usage);
        }
      }
    } catch (ArrayIndexOutOfBoundsException aio) {
        throw new Exception(EOL + "Missing argument for '" + args[--i] + "'" 
                            + EOL + usage); 
    }
  }
  /**
   * main() method.
   *
   * @param args  WebLogic JMS destination name
   * @exception  Exception if execution fails
   */
   public static void main(String[] args) throws Exception {

     JMSReceiver qr = new JMSReceiver();
     qr.parse(args);
     qr.init();
     qr.initDAHelper();
    
	 System.out.println("Press enter to exit...");
     // wait for enter key
     Scanner input = new Scanner(System.in);
     input.nextLine();
     input.close();

     qr.close();
   }


  /**
   * Message listener interface.
   *
   * @param msg message
   */
  public void onMessage(Message msg) {
    try {
      String msgText;
      if (msg instanceof TextMessage) {
        msgText = ((TextMessage) msg).getText();
      } else {
        msgText = msg.toString();
      }

      System.out.println("  Message received: " + msgText); 
    } catch (JMSException jmse) {
      System.err.println("An exception occurred: " + jmse.getMessage());
    }
  }


  @Override
  public void onDestinationsAvailable(String destJNDIName,
      List<DestinationDetail> list) {
    try {
      for (DestinationDetail detail : list) {
        System.out.println("Receive message from: " +  detail.getJNDIName());
        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer = session.createConsumer(destination);
        consumer.setMessageListener(this);
        synchronized (receivers){
          receivers.put(detail.getJNDIName(), session);
        }
      }
      // printReceivers();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onDestinationsUnavailable(String destJNDIName,
      List<DestinationDetail> list) {
   try {
      for (DestinationDetail detail : list) {
        System.out.println("Stop receiving message from: " +  detail.getJNDIName());
        Session session = null;
        synchronized (receivers){
          session = receivers.remove(detail.getJNDIName());
        }
        if (session != null) {
          session.close();
        }
      }
      // printReceivers();
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Override
  public void onFailure(String destJNDIName, Exception exception) {
    System.out.println("Dstination failure notification: " + destJNDIName + ", exception is " + exception.getMessage());
    exception.printStackTrace();
  }

  private void printReceivers() {
    System.out.print("Receivers: ");
    String str = null;
    for (String name : receivers.keySet()) {
      if(str == null) {
        str = name;
      } else {
        str += ", " + name;
      }
    }
    System.out.println(str);
  }

  @Override
  public void onException(JMSException jmse) {
    System.out.println("onException called, exception is " + jmse.getMessage());
    jmse.printStackTrace();
    try {
      connection.close();
    } catch (JMSException e) {
      e.printStackTrace();
    }
    System.exit(0);
  }

  /**
   * Get context objects.
   * @exception NamingException if fails to get context objects due to internal error
   */
  private InitialContext getInitialContext(String url, String username, String password) throws NamingException {
    return new InitialContext(getContextProps(url, username, password));
  }

  private Hashtable<String, String> getContextProps(String url, String username, String password){
    Hashtable<String, String> contextProps = new Hashtable<String, String>();
    contextProps.put(Context.INITIAL_CONTEXT_FACTORY, JNDI_FACTORY);
    contextProps.put(Context.PROVIDER_URL, url);
    if (username != null) {
      contextProps.put(Context.SECURITY_PRINCIPAL, username);
    }
    if (password != null) {
      contextProps.put(Context.SECURITY_CREDENTIALS, password);
    }
    return contextProps;
  }
}
