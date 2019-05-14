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
  static int interval = 10;// in ms
 
  static void parse(String [] args, String cmd) throws Exception{
    String usage =
        Const.EOL +
        cmd + EOL +
        "    -url   URL                URL to contact server.  Default " + url + EOL +
        "    -user  username           Username.  Default anonymous user" + EOL +
        "    -pass  password           Password.  " + EOL +
        "    -cf    connectionfactory  ConnectionFactory.  Default " + cfName + EOL +
        "    -dest  destination        Destination.  Default " + destName + EOL +
        "    -count msgCount           Total msg count for sending or receiving.  Default " + msgCount + EOL + EOL + 
        "    -interval interval        Sleep interval (in ms)  between each msg sending or receiving.  Default " + interval + EOL + EOL
        ;
    
    int i=0;
    for (; i < args.length; i++) {
      if (args[i].equals("-url")) {
        url = args[++i];

      } else if (args[i].startsWith("-user")) {
        username = args[++i];

      } else if (args[i].startsWith("-pass")) {
        password = args[++i];

      } else if (args[i].startsWith("-cf")) {
        cfName = args[++i];

      } else if (args[i].startsWith("-dest")) {
        destName = args[++i];

      } else if (args[i].startsWith("-count")) {
        msgCount = new Integer(args[++i]);

      } else if (args[i].startsWith("-interval")) {
        interval = new Integer(args[++i]);

      } else {
        System.out.println(usage);
        System.exit(0);

      }
    }
  }
}
