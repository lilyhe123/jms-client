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
  // Defines the JMS destination availability helper
  private final static JMSDestinationAvailabilityHelper JMSDAHELPER = JMSDestinationAvailabilityHelper.getInstance();

  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Destination destination;
  private RegistrationHandle handler;  
  private static int rcvCount = 0;  

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
    Context ctx = getInitialContext(Const.url, Const.username, Const.password);
    connectionFactory = (ConnectionFactory) ctx.lookup(Const.cfName);
    destination = (Destination) ctx.lookup(Const.destName);
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
    handler = JMSDAHELPER.register(getContextProps(Const.url, Const.username, Const.password), Const.destName, this);
  }

  
  /**
   * main() method.
   *
   * @param args  WebLogic JMS destination name
   * @exception  Exception if execution fails
   */
   public static void main(String[] args) throws Exception {

     JMSReceiver qr = new JMSReceiver();
     Const.parse(args, "java samples.JMSReceiver");
     qr.init();
     qr.initDAHelper();
    
     Thread.currentThread().join();
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
      rcvCount ++;
      if (rcvCount >= Const.msgCount) {
        System.out.println(rcvCount + " msgs have been received. Exit.");
        System.exit(0);
      }
      Thread.sleep(Const.interval);
    } catch (Exception e) {
      System.err.println("An exception occurred: " + e.getMessage());
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
    contextProps.put(Context.INITIAL_CONTEXT_FACTORY, Const.JNDI_FACTORY);
    contextProps.put(Context.PROVIDER_URL, Const.url);
    if (username != null) {
      contextProps.put(Context.SECURITY_PRINCIPAL, Const.username);
    }
    if (password != null) {
      contextProps.put(Context.SECURITY_CREDENTIALS, Const.password);
    }
    return contextProps;
  }
}
