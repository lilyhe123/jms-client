package samples;

class Const {
  final static String JNDI_FACTORY="weblogic.jndi.WLInitialContextFactory";
  final static String EOL = System.getProperty("line.separator");

  static String url = "t3://domain1-managed-server-2,domain1-managed-server-1:8001";
  static String username="weblogic";
  static String password="welcome1";
  static String cfName = "cf1";
  static String destName = "dq1";
  static int msgCount = 10;
 
  static void parse(String [] args, String cmd) throws Exception{
    String usage =
        Const.EOL +
        cmd + EOL +
        "    -url   URL                URL to contact server.  Default " + url + EOL +
        "    -user  username           Username.  Default anonymous user" + EOL +
        "    -pass  password           Password.  " + EOL +
        "    -cf    connectionfactory  ConnectionFactory.  Default " + cfName + EOL +
        "    -dest  destination        Destination.  Default " + destName + EOL +
        "    -count msgCount       MessageCount for sending messages.  Default " + msgCount + EOL + EOL
        ;
    
    int i=0;
    for (; i < args.length; i++) {
      if (args[i].equals("-url")) {
        Const.url = args[++i];

      } else if (args[i].startsWith("-user")) {
        Const.username = args[++i];

      } else if (args[i].startsWith("-pass")) {
        Const.password = args[++i];

      } else if (args[i].startsWith("-cf")) {
        Const.cfName = args[++i];

      } else if (args[i].startsWith("-dest")) {
        Const.destName = args[++i];

      } else if (args[i].startsWith("-count")) {
        Const.msgCount = new Integer(args[++i]);

      } else {
        System.out.println(usage);
        System.exit(0);

      }
    }
  }
}
