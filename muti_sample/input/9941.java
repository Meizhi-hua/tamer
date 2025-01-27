public class DebugServer {
  private void usage() {
    System.out.println("usage: java " + getClass().getName() + " <pid> [server id]");
    System.out.println("   or: java " + getClass().getName() + " <executable> <core> [server id]");
    System.out.println("\"pid\" must be the process ID of a HotSpot process.");
    System.out.println("If reading a core file, \"executable\" must (currently) be the");
    System.out.println("full path name to the precise java executable which generated");
    System.out.println("the core file (not, on Solaris, the \"java\" wrapper script in");
    System.out.println("the \"bin\" subdirectory of the JDK.)");
    System.out.println("The \"server id\" is a unique name for a specific remote debuggee.");
    System.exit(1);
  }
  public static void main(String[] args) {
    new DebugServer().run(args);
  }
  private void run(String[] args) {
    if ((args.length < 1) || (args.length > 3)) {
      usage();
    }
    if (args[0].startsWith("-")) {
      usage();
    }
    int pid = 0;
    boolean usePid = false;
    String coreFileName = null;
    String javaExecutableName = null;
    String serverID = null;
    switch (args.length) {
       case 1:
         try {
           pid = Integer.parseInt(args[0]);
           usePid = true;
         } catch (NumberFormatException e) {
           usage();
         }
         break;
       case 2:
         try {
           pid = Integer.parseInt(args[0]);
           usePid = true;
           serverID = args[1];
         } catch (NumberFormatException e) {
           pid = -1;
           usePid = false;
           javaExecutableName = args[0];
           coreFileName = args[1];
         }
         break;
       case 3:
         javaExecutableName = args[0];
         coreFileName = args[1];
         serverID = args[2];
         break;
       default:
         break;
    }
    final HotSpotAgent agent = new HotSpotAgent();
    try {
      if (usePid) {
        System.err.println("Attaching to process ID " + pid + " and starting RMI services, please wait...");
        agent.startServer(pid, serverID);
      } else {
        System.err.println("Attaching to core " + coreFileName +
                           " from executable " + javaExecutableName + " and starting RMI services, please wait...");
        agent.startServer(javaExecutableName, coreFileName, serverID);
      }
    }
    catch (DebuggerException e) {
      if (usePid) {
        System.err.print("Error attaching to process or starting server: ");
      } else {
        System.err.print("Error attaching to core file or starting server: ");
      }
      e.printStackTrace();
      System.exit(1);
    }
    Runtime.getRuntime().addShutdownHook(new java.lang.Thread(
                          new Runnable() {
                             public void run() {
                                agent.shutdownServer();
                             }
                          }));
    System.err.println("Debugger attached and RMI services started.");
  }
}
