public class sagclient {
    static AttachingConnector myCoreConn;
    static AttachingConnector myPIDConn;
    static AttachingConnector myDbgSvrConn;
    static VirtualMachine vm;
    static VirtualMachineManager vmmgr;
    public static void println(String msg) {
        System.out.println("jj: " + msg);
    }
    public static void main(String args[]) {
        vmmgr = Bootstrap.virtualMachineManager();
        List attachingConnectors = vmmgr.attachingConnectors();
        if (attachingConnectors.isEmpty()) {
            System.err.println( "ERROR: No attaching connectors");
            return;
        }
        Iterator myIt = attachingConnectors.iterator();
        while (myIt.hasNext()) {
            AttachingConnector tmpCon = (AttachingConnector)myIt.next();
            if (tmpCon.name().equals(
                "sun.jvm.hotspot.jdi.SACoreAttachingConnector")) {
                myCoreConn = tmpCon;
            } else if (tmpCon.name().equals(
                "sun.jvm.hotspot.jdi.SAPIDAttachingConnector")) {
                myPIDConn = tmpCon;
            } else if (tmpCon.name().equals(
                "sun.jvm.hotspot.jdi.SADebugServerAttachingConnector")) {
                myDbgSvrConn = tmpCon;
            }
        }
        String execPath = null;
        String pidText = null;
        String coreFilename = null;
        String debugServer = null;
        int pid = 0;
        switch (args.length) {
        case (0):
            break;
        case (1):
            try {
                pidText = args[0];
                pid = Integer.parseInt(pidText);
                System.out.println( "pid: " + pid);
                vm = attachPID(pid);
            } catch (NumberFormatException e) {
                System.out.println("trying remote server ..");
                debugServer = args[0];
                System.out.println( "remote server: " + debugServer);
                vm = attachDebugServer(debugServer);
            }
            break;
        case (2):
            execPath = args[0];
            coreFilename = args[1];
            System.out.println( "jdk: " + execPath);
            System.out.println( "core: " + coreFilename);
            vm = attachCore(coreFilename, execPath);
            break;
        }
        if (vm != null) {
            System.out.println("sagclient: attached ok!");
            sagdoit mine = new sagdoit(vm);
            mine.doAll();
            vm.dispose();
        }
    }
    private static VirtualMachine attachCore(String coreFilename, String execPath) {
        Map connArgs = myCoreConn.defaultArguments();
        System.out.println("connArgs = " + connArgs);
        VirtualMachine vm;
        Connector.StringArgument connArg = (Connector.StringArgument)connArgs.get("core");
        connArg.setValue(coreFilename);
        connArg =  (Connector.StringArgument)connArgs.get("javaExecutable");
        connArg.setValue(execPath);
        try {
            vm = myCoreConn.attach(connArgs);
        } catch (IOException ee) {
            System.err.println("ERROR: myCoreConn.attach got IO Exception:" + ee);
            vm = null;
        } catch (IllegalConnectorArgumentsException ee) {
            System.err.println("ERROR: myCoreConn.attach got illegal args exception:" + ee);
            vm = null;
        }
        return vm;
   }
   private static VirtualMachine attachPID(int pid) {
        Map connArgs = myPIDConn.defaultArguments();
        System.out.println("connArgs = " + connArgs);
        VirtualMachine vm;
        Connector.StringArgument connArg = (Connector.StringArgument)connArgs.get("pid");
        connArg.setValue(Integer.toString(pid));
        try {
            vm = myPIDConn.attach(connArgs);
        } catch (IOException ee) {
            System.err.println("ERROR: myPIDConn.attach got IO Exception:" + ee);
            vm = null;
        } catch (IllegalConnectorArgumentsException ee) {
            System.err.println("ERROR: myPIDConn.attach got illegal args exception:" + ee);
            vm = null;
        }
        return vm;
   }
   private static VirtualMachine attachDebugServer(String debugServer) {
        Map connArgs = myDbgSvrConn.defaultArguments();
        System.out.println("connArgs = " + connArgs);
        VirtualMachine vm;
        Connector.StringArgument connArg = (Connector.StringArgument)connArgs.get("debugServerName");
        connArg.setValue(debugServer);
        try {
            vm = myDbgSvrConn.attach(connArgs);
        } catch (IOException ee) {
            System.err.println("ERROR: myDbgSvrConn.attach got IO Exception:" + ee);
            vm = null;
        } catch (IllegalConnectorArgumentsException ee) {
            System.err.println("ERROR: myDbgSvrConn.attach got illegal args exception:" + ee);
            vm = null;
        }
        return vm;
   }
}
