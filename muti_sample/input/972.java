public class JInfo extends Tool {
    public JInfo(int m) {
        mode = m;
    }
    protected boolean needsJavaPrefix() {
        return false;
    }
    public String getName() {
        return "jinfo";
    }
    protected void printFlagsUsage() {
        System.out.println("    -flags\tto print VM flags");
        System.out.println("    -sysprops\tto print Java System properties");
        System.out.println("    <no option>\tto print both of the above");
        super.printFlagsUsage();
    }
    public static final int MODE_FLAGS = 0;
    public static final int MODE_SYSPROPS = 1;
    public static final int MODE_BOTH = 2;
    public void run() {
        Tool tool = null;
        switch (mode) {
        case MODE_FLAGS:
            printVMFlags();
            return;
        case MODE_SYSPROPS:
            tool = new SysPropsDumper();
            break;
        case MODE_BOTH: {
            tool = new Tool() {
                    public void run() {
                        Tool sysProps = new SysPropsDumper();
                        sysProps.setAgent(getAgent());
                        System.out.println("Java System Properties:");
                        System.out.println();
                        sysProps.run();
                        System.out.println();
                        System.out.println("VM Flags:");
                        printVMFlags();
                        System.out.println();
                    }
                };
            }
            break;
        default:
            usage();
            break;
        }
        tool.setAgent(getAgent());
        tool.run();
    }
    public static void main(String[] args) {
        int mode = -1;
        switch (args.length) {
        case 1:
            if (args[0].charAt(0) == '-') {
                new JInfo(mode).usage();
            } else {
                mode = MODE_BOTH;
            }
            break;
        case 2:
        case 3: {
            String modeFlag = args[0];
            if (modeFlag.equals("-flags")) {
                mode = MODE_FLAGS;
            } else if (modeFlag.equals("-sysprops")) {
                mode = MODE_SYSPROPS;
            } else if (modeFlag.charAt(0) == '-') {
                new JInfo(mode).usage();
            } else {
                mode = MODE_BOTH;
            }
            if (mode != MODE_BOTH) {
                String[] newArgs = new String[args.length - 1];
                for (int i = 0; i < newArgs.length; i++) {
                    newArgs[i] = args[i + 1];
                }
                args = newArgs;
            }
            break;
        }
        default:
            new JInfo(mode).usage();
        }
        JInfo jinfo = new JInfo(mode);
        jinfo.start(args);
        jinfo.stop();
    }
    private void printVMFlags() {
        String str = Arguments.getJVMFlags();
        if (str != null) {
            System.out.println(str);
        }
        str = Arguments.getJVMArgs();
        if (str != null) {
            System.out.println(str);
        }
    }
    private int mode;
}
